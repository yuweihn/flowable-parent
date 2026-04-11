package com.wei.common.utils;


import com.wei.common.core.domain.model.WechatLoginResult;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.core.encrypt.SecurityUtil;
import com.yuweix.kuafu.data.cache.Cache;
import com.yuweix.kuafu.http.HttpMethod;
import com.yuweix.kuafu.http.request.HttpFormRequest;
import com.yuweix.kuafu.http.response.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
 * @author yuwei
 */
@Slf4j
public final class WechatUtil {
	private WechatUtil() {}
	private static Cache CACHE;
	
	private static final String CACHE_ACCESS_TOKEN_KEY = "cache.ags.flowable.wechat.access.token.";
	
	
	public void setCache(Cache cache) {
		WechatUtil.CACHE = cache;
	}
	
	
	
	public static Response<Boolean, WechatLoginResult> login(String appId, String secret, String jsCode) {
		HttpResponse<Map<String, Object>> response = HttpFormRequest.create()
																	.url("https://api.weixin.qq.com/sns/jscode2session")
																	.method(HttpMethod.GET)
																	.addField("appid", appId)
																	.addField("secret", secret)
																	.addField("grant_type", "authorization_code")
																	.addField("js_code", jsCode)
																	.responseType(Map.class)
																	.execute();
		if (!response.isSuccess()) {
			log.error(response.getErrorMessage());
			return new Response<>(false, response.getErrorMessage());
		}
		
		Map<String, Object> map = response.getBody();
		log.info("Weixin response body: {}", map);
		if (map.containsKey("errcode") && !"0".equals(map.get("errcode").toString())) {
			return new Response<>(false, map.get("errmsg").toString());
		}
		
		String openId = (String) map.get("openid");//用户唯一标识
		String sessionKey = (String) map.get("session_key");//会话密钥
		String unionId = (String) map.get("unionid");//用户在开放平台的唯一标识符
		WechatLoginResult res = new WechatLoginResult(openId, sessionKey, unionId);
		return new Response<>(true, "ok", res);
	}
	
	public static String getAccessToken(String appId, String secret) {
		String key = CACHE_ACCESS_TOKEN_KEY + SecurityUtil.getMd5(appId + secret);
		String accessToken = CACHE.get(key);
		if (accessToken != null) {
			return accessToken;
		}
		try {
			HttpResponse<Map<String, Object>> response = HttpFormRequest.create()
																		.url("https://api.weixin.qq.com/cgi-bin/token")
																		.method(HttpMethod.GET)
																		.addField("appid", appId)
																		.addField("secret", secret)
																		.addField("grant_type", "client_credential")
																		.responseType(Map.class)
																		.execute();
			if (!response.isSuccess()) {
				log.error(response.getErrorMessage());
				return null;
			}
			
			Map<String, Object> map = response.getBody();
			accessToken = map.get("access_token").toString();//获取到的凭证
			long expireIn = Long.parseLong(map.get("expires_in").toString());//凭证有效时间(单位：秒)
			
			/**
			 * 为防止意外，将凭证失效时间设在微信服务器给的失效时间之前5分钟
			 **/
			CACHE.put(key, accessToken, expireIn - 5 * 60);
			return accessToken;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}


