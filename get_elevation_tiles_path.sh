#!/bin/bash

export PGUSER=postgres
export PGPASSWORD=postgres
export AWS_NO_SIGN_REQUEST=YES
export GTIFF_DIRECT_IO=YES
export MAX_ZOOM_LEVEL=9
export MIN_ZOOM_LEVEL=9

count=0

for ((i = $MIN_ZOOM_LEVEL; i <= $MAX_ZOOM_LEVEL; i++)); do
    for ((y = 0; y < (2**$i); y++ )); do
        for ((x = 0; x < (2**$i); x++)); do
        count=$((count+1))
        echo "Zoom level $i, Y= $y, X= $x, count= $count"
        echo "/vsis3/elevation-tiles-prod/v2/geotiff/$i/$x/$y.tif" >> tiles.txt
        #raster2pgsql -a -s 3857 -R -t 256x256 -k -Y /vsis3/elevation-tiles-prod/v2/geotiff/$i/$x/$y.tif elevation_tiles_prod | psql -h localhost -p 5432 -U postgres -d postgres
        done
    done
done