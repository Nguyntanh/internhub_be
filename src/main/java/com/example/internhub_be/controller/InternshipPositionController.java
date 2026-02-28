package com.example.internhub_be.controller;

import com.example.internhub_be.payload.request.InternshipPositionRequest;
import com.example.internhub_be.payload.response.InternshipPositionResponse;
import com.example.internhub_be.service.InternshipPositionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class InternshipPositionController {

    private final InternshipPositionService positionService;

    public InternshipPositionController(InternshipPositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public ResponseEntity<List<InternshipPositionResponse>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InternshipPositionResponse> getPositionById(@PathVariable Long id) {
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InternshipPositionResponse> createPosition(@Valid @RequestBody InternshipPositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(positionService.createPosition(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InternshipPositionResponse> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody InternshipPositionRequest request) {
        return ResponseEntity.ok(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}