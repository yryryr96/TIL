package com.example.cafekiosk.spring.client.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailSendClient {

    public boolean sendEmail(String fromEmail, String toEmail, String subject, String content) {

        log.info("메일 전송");
        throw new IllegalArgumentException("메일 전송");
//        return true;
    }

    public void a() {
        log.info("a");
    }

    public void b() {
        log.info("b");
    }

    public void c() {
        log.info("c");
    }
}
