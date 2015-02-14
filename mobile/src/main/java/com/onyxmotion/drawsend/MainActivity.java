package com.onyxmotion.drawsend;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

import com.onyxmotion.drawsend.communication.MobileHandler;
import com.onyxmotion.drawsend.communication.MobileService;
import com.onyxmotion.drawsend.helper.DebugLog;


public class MainActivity extends ActionBarActivity {


	private UXReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		receiver = new UXReceiver(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
			new IntentFilter(MobileHandler.ACTION));
		DebugLog.LOGD(this, "I'm onCreate");

	}


	@Override
	protected void onDestroy() {
		stopService(new Intent(this, MobileService.class));
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		receiver = null;
		super.onDestroy();
	}

	public void setImage(byte[] image) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
		if (bitmap != null)
			((ImageView) findViewById(R.id.test_image)).setImageBitmap(bitmap);
	}
}
