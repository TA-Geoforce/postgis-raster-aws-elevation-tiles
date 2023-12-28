CREATE TABLE "elevation_tiles_prod"(
    "rid" serial PRIMARY KEY,
    "rast" raster,
    z int GENERATED ALWAYS AS (split_part(ST_BandPath(rast), '/', 6)::int) STORED,
    x int GENERATED ALWAYS AS (split_part(ST_BandPath(rast), '/', 7)::int) STORED,
    y int GENERATED ALWAYS AS (replace(split_part(ST_BandPath(rast), '/', 8), '.tif', '')::int) STORED,
    UNIQUE (z, x, y)
);