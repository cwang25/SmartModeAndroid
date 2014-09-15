package com.dolphin.smartmodeelite;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dolphin.wifiInfo.WifiInfoNoQuote;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiRecord implements Parcelable{
	private String ssid;
	private String mode;
	public WifiRecord(String sid, String m) {
		// TODO Auto-generated constructor stub
		this.ssid = sid;
		this.mode = m;
	}
	public WifiRecord(Parcel in) {
		// TODO Auto-generated constructor stub
		this.ssid = in.readString();
		this.mode = in.readString();
	}
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	@Override
	public String toString() {
		return WifiInfoNoQuote.removeQuoteIfNeeded(ssid);
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(ssid);
		dest.writeString(mode);	
	}

	public static final Parcelable.Creator<WifiRecord> CREATOR = new Parcelable.Creator<WifiRecord>() {
		public WifiRecord createFromParcel(Parcel in) {
			return new WifiRecord(in);
		}

		public WifiRecord[] newArray(int size) {
			return new WifiRecord[size];
		}
	};
	
	
	

}
