package com.dolphin.smartmodeelite;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import com.dolphin.dao.SmartModeDAO;
import com.dolphin.dialog.ScannedWifiDialogFragment;
import com.dolphin.service.ModeManagerService;
import com.dolphin.service.SmartModeGenericService;
import com.dolphin.widget.SmartModeWidget;
import com.dolphin.wifiInfo.WifiInfoNoQuote;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v13.app.FragmentPagerAdapter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	public static final String KEY_WIFI_RECORD = "wifi_record";
	public static final String KEY_SCHEDULE_RECORD = "schedule_record";
	/**
	 * Tag for list view.
	 */
	public static final String WIFI_LIST_TAG = "SavedWifiList";
	public static final String SCHEDULE_LIST_TAG = "SavedScheduleList";
	public static final String INDICATOR_IMG_TAG = "IndicatorImgTag";
	public static final String INDICATOR_DESCRIPTION_TAG = "IndicatorDescriptionTag";
	public static final String IMG_BUTTON_SIL_TAG = "ImgButtonSilenceTag";
	public static final String IMG_BUTTON_VIB_TAG = "ImgButtonVibrateTag";
	public static final String NEW_TAG = "NewTag";
	public static final int MILLS_IN_SECOND = 1000;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	AudioManager audio_manager ;
	SmartModeDAO db;
	int saved_index = 0;
	static boolean is24Hour;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Initialize audio manager.
		is24Hour = DateFormat.is24HourFormat(this);
		audio_manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		AudioManagerHolder.getInstance(audio_manager);
		//Initialize data base.
		db = DAOSingleton.getInstance(this).getDb();
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	/**
	 * The method to update list contents every time user change the focus of activity.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		is24Hour = DateFormat.is24HourFormat(this);
		ListView wifiList = (ListView) mViewPager.findViewWithTag(WIFI_LIST_TAG);
		//Check if the view is ready or not.
		//If it has initiated then can proceed to update the content.
		if(wifiList != null){
			ArrayList<WifiRecord> listItems= db.getAllFromSSID();
			ArrayAdapter<WifiRecord> adapter = new ArrayAdapter<WifiRecord>(this,android.R.layout.simple_list_item_1,listItems);
			wifiList.setAdapter(adapter);
		}
		ListView scheduleList = (ListView) mViewPager.findViewWithTag(SCHEDULE_LIST_TAG);
		//Check if schedule list view has already initiated or not.
		//If it has initiated then can proceed to update the content.
		if(scheduleList != null){
			ArrayList<ScheduleRecord> listItemsSchdule= db.getAllFromSchedule();
			ArrayAdapter<ScheduleRecord> adapterSchdule = new ArrayAdapter<ScheduleRecord>(this,android.R.layout.simple_list_item_1,listItemsSchdule);
			scheduleList.setAdapter(adapterSchdule);	
		}
		ImageButton imgButSilence = (ImageButton)mViewPager.findViewWithTag(IMG_BUTTON_SIL_TAG);
		if(imgButSilence != null){
			String imgSilenceState = getPreferenceString(this.getString(R.string.silence_manual_toggle_key),this);
			imgButSilence.setImageResource(imgSilenceState.equals("1")? R.drawable.ic_silence_bar:R.drawable.ic_black_bar);
		}
		
		ImageButton imgButVibrate = (ImageButton)mViewPager.findViewWithTag(IMG_BUTTON_VIB_TAG);
		if(imgButVibrate != null){
			String imgVibrateState = getPreferenceString(this.getString(R.string.vibrate_manual_toggle_key),this);
			imgButVibrate.setImageResource(imgVibrateState.equals("1")?R.drawable.ic_vibrate_bar:R.drawable.ic_black_bar);

		}
		
		//ArrayList<ScheduleRecord> test = db.getScheduleWhichContainTime(50, null);
		//Used for testing preferences setting functionalities.
		//SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(this);
		//Boolean savedIfo = sharePrefere.getBoolean(getString(R.string.sensitive_mode_key), false);
		//Time t = new Time();
		//t.setToNow();
		
		
	}
	
	public void printMessage(Context ctx, String msg){
		Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnClickListener, OnCheckedChangeListener{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		/**Button
		 * 
		 */
		Button silence_btn;
		Button ringtone_btn;
		TextView textView;
		AudioManager audioM;
		SmartModeDAO db;
		Button blankCreateNewWifi;
		Button createConnectedWifi;
		Button createSchedule;
		Button createFromSavedWifi;
		/**
		 * List view for saved wifi ssid.
		 */
		ListView wifiList;
		/**
		 * List view for saved schedule list.
		 */
		ListView scheduleList;
		/**
		 * Switch
		 */
		Switch autoModeSwitch;
		/**
		 * Indicator Image
		 */
		ImageView indicatorImg;
		/**
		 * Indicator description.
		 */
		TextView indicatorDes;
		/**
		 * Text field to indicate the last time completed service.
		 */
		TextView lastTimeCompleteText;
		/**
		 * Default set button on main panel
		 */
		Button  defaultSetBtn;
		/**
		 * setup wizard button
		 */
		Button setupWizardBtn;
		TextUpdater lastTimeCompleteTextUpdater;
		
		Button gotItBtn;
		Button gotItBtnWifiList;
		Button gotItBtnScheduList;
		RelativeLayout overlayInstruct;
		RelativeLayout overlayWifiList;
		RelativeLayout overlayScheduleList;
		/**
		 * Text field to indicate current mode.
		 */
		TextView currentModeText;
		ImageButton imgButSilence;
		ImageButton imgButVibrate;
		Button exportDB;
		Button tweetMe;
		ImageView overlayCover;
		ImageView overlayCoverWifiList;
		ImageView overlayCoverScheduleList;
		
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
			View rootView;
			//Start initialize database and audio manager.
			db = DAOSingleton.getInstance(this.getActivity()).getDb();
			audioM = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);
			switch(getArguments().getInt(ARG_SECTION_NUMBER)){
			case 1:
			{
				rootView = inflater.inflate(R.layout.fragment_mainpanel_grid, container,false);
				currentModeText = (TextView)rootView.findViewById(R.id.currentModeText);
				currentModeText.setText(db.getCurrentMode());
				autoModeSwitch = (Switch)rootView.findViewById(R.id.autoModeSwitch);
				indicatorImg = (ImageView)rootView.findViewById(R.id.imageIndicator);
				indicatorImg.setTag(INDICATOR_IMG_TAG);
				indicatorDes = (TextView)rootView.findViewById(R.id.indicatorDescription);
				indicatorDes.setTag(INDICATOR_DESCRIPTION_TAG);
				lastTimeCompleteText = (TextView)rootView.findViewById(R.id.lastCheckedTime);
				imgButSilence = (ImageButton)rootView.findViewById(R.id.imageButtonSilenceMainPanel);
				imgButSilence.setTag(IMG_BUTTON_SIL_TAG);
				String imgSilenceState = getPreferenceString(this.getActivity().getString(R.string.silence_manual_toggle_key),this.getActivity());
				imgButSilence.setImageResource(imgSilenceState.equals("1")? R.drawable.ic_silence_bar:R.drawable.ic_black_bar);
				imgButSilence.setOnClickListener(this);
				imgButVibrate = (ImageButton)rootView.findViewById(R.id.imageButtonVibrateMainPanel);
				imgButVibrate.setTag(IMG_BUTTON_VIB_TAG);
				String imgVibrateState = getPreferenceString(this.getActivity().getString(R.string.vibrate_manual_toggle_key),this.getActivity());
				imgButVibrate.setImageResource(imgVibrateState.equals("1")?R.drawable.ic_vibrate_bar:R.drawable.ic_black_bar);
				imgButVibrate.setOnClickListener(this);
				defaultSetBtn = (Button)rootView.findViewById(R.id.defaultSetButtonMain);
				defaultSetBtn.setOnClickListener(this);
				setupWizardBtn = (Button)rootView.findViewById(R.id.SetupWizardbtn);
				setupWizardBtn.setOnClickListener(this);
				exportDB = (Button)rootView.findViewById(R.id.ExportDatabtn);
				exportDB.setOnClickListener(this);
				tweetMe = (Button)rootView.findViewById(R.id.tweetMe);
				tweetMe.setOnClickListener(this);
				overlayInstruct = (RelativeLayout)rootView.findViewById(R.id.overlayInstructionMainPanel);
				if(!getPreferenceBool(this.getActivity().getString(R.string.main_first_time_view_key), this.getActivity())){
					gotItBtn = (Button)rootView.findViewById(R.id.mainGotitBtn);
					gotItBtn.setOnClickListener(this);
					overlayCover = (ImageView)rootView.findViewById(R.id.overlay_cover_image);
					overlayCover.setOnClickListener(this);	
				}else {
					overlayInstruct.setVisibility(View.GONE);
				}
				//TextView setupWizard = (TextView)rootView.findViewById(R.id.setupWizardBtext);
				//setupWizard.setText(Integer.toString(db.getFromSSIDBySSID("\"hansamycindy\"").size()));
				ServiceStatus tmpService = db.getServiceStatus();
				String toggle = tmpService.getToggle();
				Time lTime = new Time();
//				try{
					lTime.set(Long.parseLong(tmpService.getLastTime()));
//				} catch(NumberFormatException e){
//					lTime.setToNow();
//				}
				int hour = lTime.hour * ScheduleRecord.HOUR_MASK;
				int minute = lTime.minute;
				String readableTime = ScheduleRecord.parseTimeString(hour+minute, DateFormat.is24HourFormat(this.getActivity()));
				lastTimeCompleteText.setText(lTime.format("%Y/%m/%d ")+readableTime);
				autoModeSwitch.setChecked(toggle.equals("1"));
				autoModeSwitch.setOnCheckedChangeListener(this);//In order to let setCheck not trigger oncheckchange.
				
				checkSystemStatus(tmpService);
				loopUpdateText();
			
			}
				break;
			case 2:
			{
				rootView = inflater.inflate(R.layout.fragment_savedwifi, container,false);
				blankCreateNewWifi = (Button)rootView.findViewById(R.id.addWifiByScan);
				blankCreateNewWifi.setOnClickListener(this);
				createConnectedWifi = (Button)rootView.findViewById(R.id.addCurConnectedWifi);
				createConnectedWifi.setOnClickListener(this);
				createFromSavedWifi = (Button)rootView.findViewById(R.id.addSavedWifi);
				createFromSavedWifi.setOnClickListener(this);
				wifiList = (ListView) rootView.findViewById(R.id.listView);
				wifiList.setTag(WIFI_LIST_TAG);
				ArrayList<WifiRecord> listItems= db.getAllFromSSID();
				ArrayAdapter<WifiRecord> adapter = new ArrayAdapter<WifiRecord>(this.getActivity(),android.R.layout.simple_list_item_1,listItems);
				wifiList.setAdapter(adapter);
				wifiList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						WifiRecord tmp = (WifiRecord) parent.getItemAtPosition(position);
						Intent intent = new Intent(view.getContext(), EditWifiActivity.class);
						Bundle nBundle = new Bundle();
						nBundle.putParcelable(KEY_WIFI_RECORD, tmp);
						intent.putExtras(nBundle);
						startActivity(intent);
					}
				});
				overlayWifiList = (RelativeLayout)rootView.findViewById(R.id.overlayWifiListInstruction);
				if(!getPreferenceBool(this.getActivity().getString(R.string.list_first_time_view_key), this.getActivity())){
					this.gotItBtnWifiList = (Button)rootView.findViewById(R.id.wifiGotitBtn);
					gotItBtnWifiList.setOnClickListener(this);
					overlayCoverWifiList = (ImageView)rootView.findViewById(R.id.overlayWifiListInstrucCover);
					overlayCoverWifiList.setOnClickListener(this);	
				}else{
					overlayWifiList.setVisibility(View.GONE);
				}
			}
				break;
			case 3:
			{
				rootView = inflater.inflate(R.layout.fragment_timeschedule, container,false);
				scheduleList = (ListView) rootView.findViewById(R.id.listViewSchedule);
				scheduleList.setTag(SCHEDULE_LIST_TAG);
				createSchedule = (Button) rootView.findViewById(R.id.newScheduleAdd);
				createSchedule.setOnClickListener(this);
				ArrayList<ScheduleRecord> listItems= db.getAllFromSchedule();
				ArrayAdapter<ScheduleRecord> adapter = new ArrayAdapter<ScheduleRecord>(this.getActivity(),android.R.layout.simple_list_item_1,listItems);
				scheduleList.setAdapter(adapter);
				scheduleList.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						ScheduleRecord tmp = (ScheduleRecord) parent.getItemAtPosition(position);
						Intent intent = new Intent(view.getContext(), EditScheduleActivity.class);
						Bundle nBundle = new Bundle();
						nBundle.putParcelable(KEY_SCHEDULE_RECORD, tmp);
						intent.putExtras(nBundle);
						startActivity(intent);
					}
				});
				overlayScheduleList = (RelativeLayout)rootView.findViewById(R.id.overlayScheduleListInstruction);
				if(!getPreferenceBool(this.getActivity().getString(R.string.list_first_time_view_key), this.getActivity())){
					this.gotItBtnScheduList = (Button)rootView.findViewById(R.id.scheduleGotitBtn);
					gotItBtnScheduList.setOnClickListener(this);
					overlayCoverScheduleList = (ImageView)rootView.findViewById(R.id.overlayScheduleListInstrucCover);
					overlayCoverScheduleList.setOnClickListener(this);	
				}else{
					overlayScheduleList.setVisibility(View.GONE);
				}
			}
				break;
			default:
				rootView = inflater.inflate(R.layout.fragment_mainpanel_grid, container,false);
				break;
			}
