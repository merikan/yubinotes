package com.connectutb.yubinotes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends ListActivity {
	
	private String[] nav_items = new String[0];
	private String otp = "NA";
	private String TAG = "YubiNotes";
	
	private static final Pattern otpPattern = Pattern.compile("^.*([cbdefghijklnrtuv]{44})$");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		nav_items = getResources().getStringArray(R.array.nav_items);
		setListAdapter(new MainListAdapter(this, nav_items));	
		
		Bundle extras = getIntent().getExtras();
		
		if(extras != null && extras.containsKey("otp")) {
			otp = extras.getString("otp");
			Log.d(TAG, "received otp '" + otp + "' from extras.");
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
    	super.onListItemClick(l, v, position, id);
    	// We retrieve the item that was clicked
    	Object o = this.getListAdapter().getItem(position);
    	String keyword = o.toString();
        Intent i = new Intent(MainActivity.this, ListNotesActivity.class);
        i.putExtra("list", keyword);
        startActivity(i);
    }
    
    public void onPause() {
        super.onPause();
        // disable foreground dispatch when we're paused
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
    	PendingIntent pendingIntent = PendingIntent.getActivity(
    			this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    	// register for all NDEF tags starting with http och https
    	IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    	ndef.addDataScheme("http");
    	ndef.addDataScheme("https");
    	// register for foreground dispatch so we'll receive tags according to our intent filters
    	NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(
    			this, pendingIntent, new IntentFilter[] {ndef}, null);
        
    	String data = getIntent().getDataString();
        if(data != null) {
        	Matcher otpMatch = otpPattern.matcher(data);
        	if(otpMatch.matches()) {
        		otp = otpMatch.group(1);
        		Log.d(TAG,"OTP: " + otp);
        	} else {
        		Log.i(TAG, "data from ndef didn't match, it was: " + data);
        	}
        }
    }

    public void onNewIntent(Intent intent) {
    	// get the actual URI from the ndef tag
    	String data = intent.getDataString();
        Log.d(TAG, "data: " + data);
       
    }
    
    
}