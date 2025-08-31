package com.biotrack.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${smtp.host}")
    private String smtpHost;

    @Value("${smtp.port}")
    private int smtpPort;

    @Value("${smtp.user}")
    private String smtpUser;

    @Value("${smtp.pass}")
    private String smtpPass;

    @Bean
    public JavaMailSender javaMailSender() {
        System.out.println("Creating JavaMailSender bean with host: " + smtpHost);
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);
        mailSender.setUsername(smtpUser);
        mailSender.setPassword(smtpPass);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        props.put("mail.smtp.ssl.trust", smtpHost);
        
        return mailSender;
    }
}