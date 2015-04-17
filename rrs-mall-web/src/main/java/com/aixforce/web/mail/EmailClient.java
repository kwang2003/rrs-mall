package com.aixforce.web.mail;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-03-06
 */
public class EmailClient {

    private final static Logger log = LoggerFactory.getLogger(Emails.class);
    private final JavaMailSenderImpl javaMailSender;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final String domain;

    public EmailClient(JavaMailSenderImpl javaMailSender, String domain) {
        this.javaMailSender = javaMailSender;
        this.domain = domain;
    }

    public void send(final String from, final String to, String subject, String html, String... ccList) {
        try {
            final MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, Charsets.UTF_8.name());
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setTo(to);
            helper.setFrom(!Strings.isNullOrEmpty(from) ? from : "noreply@" + domain);
            if (ccList != null && ccList.length > 0) {
                helper.setCc(ccList);
            }
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        javaMailSender.send(mimeMessage);
                    } catch (Exception e) {
                        log.error("failed to send mail from ({}) to ({}) ,cause: {}", from, to , e.getMessage());
                    }
                }
            });
        } catch (MessagingException e) {
            log.error("send mail to {} failed,cause:{}", to, Throwables.getStackTraceAsString(e));
        }
    }
}
