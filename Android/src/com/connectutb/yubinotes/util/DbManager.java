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
	private static final String NOTES_TRASH = "trashed";
	private static final String NOTES_STARRED = "starred";
	private static final String NOTES_TYPE = "type";

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
				+ NOTES_VIEWED + " TIMESTAMP," + NOTES_TRASH + " INTEGER,"
				+ NOTES_STARRED + " INTEGER," + NOTES_TYPE + " INTEGER)";
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
				//If encryption failed, return encrypted string (looks better)
				result = str;
			}
		
		return result;
	}

	public boolean addNote(String Title, String Text, String folderId, boolean isFolder){
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
		values.put(NOTES_TRASH, 0);
		values.put(NOTES_STARRED, 0);
		if (isFolder){
			values.put(NOTES_TYPE,0);
		}else{
			values.put(NOTES_TYPE,1);
		}
		
		//Inserting the record
		db.insert(TABLE_NOTES, null, values);
		db.close();
		return true;
	}
	
	public boolean updateNote(String title, String text, int noteId){
		SQLiteDatabase db = getWritableDatabase();
		//update note
		String encTitle = cryptoString(title,false);
		String encText = cryptoString(text, false);
		//Grab current time
		Time now = new Time();
		now.setToNow();
		//Format it in a format SQLite will understand
		String current_time = now.format("%Y-%m-%d %H:%M:%S");

		String sql = "UPDATE " + TABLE_NOTES + " SET " + NOTES_TITLE + "='" + encTitle + "', "
		+ NOTES_TEXT + "='" + encText + "', " + NOTES_MODIFIED + "='" + current_time + "' WHERE " + NOTES_ID + "=" + noteId;
		db.execSQL(sql);
		db.close();
		return true;
	}
	

	public void deleteNotes(String noteId, boolean trashMode){
		//Delete note or folders and notes associated with that folder
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "DELETE FROM " + TABLE_NOTES + " WHERE " + NOTES_ID + "=" + noteId;
		String sql2 = "DELETE FROM " + TABLE_NOTES + " WHERE " + NOTES_DIR + "=" + noteId;
		if (trashMode){
			sql ="UPDATE " + TABLE_NOTES + " SET " + NOTES_TRASH + "=1 WHERE " + NOTES_ID +"=" + noteId;
			sql2 = "UPDATE " + TABLE_NOTES + " SET " + NOTES_STARRED + "=1 WHERE " + NOTES_DIR +"=" + noteId;
		}	
		db.execSQL(sql);
		db.execSQL(sql2);
		db.close();
	}
	
	public void setFavoriteNotes(String noteId, boolean setFav){
		//We like to pick favorites.
		SQLiteDatabase db = this.getWritableDatabase();
		String sql;
		if (setFav){
			sql ="UPDATE " + TABLE_NOTES + " SET " + NOTES_STARRED + "=1 WHERE " + NOTES_ID +"=" + noteId;
		}else{
			sql ="UPDATE " + TABLE_NOTES + " SET " + NOTES_STARRED + "=0 WHERE " + NOTES_ID +"=" + noteId;
		}
		db.execSQL(sql);
		db.close();
	}
	
	public String[][] listNotes(String dirId, int mode){
		/**
		 * MODES
		 * 0 - All Notes (except trashed)
		 * 1 - Recent
		 * 2 - Starred
		 * 3 - Trashed
		 */
		//Retrieve a string array with the history
		
		ArrayList<String[]> notesList = new ArrayList<String[]>();
		String[][] notesArray = new String[0][10];
		
		//SQL 
		String sqlQuery = "";
		if (mode==0){
			sqlQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + NOTES_DIR + " ='"+dirId+"' AND "+ NOTES_TRASH + "=0";
		}else if (mode==1){
			sqlQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + NOTES_DIR + " ='"+dirId+"' AND "+ NOTES_TRASH + "=0 ORDER BY " + NOTES_MODIFIED + " DESC";
		}else if (mode==2){
			sqlQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + NOTES_DIR + " ='"+dirId+"' AND "+ NOTES_STARRED + "=1 OR " + NOTES_DIR + " ='"+dirId+"' AND "+ NOTES_TYPE + "=0";	
			Log.d(TAG, "Listing favorite notes");
		}else if (mode==3){
			Log.d(TAG, "Selecting trashed");
			sqlQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + NOTES_DIR + " ='"+dirId+"' AND "+ NOTES_TRASH + "=1";
		}
		//Define database and cursor
		SQLiteDatabase db = this.getWritableDatabase(); 
		Cursor c = db.rawQuery(sqlQuery, null);

		//Loop through the results and add it to the temp_array
		if (c.moveToFirst()){
			do{
				
				ArrayList<String> noteList = new ArrayList<String>();
				 String[] noteArray = new String[8];
				 String extStr;
				 for (int i = 0; i < c.getColumnCount(); i++){
					 if (i == 1 || i == 2){
						 extStr = cryptoString(c.getString(i), true);
					 }else{
						 extStr = c.getString(i);
						
					 }
					 noteList.add(extStr);
				 }
				 notesList.add((String[]) noteList.toArray(noteArray));
				
			
			}while(c.moveToNext());
		}

		//Close cursor and database
		c.close();
		db.close();
		//Convert from arraylist to string array
		notesArray = (String[][]) notesList.toArray(notesArray);
		
		//Return the string array
		return notesArray;
	}
}