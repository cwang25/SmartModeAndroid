package com.dolphin.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.smartmodeelite.DAOSingleton;
import com.dolphin.smartmodeelite.EditWifiActivity;
import com.dolphin.smartmodeelite.MainActivity;
import com.dolphin.smartmodeelite.WifiRecord;
import com.dolphin.wifiInfo.WifiInfoNoQuote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;
import android.app.DialogFragment;

public class ScannedWifiDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{
	String[] arraySSIDscanned;
	public static final int SCAN_WIFI = 1;
	public static final int SAVED_WIFI = 2;
	int listType;
	/**
	 * Constructor
	 * @param listType  type of wifi list, scanning or saved.
	 */
	public ScannedWifiDialogFragment(int listType){
		this.listType = listType;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		WifiManager wifiManager = (WifiManager) this.getActivity()
				.getSystemService(MainActivity.WIFI_SERVICE);
		if(wifiManager.getScanResults()!=null &&listType == SCAN_WIFI){
			//scanning
			List<ScanResult> scannedResult = wifiManager.getScanResults();
			ListIterator<ScanResult> cursor = scannedResult.listIterator();
			List<String> ssidScanned = new ArrayList<String>();
			while (cursor.hasNext()) {
				ssidScanned.add(cursor.next().SSID);
			}
			arraySSIDscanned = ssidScanned.toArray(new String[ssidScanned
					.size()]);
		}else if(wifiManager.getConfiguredNetworks() != null && listType == SAVED_WIFI){
			//saved
			List<WifiConfiguration> scannedResult = wifiManager.getConfiguredNetworks();
			ListIterator<WifiConfiguration> cursor = scannedResult.listIterator();
			List<String> ssidScanned = new ArrayList<String>();
			while (cursor.hasNext()) {
				//WifiConfiguration has quatation marks that need to be removed
				ssidScanned.add(WifiInfoNoQuote.removeQuoteSavedWifiConfigure(cursor.next().SSID));
			}
			arraySSIDscanned = ssidScanned.toArray(new String[ssidScanned
					.size()]);
		}else{
			Toast.makeText(getActivity(),"Turn on Wifi", Toast.LENGTH_SHORT).show();
		}
		// Use the Builder class for convenient dialog construction
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(listType==SCAN_WIFI?"Select Scanned Wifi":"Select Saved Wifi")
				.setItems(arraySSIDscanned,this)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
							}
						});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		// The 'which' argument contains the index
		// position
		// of the selected item
		String ssidScanned = arraySSIDscanned[which];
		if(checkIfEligibleNew(ssidScanned)){
			WifiRecord tmp = new WifiRecord(arraySSIDscanned[which],
					"0,0,0,0,0,0,0");
			Intent intent = new Intent(this.getActivity(),EditWifiActivity.class);
			Bundle nBundle = new Bundle();
			nBundle.putParcelable(MainActivity.KEY_WIFI_RECORD, tmp);
			nBundle.putString(MainActivity.NEW_TAG, "new");
			intent.putExtras(nBundle);
			startActivity(intent);
		}else{
			printMessage(this.getActivity(),"The SSID has already existed. Please edit the existing one instead.");
		}
		
	}
	public void printMessage(Context ctx, String msg){
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}
	public boolean checkIfEligibleNew(String ssid){
		if (ssid != null && !ssid.equals("<unknown ssid>")) {
			SmartModeDAO db = DAOSingleton.getInstance(this.getActivity())
					.getDb();
			int alreadyHave = db.getFromSSIDBySSID(ssid).size();
			if (alreadyHave == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}