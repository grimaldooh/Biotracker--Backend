package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.MutationDTO;
import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.services.MutationService;
import com.biotrack.backend.utils.MutationMapper;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mutations")
public class MutationController {
    private final MutationService mutationService;

    public MutationController(MutationService mutationService){
        this.mutationService = mutationService;
    }

    @PostMapping("/process")
    public ResponseEntity<List<MutationDTO>> process(@RequestParam UUID resultFileId){
        List<Mutation> mutations = mutationService.processResultFile(resultFileId);
        List<MutationDTO> dtos = mutations.stream().map(MutationMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

}
