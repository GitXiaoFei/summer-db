package cn.cerc.jdb.pay.wechat.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.cerc.jdb.core.IHandle;
import cn.cerc.jdb.core.Record;
import cn.cerc.jdb.oss.OssSession;
import cn.cerc.jdb.pay.wechat.WechatConnection;
import cn.cerc.jdb.pay.wechat.WechatSession;
import cn.cerc.jdb.pay.wechat.beans.AccessTokenVo;
import cn.cerc.jdb.pay.wechat.beans.MyX509TrustManager;
import cn.cerc.jdb.pay.wechat.beans.WxURLConstant;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import sun.net.www.protocol.http.HttpURLConnection;

/**
 * 公众平台通用接口工具类
 * 
 * @author chenyeen
 * @date 2016-11-09
 */
@SuppressWarnings("restriction")
public class WechatUtil {
	private static Logger log = LoggerFactory.getLogger(WechatUtil.class);
	
	/**
	 *  生产对应公众号模板消息回调路径
	 * @param corpNo_
	 * @return
	 */
	public static String getMessageUrl(IHandle handle, String redirectUri) {
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&state=0#wechat_redirect";
		WechatSession wx = (WechatSession) handle.getProperty(WechatSession.sessionId);
		url = url.replace("REDIRECT_URI", redirectUri);
		url = url.replace("APPID", wx.getAppId());
		return url;
	}

	/**
	 *  根据城市编号，生产对应公众号模板消息回调路径
	 * @param corpNo_
	 * @return
	 */
	public static String getMessageUrl(IHandle handle, String corpNo, String redirectUri) {
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&state=CORPNO#wechat_redirect";
		WechatSession wx = WechatConnection.getSession(handle, corpNo);
		url = url.replace("REDIRECT_URI", redirectUri);
		url = url.replace("CORPNO", corpNo);
		url = url.replace("APPID", wx.getAppId());
		return url;
	}

