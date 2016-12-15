package cn.cerc.jdb.pay.wechat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import cn.cerc.jdb.core.IHandle;
import cn.cerc.jdb.pay.wechat.tools.GetWxOrderno;
import cn.cerc.jdb.pay.wechat.tools.MD5Util;
import cn.cerc.jdb.pay.wechat.tools.RequestHandler;
import cn.cerc.jdb.pay.wechat.tools.TenpayUtil;
import net.sf.json.util.JSONUtils;

public class WechatPay {

	private WechatSession session;
	private Logger log = Logger.getLogger(WechatPay.class);

	/**
	 *  一个公众号
	 * @param handle
	 */
	public WechatPay(IHandle handle){
		this.session = (WechatSession) handle.getProperty(WechatSession.sessionId);
	}

	/**
	 *  多城市支付
	 * @param handle
	 * @param corpNo
	 */
	public WechatPay(IHandle handle, String corpNo){
		this.session = WechatConnection.getSession(handle, corpNo);
	}
	
	/**
	 * APP支付
	 * 
	 * @param tradeNo  交易单号
	 * @param totalFee 交易金额
	 * @param attach
	 * @param body
	 * @param ip  客户端IP地址
	 * @param notifyUrl 支付成功回调路径
	 * @return
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public Map<String, String> appPay(String tradeNo, String totalFee,
			String attach, String body, String ip, String notifyUrl) {
		String trade_type = "APP";
		String nonce_str = TenpayUtil.getNonceStr();
		String total_fee = String.valueOf(new BigDecimal(totalFee).multiply(
				new BigDecimal(100)).intValue());// 金额 微信是以分为单位的;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", session.getAppId());
		// 商户号
		packageParams.put("mch_id", session.getMch_id());
		// 随机字符串
		packageParams.put("nonce_str", nonce_str);
		// 商品描述根据情况修改
		packageParams.put("body", body);
		// 附加数据 原样返回
		packageParams.put("attach", attach);
		// 商户订单号
		packageParams.put("out_trade_no", tradeNo);
		// 总金额以分为单位，不带小数点
		packageParams.put("total_fee", total_fee);
		packageParams.put("spbill_create_ip", ip);
		// 这里notify_url是 支付完成后微信发给该链接信息，可以判断会员是否支付成功，改变订单状态等。
		packageParams.put("notify_url", notifyUrl);
		// 交易类型，原生扫码
		packageParams.put("trade_type", trade_type);

		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler
				.init(session.getAppId(), session.getApiKey());

		String sign = reqHandler.createSign(packageParams);
		log.info("my sign:" + sign);
		String xml = "<xml>" + "<appid>" + session.getAppId()
				+ "</appid>" + "<mch_id>" + session.getMch_id()
				+ "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>"
				+ "<sign>" + sign + "</sign>" + "<body><![CDATA[" + body
				+ "]]></body>" + "<out_trade_no>" + tradeNo
				+ "</out_trade_no>" + "<attach>" + attach + "</attach>"
				+ "<total_fee>" + total_fee + "</total_fee>"
				+ "<spbill_create_ip>" + ip + "</spbill_create_ip>"
				+ "<notify_url>" + notifyUrl
				+ "</notify_url>" + "<trade_type>" + trade_type
				+ "</trade_type></xml>";
		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

		Map<String, String> prepaid = new GetWxOrderno().getResultMap(createOrderURL, xml);
		prepaid.put("timestamp", Long.toString(new Date().getTime()).substring(0, 10));
		
		StringBuffer sb = new StringBuffer();
		sb.append("appid="+prepaid.get("appid"));
		sb.append("&noncestr="+prepaid.get("nonce_str"));
		sb.append("&package=Sign=WXPay");
		sb.append("&partnerid="+prepaid.get("mch_id"));
		sb.append("&prepayid="+prepaid.get("prepay_id"));
		sb.append("&timestamp="+prepaid.get("timestamp"));
		sb.append("&key=" + session.getApiKey());
		
		sign = MD5Util.MD5Encode(sb.toString(), "utf-8").toUpperCase();
		prepaid.put("sign", sign);
		
		return prepaid;
	}

	/**
	 * 公众号支付
	 * 
	 * @param tradeNo
	 * @param amount
	 * @param attach
	 * @param body
	 * @param ip
	 * @param openId
	 * @return
	 * @date 2016年1月12日 上午10:30:55
	 */
	@SuppressWarnings("static-access")
	public String jsapiPay(String tradeNo, String amount, String attach, String body,
			String ip, String notifyUrl, String openId) {
		String trade_type = "JSAPI";
		// 商户号
		String mch_id = session.getMch_id();
		// 随机字符串
		String nonce_str = TenpayUtil.getNonceStr();
		String totalFee = String.valueOf(new BigDecimal(amount).multiply(
				new BigDecimal(100)).intValue());// 金额 微信是以分为单位的;
		SortedMap<String, String> packageParams = new TreeMap<String, String>();
		packageParams.put("appid", session.getAppId());
		packageParams.put("attach", attach);
		packageParams.put("mch_id", mch_id);
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("body", body);
		packageParams.put("out_trade_no", tradeNo);
		packageParams.put("total_fee", totalFee);
		packageParams.put("spbill_create_ip", ip);
		packageParams.put("notify_url", notifyUrl);
		packageParams.put("trade_type", trade_type);
		packageParams.put("openid", openId);

		RequestHandler reqHandler = new RequestHandler(null, null);
		reqHandler.init(session.getAppId(), session.getApiKey());

		String sign = reqHandler.createSign(packageParams);
		String xml = "<xml>" + "<appid>" + session.getAppId() + "</appid>"
				+ "<attach>" + attach + "</attach>" +"<mch_id>" + mch_id
				+ "</mch_id>" + "<nonce_str>" + nonce_str + "</nonce_str>"
				+ "<sign>" + sign + "</sign>" + "<body><![CDATA[" + body
				+ "]]></body>" + "<out_trade_no>" + tradeNo
				+ "</out_trade_no>" + "<total_fee>" + totalFee + "</total_fee>"
				+ "<spbill_create_ip>" + ip + "</spbill_create_ip>"
				+ "<notify_url>" + notifyUrl + "</notify_url>"
				+ "<trade_type>" + trade_type + "</trade_type>" + "<openid>"
				+ openId + "</openid>" + "</xml>";

		String createOrderURL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		Map<String, String> map = new GetWxOrderno().getPayNo(createOrderURL,
				xml);
		if (map.get("return_code").equals("FAIL")) {
			map.put("error", "true");
			map.put("errMsg", map.get("result_msg"));
			return JSONUtils.valueToString(map);
		}
		String prepay_id = map.get("prepay_id");
		log.info("获取到的预支付ID：" + prepay_id);

		// 获取prepay_id后，拼接最后请求支付所需要的package
		SortedMap<String, String> finalpackage = new TreeMap<String, String>();
		String timestamp = TenpayUtil.getTimeStamp();
		String packages = "prepay_id=" + prepay_id;
		finalpackage.put("appId", session.getAppId());
		finalpackage.put("timeStamp", timestamp);
		finalpackage.put("nonceStr", nonce_str);
		finalpackage.put("package", packages);
		finalpackage.put("signType", "MD5");
		finalpackage.put("paySign", reqHandler.createSign(finalpackage));
		finalpackage.put("packages", packages);
		return JSONUtils.valueToString(finalpackage);
	}
	
	

}
