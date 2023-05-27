package com.myrecipe.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.starttls.required", true);
//        properties.put("mail.smtp.ssl.enable", true);
//        properties.put("spring.mail.host", "smtp.abv.bg");
//        properties.put("spring.mail.port", 465);
//        properties.put("spring.mail.username", "my-recipes@abv.bg");
//        properties.put("spring.mail.password", "myrecipes123");
        properties.put("spring.mail.host", "smtp.gmail.com");
        properties.put("spring.mail.port", 587);
        properties.put("spring.mail.username", "goshanski.n@gmail.com");
        properties.put("spring.mail.password", "zuvwkyhjyoqydace");


        return mailSender;
    }
}
