package com.onyxmotion.drawsend.communication;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.wearable.DataEvent;
import com.onyxmotion.drawsend.helper.DebugLog;

import java.io.Serializable;
import java.util.List;

/**
 * Instantiated by the WearCommunicationService to handle onStartCommand() calls
 * Created by Vivek on 2014-12-31.
 */
public class MobileHandler extends Handler {

	private static final String DATA_THREAD = "DataThread";

	public final static int
		NONE = 0,
		ON_CREATE = 1,
		ON_DESTROY = 2,

		WEAR_SEND_PATH = 3,
		WEAR_RECEIVE_MESSAGE = 8,
		WEAR_RECEIVE_OBJECT = 9,

		GCM_INITIALIZE_ID = 10;

	public final static String ACTION = MobileHandler.class.getName(),
		WHAT = ACTION + ".what", STATUS = ACTION + ".status",
		OBJ = ACTION + ".obj", SUCCESS = ACTION + ".success",
		FAILURE = ACTION + ".failure";


	private static HandlerThread getThread() {
		HandlerThread thread = new HandlerThread(DATA_THREAD);
		thread.start();
		return thread;
	}

	private WearCommunicator wearCommunicator;
	private Context context;
	private Intent intent;
	private LocalBroadcastManager local;


	public MobileHandler(@NonNull Context ctx) {
		super(getThread().getLooper());
		if (ctx != ctx.getApplicationContext())
			throw new IllegalArgumentException("ctx not application context");

		wearCommunicator = new WearCommunicator();
		context = ctx;
		sendEmptyMessage(ON_CREATE);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void handleMessage(Message msg) {
		int what = msg.what;
		Object obj = msg.obj;
		DebugLog.LOGD(this, "Handling " + what);
		switch (what) {

			case GCM_INITIALIZE_ID:
/*				if (obj instanceof Integer) {
					userId = (int) obj;
					String[] result = wearCommunicator.sendObject(
						WearCommunicator.PATH_USER, userId);
					if (result.length != 2)
						throw new IllegalStateException("Improper result");
					sendResult(what, !result[0].equals(SUCCESS) ? result
						: GcmRegisterIdHelper.registerId(context, userId));
				} else throw new IllegalArgumentException("obj not int");
*/				return;

			// TODO: Figure out what UI needs from intent for wear communication
			case WEAR_RECEIVE_OBJECT:
				if (obj instanceof List)
					sendResult(what, wearCommunicator.receiveObject(
						context, (List<DataEvent>) obj));
				else throw new IllegalArgumentException("obj not List");
				return;

			case WEAR_RECEIVE_MESSAGE:
				if (obj instanceof String)
					sendResult(what,
						wearCommunicator.receiveMessage((String) obj));
				else throw new IllegalArgumentException("obj not String");
				return;

			case WEAR_SEND_PATH:
				if (obj instanceof String)
					sendResult(what,
						wearCommunicator.sendEmptyMessage((String) obj));
				else throw new IllegalArgumentException("obj not String");
				return;

			case ON_DESTROY:
				wearCommunicator.disconnect();
				wearCommunicator.removeContext();
				wearCommunicator = null;
				local = null;
				intent = null;
				context = null;
				getLooper().quitSafely();
				sendResult(what, true);
				return;

			case ON_CREATE:
				wearCommunicator.setContext(context);
				sendResult(what, wearCommunicator.connect());
				return;

			case NONE: default:
				super.handleMessage(msg);
		}
	}

	public void sendObjectMessage(int what, Object object) {
		sendMessage(Message.obtain(this, what, object));
	}

	private void wearSendObjectTime(int what, @NonNull String base,
	    @NonNull Object obj) {
		if (obj instanceof Serializable)
			sendResult(what, wearCommunicator.sendObject(
				base + System.currentTimeMillis(),(Serializable) obj));
		else throw new IllegalArgumentException(base + " obj not Serializable");
	}

	private void sendResult(int what, boolean status) {
		sendResult(what, new String[]{status ? SUCCESS : FAILURE, ""});
	}

	private void sendResult(int what, @NonNull String[] result) {
		if (local == null)  local  = LocalBroadcastManager.getInstance(context);
		if (intent == null) intent = new Intent(MobileHandler.ACTION);
		if (result.length == 2)
			local.sendBroadcast(intent.putExtra(WHAT, what)
				.putExtra(STATUS, result[0].equals(SUCCESS))
				.putExtra(OBJ, result[1]));
		else throw new IllegalArgumentException("result not String[2]");
	}

	private void sendResult(int what, @NonNull byte[] result) {
		if (local == null)  local  = LocalBroadcastManager.getInstance(context);
		if (intent == null) intent = new Intent(MobileHandler.ACTION);
		local.sendBroadcast(intent.putExtra(WHAT, what)
			.putExtra(STATUS, true)
			.putExtra(OBJ, result));
	}
}
