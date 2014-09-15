package com.dolphin.dao;

import java.util.ArrayList;

import com.dolphin.smartmodeelite.ScheduleRecord;
import com.dolphin.smartmodeelite.ServiceStatus;
import com.dolphin.smartmodeelite.WifiRecord;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public class SmartModeDAO {
	public static final String KEY_ROWID = "rowid";
	public static final String KEY_SSID = "ssid";
	public static final String KEY_MODE = "mode";
	public static final String KEY_SCHEDULENAME = "schedulename";
	public static final String KEY_STIME = "stime";
	public static final String KEY_ETIME = "etime";
	public static final String KEY_TIME_DIFF = "timedifference";
	private static final String TAG = "SmartModeDAO";
	private static final String DATABASE_NAME = "SmartModeDB";
	private static final String TABLE_SSID = "wifissid";
	private static final String TABLE_SCHEDULE = "schedule";
	private static final String TABLE_SERVICE = "service";
	private static final String TABLE_AUTO_REPLY_WIFI = "autoreplywifi";
	private static final String TABLE_AUTO_REPLY_SCHEDULE = "autoreplyschedule";
	private static final String SERVICE_NAME = "servicename";
	private static final String SERVICE_ITEM_NAME = "service1";
	private static final String TOGGLE_STATUS = "toggle";
	private static final String SERVICE_STATUS = "status";
	private static final String LAST_TIME = "lasttime";
	private static final String BEEN_SCHEDULED = "beenscheduled";
	private static final String CURRENT_MODE = "currentmode";
	private static final String CURRENT_MODE_NAME = "currentmodename";
	private static final String CURRENT_MODE_ITEM_NAME = "currentmode1";
	private static final String CURRENT_MODE_TABLE = "currentModeTemp";
	private static final String DEFAULT_MODE_TABLE = "defaultModeTable";
	private static final String DEFAULT_RECORD_NAME = "Default";
	private static final String KEY_MESSAGE = "message";
	private static final String KEY_MODE_ID = "modeid";
	private static final String KEY_ENABLE = "enable";
	private static final String WIFITABLE_CREATE = 
			"CREATE TABLE IF NOT EXISTS wifissid (rowid INTEGER PRIMARY KEY AUTOINCREMENT, ssid VARCHAR NOT NULL, mode VARCHAR NOT NULL);";
	private static final String SCHEDULETABLE_CREATE = 
			"CREATE TABLE IF NOT EXISTS schedule (rowid INTEGER PRIMARY KEY AUTOINCREMENT, schedulename VARCHAR NOT NULL, mode VARCHAR NOT NULL, stime INT NOT NULL, etime INT NOT NULL);";
	private static final String SERVICETABLE_CREATE = 
			"CREATE TABLE IF NOT EXISTS service (rowid INTEGER PRIMARY KEY AUTOINCREMENT, servicename VARCHAR NOT NULL, toggle VARCHAR NOT NULL, status VARCHAR NOT NULL, lasttime VARCHAR NOT NULL, beenscheduled VARCHAR NOT NULL);";
	private static final String CUR_MODE_TEMP_CREATE = 
			"CREATE TABLE IF NOT EXISTS currentModeTemp(rowid INTEGER PRIMARY KEY AUTOINCREMENT, currentmodename VARCHAR NOT NULL, currentmode VARCHAR NOT NULL);";
	private static final String DEFAULT_MODE_CREATE = 
			"CREATE TABLE IF NOT EXISTS defaultModeTable(rowid INTEGER PRIMARY KEY AUTOINCREMENT, ssid VARCHAR NOT NULL, mode VARCHAR NOT NULL);";
	private static final String AUTO_REPLY_CREATE_WIFI =
			"CREATE TABLE IF NOT EXISTS autoreplywifi(rowid INTEGER PRIMARY KEY AUTOINCREMENT, modeid INTEGER REFERENCES wifissid(rowid) ON DELETE CASCADE, message VARCHAR NOT NULL, enable INT NOT NULL);";
	private static final String AUTO_REPLY_CREATE_SCHEDULE = 
			"CREATE TABLE IF NOT EXISTS autoreplyschedule(rowid INTEGER PRIMARY KEY AUTOINCREMENT, modeid INTEGER REFERENCES schedule(rowid) ON DELETE CASCADE, message VARCHAR NOT NULL, enable INT NOT NULL);";
	private static final int DATABASE_VERSION = 1;
	/**
	 * three different methods for sorting allow user to set on.
	 */
	public static final String SORT_METHOD_1 = KEY_TIME_DIFF+" ASC";
	public static final String SORT_METHOD_2 = KEY_ETIME+" ASC";
	public static final String SORT_METHOD_3 = KEY_STIME+" DESC";
	
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	public SmartModeDAO(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
		// TODO Auto-generated constructor stub
	}
	
	private SmartModeDAO open() throws SQLException{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	private void close(){
		DBHelper.close();
	}
	
	public static String escapeSingleQuote(String input){
		String out = input.replace("'", "''");
		return out;
	}
	/*
	private synchronized long insertToService(String time){
		open();
		ContentValues iv = new ContentValues();
		iv.put(SERVICE_NAME, SERVICE_ITEM_NAME);
		iv.put(TOGGLE_STATUS, "0");
		iv.put(SERVICE_STATUS, "0");
		iv.put(LAST_TIME, time);
		iv.put(BEEN_SCHEDULED, "0");
		long rid = db.insert(TABLE_SERVICE, null, iv);
		close();
		return rid;
	}
	*/
	public synchronized ServiceStatus getServiceStatus(){
		open();
		Cursor rs = db.query(TABLE_SERVICE, new String [] {KEY_ROWID, SERVICE_NAME, TOGGLE_STATUS, SERVICE_STATUS, LAST_TIME, BEEN_SCHEDULED}, null, null, null, null, null);
		ServiceStatus Item = null;
		if (rs.moveToFirst()) {
			Item = new ServiceStatus(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
		}
		rs.close();
		close();
		return Item;
	}
	public synchronized int updateServiceToggle(String t){
		open();
		ContentValues cv = new ContentValues();
		cv.put(TOGGLE_STATUS, t);
		int tmp = db.update(TABLE_SERVICE, cv, SERVICE_NAME + "=" + "'" +SERVICE_ITEM_NAME+ "'", null);
		close();
		return tmp;
	}

	/**
	 * To allow database to record if the service have been scheduled for next round.
	 * @param beenScheduled 0 for none, or any positive number to represent the time range that it has been scheduled.
	 * @return
	 */
	public synchronized int scheduleService(String beenScheduled){
		open();
		ContentValues cv = new ContentValues();
		cv.put(BEEN_SCHEDULED, beenScheduled);
		int tmp = db.update(TABLE_SERVICE, cv, SERVICE_NAME + "=" + "'" +SERVICE_ITEM_NAME+ "'", null);
		close();
		return tmp;
	}
	public synchronized int updateServiceStatus(String newStatus, String time){
		open();
		ContentValues cv = new ContentValues();
		cv.put(SERVICE_STATUS, newStatus);
		cv.put(LAST_TIME, time);
		int tmp = db.update(TABLE_SERVICE, cv, SERVICE_NAME + "=" + "'" +SERVICE_ITEM_NAME+ "'", null);
		close();
		return tmp;
	}
	/**
	 * Update currentMode record table.
	 * @param current
	 * @return
	 */
	public synchronized int updateCurrentMode(String current){
		open();
		ContentValues cv = new ContentValues();
		cv.put(CURRENT_MODE, current);
		int tmp = db.update(CURRENT_MODE_TABLE, cv, CURRENT_MODE_NAME + "="+"'"+CURRENT_MODE_ITEM_NAME+"'",null);
		close();
		return tmp;
	}
	
	public synchronized String getCurrentMode(){
		open();
		Cursor rs = db.query(CURRENT_MODE_TABLE, new String [] {KEY_ROWID, CURRENT_MODE_NAME, CURRENT_MODE}, null, null, null, null, null);
		String mode = "None";
		if (rs.moveToFirst()) {
			mode = rs.getString(2);
		}
		rs.close();
		close();
		return mode;
	}
	/**
	 * insert or update the default set.
	 * @param w
	 * @return 1 for insert, 0 for update, -1 for error
	 */
	public synchronized int insertOrUpdateToDefauleSet(WifiRecord w){
		WifiRecord tmpW = this.getDefaultSet();
		if(tmpW == null){
			//first set default  insert the record.
			open();
			ContentValues iv = new ContentValues();
			iv.put(KEY_SSID, w.getSsid());
			iv.put(KEY_MODE, w.getMode());
			long rid = db.insert(DEFAULT_MODE_TABLE, null, iv);
			close();
			return rid != -1 ? 1 : -1;
		}else{
			//Update instead, user has used it already.
			open();
			ContentValues cv = new ContentValues();
			cv.put(KEY_MODE, w.getMode());
			int tmp = db.update(DEFAULT_MODE_TABLE, cv, KEY_SSID + "=" + "'" +escapeSingleQuote(w.getSsid())+ "'", null);
			close();
			return tmp != 0? 0 : -1;
		}
	}
	/**
	 * Method to insert record into auto reply wifi table.
	 * @param modeId  Mode id where the auto reply message belongs to
	 * @param mode which mode to insert or update, 0:wifi 1:schedule
	 * @param msg message.
	 * @param enable 0:disable 1:enable
	 * @return 1 for insert, 0 for update, -1 for error
	 */
	public synchronized int insertOrUpdateAutoReply(long modeId,int mode,String msg, int enable){
		String s = getAutoMsgByMode(modeId, mode, false);
		int rst = -1;
		if(s == null){
			open();
			String msgCor = msg;
			ContentValues iv = new ContentValues();
			iv.put(KEY_MODE_ID, modeId);
			iv.put(KEY_MESSAGE, msgCor);
			iv.put(KEY_ENABLE, enable);
			long rid = db.insert(mode == 0 ?TABLE_AUTO_REPLY_WIFI:TABLE_AUTO_REPLY_SCHEDULE, null, iv);
			rst = rid != -1 ? 1: -1;
			close();
		}else{
			open();
			String msgCor = msg;
			ContentValues iv = new ContentValues();
			iv.put(KEY_MESSAGE, msgCor);
			iv.put(KEY_ENABLE, enable);
			int tmp = db.update(mode == 0 ?TABLE_AUTO_REPLY_WIFI:TABLE_AUTO_REPLY_SCHEDULE, iv, KEY_MODE_ID + "=" + modeId, null);
			rst = tmp != 0 ? 0 : -1;
			close();
		}
		return rst;
	}
	
	public synchronized long insertToSSID(WifiRecord w){
		open();
		ContentValues iv = new ContentValues();
		iv.put(KEY_SSID, w.getSsid());
		iv.put(KEY_MODE, w.getMode());
		long rid = db.insert(TABLE_SSID, null, iv);
		close();
		return rid;
	}
	
	public synchronized long insertToSchedule(ScheduleRecord s){
		open();
		ContentValues iv = new ContentValues();
		iv.put(KEY_MODE, s.getMode());
		iv.put(KEY_SCHEDULENAME, s.getSchedulename());
		iv.put(KEY_STIME, s.getStartTime());
		iv.put(KEY_ETIME, s.getEndTime());
		long rid =  db.insert(TABLE_SCHEDULE, null, iv);
		close();
		return rid;
	}
	
	public synchronized int deleteFromSSID(String s){
		open();
		String ssid = escapeSingleQuote(s);
		int tmp =  db.delete(TABLE_SSID, KEY_SSID + "=" + "'"+ssid+"'" , null);
		close();
		return tmp;
	}
	
	public synchronized int deleteFromSchedule(String s){
		open();
		String sname = escapeSingleQuote(s);
		int tmp =  db.delete(TABLE_SCHEDULE, KEY_SCHEDULENAME + "=" + "'" +sname+"'", null);
		close();
		return tmp;
	}
	/**
	 * Retrieve a default set from database.
	 * @return Return null if user has not set up a default mode yet.
	 */
	public synchronized WifiRecord getDefaultSet(){
		open();
//		db = DBHelper.getReadableDatabase();
		Cursor rs = db.query(DEFAULT_MODE_TABLE, new String[] {KEY_ROWID, KEY_SSID, KEY_MODE}, null, null, null, null, null);
		WifiRecord defaultSet = null;
		if (rs.moveToFirst()) {
			defaultSet = new WifiRecord(rs.getString(1), rs.getString(2));
		}
		close();
		rs.close();
		return defaultSet;
	}
	public synchronized ArrayList<WifiRecord> getAllFromSSID(){
		open();
//		db = DBHelper.getReadableDatabase();
		Cursor rs = db.query(TABLE_SSID, new String[] {KEY_ROWID, KEY_SSID, KEY_MODE}, null, null, null, null, null);
		ArrayList<WifiRecord> listItems=new ArrayList<WifiRecord>();
		if (rs.moveToFirst()) {
			listItems.add(new WifiRecord(rs.getString(1), rs.getString(2)));
			while (rs.moveToNext()) {
				listItems.add(new WifiRecord(rs.getString(1), rs.getString(2)));
			}
		}
		close();
		rs.close();
		return listItems;
	}
	
	public synchronized ArrayList<ScheduleRecord> getAllFromSchedule(){
		open();
//		db = DBHelper.getReadableDatabase();
		Cursor rs = db.query(TABLE_SCHEDULE, new String [] {KEY_ROWID, KEY_SCHEDULENAME,KEY_MODE , KEY_STIME,KEY_ETIME }, null, null, null, null, null);
		ArrayList<ScheduleRecord> listItems=new ArrayList<ScheduleRecord>();
		if (rs.moveToFirst()) {
			listItems.add(new ScheduleRecord(rs.getString(1), rs.getString(2),rs.getInt(3), rs.getInt(4)));
			while (rs.moveToNext()) {
				listItems.add(new ScheduleRecord(rs.getString(1), rs.getString(2),rs.getInt(3), rs.getInt(4)));
			}
		}
		close();
		rs.close();
		return listItems;
	}
	/**
	 * Method to get msg for particular mode.
	 * @param modeID mode id to edit
	 * @param mode Int value to determine which table to use wifi or schedule.  0:wifi 1:schedule.
	 * @param isEnableTagOn true when in service for checking if user want to enable the auto msg or not.
	 * @return msg
	 */
	public synchronized String getAutoMsgByMode(long modeID, int mode, boolean isEnableTagOn){
		open();
		String msgToReturn = null;
		if(mode == 0){
			Cursor rs = db.query(TABLE_AUTO_REPLY_WIFI, new String[]{KEY_ROWID, KEY_MODE_ID, KEY_MESSAGE, KEY_ENABLE}, KEY_MODE_ID+"="+modeID,null, null, null, null);
			if(rs.moveToFirst()){
				if(!isEnableTagOn||rs.getInt(3) == 1)
					msgToReturn = rs.getString(2);
			}
			rs.close();
		}else if(mode == 1){
			Cursor rs = db.query(TABLE_AUTO_REPLY_SCHEDULE, new String[]{KEY_ROWID, KEY_MODE_ID, KEY_MESSAGE, KEY_ENABLE}, KEY_MODE_ID+"="+modeID,null, null, null, null);
			if(rs.moveToFirst()){
				if(!isEnableTagOn||rs.getInt(3) == 1)
					msgToReturn = rs.getString(2);
			}
			rs.close();
		}
		close();
		return msgToReturn;
	}
	/**
	 * Method to get msg enalbe state for particular mode.
	 * @param modeID mode id to edit
	 * @param mode Int value to determine which table to use wifi or schedule.  0:wifi 1:schedule.
	 * @param isEnableTagOn true when in service for checking if user want to enable the auto msg or not.
	 * @return msg
	 */
	public synchronized boolean isEnableAutoMsgByMode(long modeID, int mode){
		open();
		boolean isEnable = false;
		if(mode == 0){
			Cursor rs = db.query(TABLE_AUTO_REPLY_WIFI, new String[]{KEY_ROWID, KEY_MODE_ID, KEY_MESSAGE, KEY_ENABLE}, KEY_MODE_ID+"="+modeID,null, null, null, null);
			if(rs.moveToFirst()){
				if(rs.getInt(3) == 1)
					isEnable = true;
			}
			rs.close();
		}else if(mode == 1){
			Cursor rs = db.query(TABLE_AUTO_REPLY_SCHEDULE, new String[]{KEY_ROWID, KEY_MODE_ID, KEY_MESSAGE, KEY_ENABLE}, KEY_MODE_ID+"="+modeID,null, null, null, null);
			if(rs.moveToFirst()){
				if(rs.getInt(3) == 1)
					isEnable = true;
			}
			rs.close();
		}
		close();
		return isEnable;
	}
	public synchronized ArrayList<WifiRecord> getFromSSIDBySSID(String s){
		open();
		String ssid = escapeSingleQuote(s);
//		db = DBHelper.getReadableDatabase();
		Cursor rs = db.query(TABLE_SSID, new String[] {KEY_ROWID, KEY_SSID, KEY_MODE}, KEY_SSID + "=" + "'"+ssid+"'", null, null, null, null);
		ArrayList<WifiRecord> listItems=new ArrayList<WifiRecord>();
		if (rs.moveToFirst()) {
			listItems.add(new WifiRecord(rs.getString(1), rs.getString(2)));
			while (rs.moveToNext()) {
				listItems.add(new WifiRecord(rs.getString(1), rs.getString(2)));
			}
		}
		close();
		rs.close();
		return listItems;
	}
	/**
	 * find rowid of wifi record base on SSID.
	 * @param s
	 * @return -1 not found, otherwise row id.
	 */
	public synchronized long getRowIDFromSSID(String s){
		long rowid = -1;
		open();
		String ssid = escapeSingleQuote(s);
//		db = DBHelper.getReadableDatabase();
		Cursor rs = db.query(TABLE_SSID, new String[] {KEY_ROWID, KEY_SSID, KEY_MODE}, KEY_SSID + "=" + "'"+ssid+"'", null, null, null, null);
		if (rs.moveToFirst()) {
			rowid = rs.getLong(0);
		}
		close();
		rs.close();
		return rowid;
	}
	/**
	 * find rowid of schedule record base on schedule name.
	 * @param s
	 * @return -1 not found, otherwise row id.
	 */
	public synchronized long getRowIDFromSchedule(String s){
		long rowid = -1;
		open();
//		db = DBHelper.getReadableDatabase();
		String name = escapeSingleQuote(s);
		Cursor rs = db.query(TABLE_SCHEDULE, new String [] {KEY_ROWID, KEY_SCHEDULENAME,KEY_MODE , KEY_STIME,KEY_ETIME}, KEY_SCHEDULENAME + "=" + "'" +name +"'", null, null, null, null);
		if (rs.moveToFirst()) {
			rowid = rs.getLong(0);
		}
		close();
		rs.close();
		return rowid;
	}
	
	
	/**
	 * Method to retrieve schedule which schedule start time and end time include the particular given time value.
	 * @param time 
	 * @param srotMethod  Use SmartModeDAO static value to pass in the sorting query. or pass in null as default
	 * @return Return arraylist  or  empty list.
	 */
	public synchronized ArrayList<ScheduleRecord> getScheduleWhichContainTime(int time, String sortMethod){
		open();
		Cursor rs = db.query(TABLE_SCHEDULE, new String[] { KEY_ROWID,
				KEY_SCHEDULENAME, KEY_MODE, KEY_STIME, KEY_ETIME, 
				"CASE WHEN "+KEY_ETIME+" >= "+KEY_STIME+" THEN "+
				KEY_ETIME +"-"+KEY_STIME
				+" ELSE "+
				KEY_ETIME+"-"+KEY_STIME+"+2400"
				+" END AS "+ KEY_TIME_DIFF
				}, 
				 "CASE WHEN "+KEY_ETIME+" >= "+KEY_STIME+" THEN "+
				 time + " BETWEEN " + KEY_STIME + " AND " + KEY_ETIME
				 +" ELSE "+
				 time +" >= "+KEY_STIME + " OR "+ time + " <= " + KEY_ETIME
				 +" END"
				 , null, null,null, SORT_METHOD_1);
		ArrayList<ScheduleRecord> listItems = new ArrayList<ScheduleRecord>();
		if (rs.moveToFirst()) {
			listItems.add(new ScheduleRecord(rs.getString(1), rs.getString(2),
					rs.getInt(3), rs.getInt(4)));
			while (rs.moveToNext()) {
				listItems.add(new ScheduleRecord(rs.getString(1), rs
						.getString(2), rs.getInt(3), rs.getInt(4)));
			}
		}
		rs.close();
		close();
		return listItems;
	}
	public synchronized ArrayList<ScheduleRecord> getFromScheduleByName(String n){
		open();
//		db = DBHelper.getReadableDatabase();
		String name = escapeSingleQuote(n);
		Cursor rs = db.query(TABLE_SCHEDULE, new String [] {KEY_ROWID, KEY_SCHEDULENAME,KEY_MODE , KEY_STIME,KEY_ETIME}, KEY_SCHEDULENAME + "=" + "'" +name +"'", null, null, null, null);
		ArrayList<ScheduleRecord> listItems=new ArrayList<ScheduleRecord>();
		if (rs.moveToFirst()) {
			listItems.add(new ScheduleRecord(rs.getString(1), rs.getString(2),rs.getInt(3), rs.getInt(4)));
			while (rs.moveToNext()) {
				listItems.add(new ScheduleRecord(rs.getString(1), rs.getString(2),rs.getInt(3), rs.getInt(4)));
			}
		}
		close();
		rs.close();
		return listItems;
	}
	
	public synchronized int updateRecordSSID(WifiRecord w){
		open();
		ContentValues cv = new ContentValues();
		cv.put(KEY_MODE, w.getMode());
		int tmp = db.update(TABLE_SSID, cv, KEY_SSID + "=" + "'" +escapeSingleQuote(w.getSsid())+ "'", null);
		close();
		return tmp;
	}
	/**
	 * Update auto reply msg
	 * @param modeId Mode id of the mode record.
	 * @param mode Which mode to use, 0:wifi, 1:schedule
	 * @param newMsg
	 * @return number of row affected
	 */
	public synchronized int  updateAutoReplyMsg(long modeId, int mode,String newMsg){
		open();
		int rowChanged = 0;
		if(mode == 0){
			ContentValues cv = new ContentValues();
			cv.put(KEY_MESSAGE, newMsg);
			rowChanged = db.update(TABLE_AUTO_REPLY_WIFI, cv, KEY_MODE_ID+"="+modeId, null);
		}else if (mode == 1){
			ContentValues cv = new ContentValues();
			cv.put(KEY_MESSAGE, newMsg);
			rowChanged = db.update(TABLE_AUTO_REPLY_SCHEDULE, cv, KEY_MODE_ID+"="+modeId, null);
		}
		close();
		return rowChanged;
	}
	public synchronized int updateRecordSchedule(ScheduleRecord s){
		open();
		ContentValues cv = new ContentValues();
		cv.put(KEY_STIME, s.getStartTime());
		cv.put(KEY_ETIME, s.getEndTime());
		cv.put(KEY_MODE, s.getMode());
		int tmp = db.update(TABLE_SCHEDULE, cv, KEY_SCHEDULENAME + "=" + "'" +escapeSingleQuote(s.getSchedulename())+"'", null);
		close();
		return tmp;
		
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper{
		DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(WIFITABLE_CREATE);
			db.execSQL(SCHEDULETABLE_CREATE);
			db.execSQL(SERVICETABLE_CREATE);
			db.execSQL(CUR_MODE_TEMP_CREATE);
			db.execSQL(DEFAULT_MODE_CREATE);
			db.execSQL(AUTO_REPLY_CREATE_WIFI);
			db.execSQL(AUTO_REPLY_CREATE_SCHEDULE);
			insertToSSID(new WifiRecord("Sample Network", "0,0,0,0,0,0,0"), db);
			insertToSchedule(new ScheduleRecord ("Sample Schedule", "0,0,0,0,0,0,0", 0, 0), db);
			Time now = new Time();
			now.setToNow();
			insertToService(now.toMillis(false), db);
			insertToCurrentMode(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Log.w(TAG, "Upgrading database from version "+ oldVersion + " to "+ newVersion+", which will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS wifissid");
			db.execSQL("DROP TABLE IF EXISTS schedule");
			db.execSQL("DROP TABLE IF EXISTS service");
			db.execSQL("DROP TABLE IF EXISTS currentModeTemp");
			db.execSQL("DROP TABLE IF EXISTS defaultModeTable");
			db.execSQL("DROP TABLE IF EXISTS autoreplywifi");
			db.execSQL("DROP TABLE IF EXISTS autoreplyschedule");
			onCreate(db);
		}
		
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			super.onOpen(db);
			if (!db.isReadOnly()) {
		        // Enable foreign key constraints
		        db.execSQL("PRAGMA foreign_keys=ON;");
		    }
		}
		//Classes to add records
		private long insertToSSID(WifiRecord w, SQLiteDatabase db){
			ContentValues iv = new ContentValues();
			iv.put(KEY_SSID, w.getSsid());
			iv.put(KEY_MODE, w.getMode());
			long rid = db.insert(TABLE_SSID, null, iv);
			return rid;
		}
		
		private long insertToSchedule(ScheduleRecord s, SQLiteDatabase db){
			ContentValues iv = new ContentValues();
			iv.put(KEY_SCHEDULENAME, s.getSchedulename());
			iv.put(KEY_MODE, s.getMode());
			iv.put(KEY_STIME, s.getStartTime());
			iv.put(KEY_ETIME, s.getEndTime());
			long rid =  db.insert(TABLE_SCHEDULE, null, iv);
			return rid;
		}
		//work here  06/08
		private long insertToService(long time, SQLiteDatabase db){
			ContentValues iv = new ContentValues();
			iv.put(SERVICE_NAME, SERVICE_ITEM_NAME);
			iv.put(TOGGLE_STATUS, "0");
			iv.put(SERVICE_STATUS, "0");
			iv.put(LAST_TIME, Long.toString(time));
			iv.put(BEEN_SCHEDULED, "0");
			long rid = db.insert(TABLE_SERVICE, null, iv);
			return rid;
		}
		private long insertToCurrentMode(SQLiteDatabase db){
			ContentValues iv = new ContentValues();
			iv.put(CURRENT_MODE_NAME, CURRENT_MODE_ITEM_NAME);
			iv.put(CURRENT_MODE, "None");
			long rid = db.insert(CURRENT_MODE_TABLE, null, iv);
			return rid;
		}
	}

}
