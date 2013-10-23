package com.chenyc.myjoke.fragment;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;

import com.chenyc.config.Constants;
import com.chenyc.myjoke.ImageViewActivity;
import com.chenyc.myjoke.JokeDetailActivity;
import com.chenyc.myjoke.MyJokeApplication;
import com.chenyc.myjoke.R;
import com.chenyc.myjoke.bean.Topic;
import com.chenyc.myjoke.util.ChannelRestClient;
import com.chenyc.myjoke.util.DatabaseHelper;
import com.chenyc.myjoke.util.Helper;
import com.chenyc.myjoke.util.ParamsUtil;
import com.chenyc.myjoke.view.MyProgressBar;
import com.chenyc.myjoke.view.ScaleImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class JokeListFragment extends Fragment implements IXListViewListener,
		android.widget.AdapterView.OnItemClickListener {

	private static final int JOKE_DETAIL_REQUEST_CODE = 0;
	private XListView mAdapterView;
	private com.chenyc.myjoke.fragment.JokeListFragment.StaggeredAdapter mAdapter;
	private MyProgressBar mProgressBar;

	private int pageNum = 1;
	private String mChannel;
	private Helper mHelper;
	private boolean mIsMyFav;
	private Dao<Topic, Integer> mTopicDao;
	private boolean mShouldShowProgressBar = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mChannel = Constants.IMAGE_JOKE_CHANNEL;
		mHelper = new Helper(getActivity());
		mIsMyFav = getActivity().getIntent().getBooleanExtra("isMyFav", false);
		if (mIsMyFav) {
			OrmLiteSqliteOpenHelper ormHelper = OpenHelperManager.getHelper(
					getActivity(), DatabaseHelper.class);
			try {
				mTopicDao = ormHelper.getDao(Topic.class);
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.topic_list, container, false);
		mProgressBar = new MyProgressBar(getActivity(), (ViewGroup) v);
		mAdapterView = (XListView) v.findViewById(R.id.list);
		mAdapterView.setXListViewListener(this);
		mAdapterView.setOnItemClickListener(this);
		mAdapterView.setPullLoadEnable(false);

		mAdapter = new StaggeredAdapter(getActivity(), mAdapterView);
		mAdapterView.setAdapter(mAdapter);
		registerForContextMenu(mAdapterView);
		return v;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 1, 0, R.string.menu_copy);
		menu.add(0, 2, 0, R.string.action_bar_share_with);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int pos = (int) info.id;
		Topic topic = (Topic) mAdapter.getItem(pos);
		switch (item.getItemId()) {
		case 1:
			doCopy(topic);
			break;
		case 2:
			doShare(topic);
			break;

		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	private void doShare(Topic topic) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, topic.getDescription());
		startActivity(shareIntent);
	}

	private void doCopy(Topic topic) {
		mHelper.copyText(topic.getDescription());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadData();
	}

	@Override
	public void onRefresh() {
		pageNum = 1;
		loadData();
	}

	@Override
	public void onLoadMore() {
		++pageNum;
		loadData();
	}

	private void loadData() {
		if (mIsMyFav) {
			loadDataFromDb();
		} else {
			loadDataFromWeb();
		}
	}

	public void loadData(String id) {
		if (id.equals(mChannel)) {
			return;
		}
		mShouldShowProgressBar = true;
		mChannel = id;
		pageNum = 1;
		loadData();

	}

	private void finishLoad() {
		mProgressBar.dismiss();
		mAdapterView.stopRefresh();
		mAdapterView.stopLoadMore();
		mAdapterView.setRefreshTime(new Date().toLocaleString());
	}

	private void loadDataFromDb() {
		List<Topic> list;
		finishLoad();
		try {
			int limit = 15;
			int startRow = (pageNum - 1) * limit;
			list = mTopicDao.queryBuilder().limit(limit).offset(startRow)
					.orderBy("time", false).query();
			if (list.size() == 0) {
				mAdapterView.setPullLoadEnable(false);
				return;
			} else {
				mAdapterView.setPullLoadEnable(true);
			}
			if (pageNum == 1) {
				mAdapter.clearAllItem();
			}
			mAdapter.addItemLast(list);
			mAdapter.notifyDataSetChanged();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void loadDataFromWeb() {
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
							if (pageNum == 1) {
								Toast.makeText(getActivity(), "没有获取到数据",
										Toast.LENGTH_SHORT).show();
							}
							mAdapterView.setPullLoadEnable(false);
							return;
						} else {
							mAdapterView.setPullLoadEnable(true);
						}
						if (pageNum == 1) {
							mAdapter.clearAllItem();
						}
						mAdapter.addItemLast(list);
						mAdapter.notifyDataSetChanged();
						if (pageNum == 1) {
							mAdapterView.setSelection(0);
						}
					}

					@Override
					public void onStart() {
						super.onStart();
						if (mShouldShowProgressBar) {
							mProgressBar.show();
							mShouldShowProgressBar = false;
						}
					}

					@Override
					public void onFinish() {
						super.onFinish();
						finishLoad();
					}

					@Override
					public void onFailure(Throwable e1, String arg1) {
						super.onFailure(e1, arg1);
						finishLoad();
						e1.printStackTrace();
					}

				});
	}

	public class StaggeredAdapter extends BaseAdapter implements
			OnClickListener {
		private Context mContext;
		private LinkedList<Topic> mInfos;
		private DisplayImageOptions imageOptions;

		public StaggeredAdapter(Context context, XListView xListView) {
			mContext = context;
			mInfos = new LinkedList<Topic>();
			this.imageOptions = new DisplayImageOptions.Builder()
					.cacheInMemory().cacheOnDisc()
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.showImageForEmptyUri(R.drawable.empty_photo)
					.showStubImage(R.drawable.empty_photo).build();
		}

		public void clearAllItem() {
			mInfos.clear();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			Log.d("position", position+"");
			Topic topic = mInfos.get(position);

			if (convertView == null) {
				LayoutInflater layoutInflator = LayoutInflater.from(parent
						.getContext());
				convertView = layoutInflator.inflate(R.layout.topic_list_item,
						null);
				holder = new ViewHolder();
				holder.titleView = (TextView) convertView
						.findViewById(R.id.news_title);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.news_pic);
				holder.contentView = (TextView) convertView
						.findViewById(R.id.news_content);
				convertView.setTag(holder);
			}

			holder = (ViewHolder) convertView.getTag();
			// holder.imageView.setImageWidth(200);
			// holder.imageView.setImageHeight(500);
			holder.contentView.setText(Html.fromHtml(topic.getDescription()));
			holder.titleView.setText(topic.getTitle());
			if (topic.getSmallImg() != null) {
				if (mHelper.shouldLoadImage()) {
					MyJokeApplication.getImageLoader()
							.displayImage(topic.getSmallImg(),
									holder.imageView, imageOptions);
				} else {
					holder.imageView.setImageResource(R.drawable.empty_photo);
				}
				holder.imageView.setVisibility(View.VISIBLE);
			} else {
				holder.imageView.setVisibility(View.GONE);
			}
			holder.imageView.setOnClickListener(this);
			holder.imageView.setTag(topic);
			return convertView;
		}

		class ViewHolder {
			TextView titleView;
			ImageView imageView;
			TextView contentView;
			TextView timeView;
		}

		@Override
		public int getCount() {
			return mInfos.size();
		}

		@Override
		public Object getItem(int pos) {
			return mInfos.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		public void addItemLast(List<Topic> datas) {
			mInfos.addAll(datas);
		}

		public void addItemTop(List<Topic> datas) {
			for (Topic info : datas) {
				mInfos.addFirst(info);
			}
		}

		public List<Topic> getitems() {
			return mInfos;
		}

		@Override
		public void onClick(View v) {
			Topic topic = (Topic) v.getTag();
			if (topic.getBigImg() != null) {
				Intent intent = new Intent(this.mContext,
						ImageViewActivity.class);
				intent.putExtra("imageUrl", topic.getBigImg());
				mContext.startActivity(intent);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(), JokeDetailActivity.class);
		JokeDetailActivity.DETAIL_TOPIC_DATA = mAdapter.getitems();
		intent.putExtra("position", (int) id);
		intent.putExtra("channel", mChannel);
		intent.putExtra("pageNum", pageNum);
		intent.putExtra("isMyFav", mIsMyFav);
		startActivityForResult(intent, JOKE_DETAIL_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == JOKE_DETAIL_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenHelperManager.releaseHelper();
	}

}
