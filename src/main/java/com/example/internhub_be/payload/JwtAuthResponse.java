package com.example.internhub_be.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    // Custom constructor for convenience
    public JwtAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
