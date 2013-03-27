package com.connectutb.yubinotes.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
