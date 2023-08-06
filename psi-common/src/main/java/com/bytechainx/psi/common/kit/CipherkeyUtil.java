package com.bytechainx.psi.common.kit;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 密码加密方法
 * 
 * @author defier.lai 2011-12-30 下午01:01:47
 * @version 1.0
 */
public class CipherkeyUtil {

	public static void main(String[] args) {
//		String sale = UUID.randomUUID().toString().replaceAll("-", StringUtils.EMPTY);
//		System.out.println(sale);
		System.out.println(CipherkeyUtil.encodePassword("shangbao123", "532f049d2a656b710e903db6192ecb0c0c1811f8"));
	}

	/**
	 * 对密码进行加密，先MD5加密，然后在使用加密后的字符串+SALT组合MD5再次加密
	 * 
	 * @param password
	 * @param salt
	 * @return
	 */
	public static String encodePassword(String password, String salt) {
		String str = DigestUtils.md5Hex(password) + ":::" + salt;
		return DigestUtils.md5Hex(str);
	}

	/**
	 * salt可以是时间戳+ID之类组合字符串
	 * 
	 * @param salt
	 * @return
	 */
	public static String encodeSalt(String salt) {
		return DigestUtils.sha1Hex(salt);
	}

	public static String getMd5(String srouce, int bit) {
		// 返回字符串
		String md5Str = null;
		try {
			// 操作字符串
			StringBuffer buf = new StringBuffer();
			MessageDigest md = MessageDigest.getInstance("MD5");

			// 添加要进行计算摘要的信息,使用 plainText 的 byte 数组更新摘要。
			md.update(srouce.getBytes());
			// 计算出摘要,完成哈希计算。
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				// 将整型 十进制 i 转换为16位，用十六进制参数表示的无符号整数值的字符串表示形式。
				buf.append(Integer.toHexString(i));
			}
			if (bit == 32) { // 32位的加密
				md5Str = buf.toString();
			} else if (bit == 16) { // 16位的加密
				md5Str = buf.toString().substring(8, 24);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return md5Str;
	}

	private static final String HMAC_SHA1 = "HmacSHA1";

	/**
	 * 生成签名数据
	 * 
	 * @param data 待加密的数据
	 * @param key  加密使用的key
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHmacsha1Signature(String data, String key) throws Exception {
		byte[] keyBytes = key.getBytes();
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1);
		Mac mac = Mac.getInstance(HMAC_SHA1);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(data.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : rawHmac) {
			sb.append(byteToHexString(b));
		}
		return sb.toString();
	}

	private static String byteToHexString(byte ib) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] ob = new char[2];
		ob[0] = Digit[(ib >>> 4) & 0X0f];
		ob[1] = Digit[ib & 0X0F];
		String s = new String(ob);
		return s;
	}

}
