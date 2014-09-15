package com.dolphin.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.smartmodeelite.DAOSingleton;
import com.dolphin.smartmodeelite.EditWifiActivity;
import com.dolphin.smartmodeelite.MainActivity;
import com.dolphin.smartmodeelite.R;
import com.dolphin.smartmodeelite.WifiRecord;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;
import android.app.DialogFragment;

public class IntroDefaultSetDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.default_set_intro_title)
				.setMessage(R.string.default_set_intro)
				.setPositiveButton("Ok!",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
		/*
		builder.setTitle("Select Scanned Wifi")
				.setItems(arraySSIDscanned,this)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
		*/
		// Create the AlertDialog object and return it
		return builder.create();
	}


	public void printMessage(Context ctx, String msg){
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

}