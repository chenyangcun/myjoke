/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.chenyc.auth;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.chenyc.auth.util.AccessToken;
import com.chenyc.auth.util.Base64Encoder;
import com.chenyc.auth.util.HttpClient;
import com.chenyc.auth.util.OAuth;
import com.chenyc.auth.util.PostParameter;
import com.chenyc.auth.util.QParameter;
import com.chenyc.auth.util.Response;

/**
 * A java reporesentation of the <a href="http://wiki.open.kaixin001.com/">KxSDK
 * API</a>
 */
public class QQSDK {
	public static String CONSUMER_KEY = "";// api key
	public static String CONSUMER_SECRET = "";// secret key
	public static String Redirect_uri = ".../callback.jsp";// 需与注册信息中网站地址的域名一致，可修改域名映射在本地进行测试
	public static String baseURL = "http://open.t.qq.com";
	public static String authorizationURL = "https://open.t.qq.com/cgi-bin/oauth2/authorize";
	public static String accessTokenURL = "http://open.t.qq.com/cgi-bin/oauth2/access_token";
	public static String accessTokenURLssl = "https://open.t.qq.com/cgi-bin/oauth2/access_token";
	public static String format = "json";

	protected HttpClient http = new HttpClient();
	protected AccessToken accessToken = null;

	public void setConsumerKey(String consumerKey) {
		CONSUMER_KEY = consumerKey;
	}

	public void setConsumerSecret(String consumerSecret) {
		CONSUMER_SECRET = consumerSecret;
	}

	public void setCallBackUrl(String callbackUrl) {
		Redirect_uri = callbackUrl;
	}

	public String getAuthorizeURLforCode(String scope, String state,
			String display) {
		return getAuthorizeURL("code", scope, state, display);
	}

	public String getAuthorizeURLforToken(String scope, String state,
			String display) {
		return getAuthorizeURL("token", scope, state, display);
	}

	/**
	 * 获取未授权的Request Token
	 * 
	 * @param oauth
	 * @return
	 * @throws Exception
	 */
	public OAuth requestToken(OAuth oauth) throws Exception {
		String url = "https://open.t.qq.com/cgi-bin/request_token";

		String queryString = getOauthParams(url, "GET",
				oauth.getOauth_consumer_secret(), "", oauth.getParams());

		System.out.println("queryString:" + queryString);
		Response responseData = http.get(url + "?" + queryString, null);
		System.out.println("responseData:" + responseData.asString());
		if (!parseToken(responseData.asString(), oauth)) {// Request Token 授权不通过
			oauth.setStatus(1);
		}

		return oauth;
	}

	/**
	 * 验证Token返回结果
	 * 
	 * @param response
	 * @param oauth
	 * @return
	 * @throws Exception
	 */
	public boolean parseToken(String response, OAuth oauth) throws Exception {
		if (response == null || response.equals("")) {
			return false;
		}

		oauth.setMsg(response);
		String[] tokenArray = response.split("&");
		if (tokenArray.length < 2) {
			return false;
		}

		String strTokenKey = tokenArray[0];
		String strTokenSecrect = tokenArray[1];

		String[] token = strTokenKey.split("=");
		if (token.length < 2) {
			return false;
		}
		oauth.setOauth_token(token[1]);

		String[] tokenSecrect = strTokenSecrect.split("=");
		if (tokenSecrect.length < 2) {
			return false;
		}
		oauth.setOauth_token_secret(tokenSecrect[1]);

		if (tokenArray.length == 3) {
			String[] params = tokenArray[2].split("=");
			if ("name".equals(params[0]) && params.length == 2) {
				// oauth.setAccount(this.getAccount(oauth));// 获取当前登录用户的账户信息
			}
		}

		return true;
	}

	protected String getAuthorizeURL(String type, String scope, String state,
			String display) {
		PostParameter[] params = new PostParameter[7];
		params[0] = new PostParameter("response_type", type);
		params[1] = new PostParameter("client_id", CONSUMER_KEY);
		params[2] = new PostParameter("redirect_uri", Redirect_uri);
		params[3] = new PostParameter("scope", scope);
		params[4] = new PostParameter("state", state);
		params[5] = new PostParameter("display", display);
		params[6] = new PostParameter("oauth_client", 1);
		String query = HttpClient.encodeParameters(params);
		return authorizationURL + "?" + query;
	}

