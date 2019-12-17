package com.ltm.backend.controller.cartonization;

/**
 * Этот класс содержит информацию о максимальных линейных измерениях и суммарном объеме товаров,
 * что должно помочь при подборе коробки
 */
public class DimensionsData {
    private double maxLongestDim;
    private double maxMiddleDim;
    private double maxShortestDim;
    private double totalVolume;

    public double getMaxLongestDim() {
        return maxLongestDim;
    }

    public double getMaxMiddleDim() {
        return maxMiddleDim;
    }

    public double getMaxShortestDim() {
        return maxShortestDim;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    /**
     * Добавить информацию о линейных размерах, объеме и количестве очередного товара.
     * Если указан нулевой объем, то он будет рассчитан как произведение линейных измерений.
     */
    public void merge(double x, double y, double z, double volume, double count) {
        Box box = Box.of(x, y, z, volume);

        maxLongestDim = Math.max(maxLongestDim, box.getLongestDim());
        maxMiddleDim = Math.max(maxMiddleDim, box.getMiddleDim());
        maxShortestDim = Math.max(maxShortestDim, box.getShortestDim());
        totalVolume += box.getVolume() * count;
    }
}
