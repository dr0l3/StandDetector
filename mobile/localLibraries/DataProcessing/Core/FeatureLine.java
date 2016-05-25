package Core;

import java.io.Serializable;

/**
 * Created by Rune on 08-03-2016.
 */
public class FeatureLine implements Serializable {
    private Double accX;
    private Double accY;
    private Double accZ;
    private Double effAccX;
    private Double effAccY;
    private Double effAccZ;
    private Double graX;
    private Double graY;
    private Double graZ;
    private Double rotX;
    private Double rotY;
    private Double rotZ;
    private ProximityValue proximity;
    private int timestamp;
    private Double accUp;
    private Double accRest;


    public FeatureLine(Double accX, Double accY, Double accZ, Double graX, Double graY, Double graZ, Double rotX, Double rotY, Double rotZ, ProximityValue proximity,  int timestamp) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.graX = graX;
        this.graY = graY;
        this.graZ = graZ;
        this.timestamp = timestamp;
        this.proximity = proximity;
        calculateRelativeMovement();
    }

    public FeatureLine(FeatureLine featureLine){
        this.accX = featureLine.getAccX();
        this.accY = featureLine.getAccY();
        this.accZ = featureLine.getAccZ();
        this.graX = featureLine.getGraX();
        this.graY = featureLine.getGraY();
        this.graZ = featureLine.getGraZ();
        this.timestamp = featureLine.getTimestamp();
        this.proximity = featureLine.getProximity();
        calculateRelativeMovement();
    }

    public FeatureLine(Double accX, Double accY, Double accZ, Double graX, Double graY, Double graZ, int timestamp, ProximityValue proximity) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.graX = graX;
        this.graY = graY;
        this.graZ = graZ;
        this.timestamp = timestamp;
        this.proximity = proximity;
        calculateRelativeMovement();
    }

    public FeatureLine(Double accX, Double accY, Double accZ, Double rotX, Double rotY, Double rotZ, Double graX, Double graY, Double graZ, int timestamp) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.graX = graX;
        this.graY = graY;
        this.graZ = graZ;
        this.timestamp = timestamp;
        calculateRelativeMovement();
    }

    public FeatureLine(Double accX, Double accY, Double accZ, Double graX, Double graY, Double graZ, int timestamp) {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.graX = graX;
        this.graY = graY;
        this.graZ = graZ;
        this.timestamp = timestamp;
        calculateRelativeMovement();
    }

    private void calculateRelativeMovement(){
        //subtract gravity
        effAccX = accX-graX;
        effAccY = accY-graY;
        effAccZ = accZ-graZ;
        //calculate C
        Double C = ((effAccX*graX)+(effAccY*graY)+(effAccZ*graZ))/((graX*graX)+(graY*graY)+(graZ*graZ));
        //calculate accUp
        Double p1 = Math.pow(graX,2);
        Double p2 = Math.pow(graY,2);
        Double p3 = Math.pow(graZ,2);
        accUp = Math.sqrt(p1 + p2 + p3) * C;
        Double overallAcc = Math.sqrt(Math.pow(effAccX,2)+Math.pow(effAccY,2)+Math.pow(effAccZ,2));
        //calculate accRest
        accRest = Math.sqrt(Math.pow(overallAcc,2)-Math.pow(accUp,2));
    }

    public Double getAccUp() {
        return accUp;
    }

    public Double getAccRest() {
        return accRest;
    }

    public Double getEffAccX() {
        return effAccX;
    }

    public Double getEffAccY() {
        return effAccY;
    }

    public Double getEffAccZ() {
        return effAccZ;
    }

    public Double getAccX() {
        return accX;
    }

    public Double getAccY() {
        return accY;
    }

    public Double getAccZ() {
        return accZ;
    }

    public Double getGraX() {
        return graX;
    }

    public Double getGraY() {
        return graY;
    }

    public Double getGraZ() {
        return graZ;
    }

    public Double getRotX() {
        return rotX;
    }

    public Double getRotY() {
        return rotY;
    }

    public Double getRotZ() {
        return rotZ;
    }

    public ProximityValue getProximity() {
        return proximity;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void applyBias(BiasConfiguration bias) {
        if (graX < 0)
            accX = accX - (graX * bias.getBias_x_neg());
        else
            accX = accX - (graX * bias.getBias_x_pos());

        if (graY < 0)
            accY = accY - (graY * bias.getBias_y_neg());
        else
            accY = accY - (graY * bias.getBias_y_pos());

        if (graZ < 0)
            accZ = accZ - (graZ * bias.getBias_z_neg());
        else
            accZ = accZ - (graZ * bias.getBias_z_pos());

        calculateRelativeMovement();
    }
}