	/**
	 * 使用授权后的Request Token换取Access Token
	 * 
	 * @param oauth
	 * @return
	 * @throws Exception
	 */
	public AccessToken getAccessToken(OAuth oauth, String code)
			throws Exception {
		oauth.setOauth_verifier(code);
		String url = "http://open.t.qq.com/cgi-bin/access_token";
		String queryString = getOauthParams(url, "GET",
				oauth.getOauth_consumer_secret(),
				oauth.getOauth_token_secret(), oauth.getAccessParams());

		Response responseData = http.get(url + "?" + queryString, null);

		parseToken(responseData.asString(), oauth);
		if (!parseToken(responseData.asString(), oauth)) {// Request Token 授权不通过
			return null;
		}
		AccessToken accessToken = new AccessToken(oauth.getOauth_token(),oauth.getOauth_token_secret());
		return accessToken;
	}

	/**
	 * 编码请求参数
	 * 
	 * @param params
	 * @return
	 */
	private static String encodeParams(List<QParameter> params) {
		StringBuilder result = new StringBuilder();
		for (QParameter param : params) {
			if (result.length() != 0) {
				result.append("&");
			}
			result.append(URLEncoder.encode(param.getName()));
			result.append("=");
			result.append(URLEncoder.encode(param.getValue()));
		}
		return result.toString();
	}

