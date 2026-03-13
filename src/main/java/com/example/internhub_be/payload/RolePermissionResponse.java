package com.example.internhub_be.payload;

import lombok.Data;

@Data
public class RolePermissionResponse {
    private Long roleId;
    private String roleName;
    private Long functionId;
    private String functionCode;
    private String functionName;
    private Boolean canAccess;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
}
