package com.taskhttpv1.demo.TaskTrackerPro.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @PostMapping("/token")
    public String receiveToken(@RequestBody TokenRequestFromFrontend request) {
        System.out.println("Email: " + request.getEmail());
        System.out.println("Token: " + request.getHttpv1token());
        return "Token received successfully";
    }
    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody UserDb user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        // Optional: prevent duplicates
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        UserDb savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }
    @PostMapping("/save-token")
    public ResponseEntity<String> saveToken(@RequestBody TokenRequestFromFrontend request) {
        Optional<UserDb> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isPresent()) {
            UserDb user = optionalUser.get();
            user.setFcmToken(request.getHttpv1token());
            userRepository.save(user);
            return ResponseEntity.ok("Token saved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    @PostMapping("/update-token")
    public ResponseEntity<String> updateFcmToken(@RequestBody TokenUpdateRequest request) {
        Optional<UserDb> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        UserDb user = userOpt.get();
        user.setFcmToken(request.getHttpv1Token());
        userRepository.save(user);
        return ResponseEntity.ok("Token updated");
    }
}