//			textView = (TextView) rootView
//					.findViewById(R.id.section_label);
//			textView.setText(Integer.toString(getArguments().getInt(
//					ARG_SECTION_NUMBER)));
//			this.silence_btn = (Button) rootView.findViewById(R.id.addCurConnectedWifi);
//			this.silence_btn.setOnClickListener(this);
//			this.ringtone_btn = (Button) rootView.findViewById(R.id.addWifiBySSID);
//			this.ringtone_btn.setOnClickListener(this);
//			audioM = AudioManagerHolder.getInstance().getManager();
			return rootView;
		}

		@Override
		public void onClick(View v) {
			if(v == this.silence_btn){
				textView.setText("Silence btn clicked");
				audioM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			} else if(v == this.ringtone_btn){
				textView.setText("Ringtone btn clicked");	
				audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

			} else if(v == this.blankCreateNewWifi){
		        FragmentManager fm =this.getActivity().getFragmentManager();
				ScannedWifiDialogFragment tmpFragment = new ScannedWifiDialogFragment(ScannedWifiDialogFragment.SCAN_WIFI);
				tmpFragment.show(fm, "tag");
				//The following activity has been tranferred to dialog fragment class onclick listener.
				/**
				WifiRecord tmp = new WifiRecord("","0,0,0,0,0,0,0");
				Intent intent = new Intent(this.getActivity(), EditWifiActivity.class);
				Bundle nBundle = new Bundle();
				nBundle.putParcelable(KEY_WIFI_RECORD, tmp);
				intent.putExtras(nBundle);
				startActivity(intent);
				**/
			} else if(v == this.createFromSavedWifi){
				FragmentManager fm =this.getActivity().getFragmentManager();
				ScannedWifiDialogFragment tmpFragment = new ScannedWifiDialogFragment(ScannedWifiDialogFragment.SAVED_WIFI);
				tmpFragment.show(fm, "tag");
			}else if(v== this.createConnectedWifi){
				WifiManager wifiManager = (WifiManager) this.getActivity().getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				String ssid = WifiInfoNoQuote.removeQuoteIfNeeded(wifiInfo.getSSID());
				if(ssid != null && !ssid.equals("<unknown ssid>")){
					int alreadyHave = this.db.getFromSSIDBySSID(ssid).size();
					if(alreadyHave == 0){
						WifiRecord tmp = new WifiRecord(ssid,"0,0,0,0,0,0,0");
						Intent intent = new Intent(this.getActivity(), EditWifiActivity.class);
						Bundle nBundle = new Bundle();
						nBundle.putParcelable(KEY_WIFI_RECORD, tmp);
						nBundle.putString(NEW_TAG, "new");
						intent.putExtras(nBundle);
						startActivity(intent);
					} else {
						Toast.makeText(this.getActivity().getBaseContext(), "The SSID has already existed. Please edit the existing one instead.", Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(this.getActivity().getBaseContext(), "There is no currently connected wifi.", Toast.LENGTH_SHORT).show();
				}
				
			} else if( v == this.createSchedule){
				Time cur = new Time();
				cur.setToNow();
				int curHour = cur.hour;
				int curMin = cur.minute;
				int formattedTime = curHour * ScheduleRecord.HOUR_MASK + curMin;
				ScheduleRecord tmp = new ScheduleRecord("","0,0,0,0,0,0,0",formattedTime,formattedTime);
				Intent intent = new Intent(this.getActivity(), EditScheduleActivity.class);
				Bundle nBundle = new Bundle();
				nBundle.putParcelable(KEY_SCHEDULE_RECORD, tmp);
				nBundle.putString(NEW_TAG, "new");
				intent.putExtras(nBundle);
				startActivity(intent);
			} else if( v == this.imgButSilence){
				Context context = this.getActivity();
				String preState = getPreferenceString(context.getString(R.string.silence_manual_toggle_key),context);
				String preStateOther = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key), context);
				AudioManager audioM = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				if (preState.equals("0")) {
					if(preStateOther.equals("1")){
						setPreferenceString("0", context.getString(R.string.vibrate_manual_toggle_key), context);
						imgButVibrate.setImageResource(R.drawable.ic_black_bar);
						updateWidgetImageButtonVibrate();
					}
					boolean newState = setPreferenceString("1", context.getString(R.string.silence_manual_toggle_key), context);
					audioM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				} else {
					boolean newState = setPreferenceString("0", context.getString(R.string.silence_manual_toggle_key), context);
					audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				}
				SmartModeGenericService.startActionUpdateMode(context);
				String newState = getPreferenceString(context.getString(R.string.silence_manual_toggle_key),context);
				imgButSilence.setImageResource(newState.equals("1")? R.drawable.ic_silence_bar:R.drawable.ic_black_bar);
				updateWidgetImageButtonSilence();
				currentModeText.setText(db.getCurrentMode());
			} else if( v == this.imgButVibrate){
				Context context = this.getActivity();
				String preState = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key),context);
		    	String preStateOther = getPreferenceString(context.getString(R.string.silence_manual_toggle_key), context);
				AudioManager audioM = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				if (preState.equals("0")) {
					if(preStateOther.equals("1")){
						setPreferenceString("0", context.getString(R.string.silence_manual_toggle_key), context);
						imgButSilence.setImageResource(R.drawable.ic_black_bar);
						updateWidgetImageButtonSilence();
					}
					boolean newState = setPreferenceString("1", context.getString(R.string.vibrate_manual_toggle_key), context);
					audioM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				} else {
					boolean newState = setPreferenceString("0", context.getString(R.string.vibrate_manual_toggle_key), context);
					audioM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				}
				SmartModeGenericService.startActionUpdateMode(context);
				String newState = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key),context);
				imgButVibrate.setImageResource(newState.equals("1")?R.drawable.ic_vibrate_bar:R.drawable.ic_black_bar);
				updateWidgetImageButtonVibrate();
				currentModeText.setText(db.getCurrentMode());
			} else if( v == this.defaultSetBtn){
				Intent intent = new Intent(this.getActivity(), EditDefaultSet.class);
				startActivity(intent);
			} else if( v == this.setupWizardBtn){
				Intent intent = new Intent(this.getActivity(), SetupWizardActivity.class);
				startActivity(intent);
			} else if( v == this.exportDB){
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"chihan09nc@gmail.com"});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Database Export - SmartMode");
				StringBuffer content = new StringBuffer();
				SmartModeDAO db = DAOSingleton.getInstance(this.getActivity()).getDb();
				ArrayList<WifiRecord> t = db.getAllFromSSID();
				for(int i = 0 ; i < t.size() ; i++){
					WifiRecord tmp = t.get(i);
					content.append(tmp.getSsid());
					content.append(", ");
					content.append(tmp.getMode());
					content.append("\n");
				}
				emailIntent.putExtra(Intent.EXTRA_TEXT, content.toString());
				try{
					startActivity(Intent.createChooser(emailIntent, "Send email"));
				} catch(android.content.ActivityNotFoundException e){
					this.printMessage(this.getActivity(), "ERROR");
				}
			} else if(v == this.gotItBtn || v == this.overlayCover){
				overlayInstruct.setVisibility(View.GONE);
				setPreferenceBool(true, this.getActivity().getString(R.string.main_first_time_view_key), this.getActivity());
			} else if(v == this.tweetMe ){
				 Uri uriUrl = Uri.parse("https://twitter.com/FattyDolphin");
			     Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			     startActivity(launchBrowser);
			} else if(v == this.gotItBtnWifiList || v== this.gotItBtnScheduList || v == this.overlayCoverWifiList || v == this.overlayCoverScheduleList){
				if(overlayWifiList != null)this.overlayWifiList.setVisibility(View.GONE);
				if(overlayScheduleList != null)this.overlayScheduleList.setVisibility(View.GONE);

				setPreferenceBool(true, this.getActivity().getString(R.string.list_first_time_view_key), this.getActivity());
			}
			
		}
		
		public void printMessage(Context ctx, String msg){
			Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if(buttonView == autoModeSwitch){
				if(isChecked){
					SmartModeGenericService.startActionRemoveNotification(this.getActivity());
					SmartModeGenericService.startCancelScheduleOfService(this.getActivity());
					ModeManagerService.startRepeatModeService(this.getActivity());
					db.updateServiceToggle("1");
					updateWidget("1");
				}else {
					db.updateServiceToggle("0");
					updateWidget("0");
					SmartModeGenericService.startActionRemoveNotification(this.getActivity());
					SmartModeGenericService.startCancelScheduleOfService(this.getActivity());
				}
				updateIndicatorFromSwitch(isChecked);
			}
		}
		
		/**
		 * Some magic method to update wdget. from stackoverflow.
		 * @param toggle
		 */
		public void updateWidget(String toggle){
			Context context = this.getActivity();
			AppWidgetManager am = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.smart_mode_widget);
			ComponentName thisWidget = new ComponentName(context, SmartModeWidget.class);
			remoteViews.setTextViewText(R.id.appwidget_text, toggle.equals("1")?"ON":"OFF");
			remoteViews.setImageViewResource(R.id.widgetImageButton,toggle.equals("1")?R.drawable.ic_launcher_3:R.drawable.ic_launcher_black);
			am.updateAppWidget(thisWidget, remoteViews);
		}
		/**
		 * Update vibrate button in widget.
		 * @param vibrateTog
		 */
		public void updateWidgetImageButtonVibrate(){
			Context context = this.getActivity();
			String vibrateTog = getPreferenceString(context.getString(R.string.vibrate_manual_toggle_key),context);
			String toggle = db.getServiceStatus().getToggle();
			AppWidgetManager am = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.smart_mode_widget);
			ComponentName thisWidget = new ComponentName(context, SmartModeWidget.class);
			int imgResource = R.drawable.ic_vibrate_logo;
			int imgResourceBar = R.drawable.ic_vibrate_bar;
			if(vibrateTog.equals("1")){
				imgResourceBar = R.drawable.ic_vibrate_bar;
				if(toggle.equals("1")){
					imgResource = R.drawable.ic_vibrate_logo;
				}else{
					imgResource = R.drawable.ic_vibrate_system_off_logo;
				}
			}else{
				imgResourceBar = R.drawable.ic_black_bar;
				if(toggle.equals("1")){
					imgResource = R.drawable.ic_launcher_3;
				}else{
					imgResource = R.drawable.ic_launcher_black;
				}
			}
			remoteViews.setImageViewResource(R.id.widgetImageVibrateButton, imgResourceBar);
			remoteViews.setImageViewResource(R.id.widgetImageButton,imgResource);
			am.updateAppWidget(thisWidget, remoteViews);
		}
		
		
		/**
		 * Update silence button in the widget.
		 */
		public void updateWidgetImageButtonSilence(){
			Context context = this.getActivity();
			String silenceTog = getPreferenceString(context.getString(R.string.silence_manual_toggle_key),context);
			String toggle = db.getServiceStatus().getToggle();
			AppWidgetManager am = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.smart_mode_widget);
			ComponentName thisWidget = new ComponentName(context, SmartModeWidget.class);
			int imgResource = R.drawable.ic_silience_logo;
			int imgResourceBar = R.drawable.ic_silence_bar;
			if(silenceTog.equals("1")){
				imgResourceBar = R.drawable.ic_silence_bar;
				if(toggle.equals("1")){
					imgResource = R.drawable.ic_silience_logo;
				}else{
					imgResource = R.drawable.ic_silence_system_off_logo;
				}
			}else{
				imgResourceBar = R.drawable.ic_black_bar;
				if(toggle.equals("1")){
					imgResource = R.drawable.ic_launcher_3;
				}else{
					imgResource = R.drawable.ic_launcher_black;
				}
			}
			remoteViews.setImageViewResource(R.id.widgetImageSilenceButton, imgResourceBar);
			remoteViews.setImageViewResource(R.id.widgetImageButton,imgResource);
			am.updateAppWidget(thisWidget, remoteViews);
		}
		
		/**
		 * Updater for switch indicators while user pressing it.
		 * @param t
		 */
		public void updateIndicatorFromSwitch(boolean t){
			if(!t){
				//User turns off
				indicatorImg.setImageResource(R.drawable.black_logo_1_1_1);
				indicatorDes.setText(R.string.stop_by_user);
			}else{
				//Normal
				indicatorImg.setImageResource(R.drawable.yellow_logo_1_1_1);
				indicatorDes.setText(R.string.restarting);
			}
		}
		
		public void loopUpdateText(){
			lastTimeCompleteTextUpdater = new TextUpdater(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					ServiceStatus tmpService = db.getServiceStatus();
					Time lTime = new Time();
					lTime.set(Long.parseLong(tmpService.getLastTime()));
					int hour = lTime.hour * ScheduleRecord.HOUR_MASK;
					int minute = lTime.minute;
					String readableTime = ScheduleRecord.parseTimeString(hour+minute, is24Hour);
					lastTimeCompleteText.setText(lTime.format("%Y/%m/%d ")+readableTime);
					currentModeText.setText(db.getCurrentMode());
					ServiceStatus t = db.getServiceStatus();
					checkSystemStatus(t);
				}
				
			},MILLS_IN_SECOND);
