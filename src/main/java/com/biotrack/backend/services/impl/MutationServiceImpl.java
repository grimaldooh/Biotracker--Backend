package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.enums.Relevance;
import com.biotrack.backend.repositories.MutationRepository;
import com.biotrack.backend.services.MutationService;
import com.biotrack.backend.services.ResultFileService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MutationServiceImpl implements MutationService{
    private final ResultFileService resultFileService;
    private final MutationRepository mutationRepository;

    public MutationServiceImpl(MutationRepository mutationRepository, ResultFileService resultFileService){
        this.mutationRepository = mutationRepository;
        this.resultFileService = resultFileService;
    }

    @Override
    public List<Mutation> processResultFile(UUID resultFileId) {
        ResultFile resultFile = resultFileService.findById(resultFileId);
        Sample sample = resultFile.getSample();

        List<Mutation> mutations = new ArrayList<>();

        try {
            URL fileUrl = new URL(resultFile.getS3Url());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileUrl.openStream()));

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
                        .sample(sample)
                        .build();

                mutations.add(mutation);
            }

            return mutationRepository.saveAll(mutations);

        } catch (Exception e) {
            throw new RuntimeException("Error reading mutation file", e);
        }
    }
}
