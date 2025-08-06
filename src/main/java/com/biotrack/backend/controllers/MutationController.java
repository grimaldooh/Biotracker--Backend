package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.MutationDTO;
import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.enums.Relevance;
import com.biotrack.backend.services.MutationService;
import com.biotrack.backend.utils.MutationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mutations")
@Tag(name = "Mutations", description = "Genetic mutation management and analysis")
public class MutationController {

    private final MutationService mutationService;

    @Autowired
    public MutationController(MutationService mutationService) {
        this.mutationService = mutationService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search mutations", description = "Search mutations by sample ID, relevance, and/or gene")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mutations found"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<List<MutationDTO>> search(
            @RequestParam(required = false) UUID sampleId,
            @RequestParam(required = false) Relevance relevance,
            @RequestParam(required = false) String gene) {
        
        List<Mutation> mutations = mutationService.search(sampleId, relevance, gene);
        List<MutationDTO> dtos = mutations.stream()
                .map(MutationMapper::toDTO)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/process-result-file/{resultFileId}")
    @Operation(summary = "Process result file", description = "Process a result file to extract mutations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mutations processed successfully"),
        @ApiResponse(responseCode = "404", description = "Result file not found"),
        @ApiResponse(responseCode = "500", description = "Error processing file")
    })
    public ResponseEntity<List<MutationDTO>> processResultFile(@PathVariable UUID resultFileId) {
        List<Mutation> mutations = mutationService.processResultFile(resultFileId);
        List<MutationDTO> dtos = mutations.stream()
                .map(MutationMapper::toDTO)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/genetic-sample/{geneticSampleId}")
    @Operation(summary = "Get mutations by genetic sample", description = "Retrieve all mutations for a specific genetic sample")
    public ResponseEntity<List<MutationDTO>> getByGeneticSampleId(@PathVariable UUID geneticSampleId) {
        List<Mutation> mutations = mutationService.search(geneticSampleId, null, null);
        List<MutationDTO> dtos = mutations.stream()
                .map(MutationMapper::toDTO)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }
}
