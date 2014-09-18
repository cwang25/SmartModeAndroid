package com.dolphin.dialog;

import java.util.Calendar;

import com.dolphin.smartmodeelite.EditScheduleActivity;
import com.dolphin.smartmodeelite.ScheduleRecord;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

public class TimePickerFragment extends DialogFragment
                            implements TimePickerDialog.OnTimeSetListener {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
    	Bundle args = this.getArguments();
    	int time = args.getInt(EditScheduleActivity.TIME_BOARD_KEY);
    	int h = time/ScheduleRecord.HOUR_MASK;
    	int m = time%ScheduleRecord.HOUR_MASK;
        int hour = h;
        int minute = m;
        

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
    	Activity a = this.getActivity();
    	if(a instanceof EditScheduleActivity){
    		((EditScheduleActivity)a).updateTimeBoard(hourOfDay, minute,this);
    	}
    	
    	
    }
}