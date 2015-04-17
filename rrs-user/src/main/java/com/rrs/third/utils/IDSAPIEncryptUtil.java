package com.rrs.third.utils;

import org.apache.log4j.Logger;

import java.security.MessageDigest;

public class IDSAPIEncryptUtil {

	private static final Logger logger = Logger.getLogger(IDSAPIClient.class);

	/**
	 * 
	 * 对指定的字符串进行加密
	 * 
	 * @param data 需要加密的数据
	 * 
	 * @param digestAlgorithm 签名算法
	 * @param secretKey 密钥
	 * @return 返回签名和加密数据组成的串
	 * 
	 * @throws Exception 
	 */
	public static String encrypt(String data, String digestAlgorithm,
			String secretKey) throws Exception {
		// 2、根据拼成的整串生成消息摘要签名
		MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
		md.update(data.getBytes("UTF-8"));
		byte[] digestByte = md.digest();
		String digest = StringHelper.toString(digestByte);
		if (logger.isDebugEnabled()) {
			logger.debug("caller create digest is [" + digest
					+ "] by digestAlgorithm [" + digestAlgorithm
					+ "] , secretKey [" + secretKey + "] !");
		}
		// 3、对拼成的整串做base64编码
		String dataAfterBase64Encode = Base64Util.encode(data, "UTF-8");
		if (logger.isDebugEnabled()) {
			logger.debug("after base64 encode data is [" + dataAfterBase64Encode
					+ "] , before base64 encode data is [" + data + "] !");
		}
		// 4、对base64编码后的消息做DES加密
		String dataAfterDESEncode = DesEncryptUtil.encryptToHex(
                dataAfterBase64Encode.getBytes("UTF-8"), secretKey);
		if (logger.isDebugEnabled()) {
			logger.debug("after des encrypt data is [" + dataAfterDESEncode
					+ "] , secretKey is [" + secretKey + "] !");
		}
		// 5、将消息摘要与加密后的数据拼成整串 格式：摘要签名&加密后的数据
		String finalData = digest + "&" + dataAfterDESEncode;
		if (logger.isDebugEnabled()) {
			logger.debug("caller post ids encrypted data is [" + finalData
					+ "] , digest is [" + digest
					+ "] , dataAfterDESEncode is [" + dataAfterDESEncode
					+ "] !");
		}
		return finalData;
	}

}
