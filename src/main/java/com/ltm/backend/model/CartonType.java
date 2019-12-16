package com.ltm.backend.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class CartonType implements Comparable<CartonType>, Serializable {

    private String cartonGroup;
    private String cartonType;
    private String cartonDescription;
    private double cube;
    private double width;
    private double height;
    private double length;
    private Integer sequence;

    private int parcelsQuantity;

    public CartonType(String cartonGroup, String cartonType, String cartonDescription, double cube, double width, double height, double length, int sequence) {
        this.cartonGroup = cartonGroup != null ? cartonGroup.trim() : null;
        this.cartonType = cartonType != null ? cartonType.trim() : null;
        this.cartonDescription = cartonDescription != null ? cartonDescription.trim() : null;
        this.cube = cube;
        this.width = width;
        this.height = height;
        this.length = length;
        this.sequence = sequence;
    }

    public double getBoxVolume() {
        double cube = getCube();
        if (cube == 0) {
            return 1;
        } else {
            return cube;
        }
    }

    public String getCartonGroup() {
        return cartonGroup;
    }

    public void setCartonGroup(String cartonGroup) {
        this.cartonGroup = cartonGroup;
    }

    public String getCartonType() {
        return cartonType;
    }

    public void setCartonType(String cartonType) {
        this.cartonType = cartonType;
    }

    public String getCartonDescription() {
        return cartonDescription;
    }

    public void setCartonDescription(String cartonDescription) {
        this.cartonDescription = cartonDescription;
    }

    public double getCube() {
        return cube;
    }

    public void setCube(double cube) {
        this.cube = cube;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public int getParcelsQuantity() {
        return parcelsQuantity;
    }

    public void setParcelsQuantity(int parcelsQuantity) {
        this.parcelsQuantity = parcelsQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartonType that = (CartonType) o;
        return Double.compare(that.cube, cube) == 0 &&
            Double.compare(that.width, width) == 0 &&
            Double.compare(that.height, height) == 0 &&
            Double.compare(that.length, length) == 0 &&
            Objects.equals(cartonGroup, that.cartonGroup) &&
            Objects.equals(cartonType, that.cartonType) &&
            Objects.equals(cartonDescription, that.cartonDescription) &&
            Objects.equals(sequence, that.sequence) &&
            Double.compare(that.parcelsQuantity, parcelsQuantity) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartonGroup, cartonType, cartonDescription, cube, width, height, length, sequence, parcelsQuantity);
    }

    @Override
    public int compareTo(CartonType o) {
        return Comparator.comparingDouble(CartonType::getCube)
            .compare(this, o);
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
            ", parcelsQuantity=" + parcelsQuantity +
            '}';
    }
}
