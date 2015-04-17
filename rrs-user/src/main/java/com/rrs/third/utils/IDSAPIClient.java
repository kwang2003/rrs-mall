package com.rrs.third.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import java.security.MessageDigest;

/**
 * 加密调用IDS API 封装类
 * 
 * @author TRS
 * 
 */
public class IDSAPIClient {

	/**
	 * 接口服务地址 （需要到具体到接口地址）
	 */
	private String serviceUrl;

	private String secretKey;

	private String digestAlgorithm;

	private String responseType;

	private static final Logger logger = Logger.getLogger(IDSAPIClient.class);

	/**
	 * 错误提示：密钥为空
	 */
	private static final String ERROR_SECRETKEY_ISEMPTY = "error.secretkey.isempty";

	/**
	 * 错误提示：消息摘要签名为空
	 */
	private static final String ERROR_DIGESTALGORITHM_ISEMPTY = "error.digestalgorithm.isempty";

	/**
	 * 错误提示：消息摘要不合法
	 */
	private static final String ERRPR_DIGEST_ILLEGALITY = "error.digest.illegality";

	/**
	 * 错误提示：IDS响应格式不合法
	 */
	private static final String ERROR_RESPONSE_ILLEGALITY = "error.response.illegality";

	/**
	 * 错误提示：调用IDS接口发送异常(网络异常等)
	 */
	private static final String ERROR_CALLING_EXCEPTION = "error.calling.exception";

	/**
	 * 构造函数
	 * 
	 * @param serviceUrl
	 *            接口服务url
	 * @param secretKey
	 *            密钥
	 * @param digestAlgorithm
	 *            签名算法 如：MD5，SHA等（注意区分大小写）；
	 * @param responseType
	 *            响应结果类型 如：xml，json 默认为xml响应格式
	 */
	public IDSAPIClient(String serviceUrl, String secretKey,
			String digestAlgorithm, String responseType) {
		this.serviceUrl = serviceUrl;
		this.secretKey = secretKey;
		this.digestAlgorithm = digestAlgorithm;
		this.responseType = responseType;
	}

	/**
	 * 执行调用IDS服务接口 <br>
	 * 
	 * 说明：<br>
	 * 
	 * 1)如果参数合法，如签名算法为空或者密钥为空将抛异常。调用端自行捕获
	 * 
	 * @param appName
	 *            应用名
	 * @param data
	 *            拼成整串的参数信息
	 * @return 如果调用接口正常，则返回明文响应结果，否则抛出异常
	 * @throws Exception
	 */
	public String processor(String appName, String data) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("primitive parameters from caller serviceUrl = "
					+ serviceUrl + " , secretKey = " + secretKey
					+ " , digestAlgorithm = " + digestAlgorithm
					+ " , appName = " + appName + " , data = " + data
					+ " , responseType = " + responseType);
		}

		if (StringHelper.isEmpty(secretKey)) {
			logger.error("secretKey [" + secretKey
					+ "] is null , can not call ids api !");
			throw new Exception(ERROR_SECRETKEY_ISEMPTY);
		}

		if (StringHelper.isEmpty(digestAlgorithm)) {
			logger.error("digestAlgorithm [" + digestAlgorithm
					+ "] is null , can not call ids api !");
			throw new Exception(ERROR_DIGESTALGORITHM_ISEMPTY);
		}

		PostMethod methodPost = new PostMethod(serviceUrl);
		if (isHttpSSOAPI()) {
			methodPost.addParameter("coAppName", appName);
		} else {
			methodPost.addParameter("appName", appName);
		}
		methodPost.addParameter("type", responseType);

		String encryptedData = "";
		try {
			encryptedData = IDSAPIEncryptUtil.encrypt(data, digestAlgorithm, secretKey);
			System.out.println("encryptedData==========="+encryptedData);
		} catch (Exception e) {
			logger.error("encrypt data failed data, digestAlgorithm, secretKey =("
					+ data + "," + digestAlgorithm + "," + secretKey + ") !", e);
			throw e;
		}

		methodPost.addParameter("data", encryptedData);

		if (logger.isDebugEnabled()) {
			logger.debug("final to post ids request is [" + methodPost + "] !");
		}

		HttpClient httpClient = new HttpClient();
		try {
			httpClient.executeMethod(methodPost);
		} catch (Exception e) {
			logger.error("calling ids api has exception !", e);
			throw new Exception(ERROR_CALLING_EXCEPTION, e);
		}
		String responsePost = new String(methodPost.getResponseBody(), "UTF-8");
//		System.out.println("responsePost----------"+responsePost);
		if (logger.isDebugEnabled()) {
			logger.debug("original response from ids is [" + responsePost + "] !");
		}

		// 6、开始解析响应结果
		// 7、拆分摘要签名和加密过的响应数据
		String[] digestAndResult = StringHelper.split(responsePost, "&");
		if (digestAndResult.length != 2) {
			logger.error("response from ids format is illegality ! after split is digestAndResult ["
					+ digestAndResult + "]");
			throw new Exception(ERROR_RESPONSE_ILLEGALITY);
		}
		String digestOfServer = digestAndResult[0];
		String result = digestAndResult[1];
		// 8、对响应结果进行解密
		String afterDESResult = DesEncryptUtil.decrypt(result, secretKey);
		if (logger.isDebugEnabled()) {
			logger.debug("after des decrypt response is [" + afterDESResult
					+ "] , decrypt secretKey is [" + secretKey + "] !");
		}

		// 9、对解密后的结果做base64反解
		String plaintextResponse = Base64Util.decode(afterDESResult, "UTF-8");
		if (logger.isDebugEnabled()) {
			logger.debug("after base64 decode plaintext response is ["
					+ plaintextResponse
					+ "] , before base64 decode response is [" + afterDESResult
					+ "] !");
		}

		// 10、本地生成消息摘要与IDS返回的摘要对比，如果不相等则说明数据在传输过程中被篡改过
		MessageDigest sd = MessageDigest.getInstance(digestAlgorithm);
		sd.update(plaintextResponse.getBytes("UTF-8"));
		String digestOfAgent = StringHelper.toString(sd.digest());
		if (false == digestOfAgent.equals(digestOfServer)) {
			logger.error("response from ids digest [" + digestOfServer
					+ "] not eq client create digest [" + digestOfAgent + "] !");
			throw new Exception(ERRPR_DIGEST_ILLEGALITY);
		}
		return plaintextResponse;
	}

	/**
	 * 是否是httpssoapi
	 * 
	 * @return
	 */
	private boolean isHttpSSOAPI() {
		return serviceUrl.contains("idsServiceType=httpssoservice");
	}
}
