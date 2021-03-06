package com.aixforce.web.utils;

import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.List;


/**
 * 采用bcd码转码解密加密后的二进制数据，之后加密解密统一使用base64编码，所以应该废弃，请使用RSACoder类。
 * @author zhangr01
 */
@Deprecated
public class RSAToBCDCoder {

	private static final String PRIVATE_KEY = "private";
	
	public static final String PUBLIC_KEY = "public";


    public static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

	/** 
     * 生成公钥和私钥 
     * @throws java.security.NoSuchAlgorithmException
     * 
     */  
    public static HashMap<String, Object> getKeys() throws NoSuchAlgorithmException{  
        HashMap<String, Object> map = new HashMap<String, Object>();  
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");  
        keyPairGen.initialize(1024);  
        KeyPair keyPair = keyPairGen.generateKeyPair();  
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();  
        map.put(PUBLIC_KEY, publicKey);  
        map.put(PRIVATE_KEY, privateKey);  
        return map;  
    }  
    /** 
     * 使用模和指数生成RSA公钥 
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA 
     * /None/NoPadding】 
     *  
     * @param modulus 
     *            模 
     * @param exponent 
     *            指数 
     * @return 
     */  
    public static RSAPublicKey getPublicKey(String modulus, String exponent) {  
        try {  
            BigInteger b1 = new BigInteger(modulus);  
            BigInteger b2 = new BigInteger(exponent);  
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);  
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }  
  
    /** 
     * 使用模和指数生成RSA私钥 
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA 
     * /None/NoPadding】 
     *  
     * @param modulus 
     *            模 
     * @param exponent 
     *            指数 
     * @return 
     */  
    public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {  
        try {  
            BigInteger b1 = new BigInteger(modulus);  
            BigInteger b2 = new BigInteger(exponent);  
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);  
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }

    
    /** 
     * 公钥加密 
     *  
     * @param data 
     * @param publicKey 
     * @return 
     * @throws Exception 
     */  
    public static String encryptByPublicKey(String data, RSAPublicKey publicKey)  
            throws Exception {  
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
        // 模长  
        int key_len = publicKey.getModulus().bitLength() / 8;  
        // 加密数据长度 <= 模长-11  
        List<byte[]> datas =  StringUtils.splitString(data, key_len - 11);  
       
        String mi = "";  
        //如果明文长度大于模长-11则要分组加密  
        byte[] all = null;
        for (byte[] s : datas) {  
        	byte[] mb = cipher.doFinal(s);
        	all = ArrayUtils.addAll(all, mb);
        }  
        
        mi = bcd2Str(all);
        return mi;  
    }  
  
    /** 
     * 私钥解密 
     *  
     * @param data 
     * @param privateKey 
     * @return 
     * @throws Exception 
     */  
    public static String decryptByPrivateKey(String data, RSAPrivateKey privateKey)  
            throws Exception {  
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);  
        //模长  
        int key_len = privateKey.getModulus().bitLength() / 8;  
        byte[] bytes = data.getBytes();  
        byte[] bcd = ASCII_To_BCD(bytes, bytes.length);  
      
