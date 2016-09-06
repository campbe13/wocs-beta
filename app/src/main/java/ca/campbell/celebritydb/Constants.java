package ca.campbell.celebritydb;

public class Constants {
	public static String siteUrl = "http://php54-dawsoncloud.rhcloud.com/";
	// people table
	public final static String urlByYear = siteUrl + "/bornafter.php?year=";
	public final static String urlById = siteUrl + "/byid.php?id=";
	public final static String urlNameLike = siteUrl + "/namelike.php?name=";
	// image table
	public final static String urlImageById = siteUrl + "/imagebyid.php?personid=";
	
	/*
	 * public static String siteUrl = "http://waldo.dawsoncollege.qc.ca/pcampbell";
	
	public final static String urlByYear = siteUrl + "/BornAfter2.php?year=";
	public final static String urlById = siteUrl + "/ById.php?id=";
	*/
	public final static String TAG = "WoCS";
	public final static int LVMAX = 30;
	public final static int BUFFSIZE = 8192; // 8K

}

/*
 * The php components correspond to code on the server.
 * Included here for illustration, as is the database create statement.
 */
/*
 * bornAfter2.php: 
 * procedural code
 <?php
   mysql_connect("localhost","readonly","gibbley77");
   mysql_select_db("PeopleData");
   if (array_key_exists("year", $_REQUEST)) { 
   $q=mysql_query("SELECT * FROM people WHERE birthyear>'".$_REQUEST['year']."'");
   } else {
   $q=mysql_query("SELECT * FROM people");
   }
   while($e=mysql_fetch_assoc($q))
      $output[]=$e;
 
   print(json_encode($output));
 
   mysql_close();
?>
*/
/*
 * ById.php:
 * procedural code
 <?php
   mysql_connect("localhost","readonly","gibbley77");
   mysql_select_db("PeopleData");
   if (array_key_exists("id", $_REQUEST)) { 
   $q=mysql_query("SELECT * FROM people WHERE id ='".$_REQUEST['id']."'");
   } else {
   $q=mysql_query("SELECT * FROM people");
   }
   while($e=mysql_fetch_assoc($q))
      $output[]=$e;
 
   print(json_encode($output));
 
   mysql_close();
?>
*/
/*
*CREATE TABLE IF NOT EXISTS `people` (
*  `id` int(11) NOT NULL AUTO_INCREMENT, 
*  `name` varchar(100) NOT NULL,
*  `imageUrl` varchar(50) NOT NULL, 
*  `sex` tinyint(1) NOT NULL DEFAULT '1',
*  `birthyear` int(11) NOT NULL,
*   PRIMARY KEY (`id`)  );
*/