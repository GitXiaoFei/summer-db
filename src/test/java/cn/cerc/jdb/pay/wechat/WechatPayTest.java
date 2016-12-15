package cn.cerc.jdb.pay.wechat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cn.cerc.jdb.core.StubHandle;
import net.sf.json.util.JSONUtils;

public class WechatPayTest {
	private StubHandle handle;

	@Before
	public void setUp() throws Exception {
		handle = new StubHandle();
	}

	@Test
	public void test() {
		String corpNo = "727000";
		String orderNo = "20161104";
		String amount = "0.01";
		String body = "测试";
		String spbillCreateIp = "192.168.1.1";
		String notifyUrl = "http://115.28.150.165/forms/FrmWxMessage ";
		String openId = "oe4F_jqPHrRF8g2xnBjNH3KX5zY4";
		
		//WechatPay pay = new WechatPay(handle);
		WechatPay pay = new WechatPay(handle, corpNo);
		//微信公众号支付
		String json = pay.jsapiPay(orderNo, amount, "", body, spbillCreateIp, notifyUrl, openId);
		System.out.println(json);
		//APP支付
		Map<String, String> map = pay.appPay(orderNo, amount, body, "", spbillCreateIp, notifyUrl);
		System.out.println(JSONUtils.valueToString(map));
	}

}
