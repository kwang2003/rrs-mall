package com.rrs.third.service;

import com.aixforce.user.model.User;
import com.aixforce.user.mysql.UserDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrs.third.common.CommonUtils;
import com.rrs.third.common.ThirdPartEnum;
import com.rrs.third.dao.ThirdPartLoginDao;
import com.rrs.third.model.ThirdUser;
import com.rrs.third.utils.IDSAPIClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaop01 on 2014/9/2.
 */
@Service
public class ThirdPartLoginServiceImpl implements  ThirdPartLoginService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private ThirdPartLoginDao thirdPartLoginDao;

    @Override
    public String process(String source,String env,String ssoSessionId,String sessionId) throws Exception{
//        HttpClient client = HttpClientBuilder.create().build();
        String username = null;
        if("haier".equals(source)){
            StringBuffer bf = new StringBuffer();
            bf.append("ssoSessionId=").append(ssoSessionId.split("_")[0]).append("&");
            bf.append("coSessionId=").append(sessionId);
            String data =bf.toString();
            // 2、调用ids接口
            IDSAPIClient client = new IDSAPIClient(CommonUtils.getHaierApiParams(env).getUrl(),CommonUtils.getHaierApiParams(env).getSecurityType(), "MD5","json");
            // 从ids得到的明文响应结果
            String responseText = "";
            try {
                responseText = client.processor("rrs", data);
                // responseText = "{\"serviceName\":\"findUserBySSOID\",\"code\":200,\"data\":{\"sourceName\":\"ids_internal\",\"user\":{\"userId\":\"3540656\",\"IDSEXT_EXTRALABEL\":\"\",\"userName\":\"bianxinhui\",\"uuid\":\"95e53665-c099-423d-a1d8-46bc2d3d99b7\"}}}";;
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> dataMap = mapper.readValue(responseText, Map.class);
                if(dataMap.get("code").toString().equals("200")){ // 数据获取成功
                    String text = dataMap.get("data").toString();
                    String[] values = text.split(",");
                    // 解析数据
                    for(String v : values){
                        if(v.contains("userName")){
                           username = v.split("=")[1];
                            break;
                        }
                    }
                }else{
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO 自行处理
            }
        }else if("ehaier".equals(source)){
            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(CommonUtils.getEhaierApiParams(env).getUrl());
            method.addParameter("ssoSessionId",ssoSessionId);
            method.addParameter("secretKey",CommonUtils.getEhaierApiParams(env).getSecretKey());
            method.addParameter("coAppName",CommonUtils.getEhaierApiParams(env).getCoAppName());
            int requestStatus = client.executeMethod(method);
            if(requestStatus==200){
                String xmlText = new String(method.getResponseBody(),"UTF-8");
                List<Node> codeList = (List<Node>) CommonUtils.getXmlValue(xmlText, "/idsHttpSSOServiceResponse/responseHead/responseCode");
                if(codeList!=null&&codeList.size()>0){
                    String code = codeList.get(0).getText();
                    if("200".equals(code)){
                        // 接口用户信息
                        List<Node> nodeList = (List<Node>) CommonUtils.getXmlValue(xmlText, "/idsHttpSSOServiceResponse/responseBody/responseContent/userName");
                        username = (nodeList!=null&&nodeList.size()>0)?nodeList.get(0).getText():"";
                        return username;
                    }
                }
            }else{
                return null;
            }
        }
        return username;
    }

    @Override
    public boolean isExistsByUserName(String username) {
        ThirdUser tu = new ThirdUser();
        tu.setSourceName(username);
        return this.thirdPartLoginDao.isExists(tu);
    }
    /**
     * 本接口完成RRS系统用户注册以及第三方用户信息的保存
     * @param user
     * @param thirdPartName
     */
    @Transactional
    @Override
    public void saveUser(User user,String thirdPartName) {
        ThirdUser tu = new ThirdUser();
        tu.setSourceName(user.getName());
        if(thirdPartName.equalsIgnoreCase(ThirdPartEnum.HAIER.name())) {
            user.setName(ThirdPartEnum.HAIER.name() + "_" + CommonUtils.getTime());
        }else if(thirdPartName.equalsIgnoreCase(ThirdPartEnum.EHAIER.name())){
            user.setName(ThirdPartEnum.EHAIER.name() + "_" + CommonUtils.getTime());
        }else{
            // 等待其他第三方平台接入
        }
        user.setStatus(1);
        user.setType(1);
        user.setEncryptedPassword("haier_not_init");
        this.userDao.create(user);
        tu.setRrsUserName(user.getName());
        this.thirdPartLoginDao.saveThirdUser(tu);
    }

    @Override
    public ThirdUser findBySourceName(String sourceName) {
        ThirdUser tu = new ThirdUser();
        tu.setSourceName(sourceName);
        return this.thirdPartLoginDao.findThirdUserBySourceName(tu);
    }

    @Override
    public User findByName(String name) {
        return this.userDao.findByName(name);
    }
}
