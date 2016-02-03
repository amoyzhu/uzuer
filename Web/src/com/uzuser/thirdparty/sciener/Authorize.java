package com.uzuser.thirdparty.sciener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import sun.misc.BASE64Decoder;

import com.google.gson.Gson;
import com.lingtong.model.Tenants;
import com.lingtong.util.SpringManage;
import com.lingtong.util.SystemConfiguration;
import com.ssqian.common.constant.CoreConstants;
import com.ssqian.common.util.MD5Utils;

/**
 * @author xqq
 * @date 2015-9-21 下午10:00:57
 * 
 */
public class Authorize {
	private static Authorize util;
	
	private Authorize (){}
	
	public static Authorize getInstance(){
		if( util == null ){
			util = new Authorize();
		}
		return util;
	}
	
	private String getCode(String username, String password) {
		String client_id = SystemConfiguration.getString("sciener.client_id");
		String response_type = "code";
		String code = "";
		String redirect_uri = SystemConfiguration.getString("sciener.redirect_uri");
		String url = MessageFormat
				.format("https://api.sciener.cn/oauth2/authorize?client_id={0}&response_type={1}&redirect_uri={2}&username={3}&password={4}",
						client_id, response_type, redirect_uri, username,
						password);
		System.out.println("获得url的接口地址:"+url);
		try {
			HttpGet get = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			client = WrapClientUtils.wrapClient(client);
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT,
					CoreConstants.REQUESTTIMEOUTINMILLIS);
			HttpParams params = new BasicHttpParams();
			params.setParameter("http.protocol.handle-redirects", false);
			get.setParams(params);
			HttpResponse status = client.execute(get);
			if (status.getStatusLine().getStatusCode() == 302) {
				Header locationHeader = status.getFirstHeader("Location");
				if (locationHeader != null) {
					String location = locationHeader.getValue();
					Map kvMap;
					kvMap = com.lingtong.util.URLUtil.splitQuery(location);
					code = (String) kvMap.get("code");
				}

			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code;
	}

	private String getToken(String code) {
		HttpPost post = new HttpPost("https://api.sciener.cn/oauth2/token");
		HttpClient client = new DefaultHttpClient();
		client = WrapClientUtils.wrapClient(client);
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("client_id",
				SystemConfiguration.getString("sciener.client_id")));
		params.add(new BasicNameValuePair("sciener.client_secret",
				SystemConfiguration.getString("sciener.client_secret")));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		params.add(new BasicNameValuePair("code", code));
		params.add(new BasicNameValuePair("redirect_uri",
				SystemConfiguration.getString("sciener.redirect_uri")));

		UrlEncodedFormEntity formentity;
		StringBuilder sb = new StringBuilder("");
		try {
			formentity = new UrlEncodedFormEntity(params, "utf-8");
			post.setEntity(formentity);
			HttpResponse status = client.execute(post);
			System.out.println(status.getStatusLine().getStatusCode());
			if (status.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = status.getEntity();
				InputStream in = entity.getContent();
				if (in != null) {
					int l = -1;
					byte[] tmp = new byte[1024];
					while ((l = in.read(tmp)) != -1) {
						//System.out.println(new String(tmp, 0, l));
						sb.append( new String(tmp, 0, l) );
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return sb.toString();
		}
	}
	
	/***
	 * 采用直接的授权方式
	 * @param username
	 * @param password
	 * @return
	 */
	public Map authorizeDirect(String username, String password){
		Map kvMap = null;
		String client_id = SystemConfiguration.getString("sciener.client_id");
		String response_type = "token";
		String redirect_uri = SystemConfiguration.getString("sciener.redirect_uri");
		String url = MessageFormat
				.format("https://api.sciener.cn/oauth2/authorize?client_id={0}&response_type={1}&redirect_uri={2}&username={3}&password={4}&scope={5}",
						client_id, response_type, redirect_uri, username,
						password, "user,key,room");
		System.out.println("获得url的接口地址:"+url);
		try {
			HttpGet get = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			client = WrapClientUtils.wrapClient(client);
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT,
					CoreConstants.REQUESTTIMEOUTINMILLIS);
			HttpParams params = new BasicHttpParams();
			params.setParameter("http.protocol.handle-redirects", false);
			get.setParams(params);
			HttpResponse status = client.execute(get);
			if (status.getStatusLine().getStatusCode() == 302) {
				Header locationHeader = status.getFirstHeader("Location");
				if (locationHeader != null) {
					String location = locationHeader.getValue();
					kvMap = com.lingtong.util.URLUtil.splitQuery(location);
					System.out.println(kvMap.toString());
				}

			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return kvMap;
		}
	}
	
	public JSONObject authorize(String username, String password) {
		//ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		String code = getCode(username, password);
		String token = getToken(code);
		System.out.println("token:"+token);
		JSONObject json = null;
		try {
			return JSONObject.fromObject(token);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return json;
		}
	}

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		//JSONObject json = Authorize.getInstance().authorize("api_sciener@uzuer.com", "47f71f4336d30258e7e7ec813c92f3c2");
		//JSONObject json = Authorize.getInstance().authorize("test20150925", PasswordMD5.getInstance().getPassword("123456"));
		//Authorize.getInstance().getCode("test20150925", MD5Utils.md5("123456"));
		//Authorize.getInstance().authorizeDirect("uzu_15118080665", MD5Utils.md5("080665"));
		//System.out.println(json.toString());
		Map map = Authorize.getInstance().authorizeDirect("uzu_15215731373", "c6b7c91ab9a72aa8d1ea1090fb9b7c05");
		System.out.println(map.get("openid"));
		System.out.println( MD5Utils.md5("800143"));
	}
	
	
}
