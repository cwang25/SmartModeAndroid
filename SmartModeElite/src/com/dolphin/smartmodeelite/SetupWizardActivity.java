package com.dolphin.smartmodeelite;

import java.util.Locale;

import com.dolphin.dialog.ScannedWifiDialogFragment;
import com.dolphin.service.ModeManagerService;
import com.dolphin.service.SmartModeGenericService;
import com.dolphin.wifiInfo.WifiInfoNoQuote;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class SetupWizardActivity extends Activity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_wizard);
		this.setTitle(R.string.setUpWizardTitle);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		//allow static views to update corresponding to user pager current posistion.
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				//add 1 because index start with 0, section start with 1.
				updateStaticViews(arg0+1);
			}});
		//Start on first page, 
		updateStaticViews(1);
		((ProgressBar) this.findViewById(R.id.progressBarSetupWizard))
				.setMax(8);
		((ImageView) this.findViewById(R.id.setupWizardRightArrow))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						moveNextPage();
					}
				});
		((ImageView) this.findViewById(R.id.setupWizardLeftArrow))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						movePrevPage();
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setup_wizard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	//Move back and forth page.
	public void moveNextPage(){
		int cur = mViewPager.getCurrentItem();
		mViewPager.setCurrentItem(cur + 1, true);
	}
	
	public void movePrevPage(){
		int cur = mViewPager.getCurrentItem();
		mViewPager.setCurrentItem(cur - 1, true);
	}
	//Whole bunch of sharedpreference updater and getter for setup.
	public boolean getSensitiveSwitchState(){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		return sharePreference.getBoolean(
				getString(R.string.sensitive_mode_key), false);
	}
	public void setSensitiveSwitchState(boolean newState){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharePreference.edit().putBoolean(getString(R.string.sensitive_mode_key), newState).commit();
	}
	public boolean getNotificationShowState(){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		return sharePreference.getBoolean(
				getString(R.string.notification_toggle_key), true);
	}
	public void setNotificationShowState(boolean newState){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharePreference.edit().putBoolean(getString(R.string.notification_toggle_key), newState).commit();
	}
	public String getFrequencyState(){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		return 	sharePreference.getString(getString(R.string.frequency_saved_key), "5");
	}
	public void setFrequencyState(String newState){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharePreference.edit().putString(getString(R.string.frequency_saved_key), newState).commit();
	}
	public String getDetectionPriority(){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		return  sharePrefere.getString(getString(R.string.mode_priority_key), "0");
	}
	public void setDetectionPriority(String newState){
		SharedPreferences sharePreference = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharePreference.edit().putString(getString(R.string.mode_priority_key), newState).commit();
	}
	
	public void updateStaticViews(int sectionnumber){
		if(sectionnumber == 1){
			this.findViewById(R.id.setupWizardLeftArrow).setVisibility(ImageView.INVISIBLE);
		} else if(sectionnumber == 8){
			this.findViewById(R.id.setupWizardRightArrow).setVisibility(ImageView.INVISIBLE);
		} else{
			this.findViewById(R.id.setupWizardLeftArrow).setVisibility(ImageView.VISIBLE);
			this.findViewById(R.id.setupWizardRightArrow).setVisibility(ImageView.VISIBLE);
		}
		((ProgressBar)this.findViewById(R.id.progressBarSetupWizard)).setProgress(sectionnumber);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 8;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.step_1_title).toUpperCase(l);
			case 1:
				return getString(R.string.step_2_title).toUpperCase(l);
			case 2:
				return getString(R.string.step_3_title).toUpperCase(l);
			case 3:
				return getString(R.string.step_4_title).toUpperCase(l);
			case 4:
				return getString(R.string.step_5_title).toUpperCase(l);
			case 5:
				return getString(R.string.step_6_title).toUpperCase(l);
			case 6:
				return getString(R.string.step_7_title).toUpperCase(l);
			case 7:
				return getString(R.string.step_8_title).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnClickListener, OnCheckedChangeListener, OnItemSelectedListener{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		/**
		 * table for grabbing step title.
		 */
		private static int[] titleTable = { R.string.step_1_title,
				R.string.step_2_title, R.string.step_3_title,
				R.string.step_4_title, R.string.step_5_title,
				R.string.step_6_title, R.string.step_7_title,
				R.string.step_8_title };
		/**
		 * table for grabbing step intro.
		 */
		private static int[] introTable =  { R.string.step_1_intro,
			R.string.step_2_intro, R.string.step_3_intro,
			R.string.step_4_intro, R.string.step_5_intro,
			R.string.step_6_intro, R.string.step_7_intro,
			R.string.step_8_intro };
		/**
		 * Button add wifi from current connected
		 */
		Button addCurrentWifibtn;
		static final int addCurrentWifibtnID = 1;
		/**
		 * Button add wifi from scanning.
		 */
		Button addScanWifibtn;
		static final int addScanWifibtnID = 2;
		/**
		 * Button add wifi from saved wifi.
		 */
		Button addSavedWifibtn;
		static final int addSavedWifibtnID = 3;
		/**
		 * Button add schedule.
		 */
		Button addSchedulebtn;
		static final int addSchedulebtnID = 4;
		/**
		 * End button
		 */
		Button endSetupWizard;
		static final int endSetupWizardID = 5;
		/**
		 * Switch sensitive mode
		 */
		Switch sensitiveModeSwitch;
		static final int sensitiveModeSwitchID = 6;
		/**
		 * Spinner for frequency.
		 */
		Spinner frequencyList;
		static final int frequencyListID = 7;
		/**
		 * Left of spinner.
		 */
		TextView preText;
		static final int preTextID = 8;
		/**
		 * Right of spinner.
		 */
		TextView postText;
		static final int postTextID = 9;
		/**
		 * Notification switch;
		 */
		Switch notificationSwitch;
		static final int notificationSwitchID = 10;
		/**
		 * Mode priority list.
		 */
		Spinner modePriorityList;
		static final int modePriorityListID = 11;
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_setupwizard_1,container, false);
			int args = getArguments().getInt(ARG_SECTION_NUMBER);
			((TextView)rootView.findViewById(R.id.stepTitle)).setText(this.getString(titleTable[args-1]));
			((TextView)rootView.findViewById(R.id.stepIntro)).setText(this.getString(introTable[args-1]));
			RelativeLayout buttomLayout = (RelativeLayout)rootView.findViewById(R.id.stepSetupPanel);
			switch(args){
			case 1:
			{
				TextView instruction = new TextView(this.getActivity());
				instruction.setText(this.getString(R.string.step_1_instruction));
				instruction.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_Medium);
				RelativeLayout.LayoutParams instructParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				instructParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				instructParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				buttomLayout.addView(instruction, instructParams);
				/*
				Button nextBtn = new Button(this.getActivity(), null, android.R.attr.buttonStyleSmall);
				nextBtn.setText(R.string.nextBtnText);
				RelativeLayout.LayoutParams nextBtnParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				nextBtnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
				nextBtnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				nextBtnParams.setMargins(20, 20, 20, 20);
				buttomLayout.addView(nextBtn, nextBtnParams);
				*/
			}
				break;
			case 2:
			{
				addCurrentWifibtn = new Button(this.getActivity());
				addCurrentWifibtn.setId(addCurrentWifibtnID);
				addCurrentWifibtn.setText(R.string.add_current_wifi_text);
				RelativeLayout.LayoutParams curBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT);
				curBtnParam.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				curBtnParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				addCurrentWifibtn.setOnClickListener(this);
				buttomLayout.addView(addCurrentWifibtn, curBtnParam);
				addScanWifibtn = new Button(this.getActivity());
				addScanWifibtn.setId(addScanWifibtnID);
				addScanWifibtn.setText(R.string.add_scan_wifi_text);
				RelativeLayout.LayoutParams scanBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				scanBtnParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				scanBtnParam.addRule(RelativeLayout.BELOW, addCurrentWifibtn.getId());
				addScanWifibtn.setOnClickListener(this);
				buttomLayout.addView(addScanWifibtn, scanBtnParam);
				addSavedWifibtn = new Button(this.getActivity());
				addSavedWifibtn.setId(addSavedWifibtnID);
				addSavedWifibtn.setText(R.string.add_saved_wifi_text);
				RelativeLayout.LayoutParams savedBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				savedBtnParam.addRule(RelativeLayout.BELOW, addScanWifibtn.getId());
				savedBtnParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				addSavedWifibtn.setOnClickListener(this);
				buttomLayout.addView(addSavedWifibtn, savedBtnParam);
			}
				break;
			case 3:
			{
				addSchedulebtn = new Button(this.getActivity());
				addSchedulebtn.setId(addSchedulebtnID);
				addSchedulebtn.setText(R.string.add_schedule_text);
				RelativeLayout.LayoutParams curBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT);
				curBtnParam.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				curBtnParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				addSchedulebtn.setOnClickListener(this);
				buttomLayout.addView(addSchedulebtn, curBtnParam);
			}
				break;
			case 4:
			{
				sensitiveModeSwitch = new Switch(this.getActivity());
				sensitiveModeSwitch.setId(sensitiveModeSwitchID);
				RelativeLayout.LayoutParams modeSwitchParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				modeSwitchParam.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
				modeSwitchParam.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				sensitiveModeSwitch.setOnCheckedChangeListener(this);
				boolean check = ((SetupWizardActivity)this.getActivity()).getSensitiveSwitchState();
				sensitiveModeSwitch.setChecked(check);
				buttomLayout.addView(sensitiveModeSwitch, modeSwitchParam);
			}
				break;
			case 5:
			{
				preText = new TextView(this.getActivity());
				preText.setId(preTextID);
				preText.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_Medium);
				preText.setText("Smart mode will check every:");
				RelativeLayout.LayoutParams preTextParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				preTextParam.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
				preTextParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				buttomLayout.addView(preText, preTextParam);
				frequencyList = new Spinner(this.getActivity());
				frequencyList.setId(frequencyListID);
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.frequencySettingSystem, android.R.layout.simple_spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				frequencyList.setAdapter(adapter);
				String cur_set_freq = ((SetupWizardActivity)this.getActivity()).getFrequencyState();
				int pos = adapter.getPosition(cur_set_freq);
				frequencyList.setSelection(pos);
				frequencyList.setOnItemSelectedListener(this);
				RelativeLayout.LayoutParams spinnerParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				spinnerParam.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
				spinnerParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				spinnerParam.addRule(RelativeLayout.BELOW, preText.getId());
				buttomLayout.addView(frequencyList, spinnerParam);
				postText = new TextView(this.getActivity());
				postText.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_Medium);
				postText.setId(postTextID);
				postText.setText(" minutes.");
				RelativeLayout.LayoutParams postTextParam  = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				postTextParam.addRule(RelativeLayout.RIGHT_OF, frequencyList.getId());
				postTextParam.addRule(RelativeLayout.ALIGN_BASELINE, frequencyList.getId());
				buttomLayout.addView(postText, postTextParam);
			}
				break;
			case 6:
			{
				notificationSwitch = new Switch(this.getActivity());
				notificationSwitch.setId(notificationSwitchID);
				RelativeLayout.LayoutParams modeSwitchParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
				modeSwitchParam.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
				modeSwitchParam.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				notificationSwitch.setOnCheckedChangeListener(this);
				boolean check = ((SetupWizardActivity)this.getActivity()).getNotificationShowState();
				notificationSwitch.setChecked(check);
				buttomLayout.addView(notificationSwitch, modeSwitchParam);
			}
				break;
			case 7:
			{
				modePriorityList = new Spinner(this.getActivity());
				modePriorityList.setId(modePriorityListID);
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.modePiorty, android.R.layout.simple_spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				modePriorityList.setAdapter(adapter);
				String cur_set_pos = ((SetupWizardActivity)this.getActivity()).getDetectionPriority();
				int pos = Integer.parseInt(cur_set_pos);
				modePriorityList.setSelection(pos);
				modePriorityList.setOnItemSelectedListener(this);
				RelativeLayout.LayoutParams spinnerParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				spinnerParam.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
				spinnerParam.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				buttomLayout.addView(modePriorityList, spinnerParam);
			}
				break;
			case 8:
			{
				endSetupWizard = new Button(this.getActivity());
				endSetupWizard.setId(endSetupWizardID);
				endSetupWizard.setText(R.string.done_text);
				RelativeLayout.LayoutParams curBtnParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT);
				curBtnParam.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
				curBtnParam.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				endSetupWizard.setOnClickListener(this);
				buttomLayout.addView(endSetupWizard, curBtnParam);
			}
				break;
			}
			return rootView;
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(arg0 == this.addCurrentWifibtn){
				WifiManager wifiManager = (WifiManager) this.getActivity().getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				String ssid = WifiInfoNoQuote.removeQuoteIfNeeded(wifiInfo.getSSID());
				if(ssid != null && !ssid.equals("<unknown ssid>")){
					int alreadyHave = DAOSingleton.getInstance(this.getActivity()).getDb().getFromSSIDBySSID(ssid).size();
					if(alreadyHave == 0){
						WifiRecord tmp = new WifiRecord(ssid,"0,0,0,0,0,0,0");
						Intent intent = new Intent(this.getActivity(), EditWifiActivity.class);
						Bundle nBundle = new Bundle();
						nBundle.putParcelable(MainActivity.KEY_WIFI_RECORD, tmp);
						nBundle.putString(MainActivity.NEW_TAG, "new");
						intent.putExtras(nBundle);
						startActivity(intent);
					} else {
						Toast.makeText(this.getActivity().getBaseContext(), "The SSID has already existed. Please edit the existing one instead.", Toast.LENGTH_SHORT).show();
					}
				}
			}else if(arg0 == this.addScanWifibtn){
				FragmentManager fm =this.getActivity().getFragmentManager();
				ScannedWifiDialogFragment tmpFragment = new ScannedWifiDialogFragment(ScannedWifiDialogFragment.SCAN_WIFI);
				tmpFragment.show(fm, "tag");
			}else if(arg0 == this.addSavedWifibtn){
				FragmentManager fm =this.getActivity().getFragmentManager();
				ScannedWifiDialogFragment tmpFragment = new ScannedWifiDialogFragment(ScannedWifiDialogFragment.SAVED_WIFI);
				tmpFragment.show(fm, "tag");
			}else if(arg0 == this.addSchedulebtn){
				Time cur = new Time();
				cur.setToNow();
				int curHour = cur.hour;
				int curMin = cur.minute;
				int formattedTime = curHour * ScheduleRecord.HOUR_MASK + curMin;
				ScheduleRecord tmp = new ScheduleRecord("","0,0,0,0,0,0,0",formattedTime,formattedTime);
				Intent intent = new Intent(this.getActivity(), EditScheduleActivity.class);
				Bundle nBundle = new Bundle();
				nBundle.putParcelable(MainActivity.KEY_SCHEDULE_RECORD, tmp);
				nBundle.putString(MainActivity.NEW_TAG, "new");
				intent.putExtras(nBundle);
				startActivity(intent);
			}else if(arg0 == this.endSetupWizard){
				SmartModeGenericService.startActionRemoveNotification(this.getActivity());
				SmartModeGenericService.startCancelScheduleOfService(this.getActivity());
				ModeManagerService.startRepeatModeService(this.getActivity());
				this.getActivity().finish();
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean check) {
			// TODO Auto-generated method stub
			if(arg0 == sensitiveModeSwitch){
				((SetupWizardActivity)this.getActivity()).setSensitiveSwitchState(check);
			}else if(arg0 == notificationSwitch){
				((SetupWizardActivity)this.getActivity()).setNotificationShowState(check);
			}
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if(arg0.getId() == frequencyListID){
				((SetupWizardActivity)this.getActivity()).setFrequencyState((String)arg0.getItemAtPosition(arg2));
			}else if(arg0.getId() == modePriorityListID){
				((SetupWizardActivity)this.getActivity()).setDetectionPriority(Integer.toString(arg2));
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
		
	}

}
