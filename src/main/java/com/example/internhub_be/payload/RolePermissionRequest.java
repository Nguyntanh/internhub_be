package com.example.internhub_be.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RolePermissionRequest {
    @NotNull(message = "Role ID không được để trống")
    private Long roleId;

    @NotNull(message = "Function ID không được để trống")
    private Long functionId;

    private Boolean canAccess = false;
    private Boolean canCreate = false;
    private Boolean canEdit = false;
    private Boolean canDelete = false;
}
