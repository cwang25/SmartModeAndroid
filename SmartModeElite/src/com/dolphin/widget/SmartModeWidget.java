package com.dolphin.widget;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.service.ModeManagerService;
import com.dolphin.service.SmartModeGenericService;
import com.dolphin.smartmodeelite.DAOSingleton;
import com.dolphin.smartmodeelite.R;
import com.dolphin.smartmodeelite.ServiceStatus;
import com.dolphin.smartmodeelite.R.id;
import com.dolphin.smartmodeelite.R.layout;
import com.dolphin.smartmodeelite.R.string;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 */
public class SmartModeWidget extends AppWidgetProvider {
	private static final String SYSTEM_TOGGLE = "systemToggle";
	private static final String SILENCE_MANUAL_TOGGLE = "silenceManualToggle";
	private static final String VIBRATE_MANUAL_TOGGLE = "vibrateManualToggle";
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

	public void updateAppWidget(Context context,AppWidgetManager appWidgetManager, int appWidgetId) {
		//CharSequence widgetText = context.getString(R.string.appwidget_text);
		// Construct the RemoteViews object
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.smart_mode_widget);
		SmartModeDAO db = DAOSingleton.getInstance(context).getDb();
		String toggle = db.getServiceStatus().getToggle();
		String silenceManualToggle = getPreferenceString(context.getString(R.string.silence_manual_toggle_key), context);
		String vibrateManualToggle = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key), context);
		remoteViews.setTextViewText(R.id.appwidget_text, toggle.equals("1")?"ON":"OFF");
		remoteViews.setImageViewResource(R.id.widgetImageSilenceButton,  silenceManualToggle.equals("1")?R.drawable.ic_silence_bar : R.drawable.ic_black_bar);
		remoteViews.setImageViewResource(R.id.widgetImageVibrateButton, vibrateManualToggle.equals("1")?R.drawable.ic_vibrate_bar : R.drawable.ic_black_bar);
		int systemIcon;
		if(toggle.equals("1")){
			if(silenceManualToggle.equals("1")){
				systemIcon = R.drawable.ic_silience_logo;
			}else if(vibrateManualToggle.equals("1")){
				systemIcon = R.drawable.ic_vibrate_logo;
			}else{
				systemIcon = R.drawable.ic_launcher_3;
			}
		}else{
			if(silenceManualToggle.equals("1")){
				systemIcon = R.drawable.ic_silence_system_off_logo;
			}else if(vibrateManualToggle.equals("1")){
				systemIcon = R.drawable.ic_vibrate_system_off_logo;
			}else{
				systemIcon = R.drawable.ic_launcher_black;
			}
		}
		remoteViews.setImageViewResource(R.id.widgetImageButton, systemIcon);
		remoteViews.setOnClickPendingIntent(R.id.widgetImageButton, getPendingSelfIntent(context, SYSTEM_TOGGLE));
		remoteViews.setOnClickPendingIntent(R.id.widgetImageSilenceButton, getPendingSelfIntent(context, SILENCE_MANUAL_TOGGLE));
		remoteViews.setOnClickPendingIntent(R.id.widgetImageVibrateButton, getPendingSelfIntent(context, VIBRATE_MANUAL_TOGGLE));
		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
	}
	/**
	 * The method to retrieve a saved instance of manual vibrate and silence from key.
	 * From sharedpreference.
	 * @param key A string key that pairs with the desired value.
	 * @param context A context object.
	 * @return Return string from shared preference.
	 */
	private String getPreferenceString(String key, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		String savedIfo = sharePrefere.getString(key, "0");
		return savedIfo;
	}
	
	/**
	 * The method to write instance of manual vibrate and silence into shared preference.
	 * @param val A value to store into shared preference. (silence, vibrate manual should be "0" or "1")
	 * @param key A string key that pairs with the desired value.
	 * @param context A context object.
	 * @return Return string that is saved to shared preference.
	 */
	private boolean setPreferenceString(String val, String key, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putString(key, val).commit();
		return rst;
	}
	/**
	 * For self intent contructing.
	 * @param context
	 * @param action
	 * @return
	 */
	protected PendingIntent getPendingSelfIntent(Context context, String action) {
	    Intent intent = new Intent(context, getClass());
	    intent.setAction(action);
	    return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		String actionKey = intent.getAction();
		if (SYSTEM_TOGGLE.equals(actionKey)){
	        //your onClick action is here
			SmartModeDAO db = DAOSingleton.getInstance(context).getDb();
			String toggle = db.getServiceStatus().getToggle();
			if(toggle.equals("1")){
				db.updateServiceToggle("0");
				SmartModeGenericService.startActionRemoveNotification(context);
				SmartModeGenericService.startCancelScheduleOfService(context);
			}else{
				ServiceStatus t = db.getServiceStatus();
				Time lTime = new Time();
				lTime.set(Long.parseLong(t.getLastTime()));
				Time cur = new Time();
				cur.setToNow();
				long past = lTime.toMillis(false);
				long current = cur.toMillis(false);
				String prefSchedule = t.getBeenScheduled();
				int prefScheduleINT = Integer.parseInt(prefSchedule);
				if(t.getStatus().equals("0") && toggle.equals("0") && (t.getBeenScheduled().equals("0") || (past + prefScheduleINT * DateUtils.MINUTE_IN_MILLIS) < current)){
					Intent mServiceIntent = new Intent(context, ModeManagerService.class);
					context.startService(mServiceIntent);	
				//	SmartModeGenericService.startActionRepeatUpdateMode(context);
					db.updateServiceToggle("1");
				}else{
					db.updateServiceToggle("1");
				}
			}
			// Bundle extras = intent.getExtras();
			// if(extras!=null) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), SmartModeWidget.class.getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);
			onUpdate(context, appWidgetManager, appWidgetIds);
			// }
	    }else if(SILENCE_MANUAL_TOGGLE.equals(actionKey)){
			String preState = getPreferenceString(context.getString(R.string.silence_manual_toggle_key),context);
			String preStateOther = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key), context);
			AudioManager audioM = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			if (preState.equals("0")) {
				if(preStateOther.equals("1")){
					setPreferenceString("0", context.getString(R.string.vibrate_manual_toggle_key), context);
				}
				boolean newState = setPreferenceString("1", context.getString(R.string.silence_manual_toggle_key), context);
				audioM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			} else {
				boolean newState = setPreferenceString("0", context.getString(R.string.silence_manual_toggle_key), context);
				audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			}
			SmartModeGenericService.startActionUpdateMode(context);
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), SmartModeWidget.class.getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);
			onUpdate(context, appWidgetManager, appWidgetIds);
	    }else if(VIBRATE_MANUAL_TOGGLE.equals(actionKey)){
	    	String preState = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key),context);
	    	String preStateOther = getPreferenceString(context.getString(R.string.silence_manual_toggle_key), context);
			AudioManager audioM = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			if (preState.equals("0")) {
				if(preStateOther.equals("1")){
					setPreferenceString("0", context.getString(R.string.silence_manual_toggle_key), context);
				}
				boolean newState = setPreferenceString("1", context.getString(R.string.vibrate_manual_toggle_key), context);
				audioM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			} else {
				boolean newState = setPreferenceString("0", context.getString(R.string.vibrate_manual_toggle_key), context);
				audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			}
			SmartModeGenericService.startActionUpdateMode(context);
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), SmartModeWidget.class.getName());
			int[] appWidgetIds = appWidgetManager
					.getAppWidgetIds(thisAppWidget);
			onUpdate(context, appWidgetManager, appWidgetIds);
	    }
	}
	
	
	
	
}
