package com.connectutb.yubinotes.util;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.connectutb.yubinotes.util.Crypto;

public class DbManager extends SQLiteOpenHelper{
	private static final String TAG ="YubiNotes";
	
	/* Our database variables */
	private static final String DATABASE_NAME = "YubiNotesDB";
	private static final int DATABASE_VERSION = 1;
	
	/* Our tables and fields */
	private static final String TABLE_NOTES= "notes";
	private static final String NOTES_ID = "id";
	private static final String NOTES_TITLE = "title";
	private static final String NOTES_TEXT = "text";
	private static final String NOTES_DIR = "dir";
	private static final String NOTES_CREATED = "created_timestamp";
	private static final String NOTES_MODIFIED = "modified_timestamp";
	private static final String NOTES_VIEWED = "viewed_timestamp";	

	//Context
	private Context context;

	//constructor
	public DbManager(Context context){
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the tables
		String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
				+ NOTES_ID + " INTEGER PRIMARY KEY,"
				+ NOTES_TITLE + " TEXT," + NOTES_TEXT + " TEXT," + NOTES_DIR + " TEXT,"
				+ NOTES_CREATED + " TIMESTAMP," + NOTES_MODIFIED + " TIMESTAMP,"
				+ NOTES_VIEWED + " TIMESTAMP)";
		db.execSQL(CREATE_NOTES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Figure out how to implement database upgrades without dropping the tables
		
	}
	
	public String cryptoString(String str, boolean wantDecrypt){
		//Get iv and key
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String iv = settings.getString("crypt3", "");
		String key = settings.getString("crypt4","");
		Log.d(TAG,iv + " - " + key);
		//Encrypt or decrypt strings
		Crypto crypt = new Crypto(iv,key);
		String result = "";
			try {
				if (wantDecrypt){
					result = new String(crypt.decrypt(str));
				}else{
					result = Crypto.bytesToHex(crypt.encrypt(str));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"Crypto failed: " + e.getMessage());
				e.printStackTrace();
			}
		
		return result;
	}

	public boolean addNote(String Title, String Text, String folderId){
		SQLiteDatabase db = getWritableDatabase();
		//Add a new note
		//Grab current time
		Time now = new Time();
		now.setToNow();
		//Format it in a format SQLite will understand
		String current_time = now.format("%Y-%m-%d %H:%M:%S");

		ContentValues values = new ContentValues();
		values.put(NOTES_TITLE, cryptoString(Title,false));
		values.put(NOTES_TEXT, cryptoString(Text,false));
		values.put(NOTES_DIR, folderId);
		values.put(NOTES_CREATED, current_time);
		values.put(NOTES_MODIFIED, current_time);
		values.put(NOTES_VIEWED, current_time);
		
		//Inserting the record
		db.insert(TABLE_NOTES, null, values);
		db.close();
		return true;
	}
	
	public String[] listNotes(String dirId){
		//Retrieve a string array with the history
				ArrayList<String> temp_array = new ArrayList<String>();
				String[] notes_array = new String[0];
				//SQL 
				String sqlQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + NOTES_DIR + " ='"+dirId+"'";
				//Define database and cursor
				SQLiteDatabase db = this.getWritableDatabase(); 
				Cursor c = db.rawQuery(sqlQuery, null);

				//Loop through the results and add it to the temp_array
				if (c.moveToFirst()){
					do{
						temp_array.add(c.getString(c.getColumnIndex(NOTES_ID)) + ";" + cryptoString(c.getString(c.getColumnIndex(NOTES_TITLE)),true)+ ";" 
								+ c.getString(c.getColumnIndex(NOTES_TEXT)) +  ";" + c.getString(c.getColumnIndex(NOTES_DIR))+ ";" + c.getString(c.getColumnIndex(NOTES_MODIFIED))); 			
					}while(c.moveToNext());
				}

				//Close cursor
				c.close();
				//Convert from arraylist to string array
				notes_array = (String[]) temp_array.toArray(notes_array);
				//Return the string array
				return notes_array;
	}
}