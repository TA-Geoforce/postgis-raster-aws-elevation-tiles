package com.mapserver.elevationtiles.repository;

import com.mapserver.elevationtiles.domain.ElevationTilesProd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ElevationTilesProdRepository extends JpaRepository<ElevationTilesProd, Long> {

    @Query(value = """
            SELECT (stats).min as min,
                (stats).max as max,
                (stats).count as count,
                (stats).sum as sum,
                (stats).mean as mean,
                (stats).stddev as stddev
            FROM (SELECT rid, ST_SummaryStats(rast, 1) As stats
                FROM elevation_tiles_prod
                where z= :z and x= :x and y= :y) AS etp
            """, nativeQuery = true)
    Optional<Statistics> findStatisticsByZXY(@Param("z") int z, @Param("x") int x, @Param("y") int y);

    @Query(value = """
            WITH rectangle AS (
                   SELECT
                       ST_Transform(ST_MakeEnvelope(:xMin, :yMin, :xMax, :yMax, 4326), 3857) geom
               ), statistics AS (
               SELECT
                   ST_SummaryStats(ST_UNION(ST_Clip(rast, r.geom)), 1) AS stats
               FROM
                   elevation_tiles_prod etp,
                   rectangle r
               WHERE
                   z = :z
                   AND ST_Intersects(r.geom, etp.rast))
            SELECT (stats).min as min,
                (stats).max as max,
                (stats).count as count,
                (stats).sum as sum,
                (stats).mean as mean,
                (stats).stddev as stddev
            FROM statistics
            """, nativeQuery = true)
    Optional<Statistics> findStatisticsByZAndEnvelope(@Param("z") int z, @Param("xMin") float xMin, @Param("yMin") float yMin, @Param("xMax") float xMax, @Param("yMax") float yMax);

    @Query(value = """
            SELECT (stats).min as min,
                (stats).max as max,
                (stats).count as count,
                (stats).percent as percent
            FROM (SELECT rid, ST_Histogram(rast, 1, :bins) As stats
                FROM elevation_tiles_prod
                where z= :z and x= :x and y= :y) AS etp
            ORDER BY (stats).min
            """, nativeQuery = true)
    Collection<Histogram> findHistogramByZXY(@Param("z") int z, @Param("x") int x, @Param("y") int y, @Param("bins") int bins);


    @Query(value = """
            SELECT (quantile).*
            FROM (SELECT rid, ST_Quantile(rast, 1) AS quantile
                FROM elevation_tiles_prod
                WHERE z= :z AND x= :x AND y= :y) AS etp
            """, nativeQuery = true)
    Collection<Quantile> findQuantileByZXY(@Param("z") int z, @Param("x") int x, @Param("y") int y);


    @Query(value = """
            SELECT ST_AsPNG(ST_ColorMap(rast, 1, :colormap))
                FROM elevation_tiles_prod
            WHERE z= :z AND x= :x AND y= :y
            """, nativeQuery = true)
    byte[] getPng(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);


    @Query(value = """
            SELECT ST_AsPNG(ST_ColorMap(ST_Slope(rast), 1, :colormap))
                FROM elevation_tiles_prod
            WHERE z= :z AND x= :x AND y= :y
            """, nativeQuery = true)
    byte[] getSlope(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);


    @Query(value = """
            SELECT ST_AsPNG(ST_ColorMap(ST_TRI(rast), 1, :colormap))
                FROM elevation_tiles_prod
            WHERE z= :z AND x= :x AND y= :y
            """, nativeQuery = true)
    byte[] getTerrainRoughnessIndex(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);

    @Query(value = """
            SELECT ST_AsPNG(ST_ColorMap(ST_TPI(rast), 1, :colormap))
                FROM elevation_tiles_prod
            WHERE z= :z AND x= :x AND y= :y
            """, nativeQuery = true)
    byte[] getTopographicPositionIndex(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);

    @Query(value = """
            SELECT ST_AsPNG(ST_ColorMap(ST_HillShade(rast), 1, :colormap))
                FROM elevation_tiles_prod
            WHERE z= :z AND x= :x AND y= :y
            """, nativeQuery = true)
    byte[] getHillShadeOnlyTile(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);

    @Query(value = """
            WITH metadata_tile AS (
                SELECT
                    ST_Envelope(rast) AS envelope,
            (ST_MetaData(rast)).*
                FROM
                    elevation_tiles_prod
                WHERE
                    z = :z
                    AND x = :x
                    AND y = :y
            ),
            buffered_envelope AS (
                SELECT
                    ST_Buffer(mt.envelope, mt.scalex*100) AS geom
                FROM
                    metadata_tile mt
            ),
            clipped AS (
                SELECT
                    ST_Clip(St_Union(rast), bf.geom) AS rast
                FROM
                    elevation_tiles_prod etp,
                    buffered_envelope bf
                WHERE
                    ST_Intersects(etp.rast, bf.geom)
                    AND z = :z
                GROUP BY
                    bf.geom
            )
            SELECT
                ST_AsPNG(ST_ColorMap(ST_HillShade(rast), 1, :colormap))
            FROM
                clipped
            """, nativeQuery = true)
    byte[] getHillShade(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);

    @Query(value = """
            SELECT ST_AsPNG(ST_ColorMap(ST_Aspect(rast), 1, :colormap))
                FROM elevation_tiles_prod
            WHERE z= :z AND x= :x AND y= :y
            """, nativeQuery = true)
    byte[] getAspect(@Param("z") int z, @Param("x") int x, @Param("y") int y,  @Param("colormap") String colormap);

    interface BasicStatistics {
        double getMin();
        double getMax();
        long getCount();
    }
    interface Histogram extends BasicStatistics {
        double getPercent();
    }

    interface Statistics extends BasicStatistics {
        double getSum();
        double getMean();
        double getStddev();
    }

    interface Quantile {
        double getQuantile();
        double getValue();
    }
}
