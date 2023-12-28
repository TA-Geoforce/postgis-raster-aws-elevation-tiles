package com.mapserver.elevationtiles.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "elevation_tiles_prod")
public class ElevationTilesProd {
    @Id
    private int rid;
    @Column
    private int z;
    @Column
    private int x;
    @Column
    private int y;

    public int getRid() {
        return rid;
    }

    public ElevationTilesProd setRid(int rid) {
        this.rid = rid;
        return this;
    }

    public int getZ() {
        return z;
    }

    public ElevationTilesProd setZ(int z) {
        this.z = z;
        return this;
    }

    public int getX() {
        return x;
    }

    public ElevationTilesProd setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public ElevationTilesProd setY(int y) {
        this.y = y;
        return this;
    }

    @Override
    public String toString() {
        return "ElevationTilesProd{" +
                "id=" + rid +
                ", z=" + z +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
