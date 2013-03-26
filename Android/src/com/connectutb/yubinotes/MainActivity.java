package com.connectutb.yubinotes;

import android.os.Bundle;
import android.app.ListActivity;
import android.view.Menu;

public class MainActivity extends ListActivity {
	
	private String[] nav_items = new String[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		nav_items = getResources().getStringArray(R.array.nav_items);
		setListAdapter(new MainListAdapter(this, nav_items));	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
