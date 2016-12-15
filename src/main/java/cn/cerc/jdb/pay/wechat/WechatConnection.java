package cn.cerc.jdb.pay.wechat;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import cn.cerc.jdb.core.IConfig;
import cn.cerc.jdb.core.IConnection;
import cn.cerc.jdb.core.IHandle;
import cn.cerc.jdb.mysql.SqlQuery;

public class WechatConnection implements IConnection {

	//单个公众号
	private static WechatSession session;
	//多个公众号
	private static Map<String, WechatSession> sessionMap = new HashMap<>();
	private IConfig config;

	@Override
	public void setConfig(IConfig config) {
		this.config = config;
	}

	public IConfig getConfig() {
		return config;
	}

	@Override
	public void init() {
		if (session == null) {
			session = new WechatSession();
			String appId = config.getProperty(session.getAppId());
			String appSecret = config.getProperty(session.getAppSecret());
			String token = config.getProperty(session.getToken());
			String mch_id = config.getProperty(session.getMch_id());
			String apiKey = config.getProperty(session.getApiKey());
			
			if (StringUtils.isEmpty(appId))
				throw new RuntimeException("weixin.AppID is null");
			if (StringUtils.isEmpty(appSecret))
				throw new RuntimeException("weixin.AppSecret is null");
			if (StringUtils.isEmpty(token))
				throw new RuntimeException("weixin.Token is null");
			if (StringUtils.isEmpty(mch_id))
				throw new RuntimeException("weixin.MchId is null");
			if (StringUtils.isEmpty(apiKey))
				throw new RuntimeException("weixin.ApiKey is null");
			
			session.setAppId(appId);
			session.setAppSecret(appSecret);
			session.setToken(token);
			session.setMch_id(mch_id);
			session.setApiKey(apiKey);
		}
	}
	
	private static void init2(IHandle handle, String corpNo) {
		if (sessionMap.get(corpNo) == null) {
			SqlQuery ds = new SqlQuery(handle);
			ds.add("select wxCode_,wxName_,appID_,appSecret_,token_,mchID_,apiKey_ from t_wx_config");
			ds.add("where corpNo_ = '%s'", corpNo);
			ds.open();
			if(ds.eof()){
				throw new RuntimeException("微信获取城市编号为："+ corpNo+" 的配置信息失败，数据库未查询到该城市的公众号配置信息!");
			}
			session = new WechatSession();
			session.setAppId(ds.getString("appID_"));
			session.setAppSecret(ds.getString("appSecret_"));
			session.setToken(ds.getString("token_"));
			session.setMch_id(ds.getString("mchID_"));
			session.setApiKey(ds.getString("apiKey_"));
			sessionMap.put(corpNo, session);
		}
	}

	/**
	 *  一个公众号 配置properties时使用
	 */
	@Override
	public WechatSession getSession() {
		this.init();
		return session;
	}
	
	/**
	 *  多公众号 根据城市获取
	 */
	public static WechatSession getSession(IHandle handle, String corpNo) {
		init2(handle, corpNo);
		return sessionMap.get(corpNo);
	}
}
