package de.electrogutachten.application.dto;

import de.electrogutachten.domain.valueobject.BatterieTyp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GutachtenErstellenRequest {

    @NotBlank
    private String tenantId;

    @NotBlank
    private String vin;

    private BatterieTyp batterieTyp = BatterieTyp.LI_ION_BEV;

    @Positive
    private double nennspannungVolt = 800.0;

    @Positive
    private double nennkapazitaetAh = 75.0;
}