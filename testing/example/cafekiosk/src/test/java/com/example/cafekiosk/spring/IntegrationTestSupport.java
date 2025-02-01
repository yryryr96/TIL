package com.example.cafekiosk.spring;

import com.example.cafekiosk.spring.client.mail.MailSendClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles(value = "test")
@SpringBootTest
public abstract class IntegrationTestSupport {

    @MockitoBean
    protected MailSendClient mailSendClient;
}
