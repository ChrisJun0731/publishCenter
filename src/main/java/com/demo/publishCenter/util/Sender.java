package com.demo.publishCenter.util;

import java.io.OutputStream;

public class Sender {

	private OutputStream os;
	private MyUtil util = new MyUtil();

	public Sender(OutputStream os) {
		this.os = os;
	}

	/**
	 * 发送json字符串
	 * @param json json字符串
	 */
	public void sendJson(String json){
		send(combine(json.getBytes()));
	}

	/**
	 * 将一个字符串转义为字节数组
	 *
	 * @param input 转义前的字节数组
	 * @return 转义后的字节数组
	 */
	public byte[] escape(byte[] input) {
		int extra = 0;
		for (int i = 0; i < input.length; i++) {
			if (input[i] == (byte) 0xaa || input[i] == (byte) 0xcc || input[i] == 0xee) {
				extra++;
			}
		}
		byte[] output = input;
		if (extra != 0) {
			int start = 0;
			output = new byte[input.length + extra];
			for (int i = 0; i < input.length; i++) {
				if (input[i] == (byte) 0xaa || input[i] == (byte) 0xcc || input[i] == 0xee) {
					output[start++] = (byte) 0xee;
					output[start++] = input[i];
				}
			}
		}
		return output;
	}

	/**
	 * 组装帧数据
	 *
	 * @param frameBody 帧体
	 * @return 帧数据
	 */
	public byte[] combine(byte[] frameBody) {
		byte[] escapeFrameBody = escape(frameBody);
		byte[] frame = new byte[3 + escapeFrameBody.length];
		byte[] crc_frame = new byte[5 + escapeFrameBody.length];
		byte[] start = new byte[]{(byte) 0xaa};
		byte[] cmd = new byte[]{(byte) 0x51};
		byte[] end = new byte[]{(byte) 0xcc};
		byte[] crc;
		System.arraycopy(start, 0, frame, 0, 1);
		System.arraycopy(cmd, 0, frame, 1, 1);
		System.arraycopy(escapeFrameBody, 0, frame, 2, escapeFrameBody.length);
		System.arraycopy(end, 0, frame, 2 + escapeFrameBody.length, 1);
		crc = util.Pub_CalcCRC(frame);
		System.arraycopy(frame, 0, crc_frame, 0, frame.length);
		System.arraycopy(crc, 0, crc_frame, frame.length, 2);
		return crc_frame;
	}

	/**
	 * 发送帧数据
	 *
	 * @param frame 帧数据
	 */
	public void send(byte[] frame) {
		try {
			this.os.write(frame);
			this.os.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
