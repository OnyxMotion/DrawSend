package com.onyxmotion.drawsend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.onyxmotion.drawsend.communication.MobileHandler;

/**
 * Receives messages indicating status or changes of backend
 * Created by Vivek on 2015-01-15.
 */
public class UXReceiver extends BroadcastReceiver {

	private Activity activity;

	public UXReceiver(Activity a) {
		activity = a;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		int what = extras.getInt(MobileHandler.WHAT);
		if (what == MobileHandler.WEAR_RECEIVE_OBJECT) {
			byte[] image = extras.getByteArray(MobileHandler.OBJ);

			((MainActivity) activity).setImage(image);
		}
	}

}