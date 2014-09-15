package com.dolphin.service;


import com.dolphin.dao.SmartModeDAO;
import com.dolphin.smartmodeelite.DAOSingleton;
import com.dolphin.smartmodeelite.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;

public class ModeManagerService extends SmartModeGenericService {
	public final static String ONE_TIME = "onetime";
	public final static int NOTIFICATION_ID = 0;
	public final static int WIFI_DETECT_TIME_SCHEDULE = 0;
	public final static int TIME_SCHEDULE_WIFI_DETECT = 1;
	public final static int WIFI_DETECT_ONLY = 2;
	public final static int TIME_SCHEDULE_ONLY = 3;
	public ModeManagerService() {
		super("ModeManagerService");
		// TODO Auto-generated constructor stub
	}
	public ModeManagerService(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}
	public static void startRepeatModeService(Context context){
		Intent intent = new Intent(context, ModeManagerService.class);
//		intent.putExtra(EXTRA_MANUALTOGGLE, manualSwitch);
		context.startService(intent);
	}
	SmartModeDAO db;
	boolean senseMode;
	boolean notificationShow;
	@Override
	public void onCreate() {
		super.onCreate();
		// TODO Auto-generated method stub
		db = DAOSingleton.getInstance(this).getDb();
		
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub		
		super.onDestroy();
		Time tmpTime = new Time();
		tmpTime.setToNow();
		db.updateServiceStatus("0", Long.toString(tmpTime.toMillis(false)));

	}
	@Override
	protected void onHandleIntent(Intent intent) {
		db = DAOSingleton.getInstance(this).getDb();
		//-----motion sampling processes---//
		//---------------------------------//
		boolean toggleon = db.getServiceStatus().getToggle().equals("1");
		if(toggleon){
			String errind = handleBackGroundAction();
			if (errind.equals("No error"))
				scheduleNextUpdate();
		}
	}

	protected String handleBackGroundAction() {
		// TODO Auto-generated method stub
		// android.os.Debug.waitForDebugger();
		db = DAOSingleton.getInstance(this).getDb();
		db.scheduleService("0");
		Time tmp = new Time();
		tmp.setToNow();
		db.updateServiceStatus("1", Long.toString(tmp.toMillis(false)));
		String errind = handleActionUpdateMode();
		return errind;
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	private void scheduleNextUpdate(){
	    Intent intent = new Intent(this.getApplicationContext(), ModeManagerService.class);
	    PendingIntent pendingIntent =
	        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    // The update frequency should often be user configurable.  This is not.
		String schText = getPreferenceScheduleTime();
//		android.os.Debug.waitForDebugger();  // this line is key

		int multiplier = Integer.parseInt(schText);
	    long currentTimeMillis = System.currentTimeMillis();
	    long nextUpdateTimeMillis = currentTimeMillis +  multiplier * DateUtils.MINUTE_IN_MILLIS;
	    Time nextUpdateTime =  new Time();
	    nextUpdateTime.set(nextUpdateTimeMillis);
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdateTimeMillis, pendingIntent);
		db = DAOSingleton.getInstance(this).getDb();
		db.scheduleService(schText);
	 }
	
	
	private String getPreferenceScheduleTime(){
		//Used for testing preferences setting functionalities.
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String savedIfo = sharePrefere.getString(getString(R.string.frequency_saved_key), "5");
		return savedIfo;
	}
	
}
