package com.onyxmotion.drawsend;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
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
		BitmapFactory.Options options = new BitmapFactory.Options();

		Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
		if (bitmap != null)
			((ImageView) findViewById(R.id.test_image)).setImageBitmap(bitmap);
	}
}
