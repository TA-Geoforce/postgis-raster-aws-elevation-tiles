package com.mapserver.elevationtiles.dto;

import com.mapserver.elevationtiles.repository.ElevationTilesProdRepository;
import io.swagger.v3.oas.annotations.media.Schema;

public class HistogramDto {
    @Schema(name = "min", description = "Minimum value", example = "1.0")
    private double min;
    @Schema(name = "max", description = "Maximum value", example = "10.0")
    private double max;
    @Schema(name = "count", description = "Count", example = "10")
    private long count;
    @Schema(name = "percent", description = "Percent", example = "0.2")
    private double percent;

    public HistogramDto fromDomainToDto(ElevationTilesProdRepository.Histogram histogram) {
        return new HistogramDto().setMin(histogram.getMin())
                .setMax(histogram.getMax())
                .setCount(histogram.getCount())
                .setPercent(histogram.getPercent());
    }

    public double getMin() {
        return min;
    }

    public HistogramDto setMin(double min) {
        this.min = min;
        return this;
    }

    public double getMax() {
        return max;
    }

    public HistogramDto setMax(double max) {
        this.max = max;
        return this;
    }

    public long getCount() {
        return count;
    }

    public HistogramDto setCount(long count) {
        this.count = count;
        return this;
    }

    public double getPercent() {
        return percent;
    }

    public HistogramDto setPercent(double percent) {
        this.percent = percent;
        return this;
    }

    @Override
    public String toString() {
        return "HistogramDto{" +
                "min=" + min +
                ", max=" + max +
                ", count=" + count +
                ", percent=" + percent +
                '}';
    }
}
