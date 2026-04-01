package com.example.internhub_be.controller;

import com.example.internhub_be.payload.InternRequest;
import com.example.internhub_be.payload.InternResponse;
import com.example.internhub_be.payload.StatusUpdateRequest;
import com.example.internhub_be.service.InternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interns")
@RequiredArgsConstructor
public class InternController {

    private final InternService internService;

    @GetMapping
    public List<InternResponse> getAll() {
        return internService.getAll();
    }

    @GetMapping("/{id}")
    public InternResponse getById(@PathVariable Long id) {
        return internService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternResponse create(@RequestBody InternRequest req) {
        return internService.create(req);
    }

    /**
     * POST /api/interns/from-user/{userId}
     * Tạo InternshipProfile từ User có sẵn (đã có role INTERN trong DB).
     * Tránh lỗi 409 khi email đã tồn tại.
     */
    @PostMapping("/from-user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public InternResponse createFromExistingUser(
            @PathVariable Long userId,
            @RequestBody InternRequest req) {
        return internService.createFromExistingUser(userId, req);
    }

    @PutMapping("/{id}")
    public InternResponse update(@PathVariable Long id,
                                 @RequestBody InternRequest req) {
        return internService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        internService.delete(id);
    }

    @PatchMapping("/{id}/status")
    public InternResponse updateStatus(@PathVariable Long id,
                                       @RequestBody StatusUpdateRequest req) {
        return internService.updateStatus(id, req.getStatus());
    }
}