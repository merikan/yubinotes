package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class NewFolderDialog extends DialogFragment{
	
	EditText folderTitleView;
	TextView folderTitleText;
	String folderTitle;
	String folderId; 
	
	static NewFolderDialog newInstance(int num){
		NewFolderDialog nfd = new NewFolderDialog();
		
		//Supply num input as argument
		Bundle args = new Bundle();
		args.putInt("num", num);
		nfd.setArguments(args);
		
		return nfd;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderId = getArguments().getString("folderId");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View v = inflater.inflate(R.layout.new_folder_dialog, null, false);
	    builder.setView(v)
	    // Add action buttons
	           .setPositiveButton(R.string.note_create, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // post comment
	            	   folderTitle = folderTitleView.getText().toString();
	            	   
	            	   createNote();
	            	   ListNotesActivity callingActivity = (ListNotesActivity) getActivity();
	                   callingActivity.onNoteCreation();
	               }
	           })
	           .setNegativeButton(R.string.note_cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   NewFolderDialog.this.getDialog().cancel();
	               }
	           });      
	    
	    Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    folderTitleView = (EditText) v.findViewById(R.id.textViewNewFolderTitle);
	    folderTitleText = (TextView) v.findViewById(R.id.textViewNewFolderLabel);
	    folderTitleView.setTypeface(tf);
	    folderTitleText.setTypeface(tf);
	    return builder.create();
	}

	public void createNote(){
		//Database manager
		DbManager db = new DbManager(this.getActivity());
		db.addNote(folderTitle, "", folderId, true);
	}
}