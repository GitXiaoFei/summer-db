package cn.cerc.jdb.pay.wechat.beans;

public class AccessTokenVo {
	
	private String token;// 获取到的凭证
	private int expiresIn;// 凭证有效时间，单位：秒
	private String ticket;//获取到的JSAPI凭证
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public int getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
	
}
