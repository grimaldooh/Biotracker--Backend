package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.MutationDTO;
import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.enums.Relevance;
import com.biotrack.backend.services.MutationService;
import com.biotrack.backend.utils.MutationMapper;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<MutationDTO>> getAll(
            @RequestParam(required = false) UUID sampleId,
            @RequestParam(required = false) Relevance relevance,
            @RequestParam(required = false) String gene
    ) {
        List<Mutation> mutations = mutationService.search(sampleId, relevance, gene);
        List<MutationDTO> dtos = mutations.stream().map(MutationMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

}