	/**
	 * 处理请求URL
	 * 
	 * @param url
	 * @return
	 */
	private static String getNormalizedUrl(URL url) {
		try {
			StringBuilder buf = new StringBuilder();
			buf.append(url.getProtocol());
			buf.append("://");
			buf.append(url.getHost());
			if ((url.getProtocol().equals("http") || url.getProtocol().equals(
					"https"))
					&& url.getPort() != -1) {
				buf.append(":");
				buf.append(url.getPort());
			}
			buf.append(url.getPath());
			return buf.toString();
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 处理签名请求和参数
	 * 
	 * @param url
	 * @param httpMethod
	 * @param parameters
	 * @return
	 */
	private String generateSignatureBase(URL url, String httpMethod,
			List<QParameter> parameters) {

		StringBuilder base = new StringBuilder();
		base.append(httpMethod.toUpperCase());
		base.append("&");
		base.append(URLEncoder.encode(getNormalizedUrl(url)));
		base.append("&");
		base.append(URLEncoder.encode(encodeParams(parameters)));

		return base.toString();
	}

	/**
	 * 生成签名值
	 * 
	 * @param url
	 * @param consumerSecret
	 * @param accessTokenSecret
	 * @param httpMethod
	 * @param parameters
	 * @return
	 */
	private String generateSignature(URL url, String consumerSecret,
			String accessTokenSecret, String httpMethod,
			List<QParameter> parameters) {
		String base = this.generateSignatureBase(url, httpMethod, parameters);
		return this.generateSignature(base, consumerSecret, accessTokenSecret);
	}

	private static final String hashAlgorithmName = "HmacSHA1";

	/**
	 * 生成签名值
	 * 
	 * @param base
	 * @param consumerSecret
	 * @param accessTokenSecret
	 * @return
	 */
	private String generateSignature(String base, String consumerSecret,
			String accessTokenSecret) {
		try {
			Mac mac = Mac.getInstance(hashAlgorithmName);
			String oauthSignature = URLEncoder.encode(consumerSecret)
					+ "&"
					+ ((accessTokenSecret == null) ? "" : URLEncoder
							.encode(accessTokenSecret));
			SecretKeySpec spec = new SecretKeySpec(oauthSignature.getBytes(),
					hashAlgorithmName);
			mac.init(spec);
			byte[] bytes = mac.doFinal(base.getBytes());
			return new String(Base64Encoder.encode(bytes));
		} catch (Exception e) {
		}
		return null;
	}

	public String getOauthParams(String url, String httpMethod,
			String consumerSecret, String tokenSecrect,
			List<QParameter> parameters) {
		Collections.sort(parameters);

		String urlWithParameter = url;

		String parameterString = encodeParams(parameters);
		if (parameterString != null && !parameterString.equals("")) {
			urlWithParameter += "?" + parameterString;
		}

		URL aUrl = null;
		try {
			aUrl = new URL(urlWithParameter);
		} catch (MalformedURLException e) {
			System.err.println("URL parse error:" + e.getLocalizedMessage());
		}

		String signature = this.generateSignature(aUrl, consumerSecret,
				tokenSecrect, httpMethod, parameters);

		parameterString += "&oauth_signature=";
		parameterString += URLEncoder.encode(signature);

		return parameterString;
	}

	protected String getOAuthAccessTokenUrl(String code) {
		PostParameter[] params = new PostParameter[5];
		params[0] = new PostParameter("grant_type", "authorization_code");
		params[1] = new PostParameter("client_id", CONSUMER_KEY);
		params[2] = new PostParameter("client_secret", CONSUMER_SECRET);
		params[3] = new PostParameter("code", code);
		params[4] = new PostParameter("redirect_uri", Redirect_uri);
		String query = HttpClient.encodeParameters(params);
		return accessTokenURL + "?" + query;
	}

	public AccessToken getOAuthAccessTokenFromCode(String code)
			throws Exception {
		AccessToken oauthToken = null;
		try {
			PostParameter[] params = new PostParameter[5];
			params[0] = new PostParameter("grant_type", "authorization_code");
			params[1] = new PostParameter("client_id", CONSUMER_KEY);
			params[2] = new PostParameter("client_secret", CONSUMER_SECRET);
			params[3] = new PostParameter("code", code);
			params[4] = new PostParameter("redirect_uri", Redirect_uri);

			oauthToken = new AccessToken(http.get(accessTokenURL, params));
		} catch (Exception te) {
			throw te;
		}
		return oauthToken;
	}

	public AccessToken getOAuthAccessTokenFromPassword(String username,
			String password, String scope) throws Exception {
		AccessToken oauthToken = null;
		try {
			PostParameter[] params = new PostParameter[6];
			params[0] = new PostParameter("grant_type", "password");
			params[1] = new PostParameter("client_id", CONSUMER_KEY);
			params[2] = new PostParameter("client_secret", CONSUMER_SECRET);
			params[3] = new PostParameter("username", username);
			params[4] = new PostParameter("password", password);
			params[5] = new PostParameter("scope", scope);
			oauthToken = new AccessToken(http.get(accessTokenURLssl, params));
		} catch (Exception te) {
			throw te;
		}
		return oauthToken;
	}

	public AccessToken getOAuthAccessTokenFromRefreshtoken(
			String refresh_token, String scope) throws Exception {
		AccessToken oauthToken = null;
		try {
			PostParameter[] params = new PostParameter[5];
			params[0] = new PostParameter("grant_type", "refresh_token");
			params[1] = new PostParameter("client_id", CONSUMER_KEY);
			params[2] = new PostParameter("client_secret", CONSUMER_SECRET);
			params[3] = new PostParameter("refresh_token", refresh_token);
			params[4] = new PostParameter("scope", scope);
			oauthToken = new AccessToken(http.get(accessTokenURL, params));
		} catch (Exception te) {
			throw te;
		}
		return oauthToken;
	}

	public void setOAuthAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		QQSDK kxSDK = (QQSDK) o;
		if (!http.equals(kxSDK.http)) {
			return false;
		}
		if (!accessToken.equals(kxSDK.accessToken)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = http.hashCode();
		result = 31 * result + accessToken.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "QQSDK{" + "http=" + http + ", accessToken='" + accessToken
				+ '\'' + '}';
	}

	// --------------base method----------

	protected Response get(String api, PostParameter[] params) throws Exception {
		api = baseURL + api + "." + format;
		return http.get(api, params);
	}

	protected Response post(String api, PostParameter[] params)
			throws Exception {
		api = baseURL + api + "." + format;
		return http.post(api, params);
	}
}
