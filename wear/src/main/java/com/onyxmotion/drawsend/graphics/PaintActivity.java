/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onyxmotion.drawsend.graphics;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;

import com.onyxmotion.drawsend.R;
import com.onyxmotion.drawsend.graphics.communication.DataHandler;
import com.onyxmotion.drawsend.graphics.communication.WearService;

import java.io.ByteArrayOutputStream;

public class PaintActivity extends Activity
    implements ColorPickerDialog.OnColorChangedListener,
	View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

		paintView = (PaintView) findViewById(R.id.paint_view);
	    findViewById(R.id.colorpicker).setOnClickListener(this);
	    findViewById(R.id.send).setOnClickListener(this);
	    findViewById(R.id.undo).setOnClickListener(this);
	    findViewById(R.id.home).setOnClickListener(this);
    }

	private PaintView paintView;

    public void colorChanged(int color) {
        paintView.setColor(color);
    }

	public void onColorPickerClick() {
		new ColorPickerDialog(this, this, paintView.getColor()).show();
	}

	public void sendImage() {
		Bitmap bitmap = paintView.getBitmap();
		//	    setContentView(R.layout.view_image);
		//	    ((ImageView) findViewById(R.id.test_image)).setImageBitmap(bitmap);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		startService(new Intent(this, WearService.class)
			.putExtra(DataHandler.WHAT, DataHandler.WEAR_SEND_IMAGE)
			.putExtra(DataHandler.OBJ, byteArray));
	}

	public void undo() {
		paintView.clear();
	}

	public void changePage() {

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.colorpicker: onColorPickerClick(); break;
			case R.id.send: sendImage(); break;
			case R.id.undo: undo(); break;
			case R.id.home: changePage(); break;
		}
	}
}
