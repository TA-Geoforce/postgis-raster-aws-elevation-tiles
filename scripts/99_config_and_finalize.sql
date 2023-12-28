-- create indexes and vacuum table
CREATE INDEX IF NOT EXISTS "elevation_tiles_prod_convexhull" ON "elevation_tiles_prod" USING gist(st_convexhull("rast"));

ANALYZE "elevation_tiles_prod";
