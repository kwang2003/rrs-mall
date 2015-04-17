package util;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by neusoft on 14-8-7.
 */
public class Sign {
    public static String buildParams(Map<String, Object> params, String paramKey) {
        String toVerify = "";
        String theKey = paramKey;
        // 将Map排序
        if (!(params instanceof SortedMap)) {
            params = new TreeMap<String, Object>(params);
        }
        // 拼接字符串 剔除值为空的键值对
        Iterator<String> iter = params.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            if(params.get(key) != null && params.get(key) != ""){
                toVerify += key + "=" + params.get(key) + "&";
            }
        }

        try {
            if(toVerify.length() > 0)
                toVerify = toVerify.substring(0, toVerify.length() - 1);
            //String sign = convertToMD5(toVerify + theKey);
            String sign = MD5Encoder(toVerify + theKey, "utf-8");
            return sign;
        } catch (Exception e) {
            throw new RuntimeException("failed to build params", e);
        }
    }

    public final static String MD5Encoder(String s, String charset) {
        try {
            byte[] btInput = s.getBytes(charset);
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < md.length; i++) {
                int val = ((int) md[i]) & 0xff;
                if (val < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(val));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }


}
