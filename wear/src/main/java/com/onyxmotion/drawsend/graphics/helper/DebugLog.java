package com.onyxmotion.drawsend.graphics.helper;

import android.util.Log;

/**
 * Created by Vivek on 2015-02-13.
 */
public class DebugLog {

	public static void LOGD(Object obj, String msg) {
		Log.d(obj.getClass().getSimpleName(), msg);
	}
}
