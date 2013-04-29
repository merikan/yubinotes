package com.connectutb.yubinotes;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordDialog extends DialogFragment{
	
	String passwordstr;
	TextView passwordTitle;
	EditText passwordText;
	boolean newPassword = false;
	
	static PasswordDialog newInstance(int num){
		PasswordDialog pd = new PasswordDialog();
		
		//Supply num input as argument
		Bundle args = new Bundle();
		args.putInt("num", num);
		pd.setArguments(args);
		
		return pd;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        newPassword = getArguments().getBoolean("newPassword");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View v = inflater.inflate(R.layout.password_dialog, null, false);
	    
	    passwordTitle = (TextView) v.findViewById(R.id.textViewPasswordLabel);
	    passwordText = (EditText) v.findViewById(R.id.editTextPassword);
	    if (newPassword){
	    	passwordTitle.setText(getActivity().getString(R.string.set_password));
	    }
	    Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    passwordTitle.setTypeface(tf);
	    passwordText.setTypeface(tf);
	    
	    builder.setView(v)
	    // Add action buttons
	           .setPositiveButton(R.string.password_submit, new DialogInterface.OnClickListener() {
	        	   
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // Hash input password
	            	   passwordstr = passwordText.getText().toString();
	            	   
	            	   try {
						passwordstr = Base64.encodeToString(getHash(passwordstr), Base64.DEFAULT);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            	   
	            	   MainActivity callingActivity = (MainActivity) getActivity();
	                   callingActivity.checkPassword(passwordstr, newPassword);
	               }
	           })
	           .setNegativeButton(R.string.note_cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   PasswordDialog.this.getDialog().cancel();
	               }
	           });      

	    return builder.create();
	}
	
	public byte[] getHash(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	       MessageDigest digest = MessageDigest.getInstance("SHA-1");
	       digest.reset();
	       byte[] input = digest.digest(password.getBytes("UTF-8"));
	       return input;
	 }

}