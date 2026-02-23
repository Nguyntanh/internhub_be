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
        List<InternshipPositionResponse> positions = positionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InternshipPositionResponse> getPositionById(@PathVariable Integer id) {
        InternshipPositionResponse position = positionService.getPositionById(id);
        return ResponseEntity.ok(position);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InternshipPositionResponse> createPosition(@Valid @RequestBody InternshipPositionRequest request) {
        InternshipPositionResponse createdPosition = positionService.createPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPosition);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InternshipPositionResponse> updatePosition(
            @PathVariable Integer id,
            @Valid @RequestBody InternshipPositionRequest request) {
        InternshipPositionResponse updatedPosition = positionService.updatePosition(id, request);
        return ResponseEntity.ok(updatedPosition);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePosition(@PathVariable Integer id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}