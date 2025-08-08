package com.taskhttpv1.demo.TaskTrackerPro.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.taskhttpv1.demo.TaskTrackerPro.firebase.FirebaseMessagingService;
import com.taskhttpv1.demo.TaskTrackerPro.firebase.GoogleAccessTokenUtil;
import com.taskhttpv1.demo.TaskTrackerPro.task.Task;
import com.taskhttpv1.demo.TaskTrackerPro.task.TaskRepository;
import jakarta.annotation.PostConstruct;
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

    @Value("${notification.due.threshold.minutes:60}")
    private int dueThresholdMinutes;
    public NotificationSchedulerService(FirebaseMessagingService firebaseMessagingService) {
        this.firebaseMessagingService = firebaseMessagingService;
    }
    @Transactional(readOnly = true)
    @Scheduled(fixedRate = 300000)
    public void checkDueTasks() throws IOException, FirebaseMessagingException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(dueThresholdMinutes);

        List<Task> upcomingTasks = taskRepository.findAll().stream()
                .filter(task -> task.getDueDate() != null &&
                        task.getDueDate().isAfter(now) &&
                        task.getDueDate().isBefore(threshold))
                .toList();

        System.out.println("Scheduler ran at: " + now);
        System.out.println("Upcoming tasks found: " + upcomingTasks.size());

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

            String checkedEmail = emailCheck(task.getUser().getEmail());
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
    private String emailCheck(String email) {
        if (email == null || email.isEmpty()) return null;

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex != email.lastIndexOf('@')) return null;

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);

        int dotIndex = domainPart.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == domainPart.length() - 1) return null;

        String domainName = domainPart.substring(0, dotIndex);
        String topLevelDomain = domainPart.substring(dotIndex + 1);

        if (localPart.isEmpty() || domainName.isEmpty() || topLevelDomain.isEmpty()) return null;

        if (!localPart.matches("^[A-Za-z0-9._%+-]+$")) return null;
        if (!domainName.matches("^[A-Za-z0-9.-]+$")) return null;
        if (!topLevelDomain.matches("^[A-Za-z]{2,}$")) return null;

        return email;
    }
    private String convertToTopic(String email) {
        return email.replace("@", "_at_").replace(".", "_dot_");
    }
    public void addToQueue(Task task) throws FirebaseMessagingException {
        if (task.getUser() == null || task.getUser().getEmail() == null || task.getDueDate() == null) {
            return;
        }
        String checkedEmail = emailCheck(task.getUser().getEmail());
        String userToken = task.getUser().getFcmToken();
        if (userToken == null || userToken.isEmpty()) return;
        String title = "New Task Added";
        String body = "Task: " + task.getTitle() + " is scheduled for " + task.getDueDate();
        firebaseMessagingService.sendNotification(userToken, title, body);

    }
}