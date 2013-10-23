package com.chenyc.myjoke.util;

import com.loopj.android.http.RequestParams;

public class ParamsUtil {
	public static RequestParams setUpChannelParams(String channel, int pageNum) {
		RequestParams params = new RequestParams();
		params.put("pageNum", pageNum + "");
		params.put("channel", channel);
		return params;
	}

	public static RequestParams setUpFavTopicParams(int topicId) {
		RequestParams params = new RequestParams();
		params.put("id", topicId + "");
		return params;
	}
}
