package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.models.GeneticSample; // ✅ CAMBIAR: de Sample a GeneticSample
import com.biotrack.backend.models.enums.Relevance;
import com.biotrack.backend.repositories.MutationRepository;
import com.biotrack.backend.services.MutationService;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.services.S3Service;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MutationServiceImpl implements MutationService{
    private final ResultFileService resultFileService;
    private final MutationRepository mutationRepository;
    private final S3Service s3Service;

    public MutationServiceImpl(MutationRepository mutationRepository, 
                              ResultFileService resultFileService,
                              S3Service s3Service){
        this.mutationRepository = mutationRepository;
        this.resultFileService = resultFileService;
        this.s3Service = s3Service;
    }

    @Override
    public List<Mutation> search(UUID sampleId, Relevance relevance, String gene) {
        if (sampleId != null && relevance != null && gene != null)
            return mutationRepository.findBySampleIdAndRelevanceAndGeneContainingIgnoreCase(sampleId, relevance, gene);

        if (sampleId != null && relevance != null)
            return mutationRepository.findBySampleIdAndRelevance(sampleId, relevance);

        if (sampleId != null && gene != null)
            return mutationRepository.findBySampleIdAndGeneContainingIgnoreCase(sampleId, gene);

        if (sampleId != null)
            return mutationRepository.findBySampleId(sampleId);

        if (relevance != null)
            return mutationRepository.findByRelevance(relevance);

        if (gene != null)
            return mutationRepository.findByGeneContainingIgnoreCase(gene);

        return mutationRepository.findAll();
    }

    @Override
    public List<Mutation> processResultFile(UUID resultFileId) {
        ResultFile resultFile = resultFileService.findById(resultFileId);
        // ✅ CAMBIAR: Ahora obtenemos GeneticSample en lugar de Sample
        GeneticSample geneticSample = resultFile.getGeneticSample(); // Necesitarás actualizar ResultFile también

        List<Mutation> mutations = new ArrayList<>();

        try {
            String s3Key = extractS3KeyFromUrl(resultFile.getS3Url());
            InputStream inputStream = s3Service.downloadFile(s3Key);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                Mutation mutation = Mutation.builder()
                        .gene(parts[0].trim())
                        .chromosome(parts[1].trim())
                        .type(parts[2].trim())
                        .relevance(Relevance.valueOf(parts[3].trim().toUpperCase()))
                        .comment(parts[4].trim())
                        .sample(geneticSample) // ✅ CAMBIAR: usar geneticSample
                        .build();

                mutations.add(mutation);
            }

            reader.close();
            inputStream.close();
            
            return mutationRepository.saveAll(mutations);

        } catch (Exception e) {
            throw new RuntimeException("Error reading mutation file: " + e.getMessage(), e);
        }
    }

    private String extractS3KeyFromUrl(String s3Url) {
        String bucketPattern = ".s3.amazonaws.com/";
        int keyStartIndex = s3Url.indexOf(bucketPattern);
        
        if (keyStartIndex == -1) {
            throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
        }
        
        return s3Url.substring(keyStartIndex + bucketPattern.length());
    }
}
