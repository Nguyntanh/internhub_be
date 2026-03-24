package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role_permissions")
public class RolePermission {
    @EmbeddedId
    private RolePermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("functionId")
    @JoinColumn(name = "function_id")
    private Function function;

    @Column(name = "can_access", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canAccess = false;

    @Column(name = "can_create", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canCreate = false;

    @Column(name = "can_edit", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canEdit = false;

    @Column(name = "can_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canDelete = false;
}
