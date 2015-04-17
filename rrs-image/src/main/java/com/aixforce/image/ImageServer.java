package com.aixforce.image;

import com.aixforce.image.exception.ImageDeleteException;
import com.aixforce.image.exception.ImageUploadException;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * 图片服务API接口
 *
 * Author:  <a href="mailto:remindxiao@gmail.com">xiao</a>
 * Date: 2013-13-27
 */
public interface ImageServer {


    /**
     *
     * @param fileName  文件名
     * @param file      文件
     * @return  文件上传后的相对路径
     */
    String write(String fileName, MultipartFile file) throws ImageUploadException;


    /**
     * 处理原始文件名, 并返回新的文件名
     * @param originalName  原始文件名
     * @param imageData  原始文件的字节数组
     * @return  新的文件名
     */
    String handleFileName(String originalName, byte[] imageData);


    /**
     * 刪除文件
     *
     * @param fileName  文件名
     * @return 是否刪除成功
     */
    boolean delete(String fileName) throws ImageDeleteException;
}
