package com.example.internhub_be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    private List<InternshipPositionResponse> positions;

    /** Simple name list kept for backward-compat display */
    private List<String> memberNames;

    /** Full member objects so the frontend can show/manage them */
    private List<MemberInfo> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberInfo {
        private Long id;
        private String name;
        private String email;
        private String roleName;
    }
}