package Core;

/**
 * Created by Rune on 05-04-2016.
 */
public class ClassificationEventRecord {
    private String event_label;
    private String standsit_label;
    private double[] event_distribution;
    private double[] sitstand_distribibution;

    public ClassificationEventRecord(String event_label, String standsit_label, double[] event_distribution, double[] sitstand_distribibution) {
        this.event_label = event_label;
        this.standsit_label = standsit_label;
        this.event_distribution = event_distribution;
        this.sitstand_distribibution = sitstand_distribibution;
    }

    public ClassificationEventRecord(String event_label, double[] event_distribution) {
        this.event_label = event_label;
        this.event_distribution = event_distribution;
    }

    public String getEvent_label() {
        return event_label;
    }

    public String getStandsit_label() {
        return standsit_label;
    }

    public double[] getEvent_distribution() {
        return event_distribution;
    }

    public double[] getSitstand_distribibution() {
        return sitstand_distribibution;
    }
}
