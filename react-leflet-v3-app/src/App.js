import "./styles.css";
import "leaflet/dist/leaflet.css";
import React, { useState } from 'react';
import { MapContainer, TileLayer, LayersControl, FeatureGroup } from "react-leaflet";
import { ScaleControl } from 'react-leaflet/ScaleControl'
import { EditControl } from "react-leaflet-draw";
import Control from "react-leaflet-custom-control";


export default function App() {

    let mby = "5000    255 255 255 255 \n \
    4000    206 206 206 255 \n \
    2800    161 161 161 255 \n \
    1800    130  30  30 255 \n \
    1200    163  68   0 255 \n \
    500     232 214 125 255 \n \
    50       16 123  48 255 \n \
    0         0  97  71 255 \n \
    -10     176 226 255 255 \n \
    -50     135 206 250 255 \n \
    -150     24 140 205 255 \n \
    -2500    19 108 160 255 \n \
    -4000     0  50 102 255 \n \
    -6000     0  30 100 255 \n \
    -8000     0   0  80 255";

    const [statistics, setStatistics] = useState(null)


    const _created = async (e) => {
        var bbox = e.layer.getLatLngs();
        let headers = new Headers();

        headers.append('Content-Type', 'application/json');
        headers.append('Accept', 'application/json');
        headers.append('Origin', 'http://localhost:3000');
        const response = await fetch("http://localhost:8080/api/v1/elevation-statistics/"
            + e.layer._map._zoom + "/"
            + bbox[0][0].lng + ","
            + bbox[0][0].lat + ","
            + bbox[0][2].lng + ","
            + bbox[0][2].lat);
        const data = await response.json();
        console.log(data);
        setStatistics(data)
    }

    return (
        <MapContainer center={[48.8566, 2.3522]} zoom={2}>
            {/* OPEN STREET MAPS TILES */}
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <LayersControl position="topright" collapsed={false}>
                <LayersControl.BaseLayer name="Elevation">
                    <TileLayer
                        url="http://localhost:8080/api/v1/elevation/{z}/{x}/{y}"
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Elevation with coloramp">
                    <TileLayer
                        url={"http://localhost:8080/api/v1/elevation/{z}/{x}/{y}?colormap=" + encodeURI(mby)}
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Slope">
                    <TileLayer
                        url="http://localhost:8080/api/v1/slope/{z}/{x}/{y}"
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Aspect">
                    <TileLayer
                        url="http://localhost:8080/api/v1/aspect/{z}/{x}/{y}"
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="TRI (Terrain Ruggedness Index)">
                    <TileLayer
                        url="http://localhost:8080/api/v1/tri/{z}/{x}/{y}"
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="TPI (Topographic Position Index)">
                    <TileLayer
                        url="http://localhost:8080/api/v1/tpi/{z}/{x}/{y}"
                    />
                </LayersControl.BaseLayer>
                <LayersControl.BaseLayer name="Hillshade">
                    <TileLayer
                        url="http://localhost:8080/api/v1/hillshade/{z}/{x}/{y}"
                    />
                </LayersControl.BaseLayer>
            </LayersControl>
            <ScaleControl />
            <FeatureGroup>
                <EditControl
                    position="topright"
                    onCreated={_created}
                    draw={
                        {
                            rectangle: true,
                            circle: false,
                            circlemarker: false,
                            marker: false,
                            polyline: false,
                            polygon: false,
                        }
                    }
                />
            </FeatureGroup>
            {statistics == null ? (<div></div>) : (
                <Control position="topright">
                    <div style={{ "background-color": "white" }}>
                        Elevation statistics:
                        <pre>
                            {JSON.stringify(statistics, null, 2)}
                        </pre>
                    </div>
                </Control>)}

        </MapContainer>
    );
}
