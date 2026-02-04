package com.example.internhub_be.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
