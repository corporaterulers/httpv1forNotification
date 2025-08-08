package com.taskhttpv1.demo.TaskTrackerPro.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.taskhttpv1.demo.TaskTrackerPro.firebase.FirebaseMessagingService;
import com.taskhttpv1.demo.TaskTrackerPro.firebase.GoogleAccessTokenUtil;
import com.taskhttpv1.demo.TaskTrackerPro.task.Task;
import com.taskhttpv1.demo.TaskTrackerPro.task.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationSchedulerService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Value("${notification.due.threshold.minutes:60}")// 1 hour
    private int dueThresholdMinutes;
    public NotificationSchedulerService(FirebaseMessagingService firebaseMessagingService) {
        this.firebaseMessagingService = firebaseMessagingService;
    }
    @Transactional(readOnly = true)
    @Scheduled(fixedRate = 60000)//1 minute
    public void checkDueTasks() throws IOException, FirebaseMessagingException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(dueThresholdMinutes);

        List<Task> upcomingTasks = taskRepository.findAll().stream()
                .filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isAfter(now) &&
                        task.getDueDate().isBefore(threshold))
                .toList();
        String accessToken;
        try {
            accessToken = GoogleAccessTokenUtil.getAccessToken();
            System.out.println("Access Token = " + accessToken);
        } catch (Exception e) {
            System.err.println("Failed to fetch access token: " + e.getMessage());
            return;
        }

        for (Task task : upcomingTasks) {
            if (task.getUser() == null || task.getUser().getEmail() == null) {
                System.out.println("Skipping task (ID: " + task.getId() + ") due to missing user email");
                continue;
            }
            String userToken = task.getUser().getFcmToken();
            if (userToken == null || userToken.isEmpty()) {
                System.out.println("Skipping task (ID: " + task.getId() + ") due to missing FCM token");
                continue;
            }
            System.out.println("Sending notification for task ID: " + task.getId() + " to token: " + userToken);
            String title = "Task Reminder";
            String body = "Task '" + task.getTitle() + "' is due at " + task.getDueDate();
            System.out.println("Sending notification to token: " + userToken);
            firebaseMessagingService.sendNotification(userToken, title, body);
        }
    }
    public void addToQueue(Task task) throws FirebaseMessagingException {
        if (task.getUser() == null || task.getUser().getEmail() == null || task.getDueDate() == null) {
            return;
        }
        String userToken = task.getUser().getFcmToken();
        if (userToken == null || userToken.isEmpty()) return;
        System.out.println("Push notification");
        String title = "New Task Added ";
        String body = "To do Task: " + task.getTitle() + " is scheduled for " + task.getDueDate();
        firebaseMessagingService.sendNotification(userToken, title, body);
    }
}