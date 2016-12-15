package cn.cerc.jdb.pay.wechat;

public class WechatSession {
	
	// IHandle中识别码
	public static final String sessionId = "wechatSession";

	private String appId = "weixin.AppID";
	private String appSecret = "weixin.AppSecret";
	
	//微信公众号后台设置的服务器接口token认真
	private String token = "weixin.Token";
	// 商户号
	private String mch_id = "weixin.MchId";
	
	//这个参数partnerkey是在商户后台配置的一个32位的key,微信商户平台-账户设置-安全设置-api安全
	private String apiKey = "weixin.ApiKey";

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getMch_id() {
		return mch_id;
	}

	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}