        System.err.println(bcd.length);  
        //如果密文长度大于模长则要分组解密  
        String ming = "";  
        //byte[][] arrays = splitArray(bcd, key_len);  
        List<byte[]> arrays = StringUtils.splitByteArray(bcd, key_len);
        byte[] all = null;
        for(byte[] arr : arrays){  
        	byte[] mb = cipher.doFinal(arr);
        	all = ArrayUtils.addAll(all, mb);
        }  
        ming = new String(all);  
        return ming;  
    }  
    
    /** 
     * ASCII码转BCD码 
     *  
     */  
    public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {  
        byte[] bcd = new byte[asc_len / 2];  
        int j = 0;  
        for (int i = 0; i < (asc_len + 1) / 2; i++) {  
            bcd[i] = asc_to_bcd(ascii[j++]);  
            bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));  
        }  
        return bcd;  
    }  
    public static byte asc_to_bcd(byte asc) {  
        byte bcd;  
  
        if ((asc >= '0') && (asc <= '9'))  
            bcd = (byte) (asc - '0');  
        else if ((asc >= 'A') && (asc <= 'F'))  
            bcd = (byte) (asc - 'A' + 10);  
        else if ((asc >= 'a') && (asc <= 'f'))  
            bcd = (byte) (asc - 'a' + 10);  
        else  
            bcd = (byte) (asc - 48);  
        return bcd;  
    }  
    /** 
     * BCD转字符串 
     */  
    public static String bcd2Str(byte[] bytes) {  
        char temp[] = new char[bytes.length * 2], val;  
  
        for (int i = 0; i < bytes.length; i++) {  
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);  
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');  
  
            val = (char) (bytes[i] & 0x0f);  
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');  
        }  
        return new String(temp);  
    }  
    /** 
     * 拆分字符串 
     */  
    public static String[] splitString(String string, int len) {  
        int x = string.length() / len;  
        int y = string.length() % len;  
        int z = 0;  
        if (y != 0) {  
            z = 1;  
        }  
        String[] strings = new String[x + z];  
        String str = "";  
        for (int i=0; i<x+z; i++) {  
            if (i==x+z-1 && y!=0) {  
                str = string.substring(i*len, i*len+y);  
            }else{  
                str = string.substring(i*len, i*len+len);  
            }  
            strings[i] = str;  
        }  
        return strings;  
    }  
    /** 
     *拆分数组  
     */  
    public static byte[][] splitArray(byte[] data,int len){  
        int x = data.length / len;  
        int y = data.length % len;  
        int z = 0;  
        if(y!=0){  
            z = 1;  
        }  
        byte[][] arrays = new byte[x+z][];  
        byte[] arr;  
        for(int i=0; i<x+z; i++){  
            arr = new byte[len];  
            if(i==x+z-1 && y!=0){  
                System.arraycopy(data, i*len, arr, 0, y);  
            }else{  
                System.arraycopy(data, i*len, arr, 0, len);  
            }  
            arrays[i] = arr;  
        }  
        return arrays;  
    }  

    public String enctyptBypublishKey(String mingCode){
        //模
        String modulus = "116262705441292461767938684378745573385265261433835354533161260570850418836270718461079468519086567371916662924537118880790955592987214794462208450157461042923039456080271600322310596059357051099905562221749131076225607808428923699762172481968262836388564574671403872476197309991942349096313039457035406478567";
        //公钥指数
        String public_exponent = "65537";
//        //私钥指数
//        String private_exponent = "29771285269630439192968048602226348666425402709654468769939305657872831055896595773591034693185693175389557611724398874794906949685085351490986499390306404869838845033947919691721307992734678201041388835183007534056440442570362142578136442199597719747865426902120172091441361966348776310969207773895064659473";
//        System.out.println("modulus="+modulus);
//        System.out.println("public_exponent="+public_exponent);
//        System.out.println("private_exponent="+private_exponent);

        //明文
        //使用模和指数生成公钥和私钥
        RSAPublicKey pubKey = RSAToBCDCoder.getPublicKey(modulus, public_exponent);
//        RSAPrivateKey priKey = RSAToBCDCoder.getPrivateKey(modulus, private_exponent);
        //加密后的密文
        String mi = null;
        try {
            mi = RSAToBCDCoder.encryptByPublicKey(mingCode, pubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        //解密后的明文
//        String mingw = null;
//        try {
//            mingw = RSAToBCDCoder.decryptByPrivateKey(mi, priKey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.err.println("私钥解密明文: " + mingw);

        return mi;
    }

    public String decryptByPrivateKey(String miCode){
        // TODO Auto-generated method stub
        //模
        String modulus = "116262705441292461767938684378745573385265261433835354533161260570850418836270718461079468519086567371916662924537118880790955592987214794462208450157461042923039456080271600322310596059357051099905562221749131076225607808428923699762172481968262836388564574671403872476197309991942349096313039457035406478567";
        //公钥指数
        String public_exponent = "65537";
        //私钥指数
//        String private_exponent = "65537";
        RSAPrivateKey priKey = RSAToBCDCoder.getPrivateKey(modulus, public_exponent);
        String mingw = "";
        try {
            mingw = RSAToBCDCoder.decryptByPrivateKey(miCode, priKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mingw;
    }

//    public static void main(String[] args) throws Exception {
//        // TODO Auto-generated method stub
//       /* HashMap<String, Object> map = RSACoder.getKeys();
//        //生成公钥和私钥
//        RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
//        RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
//         */
//
//        //模
//        String modulus = "116262705441292461767938684378745573385265261433835354533161260570850418836270718461079468519086567371916662924537118880790955592987214794462208450157461042923039456080271600322310596059357051099905562221749131076225607808428923699762172481968262836388564574671403872476197309991942349096313039457035406478567";
//        //公钥指数
//        String public_exponent = "65537";
//        //私钥指数
//        String private_exponent = "29771285269630439192968048602226348666425402709654468769939305657872831055896595773591034693185693175389557611724398874794906949685085351490986499390306404869838845033947919691721307992734678201041388835183007534056440442570362142578136442199597719747865426902120172091441361966348776310969207773895064659473";
//        System.out.println("modulus="+modulus);
//        System.out.println("public_exponent="+public_exponent);
//        System.out.println("private_exponent="+private_exponent);
//
//        //明文
//        String ming = "{\"itemId\":\"123456\"}";
//        //使用模和指数生成公钥和私钥
//        RSAPublicKey pubKey = RSACoder.getPublicKey(modulus, public_exponent);
//        RSAPrivateKey priKey = RSACoder.getPrivateKey(modulus, private_exponent);
//        //加密后的密文
//        String mi = RSACoder.encryptByPublicKey(ming, pubKey);
//        System.err.println("公钥加密密文 : " + mi);
//        //解密后的明文
//        String mingw = RSACoder.decryptByPrivateKey(mi, priKey);
//        System.err.println("私钥解密明文: " + mingw);
//    }
}
