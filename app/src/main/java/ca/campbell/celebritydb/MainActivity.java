package ca.campbell.celebritydb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/*
 * MainActivity.class
 * This is the main Activity , as indicated in the AndroidManifest.xml
 * 
 * This code reads in a search string (only year is implemented) 
 * 
 * It uses the search string to get data from a MySQL database using
 * ConnectionManager NetworkInfo and HttpURlConnection classes.  
 * 
 * The server side on waldo has a MySQL database with a php front end 
 * see Constants.class for info on that.
 *
 * The results of the search are returned in JSON format, the data is parsed using
 * JSONObject and JSONArray classes.   
 * 
 * The results are put into an ArrayList which is then used to populate a ListView via an ArrayAdapter.
 * 
 * The onClick for the ListView launches ShowCeleb.class to display the information about the celebrity.
 * 
 * This implementation reads the whole record into a public array, which is 
 * then used in the subclass, this is not optimal.
 * Ideally you would read in minimal information for the ListView and the id
 * then in the ShowCeleb.class you would get all of the data from the website 
 * using the id.  Depends on design, connectivity etc.
 * 
 * Remember: Wherever strings constants are used inline "like this" you should use
 * the values directories, the same with any integers, arrays, dimensions or colour constants.
 *
 * @author P Campbell
 */
public class MainActivity extends Activity {
	private EditText ed1;
	private Button bt1;
	private ListView lv1;
	private List<String> names = new ArrayList<String>();
	private ArrayAdapter<String> aa;
	private boolean SearchByName  = true;
	boolean showSearch = false;
	public enum Gender {
		MALE, FEMALE
	}

	public class CelebrityDb {
		int id;
		String name;
		String imageUrl;
		int sex;
		String birthYear;
	}

