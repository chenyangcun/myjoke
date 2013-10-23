package com.chenyc.myjoke;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.flip.FlipViewController.ViewFlipListener;
import com.chenyc.myjoke.adapter.TopicAdapter;
import com.chenyc.myjoke.bean.Topic;
import com.chenyc.myjoke.util.ChannelRestClient;
import com.chenyc.myjoke.util.DatabaseHelper;
import com.chenyc.myjoke.util.Helper;
import com.chenyc.myjoke.util.ParamsUtil;
import com.chenyc.myjoke.view.CustomButton;
import com.chenyc.myjoke.view.MyProgressBar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.UMShareMsg;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMSsoHandler;
import com.umeng.socialize.controller.UMWXHandler;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMRichMedia;
import com.umeng.socialize.media.UMediaObject.MediaType;
import com.umeng.socialize.sso.SinaSsoHandler;

public class JokeDetailActivity extends BaseActivity implements OnClickListener {

	public static List<Topic> DETAIL_TOPIC_DATA = new ArrayList<Topic>();

	private MyProgressBar mProgressBar;
	private FlipViewController flipView;
	private TopicAdapter topicAdapter;
	protected int pageNum = 1;

	protected boolean mDataChanged;

	private String mChannel;

	private Helper mHelper;

	private Topic mCurrentTopic;

	private Dao<Topic, Integer> mTopicDao;

	private boolean mIsMyFav;

	private CustomButton mCollectButton;

	private CustomButton mGoodButton;

	private CustomButton mShareButton;

	private UMSocialService mController;

	private final static Map<String, String> channlesMap = new HashMap<String, String>();

