package com.example.internhub_be.payload;

public class NewAvatarUrlResponse {
    private String newAvatarUrl;

    public NewAvatarUrlResponse(String newAvatarUrl) {
        this.newAvatarUrl = newAvatarUrl;
    }

    public String getNewAvatarUrl() {
        return newAvatarUrl;
    }

    public void setNewAvatarUrl(String newAvatarUrl) {
        this.newAvatarUrl = newAvatarUrl;
    }
}
