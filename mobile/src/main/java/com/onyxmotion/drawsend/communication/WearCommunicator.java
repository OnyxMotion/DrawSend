package com.onyxmotion.drawsend.communication;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.onyxmotion.drawsend.helper.DebugLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages communication to the wear device
 * Assumes functions are called off of the UI thread, so blocking calls occur
 * Created by Vivek on 2015-01-05.
 */
public class WearCommunicator {

	private final static String EMPTY = new String(new byte[0]);

	public final static String
		PATH_BASE = "/com/onyxmotion/swishpro/drawsend",
		PATH_IMAGE_WEAR_TO_MOBILE = PATH_BASE + "/image/weartomobile/",
		PATH_IMAGE_MOBILE_TO_WEAR = PATH_BASE + "/image/mobiletowear/";
	private GoogleApiClient client;

	public WearCommunicator() {

	}

	public void setContext(@NonNull Context context) {
		client = new GoogleApiClient.Builder(context)
			.addApi(Wearable.API)
			.build();
	}

	public boolean connect() {
		return client.blockingConnect(1000L, TimeUnit.MILLISECONDS).isSuccess();
	}

	public void disconnect() {
		client.disconnect();
	}

	public void removeContext() {
		client = null;
	}

	public String[] sendObject(@NonNull String path, @NonNull Serializable obj){
		DebugLog.LOGD(this, "Obj sent it " + obj.toString());
		String[] result = {MobileHandler.FAILURE, path};
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(baos);
			out.writeObject(obj);

			PutDataRequest putRequest = PutDataRequest.create(path);
			putRequest.setData(baos.toByteArray());

			out.close();
			baos.close();
			if (isClientAvailable() && Wearable.DataApi.putDataItem(
				client, putRequest).await().getStatus().isSuccess())
				result[0] = MobileHandler.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String[] sendMessage(@NonNull String path, @NonNull String message) {
		String[] result = {MobileHandler.FAILURE, path};
		if (isClientAvailable()) {
			List<Node> nodes
				= Wearable.NodeApi.getConnectedNodes(client).await().getNodes();
			if (!nodes.isEmpty() && Wearable.MessageApi.sendMessage(
				client, nodes.get(0).getId(), path, message.getBytes())
				.await().getStatus().isSuccess())
				result[0] = MobileHandler.SUCCESS;
		}
		return result;
	}

	public String[] sendEmptyMessage(@NonNull String path) {
		return sendMessage(path, EMPTY);
	}

	public byte[] receiveObject(@NonNull Context context,
	    @NonNull List<DataEvent> events) {
		DebugLog.LOGD(this, "I'm receiving an object");
		Object object;
		String path = null;
		DataItem data;
		boolean isSuccess = true;

		for (DataEvent event : events) {
			if (event.getType() == DataEvent.TYPE_DELETED) continue;
			data = event.getDataItem();
			object = getObject(data.getData());
			if (object == null) continue;
			path = data.getUri().getPath();

			if (path.contains(PATH_IMAGE_WEAR_TO_MOBILE)) {
				if (object instanceof byte[])
					return (byte[]) object;
			}

			if (isClientAvailable())
				isSuccess &= Wearable.DataApi.deleteDataItems(
					client, data.getUri()).await().getStatus().isSuccess();
			else isSuccess = false;
		}
		return new byte[0];
//		return new String[] {isSuccess ? MobileHandler.SUCCESS
//			: MobileHandler.FAILURE, path != null ? path : EMPTY};
	}

	public String[] receiveMessage(@NonNull String path) {
		String[] result = {MobileHandler.FAILURE, path};
		if (System.currentTimeMillis() > 0) result[0] = MobileHandler.SUCCESS;
		return result;
	}

	private Object getObject(@NonNull byte[] bytes) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInput in = new ObjectInputStream(bais);
			Object object = in.readObject();
			in.close();
			bais.close();
			return object;
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isClientAvailable() {
		boolean status = client != null && client.isConnected();
		if (!status) DebugLog.LOGD(this, "GoogleApiClient not available");
		return status;
	}
}
