package com.dolphin.smartmodeelite;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.dialog.EditAutoReplyDialogFragment;
import com.dolphin.dialog.IntroDefaultSetDialogFragment;
import com.dolphin.dialog.ScannedWifiDialogFragment;
import com.dolphin.smartmodeelite.EditWifiActivity.PlaceholderFragment;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;

public class EditDefaultSet extends Activity implements EditAutoReplyDialogFragment.EditAutoReplyDialogListener{
	SmartModeDAO db;
	WifiRecord defaultSetting;
	private String autoReplyMsg = "";
	private boolean isEnable = false;
	static final String FRAGMENT_TAG = "editDefaultSetFragmentTag";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_wifi);
		this.setTitle(this.getString(R.string.defaultModeTitle));
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), FRAGMENT_TAG).commit();
		}
		db = DAOSingleton.getInstance(this).getDb();
		WifiRecord defaultRecord = db.getDefaultSet();
		if(defaultRecord == null){
			FragmentManager fm =this.getFragmentManager();
			IntroDefaultSetDialogFragment tmpFragment = new IntroDefaultSetDialogFragment();
			tmpFragment.show(fm, "defaultIntro");
			defaultSetting = new WifiRecord("Default Set", "0,0,0,0,0,0,0");
		}else {
			defaultSetting = defaultRecord;
		}
		autoReplyMsg = this.getPreferenceAutoReplyMsg(this);
		isEnable = this.getPreferenceAutoReplyEnable(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_default_set, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
	public String getAutoReplyMsg() {
		return autoReplyMsg;
	}

	public void setAutoReplyMsg(String autoReplyMsg) {
		this.autoReplyMsg = autoReplyMsg;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}
	private String getPreferenceAutoReplyMsg(Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		String savedIfo = sharePrefere.getString(this.getString(R.string.defaultModeAutoReplyMsg), "");
		return savedIfo;
	}
	private boolean getPreferenceAutoReplyEnable(Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean savedIfo = sharePrefere.getBoolean(this.getString(R.string.defaultModeAutoReplyEnable), false);
		return savedIfo;
	}
	private boolean setPreferenceAutoReplyMsg(String val, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putString(this.getString(R.string.defaultModeAutoReplyMsg), val).commit();
		return rst;
	}
	private boolean setPreferenceAutoReplyEnable(boolean val, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putBoolean(this.getString(R.string.defaultModeAutoReplyEnable), val).commit();
		return rst;
	}
	public void printMessage(Context ctx, String msg){
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener, OnCheckedChangeListener{
		EditText editSSID;
		Button cancel;
		Button save;
		RadioGroup radioGroup;
		SeekBar ringtonSeekBar;
		SeekBar musicSeekBar;
		SeekBar alarmSeekBar;
		SeekBar notificationSeekBar;
		CheckBox isEnableCheckBox;
		EditDefaultSet activity;
		Button editAutoReply;
		/**
		 * WifiRecord that this edit activity will interact with.
		 */
		WifiRecord recordToEdit;
		public PlaceholderFragment() {
		}
		//here
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.edit_wifi_record, container,
					false);
			//Hide the ssid tag, since this is a default set window.
			TextView ssidTag = (TextView)rootView.findViewById(R.id.SSIDTag);
			ssidTag.setVisibility(TextView.INVISIBLE);
			editSSID = (EditText)rootView.findViewById(R.id.editTextSSID);
			editSSID.setVisibility(EditText.INVISIBLE);
			isEnableCheckBox = (CheckBox)rootView.findViewById(R.id.enableAutoReplyCheck);
			recordToEdit = ((EditDefaultSet)this.getActivity()).defaultSetting;
			activity = (EditDefaultSet)this.getActivity();
			isEnableCheckBox.setChecked(activity.isEnable);
			if(recordToEdit.getSsid().length() > 0){
				editSSID.setText(recordToEdit.toString());
				editSSID.setKeyListener(null);
			}
			isEnableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					// TODO Auto-generated method stub
					if(activity.autoReplyMsg.length() < 1){
						arg0.setChecked(false);
						activity.printMessage(activity, "Can't enable with empty message.  Please set up message before enabling auto reply.");
					} else {
						activity.setEnable(arg1);
					}
				}
			});
			editAutoReply = (Button)rootView.findViewById(R.id.editAutoReplyBtn);
			editAutoReply.setOnClickListener(this);
			radioGroup = (RadioGroup)rootView.findViewById(R.id.radioGroupMode);
			radioGroup.setOnCheckedChangeListener(this);
			ringtonSeekBar = (SeekBar)rootView.findViewById(R.id.ringtoneSeekBar);
			musicSeekBar = (SeekBar)rootView.findViewById(R.id.musicSeekBar);
			alarmSeekBar = (SeekBar)rootView.findViewById(R.id.alarmSeekBar);
			notificationSeekBar = (SeekBar)rootView.findViewById(R.id.notificationSeekBar);
			String modeStr = recordToEdit.getMode();
			String [] ParsedMode = modeStr.split(",");
			if(ParsedMode == null || ParsedMode.length < 3){
				radioGroup.clearCheck();
			} else if(ParsedMode[0].equals("1")){
				radioGroup.check(R.id.radioSilience);
			} else if(ParsedMode[1].equals("1")){
				radioGroup.check(R.id.radioVibrate);
			} else if(ParsedMode[2].equals("1")){
				radioGroup.check(R.id.radioNormal);
			} else {
				radioGroup.clearCheck();
			}
			AudioManager audioM = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
			ringtonSeekBar.setMax(audioM.getStreamMaxVolume(AudioManager.STREAM_RING));
			musicSeekBar.setMax(audioM.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			alarmSeekBar.setMax(audioM.getStreamMaxVolume(AudioManager.STREAM_ALARM));
			notificationSeekBar.setMax(audioM.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
			ringtonSeekBar.setOnSeekBarChangeListener(this);
			musicSeekBar.setOnSeekBarChangeListener(this);
			alarmSeekBar.setOnSeekBarChangeListener(this);
			notificationSeekBar.setOnSeekBarChangeListener(this);
			if(ParsedMode == null || ParsedMode.length < 6){
				ringtonSeekBar.setProgress(0);
				musicSeekBar.setProgress(0);
				alarmSeekBar.setProgress(0);
				notificationSeekBar.setProgress(0);
			}else{
				try{
					ringtonSeekBar.setProgress(Integer.parseInt(ParsedMode[3]));
				}catch(NumberFormatException e){
					ringtonSeekBar.setProgress(0);
				}
				try{
					musicSeekBar.setProgress(Integer.parseInt(ParsedMode[4]));
				}catch(NumberFormatException e){
					musicSeekBar.setProgress(0);
				}
				try{
					alarmSeekBar.setProgress(Integer.parseInt(ParsedMode[5]));
				}catch(NumberFormatException e){
					alarmSeekBar.setProgress(0);
				}
				try{
					notificationSeekBar.setProgress(Integer.parseInt(ParsedMode[6]));
				}catch(NumberFormatException e){
					notificationSeekBar.setProgress(0);
				}
			}
			cancel = (Button)rootView.findViewById(R.id.cancelWifiEdit);
			cancel.setOnClickListener(this);
			save = (Button)rootView.findViewById(R.id.saveWifiEdit);
			save.setOnClickListener(this);
			return rootView;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == cancel){
				this.getActivity().finish();
			} else if(v == save){
				if(editSSID.getText().toString().length() > 0){
					// Or add currently connected wifi.
					SmartModeDAO tmpDB = DAOSingleton.getInstance(
							this.getActivity()).getDb();
						int silenceB = 0;
						int vibrateB = 0;
						int normalB = 0;
						int checkedButton = radioGroup
								.getCheckedRadioButtonId();
						if (checkedButton == R.id.radioSilience) {
							silenceB = 1;
						} else if (checkedButton == R.id.radioVibrate) {
							vibrateB = 1;
						} else {
							normalB = 1;
						}
						String modeParsed = Integer.toString(silenceB) + ","
								+ Integer.toString(vibrateB) + ","
								+ Integer.toString(normalB) + ","
								+ ringtonSeekBar.getProgress() + ","
								+ musicSeekBar.getProgress() + ","
								+ alarmSeekBar.getProgress() + ","
								+ notificationSeekBar.getProgress();
						recordToEdit.setMode(modeParsed);
						int insertResult = -1;
						insertResult = tmpDB.insertOrUpdateToDefauleSet(recordToEdit);
						if (insertResult == -1) {
							Toast.makeText(
									this.getActivity().getBaseContext(),
									"Can't save the data. Something wrong. Please try it again later.",
									Toast.LENGTH_SHORT).show();
						} else {
							((EditDefaultSet)this.getActivity()).setPreferenceAutoReplyMsg(((EditDefaultSet)this.getActivity()).autoReplyMsg, this.getActivity());
							((EditDefaultSet)this.getActivity()).setPreferenceAutoReplyEnable(((EditDefaultSet)this.getActivity()).isEnable, this.getActivity());
							Toast.makeText(this.getActivity().getBaseContext(),
									"Data saved successfully!",
									Toast.LENGTH_SHORT).show();
							this.getActivity().finish();
						}
				}else{
					Toast.makeText(this.getActivity().getBaseContext(), "SSID can't be left with empty!", Toast.LENGTH_SHORT).show();
				}
			} else if(v == this.editAutoReply){
		    	FragmentManager fm =this.getActivity().getFragmentManager();
				EditAutoReplyDialogFragment tmpFragment = new EditAutoReplyDialogFragment();
				tmpFragment.show(fm, "editAutoReply");
		    }
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			int radioButtonChecked = 0;
			if(radioGroup != null)
				radioButtonChecked = radioGroup.getCheckedRadioButtonId();
			
			switch (seekBar.getId()) {
		    case R.id.ringtoneSeekBar:
		    	if(progress <= notificationSeekBar.getProgress()){
		    		notificationSeekBar.setProgress(progress);
		    	}
		    	if(radioButtonChecked == R.id.radioSilience || radioButtonChecked == R.id.radioVibrate){
		    		ringtonSeekBar.setProgress(0);
		    	}
		        break;
		    case R.id.notificationSeekBar:
		    	if(progress >= ringtonSeekBar.getProgress()){
		    		notificationSeekBar.setProgress(ringtonSeekBar.getProgress());
		    	}
		    	if(radioButtonChecked == R.id.radioSilience || radioButtonChecked == R.id.radioVibrate){
		    		notificationSeekBar.setProgress(0);
		    	}
		    	break;
		    case R.id.musicSeekBar:
		    	if(radioButtonChecked == R.id.radioSilience || radioButtonChecked == R.id.radioVibrate){
		    		musicSeekBar.setProgress(0);
		    	}
		    	break;
		    case R.id.alarmSeekBar:
		    	if(radioButtonChecked == R.id.radioSilience || radioButtonChecked == R.id.radioVibrate){
		    		alarmSeekBar.setProgress(0);
		    	}
		    	break;
		    	
		    }
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		/**
		 * On Check Change Listener for radio group button
		 */
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if(checkedId == R.id.radioSilience || checkedId == R.id.radioVibrate){
				ringtonSeekBar.setProgress(0);
				musicSeekBar.setProgress(0);
				alarmSeekBar.setProgress(0);
				notificationSeekBar.setProgress(0);
			}
		}
	}
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		//User touched the dialog's positive button
		Dialog dialogView = dialog.getDialog();
		if(dialog.getTag().equals("editAutoReply")){
			this.autoReplyMsg = ((EditText)dialogView.findViewById(R.id.replyMessageEditTextDialog)).getText().toString();
			this.isEnable = ((CheckBox)dialogView.findViewById(R.id.enableAutoReplyCheckDialog)).isChecked();
			if(this.autoReplyMsg.length() < 1){
				this.isEnable = false;
			}
			((CheckBox)this.getFragmentManager().findFragmentByTag(FRAGMENT_TAG).getView().findViewById(R.id.enableAutoReplyCheck)).setChecked(isEnable);
		}
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		//User touched the dialog's negative button
		
	}

}
