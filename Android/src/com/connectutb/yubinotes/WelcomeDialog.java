package com.connectutb.yubinotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class WelcomeDialog extends DialogFragment{
	
	static WelcomeDialog newInstance(int num){
		WelcomeDialog wd = new WelcomeDialog();
		
		Bundle args = new Bundle();
		args.putInt("num", num);
		wd.setArguments(args);
		return wd;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    View v = inflater.inflate(R.layout.welcome_dialog, null, false);
	    builder.setView(v)
	    // Add action buttons
	           .setPositiveButton(R.string.use_yubi, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                  
	            	   MainActivity callingActivity = (MainActivity) getActivity();
	                   callingActivity.onModeSelection(true);
	               }
	           })
	           .setNegativeButton(R.string.use_password, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   MainActivity callingActivity = (MainActivity) getActivity();
	                   callingActivity.onModeSelection(false);
	               }
	           });      

	    return builder.create();
	}

}