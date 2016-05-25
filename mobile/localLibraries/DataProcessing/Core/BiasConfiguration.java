package Core;

/**
 * Created by Rune on 19-04-2016.
 */
public class BiasConfiguration {
    private float bias_x_pos;
    private float bias_y_pos;
    private float bias_z_pos;
    private float bias_x_neg;
    private float bias_y_neg;
    private float bias_z_neg;

    public BiasConfiguration(float bias_x_pos, float bias_y_pos, float bias_z_pos, float bias_x_neg, float bias_y_neg, float bias_z_neg) {
        this.bias_x_pos = bias_x_pos;
        this.bias_y_pos = bias_y_pos;
        this.bias_z_pos = bias_z_pos;
        this.bias_x_neg = bias_x_neg;
        this.bias_y_neg = bias_y_neg;
        this.bias_z_neg = bias_z_neg;
    }

    public float getBias_x_pos() {
        return bias_x_pos;
    }

    public void setBias_x_pos(float bias_x_pos) {
        this.bias_x_pos = bias_x_pos;
    }

    public float getBias_y_pos() {
        return bias_y_pos;
    }

    public void setBias_y_pos(float bias_y_pos) {
        this.bias_y_pos = bias_y_pos;
    }

    public float getBias_z_pos() {
        return bias_z_pos;
    }

    public void setBias_z_pos(float bias_z_pos) {
        this.bias_z_pos = bias_z_pos;
    }

    public float getBias_x_neg() {
        return bias_x_neg;
    }

    public void setBias_x_neg(float bias_x_neg) {
        this.bias_x_neg = bias_x_neg;
    }

    public float getBias_y_neg() {
        return bias_y_neg;
    }

    public void setBias_y_neg(float bias_y_neg) {
        this.bias_y_neg = bias_y_neg;
    }

    public float getBias_z_neg() {
        return bias_z_neg;
    }

    public void setBias_z_neg(float bias_z_neg) {
        this.bias_z_neg = bias_z_neg;
    }

    @Override
    public String toString() {
        return "BiasConfiguration{" +
                "bias_x_pos=" + bias_x_pos +
                ", bias_y_pos=" + bias_y_pos +
                ", bias_z_pos=" + bias_z_pos +
                ", bias_x_neg=" + bias_x_neg +
                ", bias_y_neg=" + bias_y_neg +
                ", bias_z_neg=" + bias_z_neg +
                '}';
    }
}
