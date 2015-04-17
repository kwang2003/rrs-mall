package com.aixforce.web.mail;


import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.redis.utils.KeyUtils;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import redis.clients.jedis.Jedis;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-03-06
 */
@Controller
@RequestMapping("/api/user/mail")
public class Emails {

    private final static Logger log = LoggerFactory.getLogger(Emails.class);
    private final JedisTemplate jedisTemplate;
    private final AccountService<User> accountService;
    private final EmailClient emailClient;
    private final HashFunction md5 = Hashing.md5();
    private final MessageSources messageSources;

    @Value("#{app.domain}")
    private String domain;

    @Value("#{app.mainSite}")
    private String mainSite;

    @Autowired
    public Emails(JedisTemplate jedisTemplate, AccountService<User> accountService, EmailClient emailClient, MessageSources messageSources) throws Exception {
        this.jedisTemplate = jedisTemplate;
        this.accountService = accountService;
        this.emailClient = emailClient;
        this.messageSources = messageSources;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String bind(@RequestParam("mail") final String mail) {
        final Long userId = UserUtil.getUserId();

        Response<User> ur = accountService.findUserById(userId);
        if(!ur.isSuccess()){
            log.error("failed to find user(id={}), error code:{}",userId, ur.getError());
            throw new JsonResponseException(500,messageSources.get("user.not.found"));
        }
        User user = ur.getResult();
        if(!Strings.isNullOrEmpty(user.getEmail())){
            log.error("user(id={}) has bind email({})",userId, user.getEmail());
            throw new JsonResponseException(500, messageSources.get("user.email.bind"));
        }

        Response<User> exist = accountService.findUserBy(mail, LoginType.EMAIL);

        if(exist.isSuccess() && exist.getResult()!=null){
            log.error("email {} has been used. ", mail);
            throw new JsonResponseException(500, messageSources.get("user.email.duplicated"));
        }

        try {
            jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
                @Override
                public void action(Jedis jedis) {
                    String token = md5.hashString(UUID.randomUUID().toString(), Charsets.UTF_8).toString().substring(0, 4);
                    jedis.setex(KeyUtils.mailBinding(userId, mail), (int) TimeUnit.DAYS.toSeconds(1), token);
                    String from = "noreply@" + domain;
                    String subject = "请激活您的账号";
                    UriComponents uriComponents =
                            UriComponentsBuilder.fromUriString("http://" + mainSite + "/api/user/mail?mail={mail}&token={token}").build();
                    String uri = uriComponents.expand(mail, token).encode().toUri().toString();
                    log.debug("mail activate uri:{}",uri);
                    String content = MessageFormat.format("请点击<a href=\"{0}\">{1}</a>激活您的邮箱账号,如不是您本人操作, 请忽略", uri, uri);
                    emailClient.send(from, mail, subject, content);
                }
            });
        } catch (Exception e) {
            log.error("failed to send email to {}, cause:{}", mail, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, messageSources.get("mail.send.fail"));
        }
        return "ok";
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String activate(@RequestParam("mail") final String mail, @RequestParam("token") String token) {
        final Long userId = UserUtil.getUserId();
        final String expectedToken = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                String key = KeyUtils.mailBinding(userId, mail);
                String token = jedis.get(key);
                jedis.del(key);
                return token;
            }
        });
        if (Strings.isNullOrEmpty(expectedToken)) {
            log.error("no mail token found for user(id={}) and mail({})", userId, mail);
            throw new JsonResponseException(400, messageSources.get("token.mismatch"));
        }
        if (!Objects.equal(token, expectedToken)) {
            log.error("mail token mismatch for user(id={}) and mail({}), expected token: {}, actual token :{} ",
                    userId, mail, expectedToken, token);
            throw new JsonResponseException(400, messageSources.get("token.mismatch"));
        }
        User u = new User();
        u.setId(userId);
        u.setEmail(mail);
        Response<Boolean> r  = accountService.updateUser(u);
        if(!r.isSuccess()){
            log.error("failed to update user email ({}) , error code:{} ",mail, r.getError());
            throw new JsonResponseException(500, messageSources.get("user.update.fail")) ;
        }
        return "redirect:/user/email-success";
    }

}
