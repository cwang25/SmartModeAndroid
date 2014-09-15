package com.dolphin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.smartmodeelite.DAOSingleton;
import com.dolphin.smartmodeelite.MainActivity;
import com.dolphin.smartmodeelite.R;
import com.dolphin.smartmodeelite.ScheduleRecord;
import com.dolphin.smartmodeelite.WifiRecord;
import com.dolphin.wifiInfo.WifiInfoNoQuote;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;
import android.text.format.Time;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SmartModeGenericService extends IntentService{
	// TODO: Rename actions, choose action names that describe tasks that this
	// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
	private static final String ACTION_UPDATEMODE = "com.dolphin.smartmode.action.UPDATEMODE";
	private static final String ACTION_REMOVENOTIFICATION = "com.dolphin.smartmode.action.UPDATENOTIFICATION";
	private static final String ACTION_STOPSCHEDULE = "com.dolphin.smartmode.action.STOPSCHEDULE";
	private static final String ACTION_REPEATUPDATEMODE = "com.dolphin.smartmode.action.REPEATUPDATEMODE";
	public final static int NOTIFICATION_ID = 0;
	public final static int WIFI_DETECT_TIME_SCHEDULE = 0;
	public final static int TIME_SCHEDULE_WIFI_DETECT = 1;
	public final static int WIFI_DETECT_ONLY = 2;
	public final static int TIME_SCHEDULE_ONLY = 3;
	public final static String ONE_TIME = "onetime";
	/**
	 * Same as in phonestatereceiver class.
	 */
	public final static int DEFAULT_TYPE = 2;
	public final static int WIFI_TYPE = 0;
	public final static int SCHEDULE_TYPE = 1;
	public final static int NONE_TYPE = -1;
	SmartModeDAO db;
	boolean senseMode;
	boolean toggleOn;
	boolean notificationShow;
	boolean defaultSettingOn;
	/**
	 * Starts this service to perform update notification for smart mode notification.
	 * 
	 * @see IntentService
	 */
	// TODO: Customize helper method
	public static void startActionRemoveNotification(Context context) {
		Intent intent = new Intent(context, SmartModeGenericService.class);
		intent.setAction(ACTION_REMOVENOTIFICATION);
//		intent.putExtra(EXTRA_MANUALTOGGLE, manualSwitch);
		context.startService(intent);
	}
	public static void startCancelScheduleOfService(Context context){
		Intent intent = new Intent(context, SmartModeGenericService.class);
		intent.setAction(ACTION_STOPSCHEDULE);
//		intent.putExtra(EXTRA_MANUALTOGGLE, manualSwitch);
		context.startService(intent);
	}
	
	/**
	 * Starts this service to perform action to update mode one-time only.
	 * The ModeManagerService will update mode periodically.
	 * @see IntentService
	 */
	// TODO: Customize helper method
	public static void startActionUpdateMode(Context context) {
		Intent intent = new Intent(context, SmartModeGenericService.class);
		intent.setAction(ACTION_UPDATEMODE);
		context.startService(intent);
	}

	public SmartModeGenericService() {
		super("SmartModeGenericService");
	}
	public SmartModeGenericService(String s) {
		super(s);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_UPDATEMODE.equals(action)) {
				handleActionUpdateMode();
			} else if (ACTION_REMOVENOTIFICATION.equals(action)) {
				handleActionRemoveNotification();
			} else if (ACTION_STOPSCHEDULE.equals(action)){
				handleStopScheduleService();
			} 
		}
	}
	
	/**
	 * Handle action for repeating udpate mode.
	 */
	/*
	private void handleActionRepeatUpdateMode(){
		SmartModeDAO db = DAOSingleton.getInstance(this).getDb();
		db.scheduleService("0");
		String errind = handleActionUpdateMode();
		if (errind.equals("No error"))
			scheduleNextUpdate();

	}
	*/
	/**
	 * hadle action to stop the scheduledService, which implies to cancel the alarm.
	 */
	private void handleStopScheduleService(){
		Intent intent = new Intent(this.getApplicationContext(), ModeManagerService.class);
		//intent.setAction(ACTION_REPEATUPDATEMODE);
	    PendingIntent pendingIntent =
	        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(pendingIntent);
	    SmartModeDAO db = DAOSingleton.getInstance(this).getDb();
		db.scheduleService("0");
	}
	/**
	 * Handle action UpdateMode in the provided background thread with the provided
	 * parameters.
	 */
	protected String handleActionUpdateMode() {
		// TODO Auto-generated method stub
		// android.os.Debug.waitForDebugger();
		db = DAOSingleton.getInstance(this).getDb();
		//db.scheduleService("0");
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		senseMode = sharePreference.getBoolean(
				getString(R.string.sensitive_mode_key), false);
		notificationShow = sharePreference.getBoolean(
				getString(R.string.notification_toggle_key), true);
		defaultSettingOn = sharePreference.getBoolean(getString(R.string.default_setting_key), false);
		toggleOn = db.getServiceStatus().getToggle().equals("1");
		Time tmp = new Time();
		tmp.setToNow();
		//db.updateServiceStatus("1", Long.toString(tmp.toMillis(false)));
		// Indecate error
		String errind = "No error";
		String rst = null;
		int rstModeType = NONE_TYPE;
		String manualSet = getManualToggleStatus();
		try {
			if (manualSet == null) {
				int modePriorityPreference = Integer
						.parseInt(getPreferenceModePriority());
				switch (modePriorityPreference) {
				case WIFI_DETECT_TIME_SCHEDULE:
					rst = checkWifiCondition();
					if (rst != null){
						rstModeType = WIFI_TYPE;
						break;
					}
					rst = checkScheduleCondition();
					if(rst != null){
						rstModeType = SCHEDULE_TYPE;
					}
					break;
				case TIME_SCHEDULE_WIFI_DETECT:
					rst = checkScheduleCondition();
					if (rst != null){
						rstModeType = SCHEDULE_TYPE;
						break;
					}
					rst = checkWifiCondition();
					if(rst != null){
						rstModeType = WIFI_TYPE;
					}
					break;
				case WIFI_DETECT_ONLY:
					rst = checkWifiCondition();
					if(rst != null){
						rstModeType = WIFI_TYPE;
					}
					break;
				case TIME_SCHEDULE_ONLY:
					rst = checkScheduleCondition();
					if(rst != null){
						rstModeType = SCHEDULE_TYPE;
					}
					break;
				}
				if(rst == null && defaultSettingOn ){
					rstModeType = DEFAULT_TYPE;
					rst = updateModeForDefault();
					if(rst == null){
						rstModeType = NONE_TYPE;
					}
				}
				this.setPreferenceModeName(rst, this.getApplicationContext());
				this.setPreferenceModeType(rstModeType, this.getApplicationContext());
			} else {
				rst = manualSet;
				this.setPreferenceModeType(NONE_TYPE, this.getApplicationContext());
			}
		} catch (Exception e) {
			// Try to capture the potential error
			errind = e.getMessage();
		}
		if (toggleOn) {
			if (notificationShow) {
				if (manualSet == null) {
					pushNotification(rst != null ? "Current Mode: " + rst
							: "None", senseMode ? "Sensitive Mode On"
							: "Sensitive Mode Off",
							rst != null ? R.drawable.ic_stat_green_logo
									: R.drawable.ic_launcher_3,
							MainActivity.class);
				} else {
					pushNotification(
							rst,
							senseMode ? "Sensitive Mode On"
									: "Sensitive Mode Off",
							rst.equals(getString(R.string.vibrate_manual_on_tag)) ? R.drawable.ic_vibrate_logo
									: R.drawable.ic_silience_logo,
							MainActivity.class);
				}
			}
			// If saw error then stop rerun.
			db.updateCurrentMode(rst != null ? rst : "None");
		} else {
			db.updateCurrentMode("None");
		}
//		android.os.Debug.waitForDebugger();
		return errind;
	}

	/**
	 * Handle action Baz in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionRemoveNotification() {
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID);
		// TODO: Handle action Baz
	}
	
	
	//-------
	
	protected String checkWifiCondition(){
		WifiManager wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String cur_ssid = "";
		if(wifiInfo == null || wifiInfo.getSSID() == null){
			return null;
		}
		cur_ssid = WifiInfoNoQuote.removeQuoteIfNeeded(wifiInfo.getSSID());
		ArrayList <WifiRecord> tWifi = db.getFromSSIDBySSID(cur_ssid);
		if(senseMode){
			if(tWifi.size() == 0){
				List<ScanResult> scannedResult = wifiManager.getScanResults();
				if(scannedResult != null){
					ListIterator<ScanResult> cursor = scannedResult.listIterator();
					while (cursor.hasNext() && tWifi.size() == 0) {
						//remove add " in ssid 08/09/2014
						tWifi = db.getFromSSIDBySSID(cursor.next().SSID);
					}
				}
			}
		}
		//String msg = "Wifi: "+cur_ssid+"\n"+"Number found from database: "+tWifi.size()+"\n"+"Sensitive mode: "+senseMode+"\n";
		//SharedPreferences sharePreference = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		//sharePreference.edit().putString(this.getString(R.string.system_status_test_key), msg).commit();
		if(tWifi.size() > 0){
			AudioManager audioM = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
			WifiRecord tmp = tWifi.get(0);
			String mode =tmp.getMode();
			if(toggleOn){
				adjustMode(mode, audioM);
				return tmp.toString();
			}
		}
		return null;
	}

	protected String checkScheduleCondition(){
		Time t = new Time();
		t.setToNow();
		int hour = t.hour * ScheduleRecord.HOUR_MASK;
		int minute = t.minute ;
		int timeval = hour+minute;
		ArrayList <ScheduleRecord> tSchedule = db.getScheduleWhichContainTime(timeval, null);
		if(tSchedule.size() > 0){
			AudioManager audioM = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
			ScheduleRecord tmp = tSchedule.get(0);
			String mode = tmp.getMode();
			if(toggleOn){
				adjustMode(mode, audioM);
				return tmp.toString();
			}
		}
		return null;
	}
	
	protected String getPreferenceModePriority(){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String savedIfo = sharePrefere.getString(getString(R.string.mode_priority_key), "0");
		return savedIfo;
	}
	public String updateModeForDefault(){
		String rst = null;
		WifiRecord tmp = db.getDefaultSet();
		AudioManager audioM = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		if(tmp != null){
			adjustMode(tmp.getMode(),audioM);
			rst = this.getString(R.string.defaultModeTitle);
			return rst;
		}else{
			return rst;
		}
	}
	/**
	 * adjustMode method to adjust phone mode according to given mode
	 * @param mode Mode has to be formatted appropriately.
	 * @param audioM AudioManager.
	 */
	private void adjustMode(String mode, AudioManager audioM){
		String [] ParsedMode = mode.split(",");
		int ringerMode = audioM.getRingerMode();
		//if(ParsedMode == null || ParsedMode.length < 3){
			//Do nothing
		//} 
		//Moved to set Vol section in ringtone.
		/*else if(ParsedMode[0].equals("1")){
			if(ringerMode != AudioManager.RINGER_MODE_SILENT)
				audioM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		} else if(ParsedMode[1].equals("1")){
			if(ringerMode != AudioManager.RINGER_MODE_VIBRATE )
				audioM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		} else if(ParsedMode[2].equals("1")){
			if(ringerMode != AudioManager.RINGER_MODE_NORMAL)
				audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		}
		*/
		if(ParsedMode.length >= 6){
			int streamRingVol = 0;
			int streamMusicVol = 0;
			int streamAlarmVol = 0;
			int streamNotificationVol = 0;
			try{
				streamRingVol = Integer.parseInt(ParsedMode[3]);
				streamMusicVol = Integer.parseInt(ParsedMode[4]);
				streamAlarmVol = Integer.parseInt(ParsedMode[5]);
				streamNotificationVol = Integer.parseInt(ParsedMode[6]);
				int curRing = audioM.getStreamVolume(AudioManager.STREAM_RING);
				int curMusic = audioM.getStreamVolume(AudioManager.STREAM_MUSIC);
				int curAlarm = audioM.getStreamVolume(AudioManager.STREAM_ALARM);
				int curNotification = audioM.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
				if(streamRingVol > 0){
					if(streamRingVol != curRing){
						audioM.setStreamVolume(AudioManager.STREAM_RING, streamRingVol, AudioManager.FLAG_SHOW_UI );
					}
				}else{
					if(ParsedMode[0].equals("1")){
						if(ringerMode != AudioManager.RINGER_MODE_SILENT)
							audioM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					} else if(ParsedMode[1].equals("1")){
						if(ringerMode != AudioManager.RINGER_MODE_VIBRATE )
							audioM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					} else if(ParsedMode[2].equals("1")){
						if(ringerMode != AudioManager.RINGER_MODE_SILENT)
							audioM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					}
				}
				if(streamMusicVol != curMusic)
					audioM.setStreamVolume(AudioManager.STREAM_MUSIC, streamMusicVol, AudioManager.FLAG_SHOW_UI );
				if(streamAlarmVol != curAlarm)
					audioM.setStreamVolume(AudioManager.STREAM_ALARM, streamAlarmVol, AudioManager.FLAG_SHOW_UI );
				if(streamNotificationVol != curNotification)
					audioM.setStreamVolume(AudioManager.STREAM_NOTIFICATION,streamNotificationVol, AudioManager.FLAG_SHOW_UI );
			}catch(NumberFormatException e){
				
			}
		}
	}
	/**
	 * Method to retrieve manual toggle status.
	 * @return null if neither vibrate or silence manual is on. Otherwise will return vibrate_manual_on_tag or silence_manual_on_tag.
	 */
	protected String getManualToggleStatus(){
		SharedPreferences sharePreference = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String vibrateSet = sharePreference.getString(getString(R.string.vibrate_manual_toggle_key), "0");
		String silenceSet = sharePreference.getString(getString(R.string.silence_manual_toggle_key), "0");
		if(vibrateSet.equals("1")){
			return getString(R.string.vibrate_manual_on_tag);
		}
		if(silenceSet.equals("1")){
			return getString(R.string.silence_manual_on_tag);
		}
		return null;
	}
	
	/**
	 * Method to simply push notification with given title and text.
	 * @param title Title of the notification.
	 * @param text Text of the notification.
	 * @param pic_ID picuter id int, R.drawable.picture ...etc
	 * @param sourceActivityClass The class the it will be redirect to when user click on it.
	 */
	protected void pushNotification(String title, String text, int pic_ID,Class<?> sourceActivityClass){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(pic_ID)
		        .setContentTitle(title)
		        .setContentText(text);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, sourceActivityClass);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(sourceActivityClass);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	private boolean setPreferenceModeName(String val, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putString(this.getString(R.string.current_mode_key), val).commit();
		return rst;
	}private boolean setPreferenceModeType(int val, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putInt(this.getString(R.string.current_mode_type_key), val).commit();
		return rst;
	}
}