	public static CelebrityDb celebs[] = new CelebrityDb[Constants.LVMAX];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ed1 = (EditText) this.findViewById(R.id.editText);
		bt1 = (Button) this.findViewById(R.id.submit);
		lv1 = (ListView) findViewById(R.id.listView1);
		lv1.setOnItemClickListener(showCeleb);
		aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, names);
		lv1.setAdapter(aa);
		searchDB(null);
	}

	// onClick for search button
	public void searchDB(View v) {
		if (ed1.getText().toString() != null && netIsUp()) {
			// clear the old results
			names.clear();
			aa.notifyDataSetChanged();
			try {
				// launch background task, URL from textview as input
				new GetWebData().execute(ed1.getText().toString());
			} catch (Exception e) {
				Log.d(Constants.TAG, "Exception:" + e.getMessage());
			}
		}
		ed1.setText("");

	}

	private OnItemClickListener showCeleb = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			Intent i = new Intent(getApplicationContext(), ShowCeleb.class);
			i.putExtra("id", id);
			startActivity(i);
		}
	}; // showCeleb

	/*
	 * findData()
	 * 
	 * 1. given a string to match, set up a URL
	 * 2. create and open an HttpURLConnection
	 * 3. Check to make sure the connection returned HTTP_OK (200)
	 * 4. Read the stream from the connection 
	 * 5. Close the connection
	 */

	public String findData(String matchKey) throws MalformedURLException,
	IOException {
		String newFeed; 
		if (isNumeric(matchKey)) 
			newFeed = Constants.urlByYear + matchKey;
		else
			newFeed = Constants.urlNameLike + matchKey;
		StringBuilder response = new StringBuilder();
		Log.d(Constants.TAG, "feed url:" + newFeed);
		// 1. set up the URL
		URL url = new URL(newFeed);
		// 2. create and open the http communications
		HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
		// 3. check if connection ok
		if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			// 4. read the stream from the connection
			BufferedReader input = new BufferedReader(new InputStreamReader(
					httpconn.getInputStream()), Constants.BUFFSIZE);
			String strLine = null;
			while ((strLine = input.readLine()) != null) {
				response.append(strLine);
			}
			// 5. close the connection
			input.close();
		}
		return response.toString();
	}
	public static boolean isNumeric(String str)
	{
		return str.matches("\\d+");  // regex match a number equiv: [0-9]+
	}
	/*
	 * processJSONResponse()
	 * 
	 * Parameter is a JSON stream. Parsed using JSONArray [] JSONObject {}
	 * Populate a struct array with the data.
	 * 
	 * To see an example of the results
	 * http://waldo.dawsoncollege.qc.ca/pcampbell/bornafter2.php?year=1970
	 * http://jsoneditoronline.org/
	 */
	public void processJSONResponse(String resp) throws IllegalStateException,
	IOException, JSONException, NoSuchAlgorithmException {

		JSONArray array = new JSONArray(resp);
		JSONObject jobj;

		Log.d(Constants.TAG, "number of results:" + array.length());

		for (int i = 0; i < (array.length() > Constants.LVMAX ? Constants.LVMAX
				: array.length()); i++) {
			Log.d(Constants.TAG, i + "[ " + array.get(i).toString());
			jobj = array.getJSONObject(i);
			celebs[i] = new CelebrityDb();
			if (jobj.has("name")) {
				Log.d(Constants.TAG, i + ": " + jobj.getString("name"));
				// Collection attached to the ListView
				names.add(jobj.getString("name"));
				celebs[i].name = jobj.getString("name");
			}

			if (array.getJSONObject(i).has("id")) {
				Log.d(Constants.TAG, i + ": "
						+ array.getJSONObject(i).getString("id"));
				celebs[i].id = Integer.parseInt(array.getJSONObject(i)
						.getString("id"));
			}

			if (jobj.has("imageUrl")) {
				Log.d(Constants.TAG, i + ": " + jobj.getString("imageUrl"));
				celebs[i].imageUrl = jobj.getString("imageUrl");

			}

			if (array.getJSONObject(i).has("sex")) {
				Log.d(Constants.TAG, i + ": "
						+ array.getJSONObject(i).getString("sex"));
				celebs[i].sex = Integer.parseInt(array.getJSONObject(i)
						.getString("sex"));
			}

			if (jobj.has("birthyear")) {
				Log.d(Constants.TAG, i + jobj.getString("birthyear"));
				celebs[i].birthYear = jobj.getString("birthyear");
			}
		}
	}

	private class GetWebData extends AsyncTask<String, Integer, String> {
		// background task
		protected String doInBackground(String... matchKey) {

			String key = matchKey[0];

			Log.d(Constants.TAG, " key:" + key);
			try {
				// send the data to onPostExecute()
				return findData(key);
			} catch (Exception e) {
				Log.d(Constants.TAG,
						"doInBackground() Exception:" + e.getMessage());
				e.printStackTrace();
				return "";
			}
		}

		// on UI thread, after background task
		protected void onPostExecute(String result) {
			try {
				// data from the HTTP connection stream is in JSON format
				processJSONResponse(result);
				lv1.setVisibility(View.VISIBLE);
				aa.notifyDataSetChanged();
			} catch (Exception e) {
				Log.d(Constants.TAG,
						"onPostExecute() Exception:" + e.getMessage());
				e.printStackTrace();
			}

		}
	}

	/*
	 * Check for network connectivity.
	 */
	public boolean netIsUp() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// getActiveNetworkInfo() each time as the network may swap as the
		// device moves
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		// ALWAYS check isConnected() before initiating network traffic
		if (networkInfo != null)
			return networkInfo.isConnected();
		else
			return false;
	} // netIsUp()
	/**
	 * Menu Lifecycle Methods
	 */
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		/*
		if (SearchByName) { 
			menu.findItem(R.id.byname).setVisible(false);
			menu.findItem(R.id.byyear).setVisible(true);
		}
		else { 
			menu.findItem(R.id.byname).setVisible(true);
			menu.findItem(R.id.byyear).setVisible(false);
		}
		 */
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);	
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		case R.id.about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.search:
			if (showSearch) {
				ed1.setVisibility(View.GONE);
				bt1.setVisibility(View.GONE);
				showSearch = false;
			}
			else {
				ed1.setVisibility(View.VISIBLE);
				bt1.setVisibility(View.VISIBLE);
				showSearch = true;
			}
			return true;
		case R.id.byname:
			return true;
		case R.id.byyear:
			return true;
		default:
			return true;
		}
	}
} // class MainActivity
