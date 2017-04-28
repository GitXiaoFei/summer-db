package cn.cerc.jdb.core;

public class SyncUpdateException extends Exception {
	private static final long serialVersionUID = -7421586617677073495L;
	/**
	 * 更新异常封装
	 * @author 林俐俊
	 * @Time 2017-4-28 16:53
	 * @param e 异常内容
	 */
	public SyncUpdateException(Exception e) {
		super(e.getMessage());
		this.addSuppressed(e);
	}

	public SyncUpdateException(String message) {
		super(message);
	}
}
