package com.dolphin.broadcastreceiver;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.smartmodeelite.DAOSingleton;
import com.dolphin.smartmodeelite.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SmartModePhoneStateReceiver extends BroadcastReceiver {
	final String REPLY_TAG = "MissedCallReply";
	final String PREFERENCE_INCOMING_PHONE = "IncomingPhoneNum";
	public final static int DEFAULT_TYPE = 2;
	public final static int WIFI_TYPE = 0;
	public final static int SCHEDULE_TYPE = 1;
	public final static int NONE_TYPE = -1;
        @Override
        public void onReceive(final Context context, Intent intent) {
//                Log.d(MissedCallConstant.APP_TAG, "receive phone state change.");
                TelephonyManager telephonyManager = (TelephonyManager) context
                                .getSystemService(Service.TELEPHONY_SERVICE);
                ReplyPhoneStateListener replyPhoneStateListener = new ReplyPhoneStateListener(
                                context);
                telephonyManager.listen(replyPhoneStateListener,
                                PhoneStateListener.LISTEN_CALL_STATE);
                telephonyManager.listen(replyPhoneStateListener,
                                PhoneStateListener.LISTEN_NONE);
        }

        class ReplyPhoneStateListener extends PhoneStateListener {
                private Context context;

                public ReplyPhoneStateListener(Context context) {
                        this.context = context;
                }

                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
            			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        int olderSharedPreference = sharedPreferences.getInt(context.getString(R.string.preference_older_phone_state), -1);
//                        Log.d(ReplyConstant.REPLY_TAG, "Old phone state: "
//                                        + olderSharedPreference);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(context.getString(R.string.preference_older_phone_state), state);
                        editor.commit();
//                        Log.d(ReplyConstant.REPLY_TAG, "Write phone state: " + state);
                        switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                                if (olderSharedPreference == TelephonyManager.CALL_STATE_RINGING) {
                                		String latestIncoming = sharedPreferences.getString(PREFERENCE_INCOMING_PHONE, "");
                                        Thread replyThread = new Thread(new ReplyThread(state,
                                        		latestIncoming, context));
                                        replyThread.start();
                                }
                                break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                                break;
                        case TelephonyManager.CALL_STATE_RINGING:
                        		editor.putString(PREFERENCE_INCOMING_PHONE, incomingNumber).commit();
                                break;
                        }
                }
        }

        class ReplyThread implements Runnable {
                private int state;
                private String incomingNumber;
                private Context context;

                public ReplyThread(int state, String incomingNumber, Context context) {
                        this.state = state;
                        this.incomingNumber = incomingNumber;
                        this.context = context;
                }

                @Override
                public void run() {
//                        Log.i(ReplyConstant.REPLY_TAG, "Phone state change. state:" + state
//                                        + ", incomingNumber: " + incomingNumber);
                        try {
                                //Uri allCalls = Uri.parse("content://call_log/calls");
                                //Cursor cursor = this.context.getContentResolver().query(
                                //                allCalls, null, "type = " + CallLog.Calls.MISSED_TYPE,
                                //                null, "date DESC");

                                SmsManager smsManager = SmsManager.getDefault();
                                /*
                                Intent sentIntent = new Intent(ReplyConstant.SMS_SENT_INTENT);
                                sentIntent.putExtra(ReplyConstant.INTENT_DESTINATION_ADDRESS,
                                                incomingNumber);
                                sentIntent.putExtra(ReplyConstant.INTENT_TEXT, "I'm driving");
                                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(
                                                this.context, 0, sentIntent,
                                                PendingIntent.FLAG_UPDATE_CURRENT);

                                Intent deliverIntent = new Intent(
                                                ReplyConstant.SMS_DELIVER_INTENT);
                                PendingIntent deliverPendingIntent = PendingIntent
                                                .getBroadcast(this.context, 0, deliverIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);
								*/
                                String msgToSend = getReplyMsg();
                                //Log.i("SmartModeMessage", "Message: "+msgToSend+"-END");
                                if(msgToSend != null){
                                	 smsManager.sendMultipartTextMessage(this.incomingNumber, null, smsManager.divideMessage(msgToSend), null, null);
                                	 //smsManager.sendTextMessage(this.incomingNumber, null, "Sorry I can't answer ur call. Will call you back later.",
                                     //        null, null);
                                }
                        } catch (Exception e) {
                                Log.e(REPLY_TAG,
                                                "Unable to handle sending sms task.", e);
                        } finally {
                                this.incomingNumber = null;
                                this.context = null;
                        }
                        Log.d(REPLY_TAG, "End reply thread!");
                }
                
                public String getReplyMsg(){
                	String msg = null;
            		String modeName = getPreferenceCurrentMode(context);
                	SmartModeDAO db = DAOSingleton.getInstance(context).getDb();
                	//String curModeName = db.getCurrentMode();
                	int type = getPreferenceCurrentModeType(context);
                	switch(type){
                	case WIFI_TYPE:
                		msg = db.getAutoMsgByMode(db.getRowIDFromSSID(modeName), type, true);
                		break;
                	case SCHEDULE_TYPE:
                		msg = db.getAutoMsgByMode(db.getRowIDFromSchedule(modeName), type, true);
                		break;
                	case DEFAULT_TYPE:
                		if(getPreferenceAutoReplyEnable(context)){
                			msg = getPreferenceAutoReplyMsg(context);
                		}
                		break;
                	default:
                		break;
                	}
                	return msg;
                }
                /**
                 * Get current mode name for getting corresponding auto reply messagae.
                 * @param context
                 * @return
                 */
                private String getPreferenceCurrentMode(Context context){
            		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
            		String savedIfo = sharePrefere.getString(context.getString(R.string.current_mode_key), "");
            		return savedIfo;
            	}
                /**
                 * Get current mode type to determine which auto reply message to apply.
                 * @param context
                 * @return 0:wifi 1:schedule 2:default -1:none
                 */
            	private int getPreferenceCurrentModeType(Context context){
            		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
            		int savedIfo = sharePrefere.getInt(context.getString(R.string.current_mode_type_key), NONE_TYPE);
            		return savedIfo;
            	}
            	private String getPreferenceAutoReplyMsg(Context context){
            		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
            		String savedIfo = sharePrefere.getString(context.getString(R.string.defaultModeAutoReplyMsg), "");
            		return savedIfo;
            	}
            	private boolean getPreferenceAutoReplyEnable(Context context){
            		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
            		boolean savedIfo = sharePrefere.getBoolean(context.getString(R.string.defaultModeAutoReplyEnable), false);
            		return savedIfo;
            	}
        }

}