package cn.coder.struts.util;

import java.security.MessageDigest;
import java.util.Formatter;

public class MD5 {

	public static String encodeByMD5(String str) {
		if (StringUtils.isEmpty(str))
			return str;
		try {
			// 创建具有指定算法名称的信息摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
			byte[] results = md.digest(str.getBytes("utf-8"));
			// 将得到的字节数组变成字符串返回
			return byteToHex(results);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return str;
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

}
