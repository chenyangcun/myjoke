package com.chenyc.myjoke;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SplashScreen extends Activity {
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splashscreen);

		// Display the current version number
		PackageManager pm = getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
			TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
			versionNumber.setText("Version " + pi.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new Handler().postDelayed(new Runnable() {
			public void run() {
				Intent mainIntent = null;
				mainIntent = new Intent(SplashScreen.this, MainActivity.class);
				SplashScreen.this.startActivity(mainIntent);
				SplashScreen.this.finish();
			}
		}, 1500); // 2900 for release

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
