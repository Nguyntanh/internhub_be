package com.example.internhub_be.repository;

import com.example.internhub_be.domain.RolePermission;
import com.example.internhub_be.domain.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}
