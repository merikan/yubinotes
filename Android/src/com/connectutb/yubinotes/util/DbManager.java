package com.connectutb.yubinotes.util;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import com.connectutb.yubinotes.util.Crypto;

public class DbManager extends SQLiteOpenHelper{
	
	/* Our database variables */
	private static final String DATABASE_NAME = "YubiNotesDB";
	private static final int DATABASE_VERSION = 1;
	
	/* Our tables and fields */
	private static final String TABLE_NOTES= "notes";
	private static final String NOTES_ID = "id";
	private static final String NOTES_PT_TITLE = "pt_title";
	private static final String NOTES_TITLE = "title";
	private static final String NOTES_TEXT = "text";
	private static final String NOTES_CREATED = "created_timestamp";
	private static final String NOTES_MODIFIED = "modified_timestamp";
	private static final String NOTES_VIEWED = "viewed_timestamp";
	
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
				+ NOTES_ID + " INTEGER PRIMARY KEY," + NOTES_PT_TITLE + " TEXT,"
				+ NOTES_TITLE + " TEXT," + NOTES_TEXT + " TEXT,"
				+ NOTES_CREATED + " TIMESTAMP," + NOTES_MODIFIED + " TIMESTAMP,"
				+ NOTES_VIEWED + " TIMESTAMP)";
		db.execSQL(CREATE_NOTES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Figure out how to implement database upgrades without dropping the tables
		
	}

	public boolean addNote(String uTitle, String Title, String Text){
		SQLiteDatabase db = getWritableDatabase();
		//Add a new note
		//Grab current time
		Time now = new Time();
		now.setToNow();
		//Format it in a format SQLite will understand
		String current_time = now.format("%Y-%m-%d %H:%M:%S");
		
		//Encrypt strings
		Crypto crypt = new Crypto("a45848d53140b415","4841514b235f544e");
		
		ContentValues values = new ContentValues();
		try {
			values.put(NOTES_PT_TITLE, Crypto.bytesToHex( crypt.encrypt(uTitle)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		values.put(NOTES_TITLE, Title);
		values.put(NOTES_TEXT, Text);
		values.put(NOTES_CREATED, current_time);
		values.put(NOTES_MODIFIED, current_time);
		values.put(NOTES_VIEWED, current_time);
		
		//Inserting the record
		db.insert(TABLE_NOTES, null, values);
		db.close();
		return true;
	}
	
	public String[] listNotes(){
		//Retrieve a string array with the history
				ArrayList<String> temp_array = new ArrayList<String>();
				String[] notes_array = new String[0];
				//SQL 
				String sqlQuery = "SELECT * FROM " + TABLE_NOTES;
				//Define database and cursor
				SQLiteDatabase db = this.getWritableDatabase(); 
				Cursor c = db.rawQuery(sqlQuery, null);

				//Loop through the results and add it to the temp_array
				if (c.moveToFirst()){
					do{
						temp_array.add(c.getString(c.getColumnIndex(NOTES_ID)) + ";" + c.getString(c.getColumnIndex(NOTES_PT_TITLE))+ ";" 
								+ c.getString(c.getColumnIndex(NOTES_CREATED)) +  ";" + c.getString(c.getColumnIndex(NOTES_MODIFIED))); 			
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
