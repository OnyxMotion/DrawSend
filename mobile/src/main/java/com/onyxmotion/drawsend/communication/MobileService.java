package com.onyxmotion.drawsend.communication;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;
import com.onyxmotion.drawsend.helper.DebugLog;

/**
 * Responsible for all data side functionality
 * Called using startService() or by Android Wear
 * Generally, passes data over to DataHandler
 * Created by Vivek on 2014-10-27.
 */
public class MobileService extends WearableListenerService {

	private MobileHandler handler;

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new MobileHandler(getApplicationContext());
	}

	@Override
	public void onDestroy() {
		handler.sendEmptyMessage(MobileHandler.ON_DESTROY);
		handler = null;
        super.onDestroy();
	}

	@Override
	public void onPeerConnected(Node peer) {
		DebugLog.LOGD(this, "onPeerConnected: " + peer);
	}

	@Override
	public void onPeerDisconnected(Node peer) {
		DebugLog.LOGD(this, "onPeerDisconnected: " + peer);
	}

	@Override
	public void onMessageReceived(MessageEvent message) {
		handler.sendObjectMessage(MobileHandler.WEAR_RECEIVE_MESSAGE,
			message.getPath());
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
		DebugLog.LOGD(this, "Received an object");
		if (!dataEvents.getStatus().isSuccess()) return;
		handler.sendObjectMessage(MobileHandler.WEAR_RECEIVE_OBJECT,
			FreezableUtils.freezeIterable(dataEvents));
		dataEvents.close();
	}

	/**
	 * Send data from intent to DataHandler to handler on a separate thread
	 * If COMMAND key has no value, DataHandler does nothing
	 * If OBJECT key has no value, put in Context as intent cannot hold context
	 * @param intent    The Intent supplied to startService(Intent), as given.
	 *                      This may be null if the service is being restarted
	 *                      after its process has gone away, and it had
	 *                      previously returned anything except
	 *                      START_STICKY_COMPATIBILITY.
	 * @param flags     Additional data about this start request. Currently
	 *                     either 0, START_FLAG_REDELIVERY, or START_FLAG_RETRY.
	 * @param startId   A unique integer representing this specific request to
	 *                     start. Use with stopSelfResult(int).
	 * @return          START_NOT_STICKY as the service does not selfStop()
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras;
		if (intent != null && (extras = intent.getExtras()) != null)
			handler.sendObjectMessage(
				extras.getInt(MobileHandler.WHAT), extras.get(MobileHandler.OBJ));

		return START_NOT_STICKY;
	}
}
