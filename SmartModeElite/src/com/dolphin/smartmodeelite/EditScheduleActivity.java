package com.dolphin.smartmodeelite;

import com.dolphin.dao.SmartModeDAO;
import com.dolphin.dialog.EditAutoReplyDialogFragment;
import com.dolphin.dialog.ScannedWifiDialogFragment;
import com.dolphin.dialog.TimePickerFragment;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.os.Build;

public class EditScheduleActivity extends Activity implements EditAutoReplyDialogFragment.EditAutoReplyDialogListener{
	/**
	 * Tag for start time view tag
	 */
	public static final String START_TIME_TAG = "StartTimeTag";
	/**
	 * Tag for end time view tag
	 */
	public static final String END_TIME_TAG = "EndTimeTag";
	/**
	 * Tag for identifying start time picker fragment.
	 */
	public static final String START_TIME_PICKER_TAG = "startTimePicker";
	/**
	 * Tag for identifying end time picker fragment.
	 */
	public static final String END_TIME_PICKER_TAG = "endTimePicker";
	/**
	 * Key for temporary storing info from time(start/end) for fragment to use.
	 */
	public static final String TIME_BOARD_KEY = "TimeBoard";
	PlaceholderFragment pholderFragment;
	private String autoReplyMsg = "";
	private boolean isEnable = false;
	static final int SCHEDULE_MODE = 1;
	static final String FRAGMENT_TAG = "editScheduleFragmentTag";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_schedule);

		if (savedInstanceState == null) {
			pholderFragment = new PlaceholderFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.container, pholderFragment, FRAGMENT_TAG).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		if(this.getIntent().getStringExtra(MainActivity.NEW_TAG) == null)
			getMenuInflater().inflate(R.menu.edit_schedule, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.deleteSchedule) {
			ScheduleRecord recordToEdit = (ScheduleRecord)this.getIntent().getParcelableExtra(MainActivity.KEY_SCHEDULE_RECORD);
			String tag = this.getIntent().getStringExtra(MainActivity.NEW_TAG);
			if(tag != null){
				Toast.makeText(this.getBaseContext(), "Cannot delete the data that has not been stored to the database.", Toast.LENGTH_SHORT).show();
			} else {
				SmartModeDAO tmpDB = DAOSingleton.getInstance(this).getDb();
				int rst = tmpDB.deleteFromSchedule(recordToEdit.getSchedulename());
				if(rst > 0){
					Toast.makeText(this.getBaseContext(), recordToEdit.getSchedulename()+" has been removed.", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(this.getBaseContext(), "Some thing wrong. \n"+recordToEdit.getSchedulename()+" has not been removed.", Toast.LENGTH_SHORT).show();
				}
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void updateTimeBoard(int hourOfDay, int minute, Fragment from){
		if(from.getTag().equals(START_TIME_PICKER_TAG)){
			TextView stboard = (TextView)pholderFragment.getView().findViewWithTag(START_TIME_TAG);
			int time = hourOfDay*ScheduleRecord.HOUR_MASK + minute;
			String toShow = ScheduleRecord.parseTimeString(time, DateFormat.is24HourFormat(this));
			stboard.setText(toShow);
		}else if(from.getTag().equals(END_TIME_PICKER_TAG)){
			TextView etboard = (TextView)pholderFragment.getView().findViewWithTag(END_TIME_TAG);
			int time = hourOfDay*ScheduleRecord.HOUR_MASK + minute;
			String toShow = ScheduleRecord.parseTimeString(time, DateFormat.is24HourFormat(this));
			etboard.setText(toShow);
		}
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
	public void printMessage(Context ctx, String msg){
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}


	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener{
		/**
		 * Temp edit.
		 */
		ScheduleRecord recordToEdit;
		Button editStartTime;
		Button editEndTime;
		TextView startTime;
		TextView endTime;
		EditText editScheduleID;
		RadioGroup radioGroup;
		SeekBar ringtonSeekBar;
		SeekBar musicSeekBar;
		SeekBar alarmSeekBar;
		SeekBar notificationSeekBar;
		Button cancel;
		Button save;
		CheckBox isEnableCheckBox;
		/**
		 * Elite special
		 * Edit auto reply button.
		 */
		Button editAutoReply;
		EditScheduleActivity activity;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			SmartModeDAO db = DAOSingleton.getInstance(this.getActivity()).getDb();
			View rootView = inflater.inflate(R.layout.edit_schedule_record,
					container, false);
			recordToEdit = (ScheduleRecord)this.getActivity().getIntent().getParcelableExtra(MainActivity.KEY_SCHEDULE_RECORD);
			this.getActivity().setTitle(recordToEdit.getSchedulename());
			activity = (EditScheduleActivity)this.getActivity();
			editStartTime = (Button) rootView.findViewById(R.id.editStartTime);
			editEndTime = (Button) rootView.findViewById(R.id.editEndTime);
			editScheduleID = (EditText)rootView.findViewById(R.id.editScheduleName);
			editStartTime.setOnClickListener(this);
			editEndTime.setOnClickListener(this);
			editAutoReply = (Button)rootView.findViewById(R.id.editAutoReplyBtn);
			editAutoReply.setOnClickListener(this);
			editScheduleID = (EditText)rootView.findViewById(R.id.editScheduleName);
			isEnableCheckBox = (CheckBox)rootView.findViewById(R.id.enableAutoReplyCheck);
			if(recordToEdit.getSchedulename().length() > 0){
				editScheduleID.setText(recordToEdit.toString());
				editScheduleID.setKeyListener(null);
				long modeID = db.getRowIDFromSchedule(recordToEdit.toString());
				String autoMsg = db.getAutoMsgByMode(modeID, SCHEDULE_MODE, false);
				if(autoMsg != null){
					((EditScheduleActivity)this.getActivity()).setAutoReplyMsg(autoMsg);
					boolean isEnable = db.isEnableAutoMsgByMode(modeID, SCHEDULE_MODE);
					((EditScheduleActivity)this.getActivity()).setEnable(isEnable);
					isEnableCheckBox.setChecked(isEnable);
				}
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
			startTime = (TextView)rootView.findViewById(R.id.startTimeBoard);
			startTime.setTag(START_TIME_TAG);
			endTime = (TextView)rootView.findViewById(R.id.endTimeBoard);
			endTime.setTag(END_TIME_TAG);
			startTime.setText(recordToEdit.getStartTimeString(DateFormat.is24HourFormat(getActivity())));
			endTime.setText(recordToEdit.getEndTimeString(DateFormat.is24HourFormat(getActivity())));
			radioGroup = (RadioGroup)rootView.findViewById(R.id.radioGroupMode);
			radioGroup.setOnCheckedChangeListener(this);
			ringtonSeekBar = (SeekBar)rootView.findViewById(R.id.ringtoneSeekBar);
			musicSeekBar = (SeekBar)rootView.findViewById(R.id.musicSeekBar);
			alarmSeekBar = (SeekBar)rootView.findViewById(R.id.alarmSeekBar);
			notificationSeekBar = (SeekBar)rootView.findViewById(R.id.notificationSeekBar);
			String modeStr = recordToEdit.getPhoneMode();
			if(modeStr.equals("mute")){
				radioGroup.check(R.id.radioSilience);
			} else if(modeStr.equals("vibrate")){
				radioGroup.check(R.id.radioVibrate);
			} else if(modeStr.equals("normal")){
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
			try {
				ringtonSeekBar.setProgress(Integer.parseInt(recordToEdit.getRingToneVol()));
			} catch (NumberFormatException e) {
				ringtonSeekBar.setProgress(0);
			}
			try {
				musicSeekBar.setProgress(Integer.parseInt(recordToEdit.getMusicVol()));
			} catch (NumberFormatException e) {
				musicSeekBar.setProgress(0);
			}
			try {
				alarmSeekBar.setProgress(Integer.parseInt(recordToEdit.getAlarmVol()));
			} catch (NumberFormatException e) {
				alarmSeekBar.setProgress(0);
			}
			try {
				notificationSeekBar
						.setProgress(Integer.parseInt(recordToEdit.getNotificationVol()));
			} catch (NumberFormatException e) {
				notificationSeekBar.setProgress(0);
			}
			cancel = (Button)rootView.findViewById(R.id.cancelScheduleEdit);
			cancel.setOnClickListener(this);
			save = (Button)rootView.findViewById(R.id.saveScheduleEdit);
			save.setOnClickListener(this);
			return rootView;
		}

		@Override
		public void onClick(View v) {
		    if(v == this.editStartTime){
		    	DialogFragment newFragment = new TimePickerFragment();
		    	Bundle args = new Bundle();
		    	int st = ScheduleRecord.parseTime(startTime.getText().toString(), DateFormat.is24HourFormat(this.getActivity()));
		    	args.putInt("TimeBoard", st);		    	
		    	newFragment.setArguments(args);
			    newFragment.show(getFragmentManager(), START_TIME_PICKER_TAG);
		    } else if(v == this.editEndTime){
		    	DialogFragment newFragment = new TimePickerFragment();
		    	Bundle args = new Bundle();
		    	int et = ScheduleRecord.parseTime(endTime.getText().toString(), DateFormat.is24HourFormat(this.getActivity()));
		    	args.putInt("TimeBoard", et);
		    	newFragment.setArguments(args);
			    newFragment.show(getFragmentManager(), END_TIME_PICKER_TAG);
		    } else if(v == this.save){
		    	String tag = this.getActivity().getIntent().getStringExtra(MainActivity.NEW_TAG);
		    	if(tag != null && tag.equals("new")){
		    		//create new schedule record.
		    		boolean succ = createNewScheduleRecord();
		    		if(succ){
			    		printMessage("New schedule has been saved successfully.");
		    			this.getActivity().finish();
		    		}
		    	}else{
		    		//edit existing record schedule.
		    		boolean succ = editExistingScheduleRecord();
		    		if(succ){
			    		printMessage("Schedule has been changed successfully.");
		    			this.getActivity().finish();
		    		}
		    	}
		    } else if(v == this.cancel){
				this.getActivity().finish();
		    } else if(v == this.editAutoReply){
		    	FragmentManager fm =this.getActivity().getFragmentManager();
				EditAutoReplyDialogFragment tmpFragment = new EditAutoReplyDialogFragment();
				tmpFragment.show(fm, "editAutoReply");
		    }
			// TODO Auto-generated method stub
			
		}
		/**
		 * The method to createnew Scheudle record
		 * @return true success false fail
		 */
		public boolean createNewScheduleRecord(){
			SmartModeDAO db = DAOSingleton.getInstance(this.getActivity()).getDb();
			if(this.editScheduleID.length() == 0){
				printMessage("The schedule id cannot be empty.");
				return false;
			}
			int checkIfthere = db.getFromScheduleByName(this.editScheduleID.getText().toString()).size();
			if(checkIfthere > 0){
				printMessage("The schedule has already exist. Please edit the existing one instead.");
				return false;
			}
			int silence = 0;
			int vibrate = 0;
			int normal = 0;
			int checkedButton = radioGroup.getCheckedRadioButtonId();
			if(checkedButton == R.id.radioSilience){
				silence = 1;
			}else if(checkedButton == R.id.radioVibrate){
				vibrate = 1;
			}else {
				normal = 1;
			}
			String modeToSave = parseMode(silence, vibrate, normal,
					ringtonSeekBar.getProgress(), musicSeekBar.getProgress(),
					alarmSeekBar.getProgress(),
					notificationSeekBar.getProgress());
			String schName = this.editScheduleID.getText().toString();
			ScheduleRecord srToSave = new ScheduleRecord(schName, modeToSave,
					startTime.getText().toString(), endTime.getText()
							.toString(), DateFormat.is24HourFormat(this
							.getActivity()));
			long result1 = db.insertToSchedule(srToSave);
			long result2 = -1;
			if(result1 != -1){
				result2= db.insertOrUpdateAutoReply(result1, SCHEDULE_MODE,((EditScheduleActivity)this.getActivity()).autoReplyMsg, ((EditScheduleActivity)this.getActivity()).isEnable?1:0);
			}
			return result2 == -1 ? false : true;
		}
		
		
		/**
		 * edit schedule record
		 * @return true success false fail.
		 */
		public boolean editExistingScheduleRecord(){
			SmartModeDAO db = DAOSingleton.getInstance(this.getActivity()).getDb();
			if(this.editScheduleID.length() == 0){
				printMessage("The schedule id cannot be empty.");
				return false;
			}
			int checkIfthere = db.getFromScheduleByName(this.editScheduleID.getText().toString()).size();
			if(checkIfthere < 1){
				printMessage("Something went wrong. Editting null record.");
				return false;
			}
			int silence = 0;
			int vibrate = 0;
			int normal = 0;
			int checkedButton = radioGroup.getCheckedRadioButtonId();
			if(checkedButton == R.id.radioSilience){
				silence = 1;
			}else if(checkedButton == R.id.radioVibrate){
				vibrate = 1;
			}else {
				normal = 1;
			}
			String modeToSave = parseMode(silence, vibrate, normal,
					ringtonSeekBar.getProgress(), musicSeekBar.getProgress(),
					alarmSeekBar.getProgress(),
					notificationSeekBar.getProgress());
			String schName = this.editScheduleID.getText().toString();
			ScheduleRecord srToSave = new ScheduleRecord(schName, modeToSave,
					startTime.getText().toString(), endTime.getText()
					.toString(), DateFormat.is24HourFormat(this
					.getActivity()));
			long result = db.updateRecordSchedule(srToSave);
			long recordID = db.getRowIDFromSchedule(schName);
			long result2 = -1;
			if(result != -1){
				result2 = db.insertOrUpdateAutoReply(recordID, SCHEDULE_MODE, ((EditScheduleActivity)this.getActivity()).autoReplyMsg, ((EditScheduleActivity)this.getActivity()).isEnable?1:0);
			}
			return result2==-1?false:true;
		}
		
		/**
		 * Helper method to generate parsedmode that will be used to store in database.
		 * @param silchk  Silence radio button.
		 * @param vibratechk Vibrate Radio button.
		 * @param normalchk Normal Radio button.
		 * @param ringtonVol ringtone seek bar.
		 * @param musicVol Music seek bar.
		 * @param alarmVol Alarm seek bar.
		 * @param notificationVol Notification Seek bar.
		 * @return
		 */
		public String parseMode(int silchk, int vibratechk,
				int normalchk, int ringtonVol, int musicVol,
				int alarmVol, int notificationVol) {
			String modeParsed = silchk + "," + vibratechk + "," + normalchk
					+ "," + ringtonVol + "," + musicVol + "," + alarmVol + ","
					+ notificationVol;
			return modeParsed;
		}

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (checkedId == R.id.radioSilience
					|| checkedId == R.id.radioVibrate) {
				ringtonSeekBar.setProgress(0);
				musicSeekBar.setProgress(0);
				alarmSeekBar.setProgress(0);
				notificationSeekBar.setProgress(0);
			}

		}
		/**
		 * helper method to print message.
		 * @param msg
		 */
		public void printMessage(String msg){
			Toast.makeText(this.getActivity().getBaseContext(), msg, Toast.LENGTH_SHORT).show();
		}
		
		/**
		 * Method to check if all progress bars are zero or not.
		 * @return true if all are zeros otherwise false.
		 */
		public boolean areAllProgessesZero(){
			if(ringtonSeekBar.getProgress() > 0)
				return false;
			if(musicSeekBar.getProgress() > 0)
				return false;
			if(alarmSeekBar.getProgress() > 0)
				return false;
			if(notificationSeekBar.getProgress() > 0)
				return false;
			
			return true;
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
