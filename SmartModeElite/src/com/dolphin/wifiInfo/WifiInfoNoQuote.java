package com.dolphin.wifiInfo;

import android.os.Build;

/**
 * A class to remove quotation marks from SSID if needed.
 * @author Chi-Han Wang
 *
 */
public class WifiInfoNoQuote {
	
	public static String removeQuoteIfNeeded(String ssid) {
		int deviceVersion= Build.VERSION.SDK_INT;

	     if (deviceVersion >= 17){
	         if (ssid.startsWith("\"") && ssid.endsWith("\"")){
	             ssid = ssid.substring(1, ssid.length()-1);
	         }
	     }

	     return ssid;
	}
	
	public static String removeQuoteSavedWifiConfigure(String ssid) {

		if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
			ssid = ssid.substring(1, ssid.length() - 1);
		}

		return ssid;
	}
	

}
