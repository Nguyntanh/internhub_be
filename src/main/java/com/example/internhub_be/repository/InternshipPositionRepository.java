package com.example.internhub_be.repository;

import com.example.internhub_be.domain.InternshipPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternshipPositionRepository extends JpaRepository<InternshipPosition, Long> {

    // Dùng cho API filter theo phòng ban — cần thiết cho HR chọn vị trí ở E02
    List<InternshipPosition> findByDepartmentId(Long departmentId);

    // Dùng khi xóa department thủ công nếu không dùng cascade
    void deleteByDepartmentId(Long departmentId);
}