package com.chenyc.myjoke.adapter;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.chenyc.myjoke.ImageViewActivity;
import com.chenyc.myjoke.MyJokeApplication;
import com.chenyc.myjoke.R;
import com.chenyc.myjoke.bean.Topic;
import com.chenyc.myjoke.util.DatabaseHelper;
import com.chenyc.myjoke.util.Helper;
import com.chenyc.myjoke.view.CustomButton;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.umeng.common.Log;

public class TopicAdapter extends BaseAdapter implements OnClickListener {

	private LayoutInflater inflater;

	private int repeatCount = 1;

	private List<Topic> topicData;

	private DisplayImageOptions imageOptions;

	private Context context;

	private Helper mHelper;

	private Dao<Topic, Integer> mTopicDao;

	public TopicAdapter(Context context, List<Topic> topicData) {
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.topicData = topicData;
		this.mHelper = new Helper(context);
		this.imageOptions = new DisplayImageOptions.Builder().cacheInMemory()
				.showImageForEmptyUri(R.drawable.empty_photo)
				.showStubImage(R.drawable.empty_photo).cacheOnDisc().build();

	}

	@Override
	public int getCount() {
		return topicData.size() * repeatCount;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = convertView;
		
		if (convertView == null) {
			layout = inflater.inflate(R.layout.topic_detail_item, null);
			AphidLog.d("created new view from adapter: %d", position);
		}

		Topic data = topicData.get(position % topicData.size());

		UI.<TextView> findViewById(layout, R.id.title).setText(
				Html.fromHtml(data.getTitle()));
		UI.<TextView> findViewById(layout, R.id.description).setText(
				Html.fromHtml(data.getDescription()));

		ImageView photo = UI.<ImageView> findViewById(layout, R.id.photo);
		if (data.getSmallImg() != null) {
			if (mHelper.shouldLoadImage()) {
				MyJokeApplication.getImageLoader().displayImage(
						data.getSmallImg(), photo, imageOptions);
			} else {
				photo.setImageResource(R.drawable.empty_photo);
			}
		} else {
			photo.setImageBitmap(null);
		}
		photo.setTag(data);
		photo.setOnClickListener(this);

		return layout;
	}

	public void removeData(int index) {
		if (topicData.size() > 1) {
			topicData.remove(index);
		}
	}

	@Override
	public void onClick(View v) {
		Topic topic = null;
		switch (v.getId()) {
		case R.id.photo:
			topic = (Topic) v.getTag();
			if (topic.getBigImg() != null) {
				Intent intent = new Intent(this.context,
						ImageViewActivity.class);
				intent.putExtra("imageUrl", topic.getBigImg());
				context.startActivity(intent);
			}
			break;
		default:
			break;
		}

	}

}
