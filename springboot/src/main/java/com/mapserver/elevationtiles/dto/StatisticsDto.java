package com.mapserver.elevationtiles.dto;

import com.mapserver.elevationtiles.repository.ElevationTilesProdRepository;
import io.swagger.v3.oas.annotations.media.Schema;

public class StatisticsDto {

    @Schema(name = "min", description = "Minimum value", example = "1.0")
    private double min;
    @Schema(name = "max", description = "Maximum value", example = "10.0")
    private double max;
    @Schema(name = "count", description = "Count", example = "10")
    private long count;
    @Schema(name = "sum", description = "Sum of all values", example = "80.0")
    private double sum;
    @Schema(name = "mean", description = "Mean value", example = "5.0")
    private double mean;
    @Schema(name = "standard deviation", description = "Standard deviation value", example = "2.5")
    private double stddev;

    public StatisticsDto fromDomainToDto(ElevationTilesProdRepository.Statistics statistics) {
        return new StatisticsDto()
                .setMin(statistics.getMin())
                .setMax(statistics.getMax())
                .setCount(statistics.getCount())
                .setSum(statistics.getSum())
                .setMean(statistics.getMean())
                .setStddev(statistics.getStddev());
    }

    public double getMin() {
        return min;
    }

    public StatisticsDto setMin(double min) {
        this.min = min;
        return this;
    }

    public double getMax() {
        return max;
    }

    public StatisticsDto setMax(double max) {
        this.max = max;
        return this;
    }

    public long getCount() {
        return count;
    }

    public StatisticsDto setCount(long count) {
        this.count = count;
        return this;
    }

    public double getSum() {
        return sum;
    }

    public StatisticsDto setSum(double sum) {
        this.sum = sum;
        return this;
    }

    public double getMean() {
        return mean;
    }


    public StatisticsDto setMean(double mean) {
        this.mean = mean;
        return this;
    }

    public double getStddev() {
        return stddev;
    }

    public StatisticsDto setStddev(double stddev) {
        this.stddev = stddev;
        return this;
    }

    @Override
    public String toString() {
        return "StatisticsDto{" +
                "min=" + min +
                ", max=" + max +
                ", count=" + count +
                ", sum=" + sum +
                ", mean=" + mean +
                ", stddev=" + stddev +
                '}';
    }
}
