package com.taskhttpv1.demo.TaskTrackerPro.task;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.taskhttpv1.demo.TaskTrackerPro.client.TokenRequestFromFrontend;
import com.taskhttpv1.demo.TaskTrackerPro.client.UserDb;
import com.taskhttpv1.demo.TaskTrackerPro.client.UserRepository;
import com.taskhttpv1.demo.TaskTrackerPro.notification.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final NotificationSchedulerService notificationSchedulerService;
    private final UserRepository userRepository;

    @Autowired
    public TaskController(
            TaskService taskService,
            TaskRepository taskRepository,
            NotificationSchedulerService notificationSchedulerService,
            UserRepository userRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
        this.notificationSchedulerService = notificationSchedulerService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/token")
    public String receiveToken(@RequestBody TokenRequestFromFrontend request) {
        System.out.println("Received token: " + request.getHttpv1token() + " for email: " + request.getEmail());
        return "Token received successfully";
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) throws FirebaseMessagingException {
        if (task.getUser() == null || task.getUser().getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User email is required in the request.");
        }

        Optional<UserDb> optionalUser = userRepository.findByEmail(task.getUser().getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found for email: " + task.getUser().getEmail());
        }

        task.setUser(optionalUser.get());
        Task savedTask = taskRepository.save(task);
        notificationSchedulerService.addToQueue(savedTask);
        return ResponseEntity.ok(savedTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    task.setDueDate(updatedTask.getDueDate());
                    task.setStatus(TaskStatus.valueOf(updatedTask.getStatus().name()));
                    task.setEmail(updatedTask.getEmail());
                    return ResponseEntity.ok(taskRepository.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
