package com.droletours.com.standdetector;

/**
 * Created by Rune on 27-04-2016.
 */
public class Correction {

    private String classified_event;
    private String classified_sitstand;
    private String corrected_event;
    private String correction_sitstand;
    public static final String CORRECTION_SIT =  "/correction_sit";
    public static final String CORRECTION_STAND =  "/correction_stand";
    public static final String CORRECTION_NULL =  "/correction_null";
    public static final String CORRECTION_WRONG =  "/correction_wrong";

    public Correction(String classified_event, String classified_sitstand, String corrected_event, String correction_sitstand) {
        this.classified_event = classified_event;
        this.classified_sitstand = classified_sitstand;
        this.corrected_event = corrected_event;
        this.correction_sitstand = correction_sitstand;
    }

    public String getClassified_event() {
        return classified_event;
    }

    public String getClassified_sitstand() {
        return classified_sitstand;
    }

    public String getCorrected_event() {
        return corrected_event;
    }

    public String getCorrection_sitstand() {
        return correction_sitstand;
    }
}
