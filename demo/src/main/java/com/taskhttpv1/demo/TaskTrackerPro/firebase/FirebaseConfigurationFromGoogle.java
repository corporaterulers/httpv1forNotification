package com.taskhttpv1.demo.TaskTrackerPro.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfigurationFromGoogle {
    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");

            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(firebaseOptions);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
