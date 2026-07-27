package com.project.main.controller;

import com.project.main.dto.CaseCreateRequest;
import com.project.main.dto.RegisterResult;
import com.project.main.model.CaseEntity;
import com.project.main.model.UserSession;
import com.project.main.service.CaseService;
import com.project.main.service.SessionService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService caseService;
    private final SessionService sessionService;


    public CaseController(CaseService caseService, SessionService sessionService) {
        this.caseService = caseService;
        this.sessionService = sessionService;
    }



}