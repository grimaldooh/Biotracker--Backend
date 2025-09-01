package com.biotrack.backend.services;

import com.biotrack.backend.dto.insurance.LambdaInsuranceRequestDTO;
import com.biotrack.backend.dto.insurance.LambdaInsuranceResponseDTO;

public interface AwsLambdaInsuranceService {
    LambdaInsuranceResponseDTO calculateInsurancePremium(LambdaInsuranceRequestDTO request);
    boolean testLambdaConnection();
}