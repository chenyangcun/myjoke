package com.chenyc.myjoke;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchDoubleTapListener;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.internal.app.ActionBarImpl;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.chenyc.base.BigBitmapDisplayer;
import com.chenyc.config.Constants;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

public class ImageViewActivity extends BaseActivity {
	private ProgressBar mProgressBar;
	protected Bitmap mBitmap;
	private String mImageUrl;
	private Date date;

	private boolean isGif = false;

	private WebView gifView;
	private ImageViewTouch imageViewTouch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_view);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("图片浏览");
		// if (getSupportActionBar() instanceof ActionBarImpl) {
		// ActionBarImpl actionBar = (ActionBarImpl) getSupportActionBar();
		// actionBar.setShowHideAnimationEnabled(true);
		// }

		date = new Date();

		mImageUrl = getIntent().getStringExtra("imageUrl");

		imageViewTouch = (ImageViewTouch) findViewById(R.id.imageViewTouch);
		mProgressBar = (ProgressBar) findViewById(R.id.image_progress_bar);

		gifView = (WebView) findViewById(R.id.gif_image);

		gifView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				// view.getSettings().setLoadsImagesAutomatically(true);
				// view.getSettings().setBlockNetworkImage(true);
				if (mProgressBar != null) {
					mProgressBar.setVisibility(View.GONE);
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (mProgressBar != null) {
					mProgressBar.setVisibility(View.VISIBLE);
				}
			}

		});

		imageViewTouch.setDoubleTapEnabled(false);
		imageViewTouch
				.setDoubleTapListener(new OnImageViewTouchDoubleTapListener() {

					@Override
					public void onDoubleTap() {
						showOrHideActionBar();
					}
				});

		Log.i("aa", "url:" + mImageUrl);
		if (mImageUrl != null) {
			loadImage(mImageUrl);
		} else {
			showMessage("加载图片失败！");
		}
	}

	private void showOrHideActionBar() {
		if (getSupportActionBar().isShowing()) {
			getSupportActionBar().hide();
		} else {
			getSupportActionBar().show();
		}
	}

	private void hideActionBar() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				getSupportActionBar().hide();
			}
		}, 300);
	}

	protected void savePicture() {
		File infile = MyJokeApplication.getImageLoader().getDiscCache()
				.get(mImageUrl);
		String postfix = "jpg";
		if (isGif) {
			postfix = "gif";
		}
		String fileName = Environment.getExternalStorageDirectory() + "/"
				+ Constants.APP_NAME + "/" + date.getTime() + "." + postfix;
		FileOutputStream bos;
		FileInputStream bis;
		try {
			bis = new FileInputStream(infile);
			bos = new FileOutputStream(fileName);

			IoUtils.copyStream(bis, bos);
			MediaScannerConnection.scanFile(this, new String[] { fileName },
					null, null);
			bis.close();
			bos.close();
			showMessage("保存成功 " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			showMessage("保存失败！");
		}

	}

	private void loadImage(final String mImageUrl) {
		DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
				.displayer(new BigBitmapDisplayer()).cacheOnDisc().build();

		final ImageView imageView = new ImageView(this);
		imageView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
		imageViewTouch.setFitToWidth(true);
		MyJokeApplication.getImageLoader().displayImage(mImageUrl,
				imageViewTouch, imageOptions, new ImageLoadingListener() {
					@Override
					public void onLoadingCancelled(String arg0, View arg1) {

					}

					@Override
					public void onLoadingComplete(String arg0, View arg1,
							Bitmap bitmap) {
						mProgressBar.setVisibility(View.GONE);
						File infile = MyJokeApplication.getImageLoader()
								.getDiscCache().get(mImageUrl);
						if (!isGif(infile)) {
							hideActionBar();
							gifView.setVisibility(View.GONE);
							imageViewTouch.setVisibility(View.VISIBLE);
						} else {
							Uri uri = Uri.fromFile(infile);
							Log.d("file url", uri.toString());
							isGif = true;
							imageViewTouch.setVisibility(View.GONE);
							loadGif(uri.toString());
						}
					}

					@Override
					public void onLoadingFailed(String arg0, View arg1,
							FailReason arg2) {
						mProgressBar.setVisibility(View.GONE);
						showMessage("加载失败！");

					}

					@Override
					public void onLoadingStarted(String arg0, View arg1) {
						mProgressBar.setVisibility(View.VISIBLE);

					}
				});

	}

	private void loadGif(String url) {
		mProgressBar.setVisibility(View.GONE);
		String HTML_FORMAT = "<html><body style=\"text-align: center; background-color: null; vertical-align: middle;\"><img src = \"%s\" /></body></html>";

		final String html = String.format(HTML_FORMAT, url);

		gifView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		imageViewTouch.setImageBitmap(null);
		System.gc();
	}

	public void writeGifFile(String filename, byte[] data) {
		File downfile = new File(filename);

		try {
			DataOutputStream fileOutStream = new DataOutputStream(
					new FileOutputStream(downfile));
			fileOutStream.write(data, 0, data.length);
			fileOutStream.flush();
			fileOutStream.close();
			MediaScannerConnection.scanFile(this, new String[] { filename },
					null, null);
			showMessage(getString(R.string.save_success) + " " + filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			showMessage(getString(R.string.save_fail));
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			showMessage(getString(R.string.save_fail));
			e.printStackTrace();
		}

	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_item_share_action_provider_action_bar2:
			Intent shareIntent = createShareIntent();
			startActivity(shareIntent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean isGif(File infile) {
		FileInputStream is;
		try {
			is = new FileInputStream(infile);

			if (is != null) {
				String id = "";
				for (int i = 0; i < 6; i++) {
					try {
						id += (char) is.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (id.toUpperCase().startsWith("GIF")) {
					return true;
				}
			}
			try {
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate your menu.
		getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);

		// Set file with share history to the provider and set the share intent.
		MenuItem actionItem = menu
				.findItem(R.id.menu_item_share_action_provider_action_bar);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		// Note that you can set/change the intent any time,
		// say when the user has selected an image.
		actionProvider.setShareIntent(createShareIntent());

		// XXX: For now, ShareActionProviders must be displayed on the action
		// bar
		// Set file with share history to the provider and set the share intent.
		// MenuItem overflowItem =
		// menu.findItem(R.id.menu_item_share_action_provider_overflow);
		// ShareActionProvider overflowProvider =
		// (ShareActionProvider) overflowItem.getActionProvider();
		// overflowProvider.setShareHistoryFileName(
		// ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		// Note that you can set/change the intent any time,
		// say when the user has selected an image.
		// overflowProvider.setShareIntent(createShareIntent());

		return true;
	}

	/**
	 * Creates a sharing {@link Intent}.
	 * 
	 * @return The sharing intent.
	 */
	private Intent createShareIntent() {
		File infile = MyJokeApplication.getImageLoader().getDiscCache()
				.get(mImageUrl);
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/*");
		Uri uri = Uri.fromFile(infile);
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		return shareIntent;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
