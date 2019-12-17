package com.ltm.backend.model;

import java.io.Serializable;
import java.util.Objects;

public class Carton implements Comparable<Carton>, Serializable {
    private final String cartonGroup;
    private final String cartonType;
    private final String cartonDescription;
    private final double cube;
    private final double width;
    private final double height;
    private final double length;
    private final int sequence;

    public Carton(String cartonGroup,
                  String cartonType,
                  String cartonDescription,
                  double cube,
                  double width,
                  double height,
                  double length,
                  int sequence) {
        this.cartonGroup = cartonGroup;
        this.cartonType = cartonType;
        this.cartonDescription = cartonDescription;
        this.cube = cube;
        this.width = width;
        this.height = height;
        this.length = length;
        this.sequence = sequence;
    }

    public String getCartonGroup() {
        return cartonGroup;
    }

    public String getCartonType() {
        return cartonType;
    }

    public String getCartonDescription() {
        return cartonDescription;
    }

    public double getCube() {
        return cube;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getLength() {
        return length;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Carton that = (Carton) o;
        return Double.compare(that.cube, cube) == 0 &&
            Double.compare(that.width, width) == 0 &&
            Double.compare(that.height, height) == 0 &&
            Double.compare(that.length, length) == 0 &&
            Objects.equals(cartonGroup, that.cartonGroup) &&
            Objects.equals(cartonType, that.cartonType) &&
            Objects.equals(cartonDescription, that.cartonDescription) &&
            Objects.equals(sequence, that.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartonGroup, cartonType, cartonDescription, cube, width, height, length, sequence);
    }

    @Override
    public int compareTo(Carton carton) {
        return Double.compare(cube, carton.cube);
    }

    @Override
    public String toString() {
        return "CartonType{" +
            "cartonGroup='" + cartonGroup + '\'' +
            ", cartonType='" + cartonType + '\'' +
            ", cartonDescription='" + cartonDescription + '\'' +
            ", cube=" + cube +
            ", width=" + width +
            ", height=" + height +
            ", length=" + length +
            ", sequence=" + sequence +
            '}';
    }
}
