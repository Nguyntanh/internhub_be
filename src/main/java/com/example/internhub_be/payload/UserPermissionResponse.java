package com.example.internhub_be.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionResponse {
    private String functionCode;
    private Boolean canAccess;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
}
