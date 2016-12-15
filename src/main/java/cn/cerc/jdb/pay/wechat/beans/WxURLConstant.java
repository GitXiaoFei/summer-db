package cn.cerc.jdb.pay.wechat.beans;

public class WxURLConstant {

	
	/*----------------- 微信调用URL----------------*/
	/**
	 *  获取openId
	 */
	public final static String GET_OPENID_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
	/**
	 *  获取token
	 *  获取access_token的接口地址（GET） 限200（次/天）
	 */
	public final static String GET_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	/**
	 *  获取ticket
	 */
	public final static String GET_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
	/**
	 *  发送模板消息
	 */
	public final static String SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
	/**
	 *  创建菜单
	 */
	public final static String CREATE_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	/**
	 *  创建分组标签
	 */
	public final static String CREATE_TAGS = "https://api.weixin.qq.com/cgi-bin/tags/create?access_token=ACCESS_TOKEN";
	/**
	 *  更新分组标签
	 */
	public final static String UPDATE_TAGS = "https://api.weixin.qq.com/cgi-bin/tags/update?access_token=ACCESS_TOKEN";
	/**
	 *  删除分组标签
	 */
	public final static String DELETE_TAGS = "https://api.weixin.qq.com/cgi-bin/tags/delete?access_token=ACCESS_TOKEN";
	/**
	 *  微信用户打标签（分组）
	 */
	public final static String BATCH_TAG = "https://api.weixin.qq.com/cgi-bin/tags/members/batchtagging?access_token=ACCESS_TOKEN";
	/**
	 *  微信用户删除标签
	 */
	public final static String BATCHUN_TAG = "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token=ACCESS_TOKEN";

	/**
	 *  微信（群发想消息）多媒体文件上传
	 */
	public final static String MEDIA_UPLOAD = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
	/**
	 *  微信（群发想消息）上传图文消息素材
	 */
	public final static String UPLOAD_NEWS = "https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=ACCESS_TOKEN";
	/**
	 *  微信（群发想消息）根据OpenID列表群发消息
	 */
	public final static String SEND_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token=ACCESS_TOKEN";
	
}
