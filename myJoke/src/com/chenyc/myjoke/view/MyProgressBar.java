package com.chenyc.myjoke.view;

import org.holoeverywhere.widget.ProgressBar;

import com.chenyc.myjoke.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class MyProgressBar {

	private Activity mContext;
	private LayoutInflater mInflater;
	private ProgressBar mProgressBar;

	public MyProgressBar(Activity context, int id) {
		this(context, null);
	}

	public MyProgressBar(Activity context) {
		this(context, null);
	}

	public MyProgressBar(Activity context, ViewGroup rootView) {
		this.mContext = context;

		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (rootView == null) {
			rootView = (ViewGroup) this.mContext.getWindow().getDecorView();
		}
		FrameLayout frameLayout = (FrameLayout) mInflater.inflate(
				R.layout.progress_bar, null);
		mProgressBar = (ProgressBar) frameLayout
				.findViewById(R.id.progress_bar);
		rootView.addView(frameLayout);
	}

	public void show() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	public void hide() {
		mProgressBar.setVisibility(View.GONE);
	}

	public void dismiss() {
		mProgressBar.setVisibility(View.GONE);
	}
}
