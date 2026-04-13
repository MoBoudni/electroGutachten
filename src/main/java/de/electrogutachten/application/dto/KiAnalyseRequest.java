package de.electrogutachten.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class KiAnalyseRequest {

    @NotEmpty
    @Size(min = 1, max = 10)
    private List<String> bilderBase64;
}