	static {

		String[] channleIds = MyJokeApplication.getAppContext().getResources()
				.getStringArray(R.array.channel_ids);

		String[] channleNames = MyJokeApplication.getAppContext()
				.getResources().getStringArray(R.array.channel_names);

		for (int i = 0; i < channleIds.length; i++) {
			channlesMap.put(channleIds[i], channleNames[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chenyc.myjoke.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.topic_detail);

		mProgressBar = new MyProgressBar(this);
		mHelper = new Helper(this);
		OrmLiteSqliteOpenHelper ormHelper = OpenHelperManager.getHelper(this,
				DatabaseHelper.class);
		try {
			mTopicDao = ormHelper.getDao(Topic.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		pageNum = getIntent().getIntExtra("pageNum", 1);
		int position = getIntent().getIntExtra("position", 0);
		mChannel = getIntent().getStringExtra("channel");
		mIsMyFav = getIntent().getBooleanExtra("isMyFav", false);

		flipView = (FlipViewController) findViewById(R.id.filpView);
		topicAdapter = new TopicAdapter(this, DETAIL_TOPIC_DATA);

		flipView.setAdapter(topicAdapter);
		flipView.setOnViewFlipListener(new ViewFlipListener() {

			@Override
			public void onViewFlipped(View view, int position) {
				if (position == topicAdapter.getCount() - 1) {
					++pageNum;
					getChannelData();
				} else if (position == 0) {
					pageNum = 1;
					getChannelData();
				}
				mCurrentTopic = setUpCurrentTopic(position);
			}
		});

		mCollectButton = (CustomButton) findViewById(R.id.rlCollect);
		mGoodButton = (CustomButton) findViewById(R.id.rlGood);
		mShareButton = (CustomButton) findViewById(R.id.rlShare);

		mCollectButton.setOnClickListener(this);
		mGoodButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);

		if (!mIsMyFav) {
			String channelName = channlesMap.get(mChannel);
			getSupportActionBar().setTitle(channelName);
		} else {
			getSupportActionBar().setTitle("我的收藏");
		}

		flipView.setSelection(position);
		mCurrentTopic = setUpCurrentTopic(position);

		if (mCurrentTopic.isFav()) {
			mCollectButton
					.setImageResource(R.drawable.button_details_collect_selected);
		} else {
			mCollectButton
					.setImageResource(R.drawable.button_details_collect_default);
		}

		UMWXHandler.WX_APPID = "wxfb0df958c80212ad";// 设置微信的Appid
		UMWXHandler.CONTENT_URL = "http://www.anzhi.com/soft_974884.html";
		mController = UMServiceFactory.getUMSocialService("social",
				RequestType.SOCIAL);

		mController.getConfig().setShareMail(false);
		mController.getConfig().setShareSms(false);

		// 添加微信平台
		mController.getConfig().supportWXPlatform(this);

		// 添加微信朋友圈
		mController.getConfig().supportWXPlatform(this,
				UMServiceFactory.getUMWXHandler(this).setToCircle(true));
		mController.getConfig().setSinaSsoHandler(new SinaSsoHandler());

	}

	private Topic setUpCurrentTopic(int position) {
		Topic topic = DETAIL_TOPIC_DATA.get(position);
		try {
			if (mTopicDao.idExists(topic.getId())) {
				topic.setFav(true);
			} else {
				topic.setFav(false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return topic;
	}

	private void favTopic(Topic topic) {
		ChannelRestClient.get("topic/fav",
				ParamsUtil.setUpFavTopicParams(topic.getId()), null);
	}

	@Override
	public void onResume() {
		super.onResume();
		flipView.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		flipView.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate your menu.
		getSupportMenuInflater().inflate(R.menu.detail_action_provider, menu);

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

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**
		 * 使用SSO必须添加，指定获取授权信息的回调页面，并传给SDK进行处理
		 */
		UMSsoHandler sinaSsoHandler = mController.getConfig()
				.getSinaSsoHandler();
		if (sinaSsoHandler != null
				&& requestCode == UMSsoHandler.DEFAULT_AUTH_ACTIVITY_CODE) {
			sinaSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}

	/**
	 * Creates a sharing {@link Intent}.
	 * 
	 * @return The sharing intent.
	 */
	private Intent createShareIntent() {
		Topic topic = DETAIL_TOPIC_DATA.get(flipView.getSelectedItemPosition());
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, topic.getDescription());
		return shareIntent;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDataChanged) {
				setResult(RESULT_OK);
			}
			finish();
			return true;

		case R.id.menu_item_share_action_provider_action_bar:
			Intent shareIntent = createShareIntent();
			startActivity(shareIntent);
			favTopic(mCurrentTopic);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void getChannelData() {
		if (mIsMyFav) {
			getChannelDataFromDb();
		} else {
			getChannelDataFromWeb();
		}

	}

	public void getChannelDataFromDb() {
		int limit = 15;
		int startRow = (pageNum - 1) * limit;
		List<Topic> list;
		try {
			list = mTopicDao.queryBuilder().orderBy("time", false).limit(limit)
					.offset(startRow).query();
			if (list.size() == 0) {
				return;
			}
			if (pageNum == 1) {
				DETAIL_TOPIC_DATA.clear();
			}
			DETAIL_TOPIC_DATA.addAll(list);
			topicAdapter.notifyDataSetChanged();
			mDataChanged = true;

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void getChannelDataFromWeb() {

		ChannelRestClient.get("topics",
				ParamsUtil.setUpChannelParams(mChannel, pageNum),
				new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(JSONArray ja) {
						super.onSuccess(ja);
						Gson gson = new Gson();
						Type collectionType = new TypeToken<List<Topic>>() {
						}.getType();
						List<Topic> list = gson.fromJson(ja.toString(),
								collectionType);
						if (list.size() == 0) {
							Toast.makeText(JokeDetailActivity.this,
									"亲,没有更多内容了~", Toast.LENGTH_SHORT).show();
							return;
						}
						if (pageNum == 1) {
							DETAIL_TOPIC_DATA.clear();
						}
						DETAIL_TOPIC_DATA.addAll(list);
						topicAdapter.notifyDataSetChanged();
						mDataChanged = true;
					}

					@Override
					public void onStart() {
						super.onStart();
						mProgressBar.show();
					}

					@Override
					public void onFinish() {
						super.onFinish();
						mProgressBar.dismiss();
					}

					@Override
					public void onFailure(Throwable e1, String arg1) {
						super.onFailure(e1, arg1);
						mProgressBar.dismiss();
						e1.printStackTrace();
					}

				});

	}

	protected void onDestroy() {
		super.onDestroy();
		OpenHelperManager.releaseHelper();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rlGood:
			mGoodButton
					.setImageResource(R.drawable.button_details_good_selected);
			favTopic(mCurrentTopic);
			break;
		case R.id.rlCollect:
			doCollect();
			break;
		case R.id.rlShare:
			UMShareMsg shareMsg = new UMShareMsg();
			shareMsg.text = mCurrentTopic.getDescription();
			mController.setShareContent(mCurrentTopic.getDescription());
			mController.setShareImage(new UMImage(this, mCurrentTopic
					.getBigImg()));
			mController.openShare(this, false);

			// favTopic(mCurrentTopic);
			// Intent shareIntent = createShareIntent();
			// startActivity(shareIntent);
			break;
		}
	}

	public void doCollect() {
		try {
			if (!mCurrentTopic.isFav()) {
				mCollectButton
						.setImageResource(R.drawable.button_details_collect_selected);
				mCurrentTopic.setFav(true);
				mTopicDao.createIfNotExists(mCurrentTopic);
				favTopic(mCurrentTopic);
			} else {
				mCollectButton
						.setImageResource(R.drawable.button_details_collect_default);
				mCurrentTopic.setFav(false);
				mTopicDao.delete(mCurrentTopic);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
