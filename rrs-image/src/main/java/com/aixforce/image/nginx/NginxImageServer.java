package com.aixforce.image.nginx;

import com.aixforce.image.ImageServer;
import com.aixforce.image.exception.ImageDeleteException;
import com.aixforce.image.exception.ImageUploadException;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-02-11
 */
public class NginxImageServer implements ImageServer {

    private final static Logger log = LoggerFactory.getLogger(NginxImageServer.class);

    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("/yyyy/MM/dd/");

    public static final char SEPARATOR = '_';
    private final static Splitter splitter = Splitter.on(SEPARATOR).limit(2).omitEmptyStrings().trimResults();
    private final static HashFunction md5 = Hashing.md5();


    //图片上传路径
    @Value("#{app.imageUploadUrl}")
    private String imageUploadUrl;
    //图片读取路径
    @Value("#{app.imageBaseUrl}")
    private String imageBaseUrl;

    /**
     * @param fileName 文件名
     * @param file     文件
     * @return 文件上传后的相对路径
     */
    @Override
    public String write(String fileName, MultipartFile file) throws ImageUploadException {

        HttpRequest request = HttpRequest.post(imageUploadUrl);
        InputStream is = null;

        try {
            is = file.getInputStream();
            request.part("file", fileName, null, is);
            if (request.ok()) {
                return dtf.print(DateTime.now()) + fileName;
            } else {
                log.error("failed to upload file({}) to image server,http response code:{}, response body:{}",
                        fileName, request.code(), request.body());
                throw new ImageUploadException(request.code() + "");
            }
        } catch (Exception e) {
            log.error("upload to nginx file server failed, exception:", e);
            throw new ImageUploadException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore this fxxk
                }
            }
        }
    }

    /**
     * 处理原始文件名, 并返回新的文件名
     *
     * @param originalName 原始文件名
     * @param imageData    原始文件的字节数组
     * @return 新的文件名
     */
    @Override
    public String handleFileName(String originalName, byte[] imageData) {
        String ext = Files.getFileExtension(originalName);
        List<String> parts = splitter.splitToList(originalName);
        if(parts.size()==2) {
            String userId = parts.get(0);
            String originName = parts.get(1)+ UUID.randomUUID();
            return userId + SEPARATOR + md5.hashString(originName, Charsets.UTF_8).toString()+"."+ext;
        }else{
            return md5.hashString(originalName, Charsets.UTF_8).toString()+"."+ext;
        }
    }

    /**
     * 刪除文件
     *
     * @param fileName 文件名
     * @return 是否刪除成功
     */
    @Override
    public boolean delete(String fileName) throws ImageDeleteException {
        return false;
    }
}
