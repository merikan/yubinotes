package com.connectutb.yubinotes.util;

import com.connectutb.yubinotes.MainActivity;
import com.connectutb.yubinotes.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class LockTimerService extends IntentService {
	  public int nId = 1;
	  public int intervalInSecs;
	  public SharedPreferences settings;
	  public SharedPreferences.Editor editor;
	  public NotificationManager mNotificationManager;
	  /** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public LockTimerService() {
	      super("LockTimerService");
	      Log.d("YubiNotes", "Service started");
	  }
	  
	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("YubiNotes", "onHandleIntent");
		  /* Load our preferences */
		settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = settings.edit();
		intervalInSecs = Integer.valueOf(settings.getString("timelock_interval", "0"));
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		/* First we create a notification if set to do so */
		if (settings.getBoolean("timelock_notifcation", false) == true){
			updateNotification(intervalInSecs);
			Log.d("YubiNotes", "Showing notification");
		}
		/* Then we lock the notes after a period of time */
		int timeLeft = intervalInSecs;
		long endTime = System.currentTimeMillis() + intervalInSecs * 1000;
	      while (System.currentTimeMillis() < endTime) {
	          synchronized (this) {
	              try {
	            	  //Wait 1 second before updating notification
	                  wait(1000);
	                  //Check if notes are suddenly locked, if so, stop the service.
	                  if (settings.getBoolean("isLocked", true)==true){
	                	  timeLeft = 0;
	                	  //Dismiss the notification
	                	  mNotificationManager.cancelAll();
	                  }else{
	                  timeLeft -= 1;
	                  updateNotification(timeLeft);
	                  }
	              } catch (Exception e) {
	              }
	          }
	      }
	      	
	      	//Time's up, lock the notes if we are not actively using the note list.
	      	while (settings.getBoolean("inUse", true)){
	      		try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	      	}
	    	editor.putString("crypt3", "0000000000000000");
	    	editor.putString("crypt4", "0000000000000000");
	    	editor.putBoolean("isLocked", true);
	    	editor.commit();
	    		    
	    	mNotificationManager.cancelAll();
	    	Toast.makeText(this, R.string.keys_locked, Toast.LENGTH_SHORT).show();
	    	Log.d("YubiNotes", "Locking notes from service");
	    	stopSelf();
	}
	
	public void updateNotification(int time){
		
		String text = getString(R.string.notify_lock_text) + " " + String.valueOf(time) + " " + getString(R.string.generic_seconds);
		if (time == 0){
			text = getString(R.string.noitfy_lock_pending);
		}
		//Check that time is greater than 0, if not close down shop.
		if (time < 0){
			mNotificationManager.cancelAll();
		}else{
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_launcher)
			        .setContentTitle(getString(R.string.notify_lock_title))
			        .setContentText(text);
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this, MainActivity.class);
	
			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(MainActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			mBuilder.setProgress(intervalInSecs, time, false);
	        // Displays the progress bar for the first time.
			mBuilder.setContentIntent(resultPendingIntent);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(nId, mBuilder.build());
		}
	}
}
