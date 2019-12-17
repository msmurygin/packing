package com.ltm.backend.controller.cartonization;

import com.ltm.backend.model.Carton;

import java.util.Arrays;

/**
 * Коробка.
 * Обладает линейными размерами и объемом.
 */
public class Box {
    private final double longestDim;
    private final double middleDim;
    private final double shortestDim;
    private final double volume;

    /**
     * Создает новую коробку.
     * Если указан нулевой объем, то он будет рассчитан как произведение линейных измерений.
     */
    public static Box of(double x, double y, double z, double volume) {
        double[] dims = new double[] {x, y, z};
        Arrays.sort(dims);
        double vol = volume > 0 ? volume : x * y * z;
        return new Box(dims[2], dims[1], dims[0], vol);
    }

    public static Box of(Carton carton) {
        return of(carton.getLength(), carton.getWidth(), carton.getHeight(), carton.getCube());
    }

    private Box(double longestDim, double middleDim, double shortestDim, double volume) {
        this.longestDim = longestDim;
        this.middleDim = middleDim;
        this.shortestDim = shortestDim;
        this.volume = volume;
    }

    public double getLongestDim() {
        return longestDim;
    }

    public double getMiddleDim() {
        return middleDim;
    }

    public double getShortestDim() {
        return shortestDim;
    }

    public double getVolume() {
        return volume;
    }
}
