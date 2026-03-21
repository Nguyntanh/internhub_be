package com.example.internhub_be.service;

import com.example.internhub_be.domain.*;
import com.example.internhub_be.domain.InternshipProfile.InternshipStatus;
import com.example.internhub_be.payload.InternRequest;
import com.example.internhub_be.payload.InternResponse;
import com.example.internhub_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternService {

    private final InternshipProfileRepository profileRepo;
    private final UserRepository              userRepo;
    private final UniversityRepository        universityRepo;
    private final InternshipPositionRepository positionRepo;
    private final RoleRepository              roleRepo;
    private final DepartmentRepository        departmentRepo;

    @Transactional(readOnly = true)
    public List<InternResponse> getAll() {
        return profileRepo.findAllWithRelations()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InternResponse getById(Long id) {
        return toResponse(findProfileOrThrow(id));
    }

    @Transactional
    public InternResponse create(InternRequest req) {
        User user;

        if (req.getUserId() != null) {
            // ── Dùng user có sẵn ─────────────────────────────────────────
            user = userRepo.findById(req.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Không tìm thấy người dùng id: " + req.getUserId()));

            // Kiểm tra user đã có profile chưa
            if (profileRepo.findAll().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Người dùng này đã có hồ sơ thực tập.");
            }
        } else {
            // ── Tạo user mới (luồng cũ) ───────────────────────────────────
            if (userRepo.existsByEmail(req.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Email \"" + req.getEmail() + "\" đã tồn tại trong hệ thống.");
            }

            Role internRole = roleRepo.findByName("INTERN")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Role 'INTERN' chưa được khởi tạo trong bảng roles."));

            user = new User();
            user.setName(req.getFullName());
            user.setEmail(req.getEmail());
            user.setPhone(req.getPhone());
            user.setPassword(UUID.randomUUID().toString());
            user.setRole(internRole);
            user.setIsActive(false);
            user.setActivationToken(UUID.randomUUID().toString());
            user.setCreatedAt(LocalDateTime.now());
            userRepo.save(user);
        }

        // Cập nhật department vào user nếu có
        if (req.getDepartmentId() != null) {
            departmentRepo.findById(req.getDepartmentId())
                    .ifPresent(user::setDepartment);
            userRepo.save(user);
        }

        InternshipProfile profile = new InternshipProfile();
        profile.setUser(user);
        fillProfileFields(profile, req);
        profile.setStatus(req.getStatus() != null ? req.getStatus() : InternshipStatus.In_Progress);
        profileRepo.save(profile);

        return toResponse(findProfileOrThrow(profile.getId()));
    }

    @Transactional
    public InternResponse update(Long id, InternRequest req) {
        InternshipProfile profile = findProfileOrThrow(id);
        User user = profile.getUser();

        if (!user.getEmail().equalsIgnoreCase(req.getEmail())
                && userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email \"" + req.getEmail() + "\" đã tồn tại trong hệ thống.");
        }

        user.setName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());

        if (req.getDepartmentId() != null) {
            departmentRepo.findById(req.getDepartmentId())
                    .ifPresent(user::setDepartment);
        } else {
            user.setDepartment(null);
        }

        userRepo.save(user);

        fillProfileFields(profile, req);
        if (req.getStatus() != null) profile.setStatus(req.getStatus());
        profileRepo.save(profile);

        return toResponse(findProfileOrThrow(id));
    }

    @Transactional
    public void delete(Long id) {
        InternshipProfile profile = findProfileOrThrow(id);
        userRepo.delete(profile.getUser());
    }

    @Transactional
    public InternResponse updateStatus(Long id, InternshipStatus status) {
        InternshipProfile profile = findProfileOrThrow(id);
        profile.setStatus(status);
        profileRepo.save(profile);
        return toResponse(findProfileOrThrow(id));
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void fillProfileFields(InternshipProfile profile, InternRequest req) {
        profile.setMajor(req.getMajor());
        profile.setStartDate(req.getStartDate());
        profile.setEndDate(req.getEndDate());

        if (req.getUniversityId() != null) {
            profile.setUniversity(
                    universityRepo.findById(req.getUniversityId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Không tìm thấy trường đại học id: " + req.getUniversityId()))
            );
        } else {
            profile.setUniversity(null);
        }

        if (req.getPositionId() != null) {
            profile.setPosition(
                    positionRepo.findById(req.getPositionId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Không tìm thấy vị trí id: " + req.getPositionId()))
            );
        } else {
            profile.setPosition(null);
        }

        profile.setMentor(req.getMentorId() != null
                ? userRepo.findById(req.getMentorId()).orElse(null) : null);
        profile.setManager(req.getManagerId() != null
                ? userRepo.findById(req.getManagerId()).orElse(null) : null);
    }

    private InternResponse toResponse(InternshipProfile p) {
        InternResponse r = new InternResponse();

        r.setId(p.getId());
        r.setMajor(p.getMajor());
        r.setStartDate(p.getStartDate());
        r.setEndDate(p.getEndDate());
        r.setStatus(p.getStatus());

        if (p.getUser() != null) {
            r.setUserId(p.getUser().getId());
            r.setFullName(p.getUser().getName());
            r.setEmail(p.getUser().getEmail());
            r.setPhone(p.getUser().getPhone());
            r.setAvatar(p.getUser().getAvatar());
        }

        if (p.getUniversity() != null) {
            r.setUniversityId(p.getUniversity().getId());
            r.setUniversityName(p.getUniversity().getName());
        }

        if (p.getPosition() != null) {
            r.setPositionId(p.getPosition().getId());
            r.setPositionName(p.getPosition().getName());
            if (p.getPosition().getDepartment() != null) {
                r.setDepartmentId(p.getPosition().getDepartment().getId());
                r.setDepartmentName(p.getPosition().getDepartment().getName());
            }
        } else if (p.getUser() != null && p.getUser().getDepartment() != null) {
            r.setDepartmentId(p.getUser().getDepartment().getId());
            r.setDepartmentName(p.getUser().getDepartment().getName());
        }

        if (p.getMentor() != null) {
            r.setMentorId(p.getMentor().getId());
            r.setMentorName(p.getMentor().getName());
        }
        if (p.getManager() != null) {
            r.setManagerId(p.getManager().getId());
            r.setManagerName(p.getManager().getName());
        }

        return r;
    }

    private InternshipProfile findProfileOrThrow(Long id) {
        return profileRepo.findByIdWithRelations(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy hồ sơ id: " + id));
    }
}