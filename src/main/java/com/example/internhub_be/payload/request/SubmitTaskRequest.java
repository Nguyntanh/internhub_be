package com.example.internhub_be.payload.request;

import lombok.Data;

@Data
public class SubmitTaskRequest {

    private String submissionLink;

    private String submissionNote;

}