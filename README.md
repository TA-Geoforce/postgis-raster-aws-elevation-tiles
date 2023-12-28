# postgis-raster-aws-elevation-tiles

PoC using PostGIS Raster extension with Terrain Tiles, an AWS open dataset of elevation tiles.

![Preview](/images/aws_elevation_preview.webp)
*Figure 1. Preview application using Leaflet*

## AWS Open Data

We are using Terrain Tiles, an open dataset from AWS, more details [here](https://registry.opendata.aws/terrain-tiles/)

If you want to to produce all the `s3` uri for a specific zoom level, you can use commands like the above.

```bash
aws s3 ls --no-sign-request s3://elevation-tiles-prod/v2/geotiff/15/ --recursive | awk '{print $4}' | sed 's/^/\/vsis3\/elevation-tiles-prod\//' > elevation-tiles-prod.txt
```

## PostGis Raster

Postgres/PostGis is extended with raster capabilities using under the hood GDAL. More information here:

- [PostGis Rasters](https://postgis.net/workshops/de/postgis-intro/rasters.html)
- [Raster Data Management, Queries, and Applications](https://postgis.net/docs/using_raster_dataman.html)
- [Raster Reference](https://postgis.net/docs/RT_reference.html)

## How to setup the raster elevation dataset into a Postgres DB

Examples that uses `aws s3 ls`

```bash
export PGPASSWORD='postgres';
export AWS_NO_SIGN_REQUEST=YES
export GTIFF_DIRECT_IO=YES

// Copy geotiff locally
aws s3 cp --no-sign-request s3://elevation-tiles-prod/v2/geotiff/15/0/10850.tif 10850.tif

// Check geotiff info
gdalinfo /vsis3/elevation-tiles-prod/v2/geotiff/15/0/10850.tif

// Create an sql file with creating table
raster2pgsql -s 3857 -I -M -R -t 256x256 /vsis3/elevation-tiles-prod/v2/geotiff/15/0/10850.tif elevation_tiles_prod > inject_data_with_creating_table.sql

// Create an sql file, just importing
raster2pgsql -a -s 3857 -R -t 256x256 -k -Y /vsis3/elevation-tiles-prod/v2/geotiff/15/0/10850.tif elevation_tiles_prod > inject_data.sql

// Import to postgres DB raster
raster2pgsql -a -s 3857 -R -t 256x256 -k -Y /vsis3/elevation-tiles-prod/v2/geotiff/15/0/10850.tif elevation_tiles_prod | psql -h localhost -p 5432 -U postgres -d postgres

// Create txt file with all the import commands
aws s3 ls --no-sign-request s3://elevation-tiles-prod/v2/geotiff/1/ --recursive | awk '{print "raster2pgsql -a -s 3857 -R -t 256x256 -k -Y /vsis3/elevation-tiles-prod/" $4 " elevation_tiles_prod | psql -h localhost -p 5432 -U postgres -d postgres"}' >> inject_tiles_into_db_for_specific_zoom_level.txt

// Import raster directly to postgres DB
aws s3 ls --no-sign-request s3://elevation-tiles-prod/v2/geotiff/2/ --recursive | awk '{print "raster2pgsql -a -s 3857 -R -t 256x256 -k -Y /vsis3/elevation-tiles-prod/" $4 " elevation_tiles_prod | psql -h localhost -p 5432 -U postgres -d postgres"}' | bash
```

Or we can use a small script to autmate the proceduce. Set min and max elevation tiles in the script [get_elevation_tiles_path.sh](/get_elevation_tiles_path.sh)

```bash
./get_elevation_tiles_path.sh
```

Finally, execute this to insert the data

```bash
export PGUSER=postgres
export PGPASSWORD=postgres
export AWS_NO_SIGN_REQUEST=YES
export GTIFF_DIRECT_IO=YES

parallel --jobs 1000 -a tiles.txt raster2pgsql -a -s 3857 -R -t 256x256 -k -Y {} elevation_tiles_prod | psql -h localhost -p 5432 -U postgres -d postgres
```

*For zoom level 7 we added 16.369 from the 16.384 tiles in 20 minutes.
8 zoom level 1 hour and 30 mins.*

## Inject data to a Postgres DB that already exists

You have your own infrastructure setup and and what to inject the data, just what you need to do is run these sql [scripts](/scripts/).

### If you have your own elevation-tiles bucket

You can simply skip the sql scripts [here](/scripts/) and rename the file [13_add_data.sha](/scripts/13_add_data.sha) to `13_add_data.sh` and specify the `MAX_ZOOM_LEVEL`, `s3 path` and when you run `docker compose up` it will inject your own dataset.

## Start the application

```bash
docker compose up
```

## Dismiss everything

```bash
docker compose down -v --rmi local
```

## TMS and Statistical endpoints

We are supporting Tile Map Service [TMS](https://en.wikipedia.org/wiki/Tile_Map_Service) and statistical endpoints by raster processing: DEM (Elevation)
More information here <https://postgis.net/docs/RT_reference.html#Raster_Processing_DEM>

To check the available endpoints, please, check out here [elevation-tiles-controller](http://localhost:8080/swagger-ui/index.html)

![EndPoints Preview](/images/preview-service-api.jpeg)
*Figure 2. OpenAPI endpoints*

## Start SpringBoot service standalone

```bash
cd springboot
mvn spring-boot:run
```

## Using custom color ramps

Here the limits are endless, since for every type of layer `elevation`, `slope`, `tri`, `tpi`, `hillshade`, `aspect`, besides the TMS `{z}/{x}/{y}` you can specify the `coloramp`, by passing in `pg` format (Color-maps used by PostGIS, in particular those for the ST_ColorMap function). The best color ramps I found are here -> [cpt-city](http://soliton.vm.bytemark.co.uk/pub/cpt-city/)

## Benchmark

For this benchmarking, we used [oha](https://github.com/hatoo/oha) pointing out the `localhost`.

For example

```bash
oha  "http://localhost:8080/api/v1/elevation/0/0/0?colormap=pseudocolor"
```

### Results

![Single tile elevation](/images/benchmark-elevation.png)
*Figure 3. Single tile elevation request*

![Single tile elevation rendering with a coloramp](/images/benchmark-elevation-coloramp.png)
*Figure 4. Single tile elevation rendering with a coloramp*

![Statistics of elevation tile](/images/benchmark-elevation-statistics.png)
*Figure 5. Single elevation tile statistics request*

![Statistics by zoom level and extend](/images/bechmark-elevation-statistics-extend.png)
*Figure 6. Elevation statistics from 4 tiles (zoom level 4), by specific extend*

![Slope single tile](/images/bechmark-slope.png)
*Figure 7. Slope request of single tile*

From figures 3-7 we can conclude that: **PostGis Raster using `outdb` rasters stored in `S3` works and performs very well**

*Tip: For better benchmarking it would have been better to have our own clone of terrain-tiles in `S3` and the `Postgres` and `SpringBoot` should be in separated EC2 instances, located in the same region as the S3 bucket.*

## Conclusions

As the title says, this is a *proof of concept*, which means you can experiment more with the final setup of the infrastructure, `postgis.gdal_vsi_options` (you can find it [here](docker-compose.yaml#52)), but all you need -after you set up the data in DB- is to create your own application in every language/framework you want and use these native queries as we do in the `ElevationTilesProdRepository` class [here](/springboot/src/main/java/com/mapserver/elevationtiles/repository/ElevationTilesProdRepository.java).

**HAVE FUN!**
