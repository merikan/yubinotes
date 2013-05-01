package com.connectutb.yubinotes.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class LockTimerService extends IntentService {
	  private Context context;
	  /** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public LockTimerService(Context context) {
	      super("LockTimerService");
		  this.context = context;
	  }
	  
	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */

	@Override
	protected void onHandleIntent(Intent intent) {
		/* First we create */
		
	}
}
