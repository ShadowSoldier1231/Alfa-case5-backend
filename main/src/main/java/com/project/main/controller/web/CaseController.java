package com.project.main.controller.web;


import com.project.main.dto.cases.CasePublicDto;
import com.project.main.dto.common.PageResponse;
import com.project.main.service.cases.CaseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<PageResponse<CasePublicDto>> getAllCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "created_at,desc") String sort) {

        return ResponseEntity.ok(caseService.getPublicCases(page, size, search, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasePublicDto> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.getCaseByIdAndIncrementViews(id));
    }

    @GetMapping("/tags")
    public ResponseEntity<PageResponse<CasePublicDto.TagInfo>> getActiveTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "case_count,desc") String sort) {

        return ResponseEntity.ok(caseService.getPublicTags(page, size, search, sort));
    }
}