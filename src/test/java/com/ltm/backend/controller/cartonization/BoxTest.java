package com.ltm.backend.controller.cartonization;

import com.ltm.backend.model.Carton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoxTest {

    @Test
    void createBoxWithVolume() {
        Box box = Box.of(12.5, 45.6, 25.2, 99.9);
        assertEquals(45.6, box.getLongestDim());
        assertEquals(25.2, box.getMiddleDim());
        assertEquals(12.5, box.getShortestDim());
        assertEquals(99.9, box.getVolume());
    }

    @Test
    void createBoxWithoutVolume() {
        Box box = Box.of(354.6, 254.5, 254.51, 0);
        assertEquals(354.6, box.getLongestDim());
        assertEquals(254.51, box.getMiddleDim());
        assertEquals(254.5, box.getShortestDim());
        assertEquals(box.getLongestDim() * box.getMiddleDim() * box.getShortestDim(), box.getVolume());
    }

    @Test
    void createBoxFromCartonWithVolume() {
        Carton carton = new Carton("cg", "ct", "cd", 999.9, 123.1, 123.3, 123.2,0);
        Box box = Box.of(carton);
        assertEquals(123.3, box.getLongestDim());
        assertEquals(123.2, box.getMiddleDim());
        assertEquals(123.1, box.getShortestDim());
        assertEquals(999.9, box.getVolume());
    }

    @Test
    void createBoxFromCartonWithoutVolume() {
        Carton carton = new Carton("cg", "ct", "cd", 0, 128, 256, 512,0);
        Box box = Box.of(carton);
        assertEquals(512, box.getLongestDim());
        assertEquals(256, box.getMiddleDim());
        assertEquals(128, box.getShortestDim());
        assertEquals(carton.getHeight() * carton.getWidth() * carton.getLength() , box.getVolume());
    }
}