	/**
	 * 获取access_token
	 * 
	 * @param appid 凭证
	 * @param appsecret 密钥
	 * @return
	 */
	public static AccessTokenVo getAccessToken(IHandle handle) {
		AccessTokenVo accessToken = null;
		WechatSession wx = (WechatSession) handle.getProperty(WechatSession.sessionId);
		String requestUrl = WxURLConstant.GET_TOKEN_URL.replace("APPID", wx.getAppId()).replace("APPSECRET", wx.getAppSecret());
		String resultStr = httpRequest(requestUrl, "GET", null);
		// 如果请求成功
		if (StringUtils.isNotEmpty(resultStr)) {
			try {
				JSONObject jsonObject = JSONObject.fromObject(resultStr);
				accessToken = new AccessTokenVo();
				accessToken.setToken(jsonObject.getString("access_token"));
				accessToken.setExpiresIn(jsonObject.getInt("expires_in"));
				String ticketUrl = WxURLConstant.GET_TICKET_URL.replace("ACCESS_TOKEN", accessToken.getToken());
				resultStr = httpRequest(ticketUrl, "GET", null);
				// 如果请求成功
				if (StringUtils.isNotEmpty(resultStr)) {
					try {
						JSONObject ticketObject = JSONObject.fromObject(resultStr);
						accessToken.setTicket(ticketObject.getString("ticket"));
					} catch (JSONException e) {
						// 获取ticket失败
						log.error("获取ticket失败，错误信息：", resultStr);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				// 获取token失败
				log.error("获取token失败 ,错误信息：", resultStr);
			}
		}
		
		return accessToken;
	}

	/**
	 * 根据城市编号，获取access_token
	 * 
	 * @param appid 凭证
	 * @param appsecret 密钥
	 * @return
	 */
	public static AccessTokenVo getAccessToken(IHandle handle, String corpNo) {
		AccessTokenVo accessToken = null;
		WechatSession wx = WechatConnection.getSession(handle, corpNo);
		String requestUrl = WxURLConstant.GET_TOKEN_URL.replace("APPID", wx.getAppId()).replace("APPSECRET", wx.getAppSecret());
		String resultStr = httpRequest(requestUrl, "GET", null);
		// 如果请求成功
		if (StringUtils.isNotEmpty(resultStr)) {
			try {
				JSONObject jsonObject = JSONObject.fromObject(resultStr);
				accessToken = new AccessTokenVo();
				accessToken.setToken(jsonObject.getString("access_token"));
				accessToken.setExpiresIn(jsonObject.getInt("expires_in"));
				String ticketUrl = WxURLConstant.GET_TICKET_URL.replace("ACCESS_TOKEN", accessToken.getToken());
				resultStr = httpRequest(ticketUrl, "GET", null);
				// 如果请求成功
				if (StringUtils.isNotEmpty(resultStr)) {
					try {
						JSONObject ticketObject = JSONObject.fromObject(resultStr);
						accessToken.setTicket(ticketObject.getString("ticket"));
					} catch (JSONException e) {
						// 获取ticket失败
						log.error("获取ticket失败，错误信息：", resultStr);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				// 获取token失败
				log.error("获取token失败 ,错误信息：", resultStr);
			}
		}

		return accessToken;
	}

	/**
	 * 根据code获取 openID
	 * 
	 * @param appId 凭证
	 * @param appSecret 密钥
	 * @return
	 */
	public static String getOpenId(IHandle handle, String code){
		String openid = null;
		WechatSession wx = (WechatSession) handle.getProperty(WechatSession.sessionId);
		String url = WxURLConstant.GET_OPENID_URL;
		url = url.replace("APPID", wx.getAppId());
		url = url.replace("SECRET", wx.getAppSecret());
		url = url.replace("CODE", code==null?"":code);
		String resultStr = httpRequest(url, "GET", null);
		if (StringUtils.isNotEmpty(resultStr) && resultStr.contains("openid")) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			openid = resultJson.getString("openid");
		}
		return openid;
	}

	/**
	 * 根据城市编号，根据code获取 openID
	 * 
	 * @param appId 凭证
	 * @param appSecret 密钥
	 * @return
	 */
	public static String getOpenId(IHandle handle, String corpNo, String code){
		String openid = null;
		WechatSession wx = WechatConnection.getSession(handle, corpNo);
		String url = WxURLConstant.GET_OPENID_URL;
		url = url.replace("APPID", wx.getAppId());
		url = url.replace("SECRET", wx.getAppSecret());
		url = url.replace("CODE", code==null?"":code);
		String resultStr = httpRequest(url, "GET", null);
		if (StringUtils.isNotEmpty(resultStr) && resultStr.contains("openid")) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			openid = resultJson.getString("openid");
		}
		return openid;
	}

	/**
	 * 发送模板消息
	 */
	public static boolean sendTemplate(String accessToken, String params) throws Exception {
		String url = WxURLConstant.SEND_TEMPLATE;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String resultStr = httpRequest(url, "POST", params);
		log.info("微信模板消息推送返回结果："+ resultStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}

	/**
	 * 创建微信菜单
	 */
	public static boolean createMenu(String accessToken, String params) throws Exception {
		String url = WxURLConstant.CREATE_MENU_URL;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String resultStr = httpRequest(url, "POST", params);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}
	
	/**
	 * 创建微信标签
	 */
	public static Integer createTag(String accessToken, String tagName) throws Exception {
		String paramStr = "{\"tag\" : {\"name\" : \""+ tagName +"\"}}";
		String url = WxURLConstant.CREATE_TAGS;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String resultStr = httpRequest(url, "POST", paramStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			if(resultStr.contains("tag")){
				String tagJson = resultJson.getString("tag");
				String id = JSONObject.fromObject(tagJson).getString("id");
				return StringUtils.isNotEmpty(id)?Integer.parseInt(id):null;
			}
		}
		return null;
	}

	/**
	 * 编辑微信标签
	 */
	public static boolean updateTag(String accessToken, String tagId, String tagName) throws Exception {
		String paramStr = "{\"tag\" : {\"id\" : "+ tagId +",\"name\" : \""+ tagName +"\"}}";
		String url = WxURLConstant.UPDATE_TAGS;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String resultStr = httpRequest(url, "POST", paramStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}

	/**
	 * 删除微信标签
	 */
	public static boolean deleteTag(String accessToken, String tagId) throws Exception {
		String paramStr = "{\"tag\" : {\"id\" : "+ tagId +"}}";
		String url = WxURLConstant.DELETE_TAGS;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String resultStr = httpRequest(url, "POST", paramStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}

	/**
	 * 微信用户打标签（用户分组）
	 */
	public static boolean addTag(String accessToken, String openid,String tagId) throws Exception {
		String url = WxURLConstant.BATCH_TAG;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String paramStr = "{\"openid_list\" : [\""+ openid +"\"],\"tagid\" : "+ tagId +"}";
		String resultStr = httpRequest(url, "POST", paramStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}
	
	/**
	 * 微信用户取消标签
	 */
	public static boolean cancelTag(String accessToken, String openid,String tagId) throws Exception {
		String url = WxURLConstant.BATCHUN_TAG;
		url = url.replace("ACCESS_TOKEN", accessToken);
		String paramStr = "{\"openid_list\" : [\""+ openid +"\"],\"tagid\" : "+ tagId +"}";
		String resultStr = httpRequest(url, "POST", paramStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}
	
	/**
	 * 发送微信群发消息
	 * @throws Exception 
	 */
	public static boolean pushMessage(IHandle handle, String accessToken, List<Record> messList, List<String> openIdList) throws Exception{
		String url = null;
		String resultStr = null;
		//调用上传图文消息素材
		url = WxURLConstant.UPLOAD_NEWS.replace("ACCESS_TOKEN", accessToken);
		StringBuffer sb = new StringBuffer();
		for (Record record : messList) {
			//上传图片获取thumb_media_id
			String thumb_media_id = null;
			resultStr = sendFile(handle, accessToken, "image", record.getString("imgUrl_"));
			log.info("=======微信群发消息，上传图文消息-图片返回结果："+ resultStr);
			if (StringUtils.isNotEmpty(resultStr)) {
				JSONObject resultJson = JSONObject.fromObject(resultStr);
				thumb_media_id = resultJson.getString("media_id");
			}
			JSONObject json = new JSONObject();
			json.accumulate("thumb_media_id", thumb_media_id);
			json.accumulate("author", "");
			json.accumulate("title", record.getString("messageTitle_"));
			String redirectUrl = "FrmHealthMessage";
			String urlStr = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx8302b6636974854e&redirect_uri=http://ehealth.lucland.com/forms/FrmWeixin.template?redirectUrl="+ redirectUrl +"&response_type=code&scope=snsapi_base&state=727000#wechat_redirect";
			json.accumulate("content_source_url", urlStr);
			json.accumulate("content", record.getString("messageContent_"));
			json.accumulate("digest", "");
			json.accumulate("show_cover_pic", "1");
			if(StringUtils.isNotEmpty(sb.toString())){
				sb.append(",");
			}
			sb.append(json.toString());
		}
		String paramStr4 = "{\"articles\": ["+ sb.toString() +"]}";
		resultStr = httpRequest(url, "POST", paramStr4);
		log.info("=======微信群发消息，上传图文消息返回结果："+ resultStr);
		String media_id = null;
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			media_id = resultJson.getString("media_id");
		}
		
		//调用openid列表发送接口
		url = WxURLConstant.SEND_MESSAGE.replace("ACCESS_TOKEN", accessToken);
		StringBuffer sb2 = new StringBuffer();
		for (String openid : openIdList) {
			if(StringUtils.isNotEmpty(sb2.toString())){
				sb2.append(",");
			}
			sb2.append("\""+ openid +"\"");
		}
		String paramStr5 = "{\"touser\":["+ sb2.toString() +"],\"mpnews\":{\"media_id\":\""+ media_id +"\"},\"msgtype\":\"mpnews\"}";
		resultStr = httpRequest(url, "POST", paramStr5);
		log.info("=======微信群发消息返回结果："+ resultStr);
		if (StringUtils.isNotEmpty(resultStr)) {
			JSONObject resultJson = JSONObject.fromObject(resultStr);
			String errcode = resultJson.getString("errcode");
			return "0".equals(errcode);
		}
		return false;
	}

	/**
	 * 发起https请求并获取结果
	 * 
	 * @param requestUrl 请求地址
	 * @param requestMethod 请求方式（GET、POST）
	 * @param outputStr 提交的数据
	 * @return 
	 */
	public static String httpRequest(String requestUrl, String requestMethod, String outputStr) {
		StringBuffer buffer = new StringBuffer();
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			TrustManager[] tm = { new MyX509TrustManager() };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();

			URL url = new URL(requestUrl);
			HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
			httpUrlConn.setSSLSocketFactory(ssf);

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod(requestMethod);

			if ("GET".equalsIgnoreCase(requestMethod))
				httpUrlConn.connect();

			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			return buffer.toString();
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
     * 文件上传到微信服务器
     * @param fileType 文件类型
     * @param filePath 文件路径
     * @return JSONObject
     * @throws Exception
     */
	public static String sendFile(IHandle handle, String accessToken, String fileType, String filePath) throws Exception { 
        String result = null;  
        //创建临时存储文件夹
	    String localFile = WechatUtil.class.getClassLoader().getResource("").getPath().replace("/", "\\");
	    localFile = localFile.substring(0, localFile.length() - 16) + "\\tmp\\";
    	File file1 = new File(localFile);
    	if(!file1.exists()){
    		file1.mkdirs();
    	}
    	//创建临时文件名
    	localFile += System.currentTimeMillis() + filePath.substring(filePath.lastIndexOf("."));;
	    File file = new File(localFile);  
	    //从OSS下载图片文件
	    OssSession ossSession = (OssSession) handle.getProperty(OssSession.sessionId);
	    filePath = filePath.substring(filePath.lastIndexOf(".com") + 5);;
		ossSession.download(filePath, localFile);
    	
        String requestUrl = WxURLConstant.MEDIA_UPLOAD.replace("ACCESS_TOKEN", accessToken).replace("TYPE", fileType);
        URL url = new URL(requestUrl);
        HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
        httpUrlConn.setRequestMethod("POST");
        httpUrlConn.setDoInput(true);  
        httpUrlConn.setDoOutput(true);  
        httpUrlConn.setUseCaches(false);
        // 设置请求头信息  
        httpUrlConn.setRequestProperty("Connection", "Keep-Alive");  
        httpUrlConn.setRequestProperty("Charset", "UTF-8");  
        // 设置边界  
        String BOUNDARY = "----------" + System.currentTimeMillis();  
        httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+ BOUNDARY);  
        // 第一部分：  
        StringBuilder sb = new StringBuilder();  
        sb.append("--"); // 必须多两道线  
        sb.append(BOUNDARY);  
        sb.append("\r\n");  
        sb.append("Content-Disposition: form-data;name=\"file\";filename=\""+ file.getName() + "\"\r\n");  
        sb.append("Content-Type:application/octet-stream\r\n\r\n");  
        byte[] head = sb.toString().getBytes("utf-8");  
        // 获得输出流  
        OutputStream out = new DataOutputStream(httpUrlConn.getOutputStream());  
        // 输出表头  
        out.write(head);  
        // 把文件已流文件的方式 推入到url中  
        DataInputStream in = new DataInputStream(new FileInputStream(file));  
        int bytes = 0;  
        byte[] bufferOut = new byte[1024];  
        while ((bytes = in.read(bufferOut)) != -1) {  
        	out.write(bufferOut, 0, bytes);  
        }  
        in.close();  
        // 结尾部分  
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线  
        out.write(foot);  
        out.flush();  
        out.close();  
        StringBuffer buffer = new StringBuffer();  
        BufferedReader reader = null;  
        try {  
	        // 定义BufferedReader输入流来读取URL的响应  
	        reader = new BufferedReader(new InputStreamReader(httpUrlConn.getInputStream()));  
	        String line = null;  
	        while ((line = reader.readLine()) != null) {  
	        	buffer.append(line);  
	        }  
	        if(result==null){  
	        	result = buffer.toString();  
	        }  
	    } catch (IOException e) {  
	    	log.info("发送POST请求出现异常！" + e);  
	        e.printStackTrace();  
	        throw new IOException("数据读取异常");  
	    } finally {  
	        if(reader!=null){  
	        	reader.close();  
	        }  
        }  
        return result;  
    }
	
}