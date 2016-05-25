package com.droletours.com.standdetector;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Rune on 27-04-2016.
 */
public class Classification implements Serializable {
    public static final String NULL_CLASSIFICATION = "null";
    public static final String EVENT_CLASSIFICATION = "event";
    public static final String SIT_CLASSIFICATION = "sit";
    public static final String STAND_CLASSIFICATION = "stand";
    private static DecimalFormat decimalFormat = new DecimalFormat("00.00");
    private String event_classification;
    private String sitstand_classification;
    private double[] event_distribution;
    private double[] sitstand_distribution;
    private Date timestamp;

    public Classification(String event_classification, String sitstand_classification, double[] event_distribution, double[] sitstand_distribution, Date timestamp) {
        this.event_classification = event_classification;
        this.sitstand_classification = sitstand_classification;
        this.event_distribution = event_distribution;
        this.sitstand_distribution = sitstand_distribution;
        this.timestamp = timestamp;
    }

    public Classification(String event_classification, String sitstand_classification) {
        this.event_classification = event_classification;
        this.sitstand_classification = sitstand_classification;
    }

    public Classification(String event_classification, String sitstand_classification, double[] event_distribution, double[] sitstand_distribution) {
        this.event_classification = event_classification;
        this.sitstand_classification = sitstand_classification;
        this.event_distribution = event_distribution;
        this.sitstand_distribution = sitstand_distribution;
    }

    public String getEvent_classification() {
        return event_classification;
    }

    public String getSitstand_classification() {
        return sitstand_classification;
    }

    public double[] getEvent_distribution() {
        return event_distribution;
    }

    public double[] getSitstand_distribution() {
        return sitstand_distribution;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public static String eventClassificationFromDistribution(double[] distribution){
        if (distribution[0] > 0.5){
            return Classification.EVENT_CLASSIFICATION;
        } else {
            return Classification.NULL_CLASSIFICATION;
        }
    }

    public static String eventClassificationFromLabel(double label){
        if(label == 1){
            return Classification.NULL_CLASSIFICATION;
        } else {
            return Classification.EVENT_CLASSIFICATION;
        }
    }

    public static String sitStandClassificationFromDistribution(double[] distribution){
        if (distribution[0] > 0.5){
            return Classification.SIT_CLASSIFICATION;
        } else {
            return Classification.STAND_CLASSIFICATION;
        }
    }

    public static String sitStandClassificationFromLabel(double label){
        if(label == 1){
            return Classification.STAND_CLASSIFICATION;
        } else {
            return Classification.SIT_CLASSIFICATION;
        }
    }


    @Override
    public String toString() {
        String timesting = "";
        if(timestamp != null){
            timesting = timesting.concat(", timestapm=" + timestamp.toString());
        }
        return "Classification{" +
                "evnt_cls='" + event_classification + '\'' +
                ", stst_cls='" + sitstand_classification + '\'' +
                ", evnt_dist=" + "[" +decimalFormat.format(event_distribution[0]) + " " +decimalFormat.format(event_distribution[1]) + "]" +
                ", stst_dist=" + "[" +decimalFormat.format(sitstand_distribution[0]) + " " +decimalFormat.format(sitstand_distribution[1]) + "]" +
                timesting+
                '}';
    }
}
