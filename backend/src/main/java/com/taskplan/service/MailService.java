package com.taskplan.service;

import com.taskplan.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class MailService {

    @Autowired
    private SysConfigService sysConfigService;

    private static final String KEY_MAIL_HOST = "mail.host";
    private static final String KEY_MAIL_PORT = "mail.port";
    private static final String KEY_MAIL_USERNAME = "mail.username";
    private static final String KEY_MAIL_PASSWORD = "mail.password";
    private static final String KEY_MAIL_FROM = "mail.from";
    private static final String KEY_MAIL_SSL = "mail.ssl";

    private JavaMailSender getMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(sysConfigService.getConfigValue(KEY_MAIL_HOST));
        sender.setPort(Integer.parseInt(sysConfigService.getConfigValue(KEY_MAIL_PORT, "587")));
        sender.setUsername(sysConfigService.getConfigValue(KEY_MAIL_USERNAME));
        sender.setPassword(sysConfigService.getConfigValue(KEY_MAIL_PASSWORD));

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        
        boolean sslEnabled = Boolean.parseBoolean(sysConfigService.getConfigValue(KEY_MAIL_SSL, "true"));
        if (sslEnabled) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.enable", "true");
        }

        sender.setJavaMailProperties(props);
        return sender;
    }

    public Result<Void> sendTestMail(String toEmail) {
        try {
            String username = sysConfigService.getConfigValue(KEY_MAIL_USERNAME);
            String password = sysConfigService.getConfigValue(KEY_MAIL_PASSWORD);
            String from = sysConfigService.getConfigValue(KEY_MAIL_FROM);

            if (username == null || username.trim().isEmpty()) {
                return Result.error("邮件服务器用户名未配置");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("邮件服务器密码未配置");
            }
            if (from == null || from.trim().isEmpty()) {
                return Result.error("发件人邮箱未配置");
            }

            JavaMailSender sender = getMailSender();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("任务调度系统 - 邮件测试");
            message.setText("这是一封测试邮件，用于验证邮件服务器配置是否正确。");

            sender.send(message);
            
            log.info("测试邮件发送成功，收件人: {}", toEmail);
            return Result.success();
        } catch (Exception e) {
            log.error("发送测试邮件失败: {}", e.getMessage());
            return Result.error("发送邮件失败: " + e.getMessage());
        }
    }

    public Result<Void> sendMail(String to, String subject, String content) {
        try {
            String from = sysConfigService.getConfigValue(KEY_MAIL_FROM);
            
            JavaMailSender sender = getMailSender();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            sender.send(message);
            
            log.info("邮件发送成功，收件人: {}", to);
            return Result.success();
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            return Result.error("发送邮件失败: " + e.getMessage());
        }
    }
}