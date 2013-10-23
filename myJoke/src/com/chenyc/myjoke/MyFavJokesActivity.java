package com.chenyc.myjoke;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.chenyc.myjoke.fragment.JokeListFragment;
import com.umeng.analytics.MobclickAgent;

public class MyFavJokesActivity extends SherlockFragmentActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("我的收藏");
		getIntent().putExtra("isMyFav", true);
		JokeListFragment fragment = new JokeListFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
	};

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
