package com.dolphin.smartmodeelite;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.util.List;

import com.dolphin.service.ModeManagerService;
import com.dolphin.service.SmartModeGenericService;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Display the fragment as the main content.
	        getFragmentManager().beginTransaction()
	                .replace(android.R.id.content, new SettingsFragment())
	                .commit();
	    }
	    
	    public static class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener{
	        @Override
	        public void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);
	           // ListPreference t ;
	            
	            // Load the preferences from an XML resource
	            addPreferencesFromResource(R.xml.setting_list);
	            PreferenceManager pm = this.getPreferenceManager();
	            pm.findPreference(this.getString(R.string.frequency_saved_key)).setOnPreferenceChangeListener(this);
	            pm.findPreference(this.getString(R.string.sensitive_mode_key)).setOnPreferenceChangeListener(this);
	            pm.findPreference(this.getString(R.string.mode_priority_key)).setOnPreferenceChangeListener(this);
	            pm.findPreference(this.getString(R.string.notification_toggle_key)).setOnPreferenceChangeListener(this);
	        }

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				// TODO Auto-generated method stub
				SmartModeGenericService.startCancelScheduleOfService(this.getActivity());
				ModeManagerService.startRepeatModeService(this.getActivity());
				if(preference.getKey().equals(this.getString(R.string.notification_toggle_key))){
					if(newValue instanceof Boolean){
						boolean newSet = (Boolean)newValue;
						if(!newSet){
							SmartModeGenericService.startActionRemoveNotification(this.getActivity());
						}
					}
				}
				return true;
			}
	    }
}
