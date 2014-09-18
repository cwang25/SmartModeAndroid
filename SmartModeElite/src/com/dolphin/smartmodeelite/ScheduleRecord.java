package com.dolphin.smartmodeelite;

import android.R.array;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

public class ScheduleRecord implements Parcelable{
	private String schedulename;
	private String mode;
	private int startTime;
	private int endTime;
	private String [] parsedMode;
	public static final int HOUR_MASK = 100;
	/**
	 * constructor
	 * @param sn Schedule Name
	 * @param m Mode String
	 * @param st starting time will be stored in integer. 0000(HHMM)
	 * @param et end time will be stored in integer. 0000  (HHMM)
	 */
	public ScheduleRecord(String sn, String m, int st, int et) {
		// TODO Auto-generated constructor stub
		this.schedulename = sn;
		this.startTime = st;
		this.endTime = et;
		this.mode = m;
		parsedMode = mode.split(",");
	}
	/**
	 * constructor with formatted string input.
	 * @param sn name
	 * @param m mode string
	 * @param st start time string (00:00)
	 * @param et end time string (00:00)
	 */
	public ScheduleRecord(String sn, String m, String st, String et , boolean is24Hour) {
		// TODO Auto-generated constructor stub
		this.schedulename = sn;
		this.startTime = parseTime(st,is24Hour);
		this.endTime = parseTime(et, is24Hour);
		this.mode = m;
		parsedMode = mode.split(",");
	}
	/**
	 * Parse time from formatted string.
	 * @param input Formatted string (00:00)
	 * @param hour24 user preference if input string is 24 format or not.
	 * @return int value represent time (0000);
	 */
	public static int parseTime(String input, boolean hour24Format){
		if(hour24Format){
			String [] t = input.split(":");
			int hour = Integer.parseInt(t[0]);
			int minute = Integer.parseInt(t[1]);
			return hour * HOUR_MASK + minute;
		}else{
			boolean isAM = true;
			String [] seperateAMPM = input.split(" ");
			if(seperateAMPM[seperateAMPM.length-1].equals("PM"))
				isAM = false;
			String [] parseTime = seperateAMPM[0].split(":");
			int hour = Integer.parseInt(parseTime[0]);
			int minute = Integer.parseInt(parseTime[1]);
			if(!isAM){
				hour = hour + 12;
			}
			hour = hour == 12 ? 0 : hour;
			hour = hour == 24 ? 12: hour;
			return hour*HOUR_MASK + minute;
		}
		
	}
	
	public ScheduleRecord(Parcel in) {
		this.schedulename = in.readString();
		this.mode = in.readString();
		this.startTime = in.readInt();
		this.endTime = in.readInt();
		parsedMode = mode.split(",");
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return schedulename;
	}
	public String getSchedulename() {
		return schedulename;
	}
	public void setSchedulename(String schedulename) {
		this.schedulename = schedulename;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getEndTime() {
		return endTime;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	public String getMode() {
		return mode;
	}
	
	/**
	 * Get phone mode
	 * @return mute, vibrate, normal
	 */
	public String getPhoneMode(){
		try{
			if(this.parsedMode[0] == "1"){
				return "mute";
			}
			if(this.parsedMode[1] == "1"){
				return "vibrate";
			}
			if(this.parsedMode[2] == "1"){
				return "normal";
			}
			return "normal";
		} catch (IndexOutOfBoundsException e){
			return null;
		}
	}
	
	public String getRingToneVol(){
		try{
			return parsedMode[3];
		}catch(IndexOutOfBoundsException e){
			return "0";
		}
	}
	
	public String getMusicVol(){
		try{
			return parsedMode[4];
		}catch(IndexOutOfBoundsException e){
			return "0";
		}
	}
	public String getAlarmVol(){
		try{
			return parsedMode[5];
		}catch(IndexOutOfBoundsException e){
			return "0";
		}
	}
	
	public String getNotificationVol(){
		try{
			return parsedMode[6];
		}catch(IndexOutOfBoundsException e){
			return "0";
		}
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	public int getStartTimeHour(){
		return this.startTime/HOUR_MASK;
	}
	public int getEndTimeHour(){
		return this.endTime/HOUR_MASK;
	}
	public int getStartTimeMinute(){
		return this.startTime%HOUR_MASK;
	}
	public int getEndTimeMinute(){
		return this.endTime%HOUR_MASK;
	}
	public void setStartTimeHour(int t){
		int hour = t * HOUR_MASK;
		int minute = startTime%HOUR_MASK;
		this.startTime= hour + minute;
	}
	public void setEndTimeHour(int t){
		int hour = t * HOUR_MASK;
		int minute = endTime%HOUR_MASK;
		this.endTime= hour + minute;
	}
	public void setStartTimeMinute(int t){
		int hour = startTime/HOUR_MASK;
		int minute = t;
		this.startTime = hour * HOUR_MASK + minute;
	}
	public void setEndTimeMinute(int t){
		int hour = endTime/HOUR_MASK;
		int minute = t;
		this.endTime = hour * HOUR_MASK + minute;
	}
	
	/**
	 * Get start time from the schedule.
	 * @boolean 24 hours format or not. 
	 * @return Formatted string for print or show to user.
	 */ 
	public String getStartTimeString(boolean Hours24){
		return parseTimeString(startTime,Hours24);
	}
	
	/**
	 * Get end time from the schedule.
	 * @boolean 24 hours format or not. 
	 * @return Formatted string for print or show to user.
	 */ 
	public String getEndTimeString(boolean Hours24){
		return parseTimeString(endTime,Hours24);
	}
	
	/**
	 * Get string time from given int vale (0000).
	 * @boolean 24 hours format or not. 
	 * @int time (0000)  (HHMM)
	 * @return Formatted string for print or show to user.
	 */ 
	public static String parseTimeString(int time,boolean Hours24){
		if(Hours24){
			int hours = time/HOUR_MASK;
			int minute = time%HOUR_MASK;
			String rs= clockFormat(hours)+":"+clockFormat(minute);
			return rs;
		}else{
			int hours = time/HOUR_MASK;
			int minute = time%HOUR_MASK;
			StringBuilder t = new StringBuilder();
			String AMPM = "AM";
			if(hours > 11){
				AMPM = "PM";
			}
			if(hours > 12){
				t.append(clockFormat(hours - 12));
			}else if(hours == 0){
				t.append(12);
			}else{
				t.append(clockFormat(hours));
			}
			t.append(":");
			t.append(clockFormat(minute));
			t.append(" ");
			t.append(AMPM);
			String strReturn = t.toString();
			return strReturn;
		}
	}
	/**
	 * Format the given value in to clock format
	 * such that 1 -> 01
	 * @return input value.
	 */
	public static String clockFormat(int input){
		return input > 9 ? Integer.toString(input) : "0"+Integer.toString(input);
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(schedulename);
		dest.writeString(mode);	
		dest.writeInt(this.startTime);
		dest.writeInt(this.endTime);
	}

	public static final Parcelable.Creator<ScheduleRecord> CREATOR = new Parcelable.Creator<ScheduleRecord>() {
		public ScheduleRecord createFromParcel(Parcel in) {
			return new ScheduleRecord(in);
		}

		public ScheduleRecord[] newArray(int size) {
			return new ScheduleRecord[size];
		}
	};
	

}
