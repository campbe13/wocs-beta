package ca.campbell.celebritydb;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * ShowCeleb.class
 * 
 * This class is passed an array index as id, then it uses it as an index
 * to the public array in the launching class. It reads the URL from the array 
 * then uses it to download an image from the website, which is put into the UI ImageView.
 */
// todo missing progress bar for image load
public class ShowCeleb extends Activity {

	private String url;
	private ImageView iv;
	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showceleb);
		// Get the data from the launching Activity
		if (!getIntent().hasExtra("id")) {
			Toast.makeText(getApplicationContext(),
					"Logic Error: no id passed", Toast.LENGTH_LONG).show();
			// no data nothing to show, so finish() Activity
			// may want to set up a default instead
			// may want to sleep for some milliseconds
			finish();
		}
		int ix = (int) getIntent().getExtras().getLong("id");
		Log.d(Constants.TAG, "name " + MainActivity.celebs[ix].name);
		Log.d(Constants.TAG, "name " + MainActivity.celebs[ix].birthYear);

		TextView tv1 = (TextView) findViewById(R.id.textView2);
		TextView tv2 = (TextView) findViewById(R.id.textView4);
		tv1.setText(MainActivity.celebs[ix].name);
		tv2.setText(MainActivity.celebs[ix].birthYear);

		iv = (ImageView) findViewById(R.id.imageView1);
		// swipe left and right, get next image
		iv.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
			@Override
			public void onSwipeLeft() {
				/*
				 * on first swipe get json data set of images
				 *   
				 */
				Toast.makeText(getApplicationContext() , "left", Toast.LENGTH_SHORT).show();
			} 
			public void onSwipeRight() {
				Toast.makeText(getApplicationContext() , "right", Toast.LENGTH_SHORT).show();
			}	
		});
		// create the URL + image string
		url = Constants.siteUrl + MainActivity.celebs[ix].imageUrl;
		Log.d(Constants.TAG, url);
		// background task get the URL and populate the ImageView
		new GetImage().execute(url);

	}

	private class GetImage extends AsyncTask<String, Void, Bitmap> {
		// background task
		@Override
		protected Bitmap doInBackground(String... urls) {
			Bitmap map = downloadImage(url);
			// return -> onPostExecute(map)
			return map;
		}

		// on UI thread after background task
		@Override
		protected void onPostExecute(Bitmap map) {
			//	if (isCancelled())
			progressDialog.dismiss();
			iv.setImageBitmap(map);
		}

		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(ShowCeleb.this, "...", "Downloading");
		}
	} // AsyncTask GetImage()

	// Creates Bitmap from InputStream and returns it
	// must be called from doInBackground() 
	// network I/O must be done in background thread
	private Bitmap downloadImage(String url) {
		Bitmap bitmap = null;
		InputStream stream = null;
		/*
		 * android.graphicsBitmapFactory creates Bitmap objects from files,
		 * streams, byte arrays
		 */

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		// use image original size, not smaller image
		bmOptions.inSampleSize = 1;

		try {
			//
			stream = getHttpConnection(url);
			if (stream != null) {
				// use the BitmapFactory to read the stream and create a bitmap
				bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
				stream.close();
			}
		} catch (IOException e) {
			Log.d(Constants.TAG, getClass().getSimpleName()
					+ "  downloadImage() Exception:" + e.getMessage());
			// log the stacktrace
			e.printStackTrace();
		}
		return bitmap;
	}

	// Make HttpURLConnection and return InputStream
	// we do not read the stream here
	private InputStream getHttpConnection(String urlString) throws IOException {
		InputStream stream = null;
		if (!netIsUp()) {
			return null;
		}
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();

		try {
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			// default: httpConnection.setRequestMethod("GET");
			httpConnection.connect();

			if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				stream = httpConnection.getInputStream();
			}
		} catch (Exception e) {
			Log.d(Constants.TAG, getClass().getSimpleName()
					+ " getHttpConnection() Exception:" + e.getMessage());
			// log the stacktrace
			e.printStackTrace();
		}
		return stream;
	}
	public void showQuote(View v) {
		showAlertDialog("Soon to Show Quotes or BIO");
	}
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
	
	public void showAlertDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(msg).setCancelable(
				false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				ShowCeleb.this.finish();
			}
		});
		/*
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		 */
		AlertDialog alert = builder.create();
		alert.show();
	}
	/**
	 * Detects left and right swipes across a view.
	 */
	public class OnSwipeTouchListener implements OnTouchListener {

		private final GestureDetector gestureDetector;

		public OnSwipeTouchListener(Context context) {
			gestureDetector = new GestureDetector(context, new GestureListener());
		}

		public void onSwipeLeft() {
		}

		public void onSwipeRight() {
			
		}

		public boolean onTouch(View v, MotionEvent event) {
			return gestureDetector.onTouchEvent(event);
		}

		private final class GestureListener extends SimpleOnGestureListener {

			private static final int SWIPE_DISTANCE_THRESHOLD = 100;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float distanceX = e2.getX() - e1.getX();
				float distanceY = e2.getY() - e1.getY();
				if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
					if (distanceX > 0)
						onSwipeRight();
					else
						onSwipeLeft();
					return true;
				}
				return false;
			}
		}
	}
} // Activity class
