package com.chenyc.myjoke;

import java.lang.reflect.Field;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;

public class MyJokeApplication extends org.holoeverywhere.app.Application {
	private static ImageLoader mImageLoader;
	private static Context mContext;
	private static SlidingMenu mSlidingMenu;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this.getApplicationContext();
		mImageLoader = ImageLoader.getInstance();
		mSlidingMenu = new SlidingMenu(mContext);

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(2 * 1024 * 1024)
				.denyCacheImageMultipleSizesInMemory().discCacheFileCount(500)
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();

		// Initialize ImageLoader with created configuration. Do it once.
		mImageLoader.init(config);
		MobclickAgent.setSessionContinueMillis(300000);

	}

	public static ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public static Context getAppContext() {
		return mContext;
	}
}
