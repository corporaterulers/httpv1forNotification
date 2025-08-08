package com.taskhttpv1.demo.TaskTrackerPro.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Scanner;

import java.io.OutputStream;

@Service
public class FirebaseMessagingService {

    public String sendNotification(String token, String title, String body) throws FirebaseMessagingException {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token must not be null/empty");
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title == null ? "Notification" : title)
                        .setBody(body == null ? "" : body)
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Firebase send response: " + response);
        return response;
    }
}
