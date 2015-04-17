package com.rrs.kjtpay.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 签名验证
 */
public class VerifyClient {
    private static final Logger logger = LoggerFactory.getLogger(VerifyClient.class);

    static ITrus itrus = new ITrus();

    static{
        itrus.setCvmConfigFile(KjtConfig.cvmPath);
        itrus.setKeyPassword(KjtConfig.pfxKey);
        itrus.setPfxFileName(KjtConfig.pfxPath);
        itrus.init();
    }

    public static VerifyResult verifyBasic(String charset,String sigpType, Map<String, String> formattedParameters) throws Exception {
        return verifyBasic(charset, formattedParameters);
    }
    /**
     * @param charset
     * @param formattedParameters
     */
    public static VerifyResult verifyBasic(String charset, Map<String, String> formattedParameters) throws Exception {
        String signContent = KjtCore.createLinkString(KjtCore.paraFilter(formattedParameters), false);

        String signMsg = formattedParameters.get("sign");
        if (logger.isInfoEnabled()) {
            logger.info("verify signature: { content:" + signContent + ", signMsg:"+ signMsg+ "}");
        }

        VerifyResult result = verifyParameters(signContent, signMsg, charset);
        if (!result.isSuccess()) {
            logger.error(";request dosen't pass verify.");
            throw new Exception("验证签名失败");
        }

        String identityNo = formattedParameters.get("identity_no");
        if (result.isNeedPostCheck() && StringUtils.isNotBlank(identityNo)) {
            Map<String, Object> map = result.getInfo();
            if (map != null) {
                if (!identityNo.equals(map.get(VerifyResult.identityNo))) {
                    logger.error("签名验证错误"+identityNo+"数据新歌想"+map.get(VerifyResult.identityNo));
                    throw new Exception("验证 identityNo异常");
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("invoke verify end:" + result.isSuccess());
        }

        return result;
    }

    public  static VerifyResult verifyParameters(String content, String signature, String charset) throws Exception {
        if (signature != null) {
            signature = signature.replace(' ', '+');
        }

        VerifyResult result = new VerifyResult();

        try {

            result = itrus.verify(content, signature, null, charset);

        } catch (Exception e) {
            logger.error("verify failure for content:" + content + ",signature:" + signature , e);
            throw new Exception("验证签名异常：",e);
        }
        return result;
    }
}
