package com.taskhttpv1.demo.TaskTrackerPro.client;

import lombok.Data;

@Data
public class TokenUpdateRequest {
    private String email;
    private String fcmToken;
}

