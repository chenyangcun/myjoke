package com.chenyc.myjoke.util;

import com.loopj.android.http.*;

public class ChannelRestClient {
	//private static final String BASE_URL = "http://192.168.3.100:8080/";
	
	private static final String BASE_URL = "http://relaxing.sinaapp.com/";

	private static AsyncHttpClient client = new AsyncHttpClient();
	
	static {
		client.setTimeout(2*60*1000);
	}

	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}
}