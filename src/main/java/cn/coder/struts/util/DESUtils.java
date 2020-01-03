package cn.coder.struts.util;

import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DESUtils {
	private final static String DES = "DES";
	private final static String CHARSET = "utf-8";

	/**
	 * DES加密
	 * 
	 * @param data
	 *            要加密的字符串
	 * @param key
	 *            加密key
	 * @return 加密后字符串
	 * @throws Exception
	 *             异常
	 */
	public static String encrypt(String data, String key) throws Exception {
		if (data == null)
			return null;
		byte[] bt = encrypt(data.getBytes(CHARSET), key.getBytes(CHARSET));
		return Base64.getEncoder().encodeToString(bt);
	}

	/**
	 * DES解密
	 * 
	 * @param data
	 *            要解密的字符串
	 * @param key
	 *            解密key
	 * @return 解密后的字符串
	 * @throws Exception
	 *             异常
	 */
	public static String decrypt(String data, String key) throws Exception {
		if (data == null)
			return null;
		byte[] buf = Base64.getDecoder().decode(data);
		return new String(decrypt(buf, key.getBytes(CHARSET)), CHARSET);
	}

	/**
	 * 解密token为字符串数组
	 * @param token 要解密的token
	 * @param key 秘钥
	 * @return 解密后数组
	 */
	public static String[] decryptToken(String token, String key) {
		if (token == null || key == null)
			return new String[0];
		try {
			if (token.contains("%"))
				token = URLDecoder.decode(token, CHARSET);
			return decrypt(token, key).split(",");
		} catch (Exception e) {
			return new String[0];
		}
	}

	private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom sr = new SecureRandom();

		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);

		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance(DES);

		// 用密钥初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

		return cipher.doFinal(data);
	}

	private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom sr = new SecureRandom();

		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);

		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance(DES);

		// 用密钥初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

		return cipher.doFinal(data);
	}
}
