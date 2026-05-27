package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/test")
    public Result<Void> testMail(@RequestBody Map<String, String> request) {
        String toEmail = request.get("to");
        if (toEmail == null || toEmail.trim().isEmpty()) {
            return Result.error("收件人邮箱不能为空");
        }
        return mailService.sendTestMail(toEmail);
    }

    @PostMapping("/send")
    public Result<Void> sendMail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String content = request.get("content");
        
        if (to == null || to.trim().isEmpty()) {
            return Result.error("收件人邮箱不能为空");
        }
        if (subject == null || subject.trim().isEmpty()) {
            return Result.error("邮件主题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("邮件内容不能为空");
        }
        
        return mailService.sendMail(to, subject, content);
    }
}