package com.chenyc.myjoke;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.MenuItem;
import com.chenyc.config.Constants;
import com.chenyc.myjoke.fragment.JokeListFragment;
import com.chenyc.myjoke.fragment.MenuFragment;
import com.chenyc.myjoke.util.Helper;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.bean.SnsAccount;
import com.umeng.socialize.bean.SocializeUser;
import com.umeng.socialize.common.SocializeConstants;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.FetchUserListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class MainActivity extends SlidingFragmentActivity implements
		OnNavigationListener {

	private String[] mChannelIds;
	private Fragment mContent;
	private int mSelectedPosition;

	private String[] tags = new String[] { Constants.JOKES_LIST,
			Constants.MY_FAV_JOKES, Constants.ME, Constants.SETTING,
			Constants.ADVICE, Constants.MARKET };
	private long mLastBackTime;
	private Helper mHelper;
	private FeedbackAgent mfeedBackAgent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHelper = new Helper(this);

		// check if the content frame contains the menu frame
		if (findViewById(R.id.menu_frame) == null) {
			setBehindContentView(R.layout.menu_frame);
			getSlidingMenu().setSlidingEnabled(true);
			getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			// show home as up so we can toggle
		} else {
			// add a dummy view
			View v = new View(this);
			setBehindContentView(v);
			getSlidingMenu().setSlidingEnabled(false);
			getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}

		// set the Above View Fragment
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null) {
			mContent = createFragment(0, tags[0]);
		}
		// set the Behind View Fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new MenuFragment()).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(
				getResources().getDrawable(R.drawable.ic_action_menu));

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.channel_names, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
		mChannelIds = getResources().getStringArray(R.array.channel_ids);
		UmengUpdateAgent.update(this);
		mfeedBackAgent = new FeedbackAgent(this);
		mfeedBackAgent.sync();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			break;
		case R.id.menu_item_rateapp_action_provider_action_bar:
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri
						.parse("market://details?id=com.chenyc.myjoke"));
				startActivity(intent);
			} catch (Exception e) {
			}
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.topic_list_action_provider,
		// menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(int position) {
		if (mSelectedPosition != position) {
			String tag = tags[position];
			if (Constants.SETTING.equals(tag)) {
				Intent intent = new Intent(this, SettingActivity.class);
				startActivity(intent);
				return;
			} else if (Constants.MY_FAV_JOKES.equals(tag)) {
				Intent intent = new Intent(this, MyFavJokesActivity.class);
				startActivity(intent);
				return;
			} else if (Constants.ADVICE.equals(tag)) {
				mfeedBackAgent.startFeedbackActivity();
				return;
			} else if (Constants.MARKET.equals(tag)) {
				try {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri
							.parse("market://details?id=com.chenyc.myjoke"));
					startActivity(intent);
				} catch (Exception e) {
				}
				return;

			} else if (Constants.ME.equals(tag)) {
				UMSocialService umService = UMServiceFactory
						.getUMSocialService("user", RequestType.SOCIAL);
				int flag = SocializeConstants.FLAG_USER_CENTER_LOGIN_VERIFY
						| SocializeConstants.FLAG_USER_CENTER_HIDE_LOGININFO;
				umService.openUserCenter(this, flag);
				return;
			}

			mContent = getSupportFragmentManager().findFragmentByTag(tag);
			if (mContent == null) {
				mContent = createFragment(position, tag);
			}

			if (mContent instanceof JokeListFragment) {
				getSupportActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_LIST);
				getSupportActionBar().setDisplayShowTitleEnabled(false);
			} else {
				getSupportActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_STANDARD);
				getSupportActionBar().setDisplayShowTitleEnabled(true);
			}

			showContent(mContent, position);

			// getSupportFragmentManager().beginTransaction()
			// .add(R.id.content_frame, mContent).commit();
		}
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			public void run() {
				getSlidingMenu().showContent();
			}
		}, 50);
		mSelectedPosition = position;
	}

	private void showContent(Fragment content, int position) {
		for (int i = 0; i < tags.length; i++) {
			if (i == position) {
				continue;
			}
			String tag = tags[i];
			Fragment fragemnt = getSupportFragmentManager().findFragmentByTag(
					tag);
			if (fragemnt != null) {
				getSupportFragmentManager().beginTransaction().hide(fragemnt)
						.commit();
			}
		}
		getSupportFragmentManager().beginTransaction().show(content).commit();
	}

	private Fragment createFragment(int position, String tag) {
		Fragment fragemnt = null;
		switch (position) {
		case 0:
			fragemnt = new JokeListFragment();
			break;
		default:
			break;
		}
		getSupportFragmentManager().beginTransaction()
				.add(R.id.content_frame, fragemnt, tag).commit();
		return fragemnt;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String id = mChannelIds[itemPosition];
		JokeListFragment fragemnt = (JokeListFragment) getSupportFragmentManager()
				.findFragmentByTag(Constants.JOKES_LIST);
		fragemnt.loadData(id);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (getSlidingMenu().isMenuShowing()) {
				return super.onKeyDown(keyCode, event);
			}

			long nowTime = new Date().getTime();
			if (nowTime - mLastBackTime > 2000) {
				mHelper.showMessage("再按一次退出" + getString(R.string.app_name));
				mLastBackTime = nowTime;
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
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
