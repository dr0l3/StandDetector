package Core;

/**
 * Created by Rune on 30-03-2016.
 */
public class SensorEventRecord {

    private float[] acceleration;
    private float[] gravity;
    private float[] rotation;
    private ProximityValue proximity;
    private long timestamp;

    public SensorEventRecord(long timestamp, float[] proximity, float[] gravity, float[] acceleration) {
        this.timestamp = timestamp;
        float prox_temp = proximity[0];
        this.proximity = (prox_temp > 5)? ProximityValue.FAR : ProximityValue.NEAR;
        this.gravity = gravity;
        this.acceleration = acceleration;
    }

    public SensorEventRecord(float[] acceleration, float[] gravity, ProximityValue proximity, long timestamp) {
        this.acceleration = acceleration;
        this.gravity = gravity;
        this.proximity = proximity;
        this.timestamp = timestamp;
    }

    public SensorEventRecord(float[] acceleration, float[] gravity, float[] rotation, long timestamp) {
        this.acceleration = acceleration;
        this.gravity = gravity;
        this.rotation = rotation;
        this.timestamp = timestamp;
    }

    public SensorEventRecord(float[] acceleration, float[] gravity, long timestamp) {
        this.acceleration = acceleration;
        this.gravity = gravity;
        this.timestamp = timestamp;
    }

    public float[] getAcceleration() {
        return acceleration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float[] getGravity() {
        return gravity;
    }

    public float[] getRotation() {
        return rotation;
    }

    public ProximityValue getProximity() {
        return proximity;
    }

    public void applyBias(BiasConfiguration bias){
        if (gravity[0] < 0)
            acceleration[0] = acceleration[0] - (gravity[0] * bias.getBias_x_neg());
        else
            acceleration[0] = acceleration[0] - (gravity[0] * bias.getBias_x_pos());

        if (gravity[1] < 0)
            acceleration[1] = acceleration[1] - (gravity[1] * bias.getBias_y_neg());
        else
            acceleration[1] = acceleration[1] - (gravity[1] * bias.getBias_y_pos());

        if (gravity[2] < 0)
            acceleration[2] = acceleration[2] - (gravity[2] * bias.getBias_z_neg());
        else
            acceleration[2] = acceleration[2] - (gravity[2] * bias.getBias_z_pos());

    }
}
