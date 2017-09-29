package com.demo.publishCenter.models;

import java.util.List;

/**
 * Created by Administrator on 2017/9/5.
 */
public class IPC {
	private String ip;
	private int[] ids;
	private String cmd;
	private Object data;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int[] getIds() {
		return ids;
	}

	public void setIds(int[] ids) {
		this.ids = ids;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
