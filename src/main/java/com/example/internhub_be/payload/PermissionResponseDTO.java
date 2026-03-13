package com.example.internhub_be.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {
    private String functionCode;
    private String functionName;
    private Boolean canAccess;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
}
