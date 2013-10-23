package com.chenyc.myjoke.util;

import com.chenyc.myjoke.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Helper {

	private Context mContext;
	private SharedPreferences mPrefs;

	public Helper(Context context) {
		this.mContext = context;
		this.mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public boolean isOnlyShowImageInWifi() {
		return mPrefs.getBoolean("auto_load_img_in_wifi_preference", true);
	}

	public boolean isWifiMode() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
							&& info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean shouldLoadImage() {
		boolean result = false;
		if (isWifiMode()) {
			result = true;
		}

		if (!isOnlyShowImageInWifi()) {
			result = true;
		}

		return result;
	}
	
	public void showMessage(String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}
	
	
	@SuppressLint({ "NewApi", "ServiceCast" })
	public void copyText(String text) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext
					.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
			Toast.makeText(mContext, mContext.getString(R.string.copy_succeed), 1000)
					.show();
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext
					.getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData
					.newPlainText("text label", text);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(mContext, mContext.getString(R.string.copy_succeed), 1000)
					.show();
		}
	}


}
