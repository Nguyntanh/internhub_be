package com.example.internhub_be.service;

public interface EmailService {
    void sendActivationEmail(String to, String activationLink);
}
