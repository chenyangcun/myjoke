package com.chenyc.base;

import java.io.IOException;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.ImageView;

import com.chenyc.myjoke.util.AppUtil;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;

@TargetApi(11)
public final class BigBitmapDisplayer implements BitmapDisplayer {
	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				if (imageView.isHardwareAccelerated()) {
					bitmap = AppUtil.zoomPhoto(bitmap, 2048);
				}
			}
			imageView.setImageBitmap(bitmap);
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}