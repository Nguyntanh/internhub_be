package com.example.internhub_be.payload;

import com.example.internhub_be.domain.InternshipProfile;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private InternshipProfile.InternshipStatus status;
}
