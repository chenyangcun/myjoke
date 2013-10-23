package com.chenyc.myjoke.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AppUtil {
	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return new BitmapDrawable(null, newbmp);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

	public static Bitmap zoomPhotoAndCut(Bitmap bm, int height)
			throws IOException {
		// 照片缩放
		Matrix matrix = new Matrix();
		int max = 0;
		int diff = 0;
		if (bm.getHeight() > bm.getWidth()) {
			max = bm.getHeight();
			diff = (max - bm.getWidth()) / 2;
		} else {
			max = bm.getWidth();
			diff = (max - bm.getHeight()) / 2;
		}

		float scale = 0;

		scale = height * 1.0f / max;
		matrix.postScale(scale, scale);
		Bitmap tmBitmap = null;
		if (bm.getHeight() > bm.getWidth()) {
			tmBitmap = Bitmap.createBitmap(bm, 0, diff, bm.getWidth(),
					bm.getHeight() - (2 * diff), matrix, true);
		} else {
			tmBitmap = Bitmap.createBitmap(bm, diff, 0, bm.getWidth()
					- (2 * diff), bm.getHeight(), matrix, true);
		}
		return tmBitmap;
	}

	public static Bitmap zoomPhoto(Bitmap bm, int height) throws IOException {
		// 照片缩放
		Matrix matrix = new Matrix();
		int max = 0;
		if (bm.getHeight() > bm.getWidth()) {
			max = bm.getHeight();
		} else {
			max = bm.getWidth();
		}
		float scale = 0;
		scale = height * 1.0f / max;
		matrix.postScale(scale, scale);
		Bitmap tmBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
				bm.getHeight(), matrix, true);
		return tmBitmap;
	}

	// 缩放照片
	public static Bitmap zoomPhoto(InputStream is, int height)
			throws IOException {
		// 读取照片
		Bitmap bm = BitmapFactory.decodeStream(is);
		return zoomPhotoAndCut(bm, height);
	}

	// 压缩照片
	public static byte[] compressPhoto(Bitmap tmBitmap) throws IOException {
		// 转JPEG
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tmBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		byte[] photoData = out.toByteArray();
		out.close();
		return photoData;

	}

	public static Date getUTCTime() {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat(
				"yyyy-MMM-dd HH:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		// Local time zone
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat(
				"yyyy-MMM-dd HH:mm:ss");
		// Time in GMT
		try {
			return dateFormatLocal.parse(dateFormatGmt.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
