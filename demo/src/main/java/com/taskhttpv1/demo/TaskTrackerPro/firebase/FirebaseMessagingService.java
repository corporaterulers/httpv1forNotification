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

    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/tasktrackerapp-53ff4/messages:send";

    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(getClass().getClassLoader().getResourceAsStream("firebase-service-account.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
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
private String buildMessage(String topic, String title, String body) {
        return """
            {
              "message": {
                "topic": "%s",
                "notification": {
                  "title": "%s",
                  "body": "%s"
                }
              }
            }
            """.formatted(topic, title, body);
    }

    private void sendHttpRequest(String messageJson) throws IOException {
        URL url = new URL(FCM_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        connection.setRequestProperty("Content-Type", "application/json; UTF-8");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(messageJson.getBytes("UTF-8"));
        }

        int responseCode = connection.getResponseCode();
        Scanner scanner;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            scanner = new Scanner(connection.getInputStream());
        } else {
            scanner = new Scanner(connection.getErrorStream());
        }

        while (scanner.hasNextLine()) {
            System.out.println(scanner.nextLine());
        }

        scanner.close();
    }
}
