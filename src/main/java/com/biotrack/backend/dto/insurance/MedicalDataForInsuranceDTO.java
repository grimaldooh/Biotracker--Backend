package com.biotrack.backend.dto.insurance;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class MedicalDataForInsuranceDTO {
    private Integer age;
    private Integer chronicConditionsCount;
    private Integer consultationsThisYear;
    private Integer medicationsCount;
    private Integer recentStudiesCount;
    private Boolean hasAbnormalLabResults;
    private List<String> riskFactorsFromHistory;
}