//			lastTimeCompleteTextUpdater.startUpdates();
//			This starter trigger has been moved to onResume() so it will be able to handle user's action while hovering around
			//activity but thread can still start properly.
		}
		
		/**
		 * Helper method for runnable.
		 * @return
		 *
		public boolean is24Hour(){
			return DateFormat.is24HourFormat(this.getActivity());
		}
		*/
		boolean light_grn = true;
		/**
		 * Method to check if the service is running properly or error.
		 */
		public void checkSystemStatus(ServiceStatus tmpService){
			String toggle = tmpService.getToggle();
			String status = tmpService.getStatus();
			Time lTime = new Time();
			lTime.set(Long.parseLong(tmpService.getLastTime()));
			Time cur = new Time();
			cur.setToNow();
			long past = lTime.toMillis(false);
			long current = cur.toMillis(false);
			String prefSchedule = tmpService.getBeenScheduled();
			int prefScheduleINT = Integer.parseInt(prefSchedule);
			if(status.equals("0") && (past + (prefScheduleINT + 1) * DateUtils.MINUTE_IN_MILLIS) < current && toggle.equals("1")){
				//Error
				indicatorImg.setImageResource(R.drawable.red_logo_1_1_1);
				indicatorDes.setText(R.string.error);
				//printMessage(this.getActivity(), "Something wrong with service.");
			}else if(toggle.equals("0")){
				//User turns off
				indicatorImg.setImageResource(R.drawable.black_logo_1_1_1);
				indicatorDes.setText(R.string.stop_by_user);
			}else{
				//Normal
				indicatorImg.setImageResource(light_grn ? R.drawable.green_logo_1_1_1 : R.drawable.dark_green_logo_1_1_1);
				indicatorDes.setText(R.string.normal);
				light_grn = !light_grn;
			}
			
			//test
			//SharedPreferences sharePreference = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			//lastTimeCompleteText.setText(sharePreference.getString(this.getString(R.string.system_status_test_key), "loading."));

		}

		@Override
		public void onPause() {
			// TODO Auto-generated method stub
			if(getArguments().getInt(ARG_SECTION_NUMBER) == 1){
				lastTimeCompleteTextUpdater.stopUpdates();
			}
			super.onStop();
		}

		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			if(getArguments().getInt(ARG_SECTION_NUMBER) == 1){
				lastTimeCompleteTextUpdater.startUpdates();
				ServiceStatus tmpService = db.getServiceStatus();
				String toggle = tmpService.getToggle();
				autoModeSwitch.setChecked(toggle.equals("1"));
			}			
		}


	}
	
	/**
	 * The method to retrieve a saved instance of manual vibrate and silence from key.
	 * From sharedpreference.
	 * @param key A string key that pairs with the desired value.
	 * @param context A context object.
	 * @return Return string from shared preference.
	 */
	private static String getPreferenceString(String key, Context context){
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
	private static boolean setPreferenceString(String val, String key, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putString(key, val).commit();
		return rst;
	}
	

	/**
	 * The method to get boolean value for shareprerference for first time useage record.
	 * @param key
	 * @param context
	 * @return
	 */
	private static boolean getPreferenceBool(String key, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean savedIfo = sharePrefere.getBoolean(key, false);
		return savedIfo;
	}
	/**
	 * Method to set boolean value
	 * @param val
	 * @param key
	 * @param context
	 * @return
	 */
	private static boolean setPreferenceBool (boolean val, String key, Context context){
		SharedPreferences sharePrefere = PreferenceManager.getDefaultSharedPreferences(context);
		boolean rst = sharePrefere.edit().putBoolean(key, val).commit();
		return rst;
	}
	//Export database function.
	private class ExportDatabase extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			File dbFile= getDatabasePath("SmartModeDB");
			return null;
		}
		
	}

}
