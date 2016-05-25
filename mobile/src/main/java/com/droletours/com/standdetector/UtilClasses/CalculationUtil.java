package com.droletours.com.standdetector.UtilClasses;

import java.util.AbstractMap;

import Core.SensorEventRecord;

/**
 * Created by Rune on 27-04-2016.
 */
public class CalculationUtil {
    public static AbstractMap.SimpleEntry<Double, Double> calculateRelativeMovement(SensorEventRecord acc){
        Double graX = (double) acc.getGravity()[0];
        Double graY = (double) acc.getGravity()[1];
        Double graZ = (double) acc.getGravity()[2];
        //subtract gravity
        Double x = (double) acc.getAcceleration()[0];
        Double y = (double) acc.getAcceleration()[1];
        Double z = (double) acc.getAcceleration()[2];
        x = x- graX;
        y = y- graY;
        z = z- graZ;
        //calculate C
        Double C = ((x*graX)+(y*graY)+(z*graZ))/((graX*graX)+(graY*graY)+(graZ*graZ));
        //calculate accUp
        Double p1 = Math.pow(graX,2);
        Double p2 = Math.pow(graY,2);
        Double p3 = Math.pow(graZ,2);
        Double accUp = Math.sqrt(p1 + p2 + p3)* C;
        Double overallAcc = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        //calculate accRest
        Double accRest = Math.sqrt(Math.pow(overallAcc,2)-Math.pow(accUp,2));
        return new AbstractMap.SimpleEntry<>(accUp, accRest);
    }
}
