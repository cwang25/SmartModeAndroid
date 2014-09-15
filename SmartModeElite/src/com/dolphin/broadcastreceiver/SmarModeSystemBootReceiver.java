package com.dolphin.broadcastreceiver;

import com.dolphin.service.ModeManagerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmarModeSystemBootReceiver extends BroadcastReceiver {
	public SmarModeSystemBootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		ModeManagerService.startRepeatModeService(context);
	}
}
