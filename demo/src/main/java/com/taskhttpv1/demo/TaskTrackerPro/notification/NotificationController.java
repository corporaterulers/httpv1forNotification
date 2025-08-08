package com.taskhttpv1.demo.TaskTrackerPro.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.taskhttpv1.demo.TaskTrackerPro.client.User;
import com.taskhttpv1.demo.TaskTrackerPro.client.UserRepository;
import com.taskhttpv1.demo.TaskTrackerPro.firebase.FirebaseMessagingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final FirebaseMessagingService firebaseMessagingService;
    private final UserRepository userRepository;

    public NotificationController(FirebaseMessagingService firebaseMessagingService, UserRepository userRepository) {
        this.firebaseMessagingService = firebaseMessagingService;
        this.userRepository = userRepository;
    }
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {

        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing or empty 'token' in the request body.");
        }
        try {
            String response = firebaseMessagingService.sendNotification(request.getToken().trim(),
                    request.getTitle(),
                    request.getBody());
            return ResponseEntity.ok("Notification sent successfully: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send notification: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }
    @PostMapping("/notify-user")
    public ResponseEntity<String> notifyUser(@RequestBody NotificationRequest request) throws FirebaseMessagingException {
        // Find user by email
        User user = userRepository.findByEmail(request.getToken())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getFcmToken() == null) {
            return ResponseEntity.badRequest().body("User does not have an FCM token");
        }

        firebaseMessagingService.sendNotification(
                user.getFcmToken(),
                request.getTitle(),
                request.getBody()
        );

        return ResponseEntity.ok("Notification sent");
    }
}

