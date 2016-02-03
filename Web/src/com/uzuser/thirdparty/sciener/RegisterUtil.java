package com.uzuser.thirdparty.sciener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lingtong.model.Tenants;
import com.lingtong.util.SystemConfiguration;
import com.ssqian.common.constant.CoreConstants;
import com.ssqian.common.util.MD5Utils;

/**
 * @author xqq
 * @date 2015-9-25 下午9:44:25
 * 科技锁注册工具类
 */
public class RegisterUtil {
	private static RegisterUtil util;
	
	private RegisterUtil(){}
	
	public static RegisterUtil getInstance(){
		if( util == null ){
			util = new RegisterUtil();
		}
		return util;
	}
	
	public String register(Tenants tenant){
		String client_id = SystemConfiguration.getString("sciener.client_id");
		String client_secret = SystemConfiguration.getString("sciener.client_secret");
		String url = MessageFormat
				.format("https://api.sciener.cn/otherService/regUser?client_id={0}&client_secret={1}&username={2}&password={3}",
						client_id, client_secret, tenant.getSciener_username(), MD5Utils.md5(tenant.getSciener_password()));
		System.out.println("sciener注册用户接口地址:" + url);
		InputStream in = null;
		String sciener_username = null;
		try {
			HttpGet get = new HttpGet(url);
			HttpClient client = new DefaultHttpClient();
			client = WrapClientUtils.wrapClient(client);
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT,
					CoreConstants.REQUESTTIMEOUTINMILLIS);
			HttpResponse status = client.execute(get);
			StringBuilder sb = new StringBuilder();
			
			if (status.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = status.getEntity();
				in = entity.getContent();
				if (in != null) {
					int l = -1;
					byte[] tmp = new byte[1024];
					while ((l = in.read(tmp)) != -1) {
						sb.append(new String(tmp, 0 , l));
						System.out.println(new String(tmp, 0, l));
					}
					JSONObject json = JSONObject.fromObject(sb.toString());
					if( json != null ){
						sciener_username = json.getString("username");
					}
				}
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if( in != null ){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return sciener_username;
		}
	}
	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		Tenants tenant = new Tenants();
		tenant.setSciener_username("18458195521");
		tenant.setSciener_password("123456");
		RegisterUtil.getInstance().register(tenant);
		System.out.println(MD5Utils.md5("123456"));
	}
}
