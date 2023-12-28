package com.mapserver.elevationtiles.dto;

import com.mapserver.elevationtiles.repository.ElevationTilesProdRepository;
import io.swagger.v3.oas.annotations.media.Schema;

public class QuantileDto {
    @Schema(name = "quantile", description = "Quantile percentage", example = "0.25")
    double quantile;
    @Schema(name = "value", description = "Value of the quantile percentage", example = "2.0")
    double value;

    public double getQuantile() {
        return quantile;
    }

    public QuantileDto setQuantile(double quantile) {
        this.quantile = quantile;
        return this;
    }

    public double getValue() {
        return value;
    }

    public QuantileDto setValue(double value) {
        this.value = value;
        return this;
    }

    public QuantileDto fromDomainToDto(ElevationTilesProdRepository.Quantile quantile) {
        return new QuantileDto()
                .setQuantile(quantile.getQuantile())
                .setValue(quantile.getValue());
    }
}
