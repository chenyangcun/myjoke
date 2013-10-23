package com.chenyc.auth;

import android.content.res.Configuration;

import com.chenyc.auth.util.AccessToken;
import com.chenyc.auth.util.OAuth;
import com.chenyc.config.Constants;


public class QQClient {

	private static QQSDK sdk;
	private static OAuth oauth;
	static {
		sdk = new QQSDK();
		oauth = new OAuth();
		oauth.setOauth_consumer_key("801134623");
		oauth.setOauth_consumer_secret("e670bcf12eb063fc5aedb232213b69bf");
//		oauth.setOauth_callback(Configuration.getWeiboCallbackHost()+"/AuraMesh/authorise?type="
//				+ Constants.TENCENT);
		
	}

	public static String getAuthorzieURL() {
		try {
			oauth = sdk.requestToken(oauth);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String oauth_token = oauth.getOauth_token();
		String url = "http://open.t.qq.com/cgi-bin/authorize?oauth_token="
			+ oauth_token;
		return url;
	}

	/**
	 * 使用授权后的Request Token换取Access Token
	 * 
	 * @param oauth
	 * @return
	 * @throws Exception
	 */
	public static AccessToken getAccessToken(String code)  {
		try {
			return sdk.getAccessToken(oauth, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
