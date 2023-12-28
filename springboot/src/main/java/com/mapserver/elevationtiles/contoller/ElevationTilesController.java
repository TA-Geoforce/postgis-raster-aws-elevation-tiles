package com.mapserver.elevationtiles.contoller;

import com.mapserver.elevationtiles.dto.HistogramDto;
import com.mapserver.elevationtiles.dto.QuantileDto;
import com.mapserver.elevationtiles.dto.StatisticsDto;
import com.mapserver.elevationtiles.exception.ResourceNotFoundException;
import com.mapserver.elevationtiles.repository.ElevationTilesProdRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequestMapping("/api/v1")
public class ElevationTilesController {

    public static final String EMPTY_TILE_STRING = "R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=";

    public static final byte[] EMPTY_TILE = EMPTY_TILE_STRING.getBytes(UTF_8);


    private static final Set<String> RELATIVE_PALETTES = Set.of("grayscale", "pseudocolor", "fire", "bluered");
    private final ElevationTilesProdRepository elevationTilesProdRepository;

    public ElevationTilesController(ElevationTilesProdRepository elevationTilesProdRepository) {
        this.elevationTilesProdRepository = elevationTilesProdRepository;
    }

    @Operation(summary = "Get statistics for specific tile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns all the statistics properties",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StatisticsDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class))) })
    @GetMapping(value = "/elevation-statistics/{z}/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "Requestor-Type", exposedHeaders = "X-Get-Header")
    @Transactional(readOnly = true)
    public ResponseEntity<StatisticsDto> getStatisticsByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y
    ) throws ResourceNotFoundException {
        StatisticsDto statistics =
        elevationTilesProdRepository.findStatisticsByZXY(z,x,y)
                .stream().findAny()
                .map(stats -> new StatisticsDto().fromDomainToDto(stats))
                .orElseThrow(() -> new ResourceNotFoundException("We couldn't find statistics for specific tile"));
        return ResponseEntity.ok().body(statistics);
    }

    @Operation(summary = "Get elevation statistics for zoom level and envelope geometry in WGS84")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns all the statistics properties",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StatisticsDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Statistics not found",
                    content = @Content(schema = @Schema(implementation = ResourceNotFoundException.class))) })
    @GetMapping(value = "/elevation-statistics/{z}/{xMin},{yMin},{xMax},{yMax}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "Requestor-Type", exposedHeaders = "X-Get-Header")
    public ResponseEntity<StatisticsDto> getStatisticsByZAndEnvelope(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X Minimum", description = "Minimum longitude", example = "0.0")
            @Min(value= -180, message = "Longitude should be greater than -180 degrees")
            @Max(value = 180, message = "Longitude should be less than 180 degrees")
            @PathVariable(value = "xMin")
            Float xMin,
            @Parameter(name = "Y Minimum", description = "Minimum latitude", example = "0.0")
            @Min(value= -90, message = "Latitude should be greater than -90 degrees")
            @Max(value = 90, message = "Latitude should be less than 90 degrees")
            @PathVariable(value = "yMin")
            Float yMin,
            @Parameter(name = "X Maximum", description = "Maximum longitude", example = "0.0")
            @Min(value= -180, message = "Longitude should be greater than -180 degrees")
            @Max(value = 180, message = "Longitude should be less than 180 degrees")
            @PathVariable(value = "xMax")
            Float xMax,
            @Parameter(name = "Y Maximum", description = "Minimum latitude", example = "0.0")
            @Min(value= -90, message = "Latitude should be greater than -90 degrees")
            @Max(value = 90, message = "Latitude should be less than 90 degrees")
            @PathVariable(value = "yMax")
            Float yMax
    ) throws ResourceNotFoundException {
        StatisticsDto statistics =
                elevationTilesProdRepository.findStatisticsByZAndEnvelope(z, xMin, yMin, xMax, yMax)
                        .stream().findAny()
                        .map(stats -> new StatisticsDto().fromDomainToDto(stats))
                        .orElseThrow(() -> new ResourceNotFoundException("We couldn't find statistics for specific zoom level and envelope"));
        return ResponseEntity.ok().body(statistics);
    }


    @Operation(summary = "Get Histogram for specific tile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns histogram",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HistogramDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/elevation-histograms/{z}/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "Requestor-Type", exposedHeaders = "X-Get-Header")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HistogramDto>> getHistogramsByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name= "Bins", description = "Number of the bins, default value 6", example = "3")
            @RequestParam(value = "bins", required = false)
            @Min(value = 1, message = "Number of bins should be larger thn 0")
            Integer bins
    ) {
        List<HistogramDto> histogram =
                elevationTilesProdRepository.findHistogramByZXY(z,x,y, bins==null? 6 : bins)
                        .stream()
                        .map(hist -> new HistogramDto().fromDomainToDto(hist))
                        .toList();
        return ResponseEntity.ok().body(histogram);
    }

    @Operation(summary = "Get quantiles (0%, 25%, 50%, 75%, 100%) for specific tile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns quantiles with a specific number of bins",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuantileDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/elevation-quantiles/{z}/{x}/{y}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "Requestor-Type", exposedHeaders = "X-Get-Header")
    @Transactional(readOnly = true)
    public ResponseEntity<List<QuantileDto>> getQuantilesByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y
    ) {
        List<QuantileDto> quantileDtos =
                elevationTilesProdRepository.findQuantileByZXY(z,x,y)
                        .stream()
                        .map(stats -> new QuantileDto().fromDomainToDto(stats))
                        .toList();
        return ResponseEntity.ok().body(quantileDtos);
    }

    @Operation(summary = "Get elevation tile in PNG format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a png tile image",
                    content = { @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                            schema = @Schema(implementation = byte[].class, example = EMPTY_TILE_STRING)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/elevation/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getElevationByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name="colormap", description = """
                    Keyword of a pre-defined colormap. Allowed values:
                      - any 5 value color ramp. Default `arctic` from http://soliton.vm.bytemark.co.uk/pub/cpt-city/mby/mby.pg
                      - `grayscale` or `greyscale` for a one 8BUI band raster of shades of gray.
                      - `pseudocolor` for a four 8BUI (RGBA) band raster with colors going from blue to green to red.
                      - `fire` for a four 8BUI (RGBA) band raster with colors going from black to red to pale yellow.
                      - `bluered` for a four 8BUI (RGBA) band raster with colors going from blue to pale white to red.
                      - `arctic` from http://soliton.vm.bytemark.co.uk/pub/cpt-city/arendal/arctic.pg
                    """, example = "pseudocolor")
            @RequestParam(value = "colormap", required = false)
            String colormap
    ) throws IOException {
        final String cr;
        if ("arctic".equals(colormap) || StringUtils.isEmpty(colormap)) {
            cr = IOUtils.toString(getClass().getResourceAsStream("/arctic.pg"), UTF_8);
        }
        else if(RELATIVE_PALETTES.contains(colormap)){
            cr = colormap;
        }
        else {
            cr = IOUtils.toString(getClass().getResourceAsStream("/mby.pg"), UTF_8);
        }
        byte[] png =
                elevationTilesProdRepository.getPng(z, x, y, cr);
        if(png == null){
            png = EMPTY_TILE;
        }
        return ResponseEntity.ok()
                .body(new ByteArrayResource(png));
    }

    @Operation(summary = "Get slope tile in PNG format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns a png slope tile image",
                    content = { @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                            schema = @Schema(implementation = byte[].class, example = EMPTY_TILE_STRING)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/slope/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getSlopeByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name="colormap", description = """
                    Keyword of a pre-defined colormap. Allowed values:
                      - any 5 value color ramp. Default http://soliton.vm.bytemark.co.uk/pub/cpt-city/esri/hillshade/tn/gray_dk.png.index.html.
                      - `grayscale` or `greyscale` for a one 8BUI band raster of shades of gray.
                      - `pseudocolor` for a four 8BUI (RGBA) band raster with colors going from blue to green to red.
                      - `fire` for a four 8BUI (RGBA) band raster with colors going from black to red to pale yellow.
                      - `bluered` for a four 8BUI (RGBA) band raster with colors going from blue to pale white to red.
                    """, example = "pseudocolor")
            @RequestParam(value = "colormap", required = false)
            String colormap
    ) throws IOException {
        final String cr = colormap==null? IOUtils.toString(getClass().getResourceAsStream("/gray_dk.pg"), UTF_8): colormap;
        byte[] png =
                elevationTilesProdRepository.getSlope(z, x, y, cr);
        if(png==null){
            png = EMPTY_TILE;
        }
        return ResponseEntity.ok()
                .body(new ByteArrayResource(png));
    }

    @Operation(summary = "Get terrain roughness index (TRI) tile in PNG format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                        Returns a png TRI tile image.
                        More information here -> https://postgis.net/docs/RT_ST_TRI.html
                    """,
                    content = { @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                            schema = @Schema(implementation = byte[].class, example = EMPTY_TILE_STRING)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/tri/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getTerrainRoughnessIndexByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name="colormap", description = """
                    Keyword of a pre-defined colormap. Allowed values:
                      - any 5 value color ramp. Default http://soliton.vm.bytemark.co.uk/pub/cpt-city/esri/hillshade/tn/gray_dk.png.index.html.
                      - `grayscale` or `greyscale` for a one 8BUI band raster of shades of gray.
                      - `pseudocolor` for a four 8BUI (RGBA) band raster with colors going from blue to green to red.
                      - `fire` for a four 8BUI (RGBA) band raster with colors going from black to red to pale yellow.
                      - `bluered` for a four 8BUI (RGBA) band raster with colors going from blue to pale white to red.
                    """, example = "pseudocolor")
            @RequestParam(value = "colormap", required = false)
            String colormap
    ) throws IOException {
        final String cr = colormap==null? IOUtils.toString(getClass().getResourceAsStream("/gray_dk.pg"), UTF_8): colormap;
        byte[] png =
                elevationTilesProdRepository.getTerrainRoughnessIndex(z, x, y, cr);
        if(png==null){
            png = EMPTY_TILE;
        }
        return ResponseEntity.ok()
                .body(new ByteArrayResource(png));
    }

    @Operation(summary = "Get topographic position index (TPI) tile in PNG format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                        Returns a png TPI tile image.
                        More information here -> https://postgis.net/docs/RT_ST_TPI.html
                    """,
                    content = { @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                            schema = @Schema(implementation = byte[].class, example = EMPTY_TILE_STRING)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/tpi/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getTopographicPositionIndexByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name="colormap", description = """
                    Keyword of a pre-defined colormap. Allowed values:
                      - any 5 value color ramp. Default http://soliton.vm.bytemark.co.uk/pub/cpt-city/esri/hillshade/tn/gray_dk.png.index.html.
                      - `grayscale` or `greyscale` for a one 8BUI band raster of shades of gray.
                      - `pseudocolor` for a four 8BUI (RGBA) band raster with colors going from blue to green to red.
                      - `fire` for a four 8BUI (RGBA) band raster with colors going from black to red to pale yellow.
                      - `bluered` for a four 8BUI (RGBA) band raster with colors going from blue to pale white to red.
                    """, example = "pseudocolor")
            @RequestParam(value = "colormap", required = false)
            String colormap
    ) throws IOException {
        final String cr = colormap==null? IOUtils.toString(getClass().getResourceAsStream("/gray_dk.pg"), UTF_8): colormap;
        byte[] png =
                elevationTilesProdRepository.getTopographicPositionIndex(z, x, y, cr);
        if(png==null){
            png = EMPTY_TILE;
        }
        return ResponseEntity.ok()
                .body(new ByteArrayResource(png));
    }

    @Operation(summary = "Get hillshade tile in PNG format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                        Returns a png hillshade tile image.
                        More information here -> https://postgis.net/docs/RT_ST_HillShade.html
                    """,
                    content = { @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                            schema = @Schema(implementation = byte[].class, example = EMPTY_TILE_STRING)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/hillshade/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getHillShadeByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name="colormap", description = """
                    Keyword of a pre-defined colormap. Allowed values:
                      - any 5 value color ramp. Default `00_bw_linear` http://soliton.vm.bytemark.co.uk.
                      - `grayscale` or `greyscale` for a one 8BUI band raster of shades of gray.
                      - `pseudocolor` for a four 8BUI (RGBA) band raster with colors going from blue to green to red.
                      - `fire` for a four 8BUI (RGBA) band raster with colors going from black to red to pale yellow.
                      - `bluered` for a four 8BUI (RGBA) band raster with colors going from blue to pale white to red.
                    """, example = "pseudocolor")
            @RequestParam(value = "colormap", required = false)
            String colormap
    ) throws IOException {
        final String cr = colormap==null? IOUtils.toString(getClass().getResourceAsStream("/00_bw_linear.pg"), UTF_8): colormap;
        byte[] png =
                elevationTilesProdRepository.getHillShade(z, x, y, cr);
        if(png==null){
            png = EMPTY_TILE;
        }
        return ResponseEntity.ok()
                .body(new ByteArrayResource(png));
    }


    @Operation(summary = "Get aspect tile in PNG format")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = """
                        Returns a png aspect tile image.
                        More information here -> https://postgis.net/docs/RT_ST_Aspect.html
                    """,
                    content = { @Content(mediaType = MediaType.IMAGE_PNG_VALUE,
                            schema = @Schema(implementation = byte[].class, example = EMPTY_TILE_STRING)) }),
            @ApiResponse(responseCode = "400", description = "Invalid z, y, x",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Tile not found",
                    content = @Content) })
    @GetMapping(value = "/aspect/{z}/{x}/{y}", produces = MediaType.IMAGE_PNG_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getAspectByZXY(
            @Parameter(name = "Zoom level", description = "The zoom level or else scale denominator", example = "15")
            @PathVariable(value = "z")
            @Min(value=0, message = "zoom level should be between 0 and 15")
            @Max(value=15, message = "zoom level should be between 0 and 15")
            Integer z,
            @Parameter(name = "X tile", description = "Row index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "x")
            Integer x,
            @Parameter(name = "Y tile", description = "Column index of the tile on the selected scale denominator", example = "15")
            @Min(value=0, message = "zoom level should be between 0 and 2^z")
            @Max(value = 32767, message = "zoom level should be between 0 and 2^z")
            @PathVariable(value = "y")
            Integer y,
            @Parameter(name="colormap", description = """
                    Keyword of a pre-defined colormap. Allowed values:
                      - any 5 value color ramp. Default `gray_dk` http://soliton.vm.bytemark.co.uk.
                      - `grayscale` or `greyscale` for a one 8BUI band raster of shades of gray.
                      - `pseudocolor` for a four 8BUI (RGBA) band raster with colors going from blue to green to red.
                      - `fire` for a four 8BUI (RGBA) band raster with colors going from black to red to pale yellow.
                      - `bluered` for a four 8BUI (RGBA) band raster with colors going from blue to pale white to red.
                    """, example = "pseudocolor")
            @RequestParam(value = "colormap", required = false)
            String colormap
    ) throws IOException {
        final String cr = colormap==null? IOUtils.toString(getClass().getResourceAsStream("/gray_dk.pg"), UTF_8): colormap;
        byte[] png =
                elevationTilesProdRepository.getAspect(z, x, y, cr);
        if(png==null){
            png = EMPTY_TILE;
        }
        return ResponseEntity.ok()
                .body(new ByteArrayResource(png));
    }

}
