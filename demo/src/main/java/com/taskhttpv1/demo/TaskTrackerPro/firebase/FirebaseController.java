package com.taskhttpv1.demo.TaskTrackerPro.firebase;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/firebase/notify")
public class FirebaseController {

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestParam String topic,
                                                   @RequestParam String title,
                                                   @RequestParam String body) throws FirebaseMessagingException {
        firebaseMessagingService.sendNotification(topic, title, body);
        return ResponseEntity.ok("Notification sent successfully");
    }
}
