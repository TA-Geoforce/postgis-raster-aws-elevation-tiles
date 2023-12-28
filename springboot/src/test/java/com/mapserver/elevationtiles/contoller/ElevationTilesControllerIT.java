package com.mapserver.elevationtiles.contoller;

import com.mapserver.elevationtiles.Application;
import com.mapserver.elevationtiles.dto.HistogramDto;
import com.mapserver.elevationtiles.dto.QuantileDto;
import com.mapserver.elevationtiles.dto.StatisticsDto;
import com.mapserver.elevationtiles.repository.ElevationTilesProdRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.mapserver.elevationtiles.contoller.ElevationTilesController.EMPTY_TILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ElevationTilesControllerIT {

    @Autowired
    private ElevationTilesProdRepository elevationTilesProdRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String getRootUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Test
    void get_statistics() {
        StatisticsDto statistics = restTemplate.getForObject(getPath("elevation-statistics"), StatisticsDto.class);
        assertThat(statistics.getMin()).isNegative();
        assertThat(statistics.getMax()).isPositive();
        assertThat(statistics.getMean()).isNegative();
        assertThat(statistics.getCount()).isPositive();
        assertThat(statistics.getSum()).isNegative();
        assertThat(statistics.getStddev()).isPositive();
    }

    @Test
    void get_statistics_by_z_and_envelope() {
        StatisticsDto statistics = restTemplate.getForObject(getRootUrl() + "/%s/1/%s,%s,%s,%s".formatted("elevation-statistics", -175, -85, 175, 85), StatisticsDto.class);
        assertThat(statistics.getMin()).isNegative();
        assertThat(statistics.getMax()).isPositive();
        assertThat(statistics.getMean()).isNegative();
        assertThat(statistics.getCount()).isPositive();
        assertThat(statistics.getSum()).isNegative();
        assertThat(statistics.getStddev()).isPositive();
    }

    @Test
    void get_histograms() {
        ResponseEntity<HistogramDto[]> response =
                restTemplate.getForEntity(getPath("elevation-histograms"), HistogramDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).hasSize(6);
        var histogram = response.getBody();
        assertThat(histogram).allSatisfy(record -> {
            assertThat(record.getMin()).isNotZero();
            assertThat(record.getMax()).isNotZero();
            assertThat(record.getCount()).isNotZero();
            assertThat(record.getPercent()).isNotZero();
        });
    }

    @Test
    void get_histograms_with_specific_bins_number() {
        ResponseEntity<HistogramDto[]> response =
                restTemplate.getForEntity(getPath("elevation-histograms") + "?bins=3", HistogramDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).hasSize(3);
        var histogram = response.getBody();
        assertThat(histogram).allSatisfy(record -> {
            assertThat(record.getMin()).isNotZero();
            assertThat(record.getMax()).isNotZero();
            assertThat(record.getCount()).isNotZero();
            assertThat(record.getPercent()).isNotZero();
        });
    }

    @Test
    void get_quantiles() {
        ResponseEntity<QuantileDto[]> response =
                restTemplate.getForEntity(getPath("elevation-quantiles"), QuantileDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).hasSize(5);
        var quantiles = response.getBody();
        assertThat(quantiles).extracting(QuantileDto::getQuantile)
                        .containsExactly(0.0, 0.25, 0.5, 0.75, 1.0);
        assertThat(quantiles).allSatisfy(record -> {
            assertThat(record.getValue()).isNotZero();
        });
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        elevation
        slope
        tri
        tpi
        hillshade
        aspect
    """)
    void get_png_by_path(String path) {
        ResponseEntity<byte[]> response =
                restTemplate.getForEntity(getPath(path), byte[].class);
        verify_png_response(response);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        elevation | grayscale
        slope | grayscale
        tri | grayscale
        tpi | grayscale
        hillshade | grayscale
        aspect | grayscale
    """)
    void get_png_by_path_and_coloramp(String path, String colormap) {
        ResponseEntity<byte[]> response =
                restTemplate.getForEntity(getPath(path) + "?colormap=%s".formatted(colormap), byte[].class);
        verify_png_response(response);
    }


    @ParameterizedTest
    @CsvSource(textBlock = """
        elevation
        slope
        tri
        tpi
        hillshade
        aspect
    """)
    @Disabled
    void get_empty_png(String path) {
        ResponseEntity<byte[]> response =
                restTemplate.getForEntity(getRootUrl() + "/%s/25/0/0".formatted(path), byte[].class);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo(EMPTY_TILE);
    }

    private String getPath(String path) {
        return getRootUrl() + "/%s/2/0/0".formatted(path);
    }

    private void verify_png_response(ResponseEntity<byte[]> response) {
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).isNotEqualTo(EMPTY_TILE);
    }
}