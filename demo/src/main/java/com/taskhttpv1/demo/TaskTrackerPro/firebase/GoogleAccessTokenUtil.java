package com.taskhttpv1.demo.TaskTrackerPro.firebase;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;

public class GoogleAccessTokenUtil {

    private static final String FIREBASE_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    public static String getAccessToken() throws IOException {
        System.out.println("Config exists: " + new ClassPathResource("firebase-service-account.json").exists());

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-account.json").getInputStream())
                .createScoped(Collections.singletonList(FIREBASE_SCOPE));

        googleCredentials.refreshIfExpired();
        AccessToken token = googleCredentials.getAccessToken();

        return token.getTokenValue();
    }
}
