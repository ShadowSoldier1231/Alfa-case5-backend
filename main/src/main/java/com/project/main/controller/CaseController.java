package com.project.main.controller;


import com.project.main.dto.CasePublicDto;
import com.project.main.service.CaseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<CasePublicDto>> getAllCases() {
        return ResponseEntity.ok(caseService.getAllPublicCases());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasePublicDto> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.getCaseByIdAndIncrementViews(id));
    }

    @GetMapping("/tags")
    public ResponseEntity<Map<Long, Object>> getActiveTags() {
        return ResponseEntity.ok(caseService.getActiveTagsWithCount());
    }
}