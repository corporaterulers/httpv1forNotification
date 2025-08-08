package com.taskhttpv1.demo.TaskTrackerPro.task;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.taskhttpv1.demo.TaskTrackerPro.client.TokenRequest;
import com.taskhttpv1.demo.TaskTrackerPro.client.User;
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
@RequestMapping("/api/hilton/task") // localhost:8080
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

    @GetMapping("/")
    public String home() {
        return "Welcome to Task Tracker Pro API";
    }
    @GetMapping // REST endpoints Get
    public List <Task> getTask(){
        return taskRepository.findAll();
    }

    @PostMapping("/token")
    public String receiveToken(@RequestBody TokenRequest request) {
        // Log or process the token
        System.out.println("Received token: " + request.getToken() + " for email: " + request.getEmail());
        return "Token received successfully";
    }
    @PostMapping("/task")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task savedTask = taskService.save(task);
        return ResponseEntity.ok(savedTask);
    }
    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody Task task) throws FirebaseMessagingException {
        if (task.getUser() == null || task.getUser().getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User email is required in the request.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(task.getUser().getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found for email: " + task.getUser().getEmail());
        }

        task.setUser(optionalUser.get());
        taskRepository.save(task);
        notificationSchedulerService.addToQueue(task);
        return ResponseEntity.ok(task);
    }
    @PutMapping("/task/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task updatedTask) {
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

    @DeleteMapping("/task/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
