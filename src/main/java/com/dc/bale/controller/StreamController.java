package com.dc.bale.controller;

import com.dc.bale.component.HttpClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@RequestMapping("/stream")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StreamController {
    @NonNull
    private HttpClient httpClient;

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String notifyStream() throws javax.mail.MessagingException {
        System.out.println("Motion detected, sending email");
        String ip = httpClient.get("http://checkip.amazonaws.com/").replace("\n", "");
        if (ip.isEmpty()) {
            return "{}";
        }
        final String username = "dannycorbett890@gmail.com";
        final String password = "ptquklkfxlyozbvg";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });


        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from-email@gmail.com"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("dannycorbett890@gmail.com"));
        message.setSubject("Motion detected");
        message.setText("http://" + ip + ":58392" +
                "\n\nftp://" + ip + ":58321");

        Transport.send(message);
        System.out.println("Email sent");
        return "{\"stream\":\"http://" + ip + ":58392\"}";
    }
}
