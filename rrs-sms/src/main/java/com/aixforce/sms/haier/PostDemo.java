package com.aixforce.sms.haier;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostDemo {
    public PostDemo() {
    }

    public static void main(String[] args) {
        try {
            String urlstr = "http://221.179.180.137:9836/HttpApi_Simple/submitMessage";

            URL url = new URL(urlstr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "close");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(5000);

            conn.connect();

            //haier
            String inputXML = "<?xml version=\"1.0\" encoding=\"GBK\"?>" +
                    "<CoreSMS>" +
                    "<OperID>goodaysh</OperID>" +
                    "<OperPass>good!!!</OperPass>" +
                    "<Action>Submit</Action>" +
                    "<Category>0</Category>" +
                    "<Body>" +
                    "<SendTime>20110421120000</SendTime>" +
                    "<AppendID></AppendID>" +
                    "<Message>" +
                    "<DesMobile>13524677753</DesMobile>" +
                    "<Content>20110914Sendtype=中文中文字符字符</Content>" +
                    "<SendType></SendType>" +
                    "</Message>" +


                    "</Body>" +
                    "</CoreSMS>";

            byte[] b = inputXML.getBytes("GBK");
            OutputStream os = conn.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            dos.write(b);
            dos.flush();
            os.close();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            int r = bis.read();
            while (r >= 0) {
                bos.write(r);
                r = bis.read();
            }
            String outputXML = new String(bos.toByteArray(), "UTF-8");
            is.close();

            conn.disconnect();
            System.out.println("====" + outputXML);
        } catch (IOException ex2) {
            System.out.println(" exe IOException:" + ex2.toString());
        }
    }
}