package com.demo.publishCenter.services;


import com.demo.publishCenter.util.Util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/9/22.
 */
public class Receiver {

	private InputStream is;
	private OutputStream os;
	private boolean isFirstBlock;
	private boolean isLastBlock;
	private int blockNum;
	private byte[] block;
	private Util util = new Util();
	private List<byte[]> list = new ArrayList();
	private Sender sender = new Sender();

	public Receiver(InputStream is, OutputStream os) {
		this.is = is;
		this.os = os;
	}

	/**
	 * 工控机端接受数据帧，并将接受到的帧转换为字符串
	 * 打印出来
	 */
	public String receiveFramesAndPrintJson(){
		String json;
		while (true) {
			byte[] frame = receiveFrame();
			if (isValid(frame)) {
				System.out.println("有效帧！");
				parseFrame(frame);
				responseSuccess();
				if (isFirstBlock()) {
					list = new ArrayList();
					list.add(getBlock());
					if (isLastBlock()) {
						json = convertByte2String(list);
						System.out.println(json);
						break;
					}
				} else {
					list.add(getBlock());
					if (isLastBlock()) {
						json = convertByte2String(list);
						System.out.println(json);
						break;
					}
				}
			} else {
				System.out.println("无效帧！");
				responseFailed();
			}
		}
		return json;
	}

	/**
	 * 将获得的帧数据转为string字符串
	 */
	public void receiveFramesAndSendJson() {
		while (true) {
			byte[] frame = receiveFrame();
			if (isValid(frame)) {
				System.out.println("有效帧！");
				parseFrame(frame);
				responseSuccess();
				if (isFirstBlock()) {
					list = new ArrayList();
					list.add(getBlock());
					if (isLastBlock()) {
						sender.distributeConfigJson(convertByte2String(list));
						break;
					}
				} else {
					list.add(getBlock());
					if (isLastBlock()) {
						sender.distributeConfigJson(convertByte2String(list));
						break;
					}
				}
			} else {
				System.out.println("无效帧！");
				responseFailed();
			}
		}
	}

	/**
	 * 读取一帧数据
	 *
	 * @return 返回帧
	 */
	public byte[] receiveFrame() {
		byte[] frame = {};
		try {
			byte[] first_byte = new byte[]{(byte) this.is.read()};
			int available = this.is.available();
			byte[] data_suffix = new byte[available];
			frame = new byte[available + 1];
			this.is.read(data_suffix);
			System.arraycopy(first_byte, 0, frame, 0, 1);
			System.arraycopy(data_suffix, 0, frame, 1, available);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return frame;
	}

	/**
	 * 判断帧是否有效
	 *
	 * @param frame
	 * @return
	 */
	public boolean isValid(byte[] frame) {
		int len = frame.length;
		if (frame[0] == (byte) 0xaa && frame[1] == (byte) 0x51 && frame[len - 3] == (byte) 0xcc) {
			byte[] suffix = new byte[frame.length - 2];
			System.arraycopy(frame, 0, suffix, 0, suffix.length);
			byte[] crc = util.Pub_CalcCRC(suffix);
			if (crc[0] == frame[len - 2] && crc[1] == frame[len - 1]) {
				return true;
			} else {
				System.out.println("数据帧校验码校验错误！");
				return false;
			}
		} else {
			System.out.println("数据帧格式不正确");
			return false;
		}
	}

	/**
	 * 解析帧
	 *
	 * @param frame 帧
	 */
	public void parseFrame(byte[] frame) {
		byte[] content = new byte[frame.length - 9];
		//判断是否是第一个数据块
		int block_num = util.conventByte2Int(frame[4], frame[5]);
		if (block_num == 1) {
			setFirstBlock(true);
		} else {
			setFirstBlock(false);
		}

		//设置数据块编号
		setBlockNum(block_num);

		//判断是否是最后一个数据块
		int block_size = util.conventByte2Int(frame[2], frame[3]);
		int data_size = frame.length - 9;
		if (block_size > data_size) {
			setLastBlock(true);
		} else {
			setLastBlock(false);
		}

		//获取帧中的数据块
		System.arraycopy(frame, 6, content, 0, frame.length - 9);
		setBlock(unescape(content));
	}

	/**
	 * 响应帧，表示数据块发送成功！
	 */
	public void responseSuccess() {
		try {
			this.os.write(response((byte) 1));
			this.os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 响应帧，表示数据块发送失败！
	 */
	public void responseFailed() {
		try {
			this.os.write(response((byte) 0));
			this.os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 响应数据块发送成功或失败
	 *
	 * @param success
	 * @return
	 */
	public byte[] response(byte success) {
		byte start = (byte) 0xaa;
		byte end = (byte) 0xcc;
		byte cmd = (byte) 0x52;
		byte[] blockNum = util.convertInt2Byte(getBlockNum());
		byte data = success;
		byte[] suffix = {start, cmd, blockNum[0], blockNum[1], data, end};
		byte[] crc = util.Pub_CalcCRC(suffix);
		byte[] frame = new byte[8];
		System.arraycopy(suffix, 0, frame, 0, suffix.length);
		System.arraycopy(crc, 0, frame, suffix.length, crc.length);
		return frame;
	}

	/**
	 * 将byte[]数组转为字符串
	 *
	 * @param list 存放byte[]数组的列表
	 * @return 字符串
	 */
	public String convertByte2String(List<byte[]> list) {
		byte[] data = new byte[0];
		for (int i = 0; i < list.size(); i++) {
			int start = data.length;
			data = Arrays.copyOf(data, start + list.get(i).length);
			System.arraycopy(list.get(i), 0, data, start, list.get(i).length);
		}
		String json = new String(data);
		return json;
	}

	/**
	 * 对数据块的内容进行反转义
	 *
	 * @param input 帧中的数据块
	 * @return 反转义后的内容
	 */
	public byte[] unescape(byte[] input) {
		byte[] output;
		int count = 0;
		int start = 0;
		for (int i = 0; i < input.length; i++) {
			if (input[i] == (byte) 0xee) {
				count++;
			}
		}
		output = new byte[input.length - count];
		for (int i = 0; i < input.length; i++) {
			if (input[i] == (byte) 0xee) {
				if (input[i + 1] == (byte) 0x0a) {
					output[start++] = (byte) 0xaa;
					i++;
				}
				if (input[i + 1] == (byte) 0x0c) {
					output[start++] = (byte) 0xcc;
					i++;
				}
				if (input[i + 1] == (byte) 0x0e) {
					output[start++] = (byte) 0xee;
					i++;
				}
			} else {
				output[start++] = input[i];
			}
		}
		return output;
	}

	public boolean isFirstBlock() {
		return isFirstBlock;
	}

	public void setFirstBlock(boolean firstBlock) {
		isFirstBlock = firstBlock;
	}

	public boolean isLastBlock() {
		return isLastBlock;
	}

	public void setLastBlock(boolean lastBlock) {
		isLastBlock = lastBlock;
	}

	public byte[] getBlock() {
		return block;
	}

	public void setBlock(byte[] block) {
		this.block = block;
	}

	public int getBlockNum() {
		return blockNum;
	}

	public void setBlockNum(int blockNum) {
		this.blockNum = blockNum;
	}
}
