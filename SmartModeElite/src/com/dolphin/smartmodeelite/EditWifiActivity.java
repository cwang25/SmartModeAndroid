package com.dolphin.smartmodeelite;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.dialog.EditAutoReplyDialogFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class EditWifiActivity extends Activity implements EditAutoReplyDialogFragment.EditAutoReplyDialogListener{
	//For auto reply message database.
	private String autoReplyMsg = "";
	private boolean isEnable = false;
	static final int WIFI_MODE = 0;
	static final String FRAGMENT_TAG = "editWifiFragmentTag";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_wifi);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), FRAGMENT_TAG).commit();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_wifi, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.deleteWifi) {
			WifiRecord recordToEdit = (WifiRecord)this.getIntent().getParcelableExtra(MainActivity.KEY_WIFI_RECORD);
			String tag = this.getIntent().getStringExtra(MainActivity.NEW_TAG);
			if(tag != null){
				Toast.makeText(this.getBaseContext(), "Cannot delete the data that has not been stored to the database.", Toast.LENGTH_SHORT).show();
			} else {
				SmartModeDAO tmpDB = DAOSingleton.getInstance(this).getDb();
				int rst = tmpDB.deleteFromSSID(recordToEdit.getSsid());
				if(rst > 0){
					Toast.makeText(this.getBaseContext(), recordToEdit.toString()+" has been removed.", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this.getBaseContext(), "Something wrong. \n"+recordToEdit.getSsid()+" has not been removed.", Toast.LENGTH_SHORT).show();
				}
				finish();
			}
			return true;
		}
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

	private void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
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
		/**
		 * Elite special
		 * Edit auto reply button.
		 */
		Button editAutoReply;
		/**
		 * WifiRecord that this edit activity will interact with.
		 */
		WifiRecord recordToEdit;
		/**
		 * Activity for this fragment.
		 */
		EditWifiActivity activity;
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			SmartModeDAO db = DAOSingleton.getInstance(this.getActivity()).getDb();
			View rootView = inflater.inflate(R.layout.edit_wifi_record, container,
					false);
			recordToEdit = (WifiRecord)this.getActivity().getIntent().getParcelableExtra(MainActivity.KEY_WIFI_RECORD);
			this.getActivity().setTitle(recordToEdit.getSsid());
			activity = (EditWifiActivity)this.getActivity();
			editSSID = (EditText)rootView.findViewById(R.id.editTextSSID);
			isEnableCheckBox = (CheckBox)rootView.findViewById(R.id.enableAutoReplyCheck);
			if(recordToEdit.getSsid().length() > 0){
				editSSID.setText(recordToEdit.toString());
				editSSID.setKeyListener(null);
				long modeID = db.getRowIDFromSSID(recordToEdit.toString());
				String autoMsg = db.getAutoMsgByMode(modeID, WIFI_MODE, false);
				if(autoMsg != null){
					((EditWifiActivity)this.getActivity()).setAutoReplyMsg(autoMsg);
					boolean isEnable = db.isEnableAutoMsgByMode(modeID, WIFI_MODE);
					((EditWifiActivity)this.getActivity()).setEnable(isEnable);
					isEnableCheckBox.setChecked(isEnable);
				}

			}
			isEnableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					// TODO Auto-generated method stub
					if(activity.autoReplyMsg.length() < 1){
						arg0.setChecked(false);
						activity.printMessage(activity, activity.getString(R.string.err_reply_msg));
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
					if(recordToEdit.getSsid().length() == 0){
						//Create new record section.
						String ssidToSave = this.editSSID.getText().toString();
						SmartModeDAO tmpDB = DAOSingleton.getInstance(this.getActivity()).getDb();
						int checkIfThere = tmpDB.getFromSSIDBySSID(ssidToSave).size();
						if(checkIfThere == 0){
							int silenceB = 0;
							int vibrateB = 0;
							int normalB = 0;
							int checkedButton = radioGroup.getCheckedRadioButtonId();
							if(checkedButton == R.id.radioSilience){
								silenceB = 1;
							} else if(checkedButton == R.id.radioVibrate){
								vibrateB = 1;
							} else {
								normalB = 1;
							}
							String modeParsed = Integer.toString(silenceB)
									+ "," + Integer.toString(vibrateB) + ","
									+ Integer.toString(normalB) + "," + ringtonSeekBar.getProgress() + ","
									+ musicSeekBar.getProgress() + "," + alarmSeekBar.getProgress() + ","
									+ notificationSeekBar.getProgress();
							WifiRecord wrToSave = new WifiRecord(ssidToSave,modeParsed);
							long insertResult = tmpDB.insertToSSID(wrToSave);
							if(insertResult == -1){
								Toast.makeText(this.getActivity().getBaseContext(), this.getActivity().getString(R.string.cant_plz_try_again), Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(this.getActivity().getBaseContext(), this.getActivity().getString(R.string.data_create_succesfully), Toast.LENGTH_SHORT).show();
								tmpDB.insertOrUpdateAutoReply(insertResult, WIFI_MODE, ((EditWifiActivity)this.getActivity()).autoReplyMsg, ((EditWifiActivity)this.getActivity()).isEnable?1:0);
								this.getActivity().finish();
							}
						} else {
							Toast.makeText(this.getActivity().getBaseContext(), this.getActivity().getString(R.string.wifi_alreadyt_exist), Toast.LENGTH_SHORT).show();
						}
					} else {
						//Edit existing record section.
						//Or add currently connected wifi.
						String ssidToSave = this.editSSID.getText().toString();
						SmartModeDAO tmpDB = DAOSingleton.getInstance(this.getActivity()).getDb();
						int checkIfThere = tmpDB.getFromSSIDBySSID(ssidToSave).size();
						String tag = this.getActivity().getIntent().getStringExtra(MainActivity.NEW_TAG);
						if(checkIfThere > 0 || tag != null){
							int silenceB = 0;
							int vibrateB = 0;
							int normalB = 0;
							int checkedButton = radioGroup.getCheckedRadioButtonId();
							if(checkedButton == R.id.radioSilience){
								silenceB = 1;
							} else if(checkedButton == R.id.radioVibrate){
								vibrateB = 1;
							} else {
								normalB = 1;
							}
							String modeParsed = Integer.toString(silenceB)
									+ "," + Integer.toString(vibrateB) + ","
									+ Integer.toString(normalB) + "," + ringtonSeekBar.getProgress() + ","
									+ musicSeekBar.getProgress() + "," + alarmSeekBar.getProgress() + ","
									+ notificationSeekBar.getProgress();
							WifiRecord wrToSave = new WifiRecord(ssidToSave,modeParsed);
							long insertResult = -1;
							if (tag != null && tag.equals("new")) {
								insertResult = tmpDB.insertToSSID(wrToSave);
							} else {
								insertResult = tmpDB.updateRecordSSID(wrToSave);
							}
							if(insertResult == -1){
								Toast.makeText(this.getActivity().getBaseContext(), this.getActivity().getString(R.string.cant_plz_try_again), Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(this.getActivity().getBaseContext(), this.getActivity().getString(R.string.data_create_succesfully), Toast.LENGTH_SHORT).show();
								long recordID = tmpDB.getRowIDFromSSID(ssidToSave);
								tmpDB.insertOrUpdateAutoReply(recordID, WIFI_MODE, ((EditWifiActivity)this.getActivity()).autoReplyMsg, ((EditWifiActivity)this.getActivity()).isEnable?1:0);
								this.getActivity().finish();
							}
						} else {
							Toast.makeText(this.getActivity().getBaseContext(),this.getActivity().getString(R.string.cant_plz_try_again), Toast.LENGTH_SHORT).show();
						}
					}
				}else{
					Toast.makeText(this.getActivity().getBaseContext(), this.getActivity().getString(R.string.weifi_cant_be_empty), Toast.LENGTH_SHORT).show();
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
	public int saveAutoReplyMsg(long modeId, int mode, String msg, int enable){
		SmartModeDAO db = DAOSingleton.getInstance(this).getDb();
		int rst = db.insertOrUpdateAutoReply(modeId, mode, msg, enable);
		return rst;
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
