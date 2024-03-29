package com.connectutb.yubinotes;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.connectutb.yubinotes.util.LockTimerService;

import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	/* Our preferences */
	public SharedPreferences settings;
	public SharedPreferences.Editor editor;
	
	public Context context;
	
	private String[] nav_items = new String[0];
	private String otp = "NA";
	private String TAG = "YubiNotes";
	
	private boolean isLocked = true;
	
	private ListView catList;
	private TextView statusText;
	private RelativeLayout statusLayout;
	
	private static final Pattern otpPattern = Pattern.compile("^.*([cbdefghijklnrtuv]{44})$");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this;
		setContentView(R.layout.main_layout);
		catList = (ListView) findViewById(R.id.listViewNoteCategories);
		statusText = (TextView) findViewById(R.id.textViewLockStatus);
		statusLayout = (RelativeLayout) findViewById(R.id.layoutStatus);
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
		statusText.setTypeface(tf);
		/* Load our preferences */
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		editor = settings.edit();
		
		if (settings.getBoolean("use_trash", true)==false){
			nav_items = getResources().getStringArray(R.array.nav_items_alt);
		}else{
			nav_items = getResources().getStringArray(R.array.nav_items);
		}
		catList.setAdapter(new MainListAdapter(this, nav_items));	
		
		Bundle extras = getIntent().getExtras();
		
		
		if (settings.getString("crypt1", "").length() < 3){
			Log.d(TAG, "Generating security keys");
			generateUID();
		}
		
    	//Wipe the keys from settings when we start the app if autolock is enabled
		if (settings.getBoolean("autolock", true)==true){
			lockKeys();
		}
		
		//Update lock status
		isLocked = settings.getBoolean("isLocked", true);
		
		/* This code was retrieved from the YubiKey app, but it doesnt seem the YubiKey works that way anymore */
		if(extras != null && extras.containsKey("otp")) {
			otp = extras.getString("otp");
		}
		
		if (settings.getBoolean("firstRun", true)==true){
			// Show the setup dialog
			showWelcomeDialog();
		}
		
		updateStatusIndicator(isLocked);
		catList.setOnItemClickListener(new OnItemClickListener(){


			@Override
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				//Update lock status
				isLocked = settings.getBoolean("isLocked", true);
				updateStatusIndicator(isLocked);
		        Intent i = new Intent(MainActivity.this, ListNotesActivity.class);
		        i.putExtra("mode", (int)id);
		    	//If ignore lock is disabled, only proceed if notes are unlocked
		    	if (settings.getBoolean("ignore_lock", false)==true){
		    		startActivity(i);
		    	}else{
		    		if (isLocked){
		    			Toast.makeText(getBaseContext(), R.string.unlock_first, Toast.LENGTH_SHORT).show();
		    			
		    			//If we use password unlock, ask users for password here
		    			if (settings.getBoolean("use_yubi", true)==false){
		    				if (settings.getBoolean("password_set",false)==false){
		    					showPasswordDialog(true);
		    				}else {
		    					showPasswordDialog(false);
		    				}
		    			}
		    		}else{
		    			startActivity(i);
		    		}
		    	}
			}
			});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
	    if (isLocked){
	        menu.getItem(0).setEnabled(false);
	    }
	    return true;
	   
	}
	
	public void startLockTimer(){
		if (settings.getBoolean("timelock", true) == true){
			Intent intent = new Intent(context, LockTimerService.class);
			startService(intent);
		}
	}
	
	/* Action on menu selection */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	//Go home
    	case android.R.id.home:
    		Intent i = new Intent(this, MainActivity.class);
        	startActivity(i);	 
    		return true;
    	//Settings
    	case R.id.action_settings:
    		//Update lock status
			isLocked = settings.getBoolean("isLocked", true);
			updateStatusIndicator(isLocked);
    		Intent settingsIntent = new Intent(this, Preferences.class);
    		if (isLocked){
    			Toast.makeText(getBaseContext(), R.string.unlock_first, Toast.LENGTH_SHORT).show();
    		}else{
        	startActivity(settingsIntent);	 
    		}
    		return true;
    	//Lock Notes
    	case R.id.action_lock:
    		lockKeys();
    		return true;
    	case R.id.action_feedback:
    		/* Show app listing */
        	Intent intent = new Intent(Intent.ACTION_VIEW); 
        	intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.connectutb.yubinotes")); 
        	startActivity(intent);
        	return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
	
	public void generateUID(){
		String id1 = UUID.randomUUID().toString().replace("-", "");
		String id2 = UUID.randomUUID().toString().replace("-","");
		editor.putString("crypt1", id1.substring(0,16));
		editor.putString("crypt2", id2.substring(0,16));
		editor.commit();
	}
    
    public void showPasswordDialog(boolean newPassword){
    	int mStackLevel = 1;

	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("yubinotepwdialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
	    
        Bundle args = new Bundle();
	    // Create and show the dialog.
        args.putBoolean("newPassword", newPassword);
	    DialogFragment newFragment = PasswordDialog.newInstance(mStackLevel);
	    newFragment.setArguments(args);
	    newFragment.show(ft, "yubinotepwdialog");
    }
    
    public void showWelcomeDialog(){
    	int mStackLevel = 1;

	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("yubinotewelcomedialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
	    
        Bundle args = new Bundle();
	    // Create and show the dialog.
	    DialogFragment newFragment = WelcomeDialog.newInstance(mStackLevel);
	    newFragment.setArguments(args);
	    newFragment.show(ft, "yubinotewelcomedialog");
    }
    
    public void onModeSelection(boolean yubiMode){
    	//If we selected yubikey mode, set that in the settings
    	if (yubiMode){
    		try{
    	        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    	        editor.putBoolean("nfc_present", true);
    	        }catch (NullPointerException e){
    	        	//No NFC present
    	    		Log.d(TAG, "No NFC Present, moving on");
    	    		editor.putBoolean("nfc_present", false);
    	        }
    		
    		if(settings.getBoolean("nfc_present", true)==true){
    			Toast.makeText(this, R.string.yubi_confirmation, Toast.LENGTH_SHORT).show();
    		}else{
    			//NFC not present..
    			Toast.makeText(this,R.string.yubi_no_nfc, Toast.LENGTH_LONG).show();
    		}
    		editor.putBoolean("use_yubi", true);
    	}else{
    		Toast.makeText(this, R.string.password_confirmation, Toast.LENGTH_SHORT).show();
    		editor.putBoolean("use_yubi", false);
    	}
		editor.putBoolean("firstRun", false);
    	editor.commit();
    }
    
    public void onPause() {
        super.onPause();
        // disable foreground dispatch when we're paused
        try{
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
        editor.putBoolean("nfc_present", true);
        }catch (NullPointerException e){
        	//No NFC present
    		Log.d(TAG, "No NFC Present, moving on");
    		editor.putBoolean("nfc_present", false);
        }
    }
    
    @Override
    public void onResume() { 	
        super.onResume();
    	PendingIntent pendingIntent = PendingIntent.getActivity(
    			this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    	// lock keys (I think its a good idea)
    	/* Load our preferences */
		settings = PreferenceManager.getDefaultSharedPreferences(this);
    	if (settings.getBoolean("autolock", true) == true){
    		//.. Its not a good idea..
    	}
    	// register for all NDEF tags starting with http and https
    	IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    	ndef.addDataScheme("http");
    	ndef.addDataScheme("https");
    	try{
	    	// register for foreground dispatch so we'll receive tags according to our intent filters
	    	NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(
	    			this, pendingIntent, new IntentFilter[] {ndef}, null);
	        
	    	String data = getIntent().getDataString();
	    	editor.putBoolean("nfc_present", true);
	        if(data != null) {
	        	handleOTP(data);
	        }
    	} catch (NullPointerException e){
    		//No NFC present
    		Log.d(TAG, "No NFC Present, moving on");
    		editor.putBoolean("nfc_present", false);
    	}
    }
    
    public void handleOTP(String data){
    	Matcher otpMatch = otpPattern.matcher(data);
    	if(otpMatch.matches()) {
    		otp = otpMatch.group(1);
    		Log.d(TAG,"OTP: " + otp);
    		if (settings.getBoolean("use_yubi", true)==true){
    			unlockNotesYubiOffline();
    		}else{
    			Toast.makeText(this, R.string.yubi_disabled, Toast.LENGTH_SHORT).show();
    		}
    	} else {
    		Log.i(TAG, "data from ndef didn't match, it was: " + data);
    	}
    }
    
    public void lockKeys(){
    	//Wipe the keys from settings
    	editor.putString("crypt3", "0000000000000000");
    	editor.putString("crypt4", "0000000000000000");
    	editor.putBoolean("isLocked", true);
    	editor.commit();
    	Toast.makeText(this, R.string.keys_locked, Toast.LENGTH_SHORT).show();
    	Log.d(TAG, "Locking notes");
    	isLocked = true;
    	updateStatusIndicator(isLocked);
    	invalidateOptionsMenu();
    }
    
    public void updateStatusIndicator(boolean locked){
    	locked = settings.getBoolean("isLocked", true);
    	Log.d(TAG, "Updating lock status");
    	if (locked){
    		statusLayout.setBackgroundColor(Color.parseColor("#ed0000"));
    		statusText.setText(R.string.status_notes_locked);
    	}else{
    		statusLayout.setBackgroundColor(Color.parseColor("#7bab32"));
    		statusText.setText(R.string.status_notes_unlocked);
    	}
    	invalidateOptionsMenu();
    }
    public void unlockNotesYubiOffline(){
    	/*
    	 * Generates the IV and secret key using XOR
    	 */
    	
    	String ivs = settings.getString("crypt1", "");
    	String ivkey = otp.substring(0,16);
    	String secrets = settings.getString("crypt2", "");
    	
    	//XOR the  IV
    	String iv = xorTheKeys(ivs, ivkey);
    	//XOR the Secret Key
    	String secret = xorTheKeys(secrets, ivkey);
    	
    	editor.putString("crypt3", iv);
    	editor.putString("crypt4", secret);
    	editor.putBoolean("isLocked", false);
    	editor.commit();
    	isLocked = false;
    	updateStatusIndicator(isLocked);
    	invalidateOptionsMenu();
    	Toast.makeText(this, R.string.keys_unlocked, Toast.LENGTH_SHORT).show();
    	startLockTimer();
    }
    
    public String xorTheKeys(String s, String key ){
    	
    	byte[] bResult = xorWithKey(s.getBytes(), key.getBytes());
    	String result = Base64.encodeToString(bResult, Base64.DEFAULT);
    	
    	Log.d(TAG, "Result: " + result.substring(0,16));
    	
    	return result.substring(0,16);
    }
    
    private byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        
        return out;  
    }
    

    
    public void checkPassword(String hash, boolean newPassword){
    	String iv = settings.getString("crypt1", "");
    	String secret = settings.getString("crypt2", "");
    	//Save or check password
    	if (newPassword){
    		editor.putString("password", hash);
    		editor.putBoolean("password_set",true);
    		editor.commit();
    	}
    	if (settings.getString("password", "0").equals(hash)){
    			//Password hash matches, unlock notes
    			editor.putString("crypt3", xorTheKeys(hash.substring(0,16), iv));
    	    	editor.putString("crypt4", xorTheKeys(hash.substring(4,20),secret));
    	    	editor.putBoolean("isLocked", false);
    	    	editor.commit();
    	    	isLocked = false;
    	    	updateStatusIndicator(isLocked);
    	    	invalidateOptionsMenu();
    	    	startLockTimer();
    			Toast.makeText(this, R.string.keys_unlocked, Toast.LENGTH_SHORT).show();
    		} else{
    			Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
    		}
    }

    public void onNewIntent(Intent intent) {
    	// get the actual URI from the ndef tag
    	String data = intent.getDataString();
        if(data != null) {
        	handleOTP(data);
        }
    }  
}