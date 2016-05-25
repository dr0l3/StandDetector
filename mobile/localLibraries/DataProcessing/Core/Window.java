package Core;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Rune on 07-03-2016.
 */
public class Window implements Serializable {

    private List<String> listOfLines;
    private String label;
    private List<FeatureLine> listOfFeatureLines;
    private List<Pair<?,?>> listOfFeatures;

    public Window(Window window){
        this.listOfFeatureLines = new ArrayList<>();
        this.listOfFeatures = new ArrayList<>();
        for (FeatureLine fl : window.getListOfFeatureLines()) {
            listOfFeatureLines.add(new FeatureLine(fl));
        }
        this.label = window.getLabel();
    }

    public static Window createFromEventRecordList(List<SensorEventRecord> listOfEvents, String label){
        Window w = new Window();
        w.setLabel(label);
        for (SensorEventRecord ser :listOfEvents) {
            double accX = (null != ser.getAcceleration()) ? ser.getAcceleration()[0] : 0;
            double accY = (null != ser.getAcceleration()) ? ser.getAcceleration()[1] : 0;
            double accZ = (null != ser.getAcceleration()) ? ser.getAcceleration()[2] : 0;
            double rotX = (null != ser.getRotation()) ? ser.getRotation()[0] : 0;
            double rotY = (null != ser.getRotation()) ? ser.getRotation()[1] : 0;
            double rotZ = (null != ser.getRotation()) ? ser.getRotation()[2] : 0;
            double graX = (null != ser.getGravity()) ? ser.getGravity()[0] : 0;
            double graY = (null != ser.getGravity()) ? ser.getGravity()[1] : 0;
            double graZ = (null != ser.getGravity()) ? ser.getGravity()[2] : 0;
            int ts = (int) ser.getTimestamp();
            FeatureLine fl = new FeatureLine(accX,accY,accZ,rotX,rotY,rotZ,graX,graY,graZ,ts);
            w.addFeatureLine(fl);
        }
        return w;
    }

    public static Window createStandSitWindowWithProximity(List<SensorEventRecord> listOfRecords, String label){
        Window w = new Window();
        w.setLabel(label);
        for (SensorEventRecord ser : listOfRecords) {
            double accX = (null != ser.getAcceleration()) ? ser.getAcceleration()[0] : 0;
            double accY = (null != ser.getAcceleration()) ? ser.getAcceleration()[1] : 0;
            double accZ = (null != ser.getAcceleration()) ? ser.getAcceleration()[2] : 0;
            double rotX = (null != ser.getRotation()) ? ser.getRotation()[0] : 0;
            double rotY = (null != ser.getRotation()) ? ser.getRotation()[1] : 0;
            double rotZ = (null != ser.getRotation()) ? ser.getRotation()[2] : 0;
            double graX = (null != ser.getGravity()) ? ser.getGravity()[0] : 0;
            double graY = (null != ser.getGravity()) ? ser.getGravity()[1] : 0;
            double graZ = (null != ser.getGravity()) ? ser.getGravity()[2] : 0;
            ProximityValue proximity = ser.getProximity();
            int ts = (int) ser.getTimestamp();
            FeatureLine fl = new FeatureLine(accX,accY,accZ,rotX,rotY,rotZ,graX,graY,graZ,proximity,ts);
            w.addFeatureLine(fl);
        }
        return w;
    }

    public static Window createStandSitWindowWithProximityFromString(List<String> listOfLines, String label, BiasConfiguration bias){
        Window w = new Window();
        w.setLabel(label);
        for (String str: listOfLines){
            if(!str.contains("---")) {
                FeatureLine fl = convertToFeatureLineWithProximity(str);
                fl.applyBias(bias);
                w.addFeatureLine(fl);
            }
        }
        return w;
    }

    public static Window createStandSitWindowWithProximityFromString(List<String> listOfLines, String label){
        Window w = new Window();
        w.setLabel(label);
        for (String str: listOfLines){
            if(!str.contains("---")) {
                FeatureLine fl = convertToFeatureLineWithProximity(str);
                w.addFeatureLine(fl);
            }
        }
        return w;
    }

    private static FeatureLine convertToFeatureLineWithProximity(String str) {
        String[] listOfStuff = str.split(" | ");
        return new FeatureLine(
                Double.valueOf(listOfStuff[0]), Double.valueOf(listOfStuff[2]), Double.valueOf(listOfStuff[4]),
                Double.valueOf(listOfStuff[6]), Double.valueOf(listOfStuff[8]), Double.valueOf(listOfStuff[10]),
                Double.valueOf(listOfStuff[12]), Double.valueOf(listOfStuff[14]), Double.valueOf(listOfStuff[16]),
                (listOfStuff[18].contains("FAR"))? ProximityValue.FAR : ProximityValue.NEAR,
                (int) Long.parseLong(listOfStuff[20]));
    }

    public static Window createTapWindowFromString(List<String> listOfLines, String label){
        Window w = new Window();
        w.setLabel(label);
        for (String str: listOfLines){
            FeatureLine fl = convertToTapFeatureLine(str);
            w.addFeatureLine(fl);
        }
        return w;
    }

    public Window(){
        //"Factory method" constructor
        this.listOfFeatureLines = new ArrayList<>();
        this.listOfFeatures = new ArrayList<>();
    }

    public Window(List<String> listOfLines, String label) {
        this.listOfLines = listOfLines;
        this.label = label;
        this.listOfFeatureLines = new ArrayList<>();
        this.listOfFeatures = new ArrayList<>();
        createFeatureLinesFromStrings();
    }


    public void createFeatureLinesFromStrings(){
        listOfFeatureLines = new ArrayList<>();
        for (String line : listOfLines) {
            if(!line.contains("---"))
                listOfFeatureLines.add(convertStringToFeatureLine(line));
        }
    }

    private static FeatureLine convertToTapFeatureLine(String str) {
        String[] listOfStuff = str.split(" | ");
        return new FeatureLine(
                Double.valueOf(listOfStuff[0]), Double.valueOf(listOfStuff[2]), Double.valueOf(listOfStuff[4]),
                Double.valueOf(listOfStuff[6]), Double.valueOf(listOfStuff[8]), Double.valueOf(listOfStuff[10]),
                (int) Long.parseLong(listOfStuff[12]));
    }


    public void addFeatureLine(FeatureLine fl){
        this.listOfFeatureLines.add(fl);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getListOfLines() {
        return listOfLines;
    }

    public void addLine(String line){
        listOfLines.add(line);
    }

    private FeatureLine convertStringToFeatureLine(String line) {
        String[] listOfStuff = line.split(" | ");
        return new FeatureLine(
                Double.valueOf(listOfStuff[0]), Double.valueOf(listOfStuff[2]), Double.valueOf(listOfStuff[4]),
                Double.valueOf(listOfStuff[12]), Double.valueOf(listOfStuff[14]), Double.valueOf(listOfStuff[16]),
                Double.valueOf(listOfStuff[6]), Double.valueOf(listOfStuff[8]), Double.valueOf(listOfStuff[10]),
                (int) Long.parseLong(listOfStuff[18]));
    }

    public void calculateAllFeatures(){
        calculateECDFRepresentationDisc(30);
        calculateECDFRepresentationRaw(30);
        calculateECDFRepresentationUpDown(30);
        calculateFeaturesForRawMovement();
        calculateFeaturesForGravityDiscountedMovement();
        calculateFeaturesForRelativeMovement();
        calculateStartingOrientation();
        calculateEndingOrientation();
//        calculateOrientationChange();
        calculateOrientationJitter();
        calculateVerticalTimedDistribution(30);
        calculateSumOfUpwardsAcceleration();
        calculateSumOfDownwardsAcceleration();
        calculateZeroCrossings();
        calculateNumberOfTaps();
        calculateProximity();
    }

    public void calculateProximity(){
        Double start = proximityValueToDouble(listOfFeatureLines.get(0).getProximity());
        Double end = proximityValueToDouble(listOfFeatureLines.get(listOfFeatureLines.size()-1).getProximity());
        Double total = 0.0;
        for (FeatureLine fl : listOfFeatureLines) {
            total+= proximityValueToDouble(fl.getProximity());
        }
        Double mean = total/listOfFeatureLines.size();
        listOfFeatures.add(new Pair<Object, Object>("PROXIMITY_START",start));
        listOfFeatures.add(new Pair<Object, Object>("PROXIMITY_END", end));
        listOfFeatures.add(new Pair<Object, Object>("PROXIMITY_MEAN", mean));
    }

    private Double proximityValueToDouble(ProximityValue proximity) {
        return (proximity == ProximityValue.FAR)? 5.0 : 0.0;
    }

    public void calculateNumberOfTaps(){
        ArrayList<Double> upValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            upValues.add(featureLine.getAccZ());
            xAxisDummy.add(i);
            i++;
        }

        double[] up = new double[upValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < upValues.size(); j++) {
            up[j] = upValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction effAccZFunction = interpolator.interpolate(dummy,up);
        UnivariateFunction derivativeEffAccZFunction = effAccZFunction.derivative();
        ArrayList<Double> derivativeValues = new ArrayList<>();
        for (int j = 0; j < listOfFeatureLines.size(); j++) {
            derivativeValues.add(derivativeEffAccZFunction.value(j));
        }

        ArrayList<Integer> possibleTaps = new ArrayList<>();
        int max_measurements_between_taps = 70;
        Double taps = 0.0;
        int measurements_to_consider = 10;
        int jump_after_tap = 15;
        double threshhold_for_window = 60;
        double running_total = 0;
        Queue<Double> running_window = new LinkedBlockingQueue<>();
        for (int j = 0; j < listOfFeatureLines.size(); j++) {
            if(running_window.size() >= measurements_to_consider) {
                running_window.poll();
            } else {
                running_window.add(derivativeValues.get(j));
                continue;
            }
            running_window.add(derivativeValues.get(j));


            double total_for_window = sum_of_running_window(running_window.iterator());
            if( total_for_window > threshhold_for_window) {
                Integer middle_of_window = j + (measurements_to_consider/2);
                possibleTaps.add(middle_of_window);
                //flush the running window
                while(running_window.size() > 0)
                    running_window.poll();
                j = j + jump_after_tap;
            }
        }
        ArrayList<Integer> tap_dists = new ArrayList<>(possibleTaps);
        if(possibleTaps.size()> 1) {
            for (int j = 0; j < possibleTaps.size(); j++) {
                int dist_left = Integer.MAX_VALUE;
                int dist_right = Integer.MAX_VALUE;
                if(j > 0){
                    dist_left = possibleTaps.get(j) - possibleTaps.get(j-1);
                }
                if(j< possibleTaps.size()-1){
                    dist_right = possibleTaps.get(j+1) - possibleTaps.get(j);
                }
                int minimum = Math.min(dist_left, dist_right);
                tap_dists.set(j, minimum);
            }

            for (int j = 0; j < tap_dists.size(); j++) {
                if (tap_dists.get(j) < max_measurements_between_taps)
                    taps++;
            }

        } else {
            taps = 0.0;
        }
//        System.out.println("PossibleTaps: " + possibleTaps.toString() + " TapDists: " +tap_dists.toString());
        listOfFeatures.add(new Pair<Object, Object>("DERIVATIVE_OF_DISC_Z_NUMBER_OF_TAPS", taps));
    }

    private double sum_of_running_window(Iterator<Double> iterator) {
        double previous = iterator.next();
        double total = 0;
        while(iterator.hasNext()){
            double current = iterator.next();
            total += Math.abs(current-previous);
            previous = current;
        }
        return total;
    }


    public void calculateFrequencyFeaturesRawX(){
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] x_values = new double[size];
        Arrays.fill(x_values, 0);
        for (int i = 0; i < samples; i++) {
            x_values[i] = listOfFeatureLines.get(i).getAccX();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(x_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("RAW_X_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("RAW_X_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("RAW_X_ENTROPY", entropy));
    }

    public void calculateFrequencyFeaturesRawY() {
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] y_values = new double[size];
        Arrays.fill(y_values, 0);
        for (int i = 0; i < samples; i++) {
            y_values[i] = listOfFeatureLines.get(i).getAccY();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(y_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("RAW_Y_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("RAW_Y_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("RAW_Y_ENTROPY", entropy));
    }
    public void calculateZeroCrossings(){
        ArrayList<Integer> zeroCrossingsList = new ArrayList<>();
        double previous = Double.MIN_VALUE;
        for (int i = 0; i < listOfFeatureLines.size(); i++) {
            double current = listOfFeatureLines.get(i).getAccUp();
            if(previous == Double.MIN_VALUE){
                previous = current;
            } else {
                boolean previous_positive = previous >= 0;
                boolean current_positive = current >= 0;
                if((previous_positive && current_positive) || (!previous_positive && !current_positive)){
                    //no zerocrossing
                } else {
                    zeroCrossingsList.add(i);
                }
                previous = current;
            }
        }

        //for all zero crossings get positive acceleration and negative acceleration on both sides
        if(zeroCrossingsList.size() < 1){
            listOfFeatures.add(new Pair<Object, Object>("ZERO_CROSSINGS_VERTICAL", zeroCrossingsList.size()));
            listOfFeatures.add(new Pair<Object, Object>("PURITY_BEFORE_VERTICAL", 0));
            listOfFeatures.add(new Pair<Object, Object>("PURITY_AFTER_VERTICAL", 0));
            listOfFeatures.add(new Pair<Object, Object>("VERTICAL_POSITIVE_ACCELERATION_BEFORE", 0));
            listOfFeatures.add(new Pair<Object, Object>("VERTICAL_NEGATIVE_ACCELERATION_BEFORE", 0));
            listOfFeatures.add(new Pair<Object, Object>("VERTICAL_POSITIVE_ACCELERATION_AFTER", 0));
            listOfFeatures.add(new Pair<Object, Object>("VERTICAL_NEGATIVE_ACCELERATION_AFTER", 0));
            return;
        }
        double best_purity = Double.MIN_VALUE;
        double best_positive_before = 0;
        double best_negative_before = 0;
        double best_positive_after = 0;
        double best_negative_after = 0;
        int best_zero_crossing = 0;
        for (int i = 0; i < zeroCrossingsList.size(); i++) {
            double positive_before = 0;
            double negative_before = 0;
            double positive_after = 0;
            double negative_after = 0;
            for (int j = 0; j < listOfFeatureLines.size(); j++) {
                double current = listOfFeatureLines.get(j).getAccUp();
                double processed_current = Math.abs(current);//Math.pow(current,2);
                if(j < zeroCrossingsList.get(i)){
                    //before
                    if(current >= 0)
                        positive_before += processed_current;
                    else
                        negative_before += processed_current;
                } else {
                    //after
                    if(current >= 0)
                        positive_after += processed_current;
                    else
                        negative_after += processed_current;
                }
            }
            double total_acceleration_before = positive_before - negative_before;
            double total_acceleration_after = positive_after - negative_after;
            double purity_before = Math.max(positive_before/total_acceleration_before, negative_before/total_acceleration_before);
            double purity_after = Math.max(positive_after/total_acceleration_after, negative_after/total_acceleration_after);
            //double purity = Math.min(purity_before, purity_after);
            double purity = Math.abs(total_acceleration_after-total_acceleration_before);
            if(best_purity < purity){
                best_purity = purity;
                best_negative_after = negative_after;
                best_negative_before = negative_before;
                best_positive_after = positive_after;
                best_positive_before = positive_before;
                best_zero_crossing = zeroCrossingsList.get(i);
            }
        }
        double best_total_acceleration_before = best_negative_before + best_positive_before;
        double best_total_acceleration_after = best_positive_after + best_negative_after;
        double best_purity_before = Math.max(best_positive_before/best_total_acceleration_before, best_negative_before/best_total_acceleration_before);
        double best_purity_after = Math.max(best_positive_after/best_total_acceleration_after, best_negative_after/best_total_acceleration_after);

        listOfFeatures.add(new Pair<Object, Object>("ZERO_CROSSINGS_VERTICAL", zeroCrossingsList.size()));
        listOfFeatures.add(new Pair<Object, Object>("PURITY_BEFORE_VERTICAL", best_purity_before));
        listOfFeatures.add(new Pair<Object, Object>("PURITY_AFTER_VERTICAL", best_purity_after));
        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_POSITIVE_ACCELERATION_BEFORE", best_positive_before));
        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_NEGATIVE_ACCELERATION_BEFORE", best_negative_before));
        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_POSITIVE_ACCELERATION_AFTER", best_positive_after));
        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_NEGATIVE_ACCELERATION_AFTER", best_negative_after));
    }

    public void calculateFrequencyFeaturesRawZ() {
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] z_values = new double[size];
        Arrays.fill(z_values, 0);
        for (int i = 0; i < samples; i++) {
            z_values[i] = listOfFeatureLines.get(i).getAccZ();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(z_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("RAW_Z_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("RAW_Z_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("RAW_Z_ENTROPY", entropy));
    }

    public void calculateFrequencyFeaturesDiscX(){
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] x_values = new double[size];
        Arrays.fill(x_values, 0);
        for (int i = 0; i < samples; i++) {
            x_values[i] = listOfFeatureLines.get(i).getEffAccX();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(x_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("EFF_X_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("EFF_X_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("EFF_X_ENTROPY", entropy));
    }

    public void calculateFrequencyFeaturesDiscY() {
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] y_values = new double[size];
        Arrays.fill(y_values, 0);
        for (int i = 0; i < samples; i++) {
            y_values[i] = listOfFeatureLines.get(i).getEffAccY();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(y_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("EFF_Y_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("EFF_Y_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("EFF_Y_ENTROPY", entropy));
    }

    public void calculateFrequencyFeaturesDiscZ() {
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] z_values = new double[size];
        Arrays.fill(z_values, 0);
        for (int i = 0; i < samples; i++) {
            z_values[i] = listOfFeatureLines.get(i).getEffAccZ();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(z_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("EFF_Z_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("EFF_Z_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("EFF_Z_ENTROPY", entropy));
    }

    public void calculateFrequencyFeaturesHorizontal() {
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] hor_values = new double[size];
        Arrays.fill(hor_values, 0);
        for (int i = 0; i < samples; i++) {
            hor_values[i] = listOfFeatureLines.get(i).getAccRest();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(hor_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("HORIZONTAL_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("HORIZONTAL_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("HORIZONTAL_ENTROPY", entropy));
    }
    public void calculateFrequencyFeaturesVertical() {
        int samples = listOfFeatureLines.size();
        int size = 2;
        while(size < samples)
            size = size * 2;
        double[] ver_values = new double[size];
        Arrays.fill(ver_values, 0);
        for (int i = 0; i < samples; i++) {
            ver_values[i] = listOfFeatureLines.get(i).getEffAccZ();
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] coefficients = fft.transform(ver_values, TransformType.FORWARD);

        double DC = coefficients[0].getReal();
        double sumOfSquaredCoefficients = 0;
        for (int i = 0; i < samples; i++) {
            sumOfSquaredCoefficients += Math.pow(coefficients[i].getReal(),2) + Math.pow(coefficients[i].getImaginary(),2);
        }
        double spectral_energy = sumOfSquaredCoefficients/samples;
        double[] c = new double[samples];
        for (int i = 0; i < samples; i++) {
            c[i] = Math.sqrt(Math.pow(coefficients[i].getReal(),2)+ Math.pow(coefficients[i].getImaginary(),2))/Math.sqrt(sumOfSquaredCoefficients);
        }
        double entropy = 0;
        for (int i = 0; i < samples; i++) {
            entropy += c[i] + Math.log(c[i]);
        }

        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_DC", DC));
        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_SPECTRAL_ENERGY", spectral_energy));
        listOfFeatures.add(new Pair<Object, Object>("VERTICAL_ENTROPY", entropy));
    }

    public void calculateMeanVerticalAcceleration(){
        double sumVert = 0;
        for (FeatureLine fl : listOfFeatureLines) {
            sumVert += fl.getAccUp();
        }
        listOfFeatures.add(new Pair<>("VerAcc_Mean", (sumVert/listOfFeatureLines.size())));
    }

    public void calculateVerticalSamplesAboveThreshold(double threshold){
        int n = 0;
        for (FeatureLine fl : listOfFeatureLines) {
            if (fl.getAccUp()> threshold)
                n++;
        }
        listOfFeatures.add(new Pair<>("SAMPLES_VERTICAL_ACC_OVER_"+threshold, n));
    }

    public void calculateVerticalSamplesBelowThreshold(double threshold){
        int n = 0;
        for (FeatureLine fl : listOfFeatureLines) {
            if (fl.getAccUp()< threshold)
                n++;
        }
        listOfFeatures.add(new Pair<>("SAMPLES_VERTICAL_ACC_BELOW_"+threshold, n));
    }

    public void calculateFeaturesForRelativeMovement(){
        double sumAccUp = 0;
        double sumAccRest = 0;
        double upAcc_MinVal = Double.MAX_VALUE;
        double restAcc_MinVal = Double.MAX_VALUE;
        double upAcc_MaxVal = -Double.MAX_VALUE;
        double restAcc_MaxVal = -Double.MAX_VALUE;
        double upAcc_MinDif = Double.MAX_VALUE;
        double restAcc_MinDif = Double.MAX_VALUE;
        double upAcc_MaxDif = -Double.MAX_VALUE;
        double restAcc_MaxDif = -Double.MAX_VALUE;
        double sumDifUp = 0;
        double sumDifRest = 0;
        double squaredSumUp = 0;
        double squaredSumRest = 0;
        double sumPosUp = 0;
        double sumNegUp = 0;
        double sumAbsRest = 0;
        for (int i = 0; i < listOfFeatureLines.size(); i++) {
            double accUpTemp = listOfFeatureLines.get(i).getAccUp();
            double accRestTemp = listOfFeatureLines.get(i).getAccRest();
            sumAccUp+=accUpTemp;
            sumAccRest+=accRestTemp;
            squaredSumUp+= Math.pow(accUpTemp,2);
            squaredSumRest+= Math.pow(accRestTemp,2);
            if(upAcc_MinVal > accUpTemp)
                upAcc_MinVal = accUpTemp;
            if(restAcc_MinVal > accRestTemp)
                restAcc_MinVal = accRestTemp;
            if(upAcc_MaxVal < accUpTemp)
                upAcc_MaxVal = accUpTemp;
            if(restAcc_MaxVal < accRestTemp)
                restAcc_MaxVal = accRestTemp;
            if (i> 0){
                double diffUp = accUpTemp - listOfFeatureLines.get(i-1).getAccUp();
                double diffRest =  accRestTemp - listOfFeatureLines.get(i-1).getAccRest();
                if(upAcc_MinDif > diffUp)
                    upAcc_MinDif = diffUp;
                if(upAcc_MaxDif < diffUp)
                    upAcc_MaxDif = diffUp;
                if(restAcc_MinDif > diffRest)
                    restAcc_MinDif = diffRest;
                if(restAcc_MaxDif < diffRest)
                    restAcc_MaxDif = diffRest;
                sumDifUp+=diffUp;
                sumDifRest+=diffRest;
                if(diffUp> 0)
                    sumPosUp+=diffUp;
                else
                    sumNegUp+=diffUp;
                sumAbsRest+=diffRest;
            }
        }
        double upAcc_Mean = sumAccUp / listOfFeatureLines.size();
        double restAcc_Mean = sumAccRest / listOfFeatureLines.size();
        double restAcc_Mean_Absolute = sumAbsRest / listOfFeatureLines.size();
        double temp = squaredSumUp/listOfFeatureLines.size();
        double upAcc_RootMeanSquare = Math.sqrt(temp);
        temp = squaredSumRest/listOfFeatureLines.size();
        double restAcc_RootMeanSquare = Math.sqrt(temp);
        double upAcc_AverageDif = sumDifUp / listOfFeatureLines.size();
        double restAcc_AverageDif = sumDifRest / listOfFeatureLines.size();

        listOfFeatures.add(new Pair<>("UpAcc_Mean", upAcc_Mean));
        listOfFeatures.add(new Pair<>("UpAcc_MaxVal", upAcc_MaxVal));
        listOfFeatures.add(new Pair<>("UpAcc_MaxDif", upAcc_MaxDif));
        listOfFeatures.add(new Pair<>("UpAcc_MinDif", upAcc_MinDif));
        listOfFeatures.add(new Pair<>("UpAcc_MinVal", upAcc_MinVal));
        listOfFeatures.add(new Pair<>("UpAcc_RootMeanSquare", upAcc_RootMeanSquare));
        listOfFeatures.add(new Pair<>("UpAcc_AverageDif", upAcc_AverageDif));

        listOfFeatures.add(new Pair<>("RestAcc_Mean", restAcc_Mean));
        listOfFeatures.add(new Pair<>("RestAcc_Mean_Absolute", restAcc_Mean_Absolute));
        listOfFeatures.add(new Pair<>("RestAcc_MaxVal", restAcc_MaxVal));
        listOfFeatures.add(new Pair<>("RestAcc_MaxDif", restAcc_MaxDif));
        listOfFeatures.add(new Pair<>("RestAcc_MinDif", restAcc_MinDif));
        listOfFeatures.add(new Pair<>("RestAcc_MinVal", restAcc_MinVal));
        listOfFeatures.add(new Pair<>("RestAcc_RootMeanSquare", restAcc_RootMeanSquare));
        listOfFeatures.add(new Pair<>("RestAcc_AverageDif", restAcc_AverageDif));
    }

    public void calculateFeaturesForRawMovement(){
        /* x_Raw*/
        double xAccRaw_Sum = 0;
        double xAccRaw_MinVal = Double.MAX_VALUE;
        double xAccRaw_MaxVal = -Double.MAX_VALUE;
        double xAccRaw_MinDif = Double.MAX_VALUE;
        double xAccRaw_MaxDif = -Double.MAX_VALUE;
        double xAccRaw_DifSum = 0;
        double xAccRaw_SumSquared = 0;

        /* y_Raw*/
        double yAccRaw_Sum = 0;
        double yAccRaw_MinVal = Double.MAX_VALUE;
        double yAccRaw_MaxVal = -Double.MAX_VALUE;
        double yAccRaw_MinDif = Double.MAX_VALUE;
        double yAccRaw_MaxDif = -Double.MAX_VALUE;
        double yAccRaw_DifSum = 0;
        double yAccRaw_SumSquared = 0;

        /* z_Raw*/
        double zAccRaw_Sum = 0;
        double zAccRaw_MinVal = Double.MAX_VALUE;
        double zAccRaw_MaxVal = -Double.MAX_VALUE;
        double zAccRaw_MinDif = Double.MAX_VALUE;
        double zAccRaw_MaxDif = -Double.MAX_VALUE;
        double zAccRaw_DifSum = 0;
        double zAccRaw_SumSquared = 0;
        for (int i = 0; i < listOfFeatureLines.size(); i++) {
            /* x_Raw*/
            double xAccRaw_Temp = listOfFeatureLines.get(i).getAccX();
            xAccRaw_Sum+=xAccRaw_Temp;
            xAccRaw_SumSquared+= Math.pow(xAccRaw_Temp,2);
            if(xAccRaw_MinVal > xAccRaw_Temp)
                xAccRaw_MinVal = xAccRaw_Temp;
            if(xAccRaw_MaxVal < xAccRaw_Temp)
                xAccRaw_MaxVal = xAccRaw_Temp;

            /* y_Raw*/
            double yAccRaw_Temp = listOfFeatureLines.get(i).getAccY();
            yAccRaw_Sum+=yAccRaw_Temp;
            yAccRaw_SumSquared+= Math.pow(yAccRaw_Temp,2);
            if(yAccRaw_MinVal > yAccRaw_Temp)
                yAccRaw_MinVal = yAccRaw_Temp;
            if(yAccRaw_MaxVal < yAccRaw_Temp)
                yAccRaw_MaxVal = yAccRaw_Temp;

            /* z_Raw*/
            double zAccRaw_Temp = listOfFeatureLines.get(i).getAccZ();
            zAccRaw_Sum+=zAccRaw_Temp;
            zAccRaw_SumSquared+= Math.pow(zAccRaw_Temp,2);
            if(zAccRaw_MinVal > zAccRaw_Temp)
                zAccRaw_MinVal = zAccRaw_Temp;
            if(zAccRaw_MaxVal < zAccRaw_Temp)
                zAccRaw_MaxVal = zAccRaw_Temp;
            if (i> 0){
                /* x_Raw*/
                double xAccRaw_Dif = xAccRaw_Temp - listOfFeatureLines.get(i-1).getAccX();
                if(xAccRaw_MinDif > xAccRaw_Dif)
                    xAccRaw_MinDif = xAccRaw_Dif;
                if(xAccRaw_MaxDif < xAccRaw_Dif)
                    xAccRaw_MaxDif = xAccRaw_Dif;
                xAccRaw_DifSum+=xAccRaw_Dif;

                /* y_Raw*/
                double yAccRaw_Dif = yAccRaw_Temp - listOfFeatureLines.get(i-1).getAccY();
                if(yAccRaw_MinDif > yAccRaw_Dif)
                    yAccRaw_MinDif = yAccRaw_Dif;
                if(yAccRaw_MaxDif < yAccRaw_Dif)
                    yAccRaw_MaxDif = yAccRaw_Dif;
                yAccRaw_DifSum+=yAccRaw_Dif;

                /* z_Raw*/
                double zAccRaw_Dif = zAccRaw_Temp - listOfFeatureLines.get(i-1).getAccZ();
                if(zAccRaw_MinDif > zAccRaw_Dif)
                    zAccRaw_MinDif = zAccRaw_Dif;
                if(zAccRaw_MaxDif < zAccRaw_Dif)
                    zAccRaw_MaxDif = zAccRaw_Dif;
                zAccRaw_DifSum+=zAccRaw_Dif;
            }
        }
        /* x_Raw*/
        double xAccRaw_Mean = xAccRaw_Sum / listOfFeatureLines.size();
        double tempX = xAccRaw_SumSquared/listOfFeatureLines.size();
        double xAccRaw_RootMeanSquare = Math.sqrt(tempX);
        double xAccRaw_AverageDif = xAccRaw_DifSum / listOfFeatureLines.size();

        /* y_Raw*/
        double yAccRaw_Mean = yAccRaw_Sum / listOfFeatureLines.size();
        double tempY = yAccRaw_SumSquared/listOfFeatureLines.size();
        double yAccRaw_RootMeanSquare = Math.sqrt(tempY);
        double yAccRaw_AverageDif = yAccRaw_DifSum / listOfFeatureLines.size();

        /* z_Raw*/
        double zAccRaw_Mean = zAccRaw_Sum / listOfFeatureLines.size();
        double tempZ = zAccRaw_SumSquared/listOfFeatureLines.size();
        double zAccRaw_RootMeanSquare = Math.sqrt(tempZ);
        double zAccRaw_AverageDif = zAccRaw_DifSum / listOfFeatureLines.size();

        listOfFeatures.add(new Pair<>("XAccRaw_Mean", xAccRaw_Mean));
        listOfFeatures.add(new Pair<>("XAccRaw_MaxVal", xAccRaw_MaxVal));
        listOfFeatures.add(new Pair<>("XAccRaw_MaxDif", xAccRaw_MaxDif));
        listOfFeatures.add(new Pair<>("XAccRaw_MinDif", xAccRaw_MinDif));
        listOfFeatures.add(new Pair<>("XAccRaw_MinVal", xAccRaw_MinVal));
        listOfFeatures.add(new Pair<>("XAccRaw_RootMeanSquare", xAccRaw_RootMeanSquare));
        listOfFeatures.add(new Pair<>("XAccRaw_AverageDif", xAccRaw_AverageDif));

        listOfFeatures.add(new Pair<>("YAccRaw_Mean", yAccRaw_Mean));
        listOfFeatures.add(new Pair<>("YAccRaw_MaxVal", yAccRaw_MaxVal));
        listOfFeatures.add(new Pair<>("YAccRaw_MaxDif", yAccRaw_MaxDif));
        listOfFeatures.add(new Pair<>("YAccRaw_MinDif", yAccRaw_MinDif));
        listOfFeatures.add(new Pair<>("YAccRaw_MinVal", yAccRaw_MinVal));
        listOfFeatures.add(new Pair<>("YAccRaw_RootMeanSquare", yAccRaw_RootMeanSquare));
        listOfFeatures.add(new Pair<>("YAccRaw_AverageDif", yAccRaw_AverageDif));

        listOfFeatures.add(new Pair<>("ZAccRaw_Mean", zAccRaw_Mean));
        listOfFeatures.add(new Pair<>("ZAccRaw_MaxVal", zAccRaw_MaxVal));
        listOfFeatures.add(new Pair<>("ZAccRaw_MaxDif", zAccRaw_MaxDif));
        listOfFeatures.add(new Pair<>("ZAccRaw_MinDif", zAccRaw_MinDif));
        listOfFeatures.add(new Pair<>("ZAccRaw_MinVal", zAccRaw_MinVal));
        listOfFeatures.add(new Pair<>("ZAccRaw_RootMeanSquare", zAccRaw_RootMeanSquare));
        listOfFeatures.add(new Pair<>("ZAccRaw_AverageDif", zAccRaw_AverageDif));
    }

    public void calculateFeaturesForGravityDiscountedMovement(){
        /* x_Disc*/
        double xAccDisc_Sum = 0;
        double xAccDisc_MinVal = Double.MAX_VALUE;
        double xAccDisc_MaxVal = -Double.MAX_VALUE;
        double xAccDisc_MinDif = Double.MAX_VALUE;
        double xAccDisc_MaxDif = -Double.MAX_VALUE;
        double xAccDisc_DifSum = 0;
        double xAccDisc_SumSquared = 0;

        /* y_Disc*/
        double yAccDisc_Sum = 0;
        double yAccDisc_MinVal = Double.MAX_VALUE;
        double yAccDisc_MaxVal = -Double.MAX_VALUE;
        double yAccDisc_MinDif = Double.MAX_VALUE;
        double yAccDisc_MaxDif = -Double.MAX_VALUE;
        double yAccDisc_DifSum = 0;
        double yAccDisc_SumSquared = 0;

        /* z_Disc*/
        double zAccDisc_Sum = 0;
        double zAccDisc_MinVal = Double.MAX_VALUE;
        double zAccDisc_MaxVal = -Double.MAX_VALUE;
        double zAccDisc_MinDif = Double.MAX_VALUE;
        double zAccDisc_MaxDif = -Double.MAX_VALUE;
        double zAccDisc_DifSum = 0;
        double zAccDisc_SumSquared = 0;
        for (int i = 0; i < listOfFeatureLines.size(); i++) {
            /* x_Disc*/
            double xAccDisc_Temp = listOfFeatureLines.get(i).getEffAccX();
            xAccDisc_Sum+=xAccDisc_Temp;
            xAccDisc_SumSquared+= Math.pow(xAccDisc_Temp,2);
            if(xAccDisc_MinVal > xAccDisc_Temp)
                xAccDisc_MinVal = xAccDisc_Temp;
            if(xAccDisc_MaxVal < xAccDisc_Temp)
                xAccDisc_MaxVal = xAccDisc_Temp;

            /* y_Disc*/
            double yAccDisc_Temp = listOfFeatureLines.get(i).getEffAccY();
            yAccDisc_Sum+=yAccDisc_Temp;
            yAccDisc_SumSquared+= Math.pow(yAccDisc_Temp,2);
            if(yAccDisc_MinVal > yAccDisc_Temp)
                yAccDisc_MinVal = yAccDisc_Temp;
            if(yAccDisc_MaxVal < yAccDisc_Temp)
                yAccDisc_MaxVal = yAccDisc_Temp;

            /* z_Disc*/
            double zAccDisc_Temp = listOfFeatureLines.get(i).getEffAccZ();
            zAccDisc_Sum+=zAccDisc_Temp;
            zAccDisc_SumSquared+= Math.pow(zAccDisc_Temp,2);
            if(zAccDisc_MinVal > zAccDisc_Temp)
                zAccDisc_MinVal = zAccDisc_Temp;
            if(zAccDisc_MaxVal < zAccDisc_Temp)
                zAccDisc_MaxVal = zAccDisc_Temp;
            if (i> 0){
                /* x_Disc*/
                double xAccDisc_Dif = xAccDisc_Temp - listOfFeatureLines.get(i-1).getEffAccX();
                if(xAccDisc_MinDif > xAccDisc_Dif)
                    xAccDisc_MinDif = xAccDisc_Dif;
                if(xAccDisc_MaxDif < xAccDisc_Dif)
                    xAccDisc_MaxDif = xAccDisc_Dif;
                xAccDisc_DifSum+=xAccDisc_Dif;

                /* y_Disc*/
                double yAccDisc_Dif = yAccDisc_Temp - listOfFeatureLines.get(i-1).getEffAccY();
                if(yAccDisc_MinDif > yAccDisc_Dif)
                    yAccDisc_MinDif = yAccDisc_Dif;
                if(yAccDisc_MaxDif < yAccDisc_Dif)
                    yAccDisc_MaxDif = yAccDisc_Dif;
                yAccDisc_DifSum+=yAccDisc_Dif;

                /* z_Disc*/
                double zAccDisc_Dif = zAccDisc_Temp - listOfFeatureLines.get(i-1).getEffAccZ();
                if(zAccDisc_MinDif > zAccDisc_Dif)
                    zAccDisc_MinDif = zAccDisc_Dif;
                if(zAccDisc_MaxDif < zAccDisc_Dif)
                    zAccDisc_MaxDif = zAccDisc_Dif;
                zAccDisc_DifSum+=zAccDisc_Dif;
            }
        }
        /* x_Disc*/
        double xAccDisc_Mean = xAccDisc_Sum / listOfFeatureLines.size();
        double tempX = xAccDisc_SumSquared/listOfFeatureLines.size();
        double xAccDisc_RootMeanSquare = Math.sqrt(tempX);
        double xAccDisc_AverageDif = xAccDisc_DifSum / listOfFeatureLines.size();

        /* y_Disc*/
        double yAccDisc_Mean = yAccDisc_Sum / listOfFeatureLines.size();
        double tempY = yAccDisc_SumSquared/listOfFeatureLines.size();
        double yAccDisc_RootMeanSquare = Math.sqrt(tempY);
        double yAccDisc_AverageDif = yAccDisc_DifSum / listOfFeatureLines.size();

        /* z_Disc*/
        double zAccDisc_Mean = zAccDisc_Sum / listOfFeatureLines.size();
        double tempZ = zAccDisc_SumSquared/listOfFeatureLines.size();
        double zAccDisc_RootMeanSquare = Math.sqrt(tempZ);
        double zAccDisc_AverageDif = zAccDisc_DifSum / listOfFeatureLines.size();

        listOfFeatures.add(new Pair<>("XAccDisc_Mean", xAccDisc_Mean));
        listOfFeatures.add(new Pair<>("XAccDisc_MaxVal", xAccDisc_MaxVal));
        listOfFeatures.add(new Pair<>("XAccDisc_MaxDif", xAccDisc_MaxDif));
        listOfFeatures.add(new Pair<>("XAccDisc_MinDif", xAccDisc_MinDif));
        listOfFeatures.add(new Pair<>("XAccDisc_MinVal", xAccDisc_MinVal));
        listOfFeatures.add(new Pair<>("XAccDisc_RootMeanSquare", xAccDisc_RootMeanSquare));
        listOfFeatures.add(new Pair<>("XAccDisc_AverageDif", xAccDisc_AverageDif));

        listOfFeatures.add(new Pair<>("YAccDisc_Mean", yAccDisc_Mean));
        listOfFeatures.add(new Pair<>("YAccDisc_MaxVal", yAccDisc_MaxVal));
        listOfFeatures.add(new Pair<>("YAccDisc_MaxDif", yAccDisc_MaxDif));
        listOfFeatures.add(new Pair<>("YAccDisc_MinDif", yAccDisc_MinDif));
        listOfFeatures.add(new Pair<>("YAccDisc_MinVal", yAccDisc_MinVal));
        listOfFeatures.add(new Pair<>("YAccDisc_RootMeanSquare", yAccDisc_RootMeanSquare));
        listOfFeatures.add(new Pair<>("YAccDisc_AverageDif", yAccDisc_AverageDif));

        listOfFeatures.add(new Pair<>("ZAccDisc_Mean", zAccDisc_Mean));
        listOfFeatures.add(new Pair<>("ZAccDisc_MaxVal", zAccDisc_MaxVal));
        listOfFeatures.add(new Pair<>("ZAccDisc_MaxDif", zAccDisc_MaxDif));
        listOfFeatures.add(new Pair<>("ZAccDisc_MinDif", zAccDisc_MinDif));
        listOfFeatures.add(new Pair<>("ZAccDisc_MinVal", zAccDisc_MinVal));
        listOfFeatures.add(new Pair<>("ZAccDisc_RootMeanSquare", zAccDisc_RootMeanSquare));
        listOfFeatures.add(new Pair<>("ZAccDisc_AverageDif", zAccDisc_AverageDif));
    }

    public void calculateECDFRepresentationDisc(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> xValues = new ArrayList<>();
        ArrayList<Double> yValues = new ArrayList<>();
        ArrayList<Double> zValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            xValues.add(featureLine.getEffAccX());
            yValues.add(featureLine.getEffAccY());
            zValues.add(featureLine.getEffAccZ());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(xValues);
        Collections.sort(yValues);
        Collections.sort(zValues);
        double[] x = new double[xValues.size()];
        double[] y = new double[yValues.size()];
        double[] z = new double[zValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < xValues.size(); j++) {
            x[j] = xValues.get(j);
        }
        for (int j = 0; j < yValues.size(); j++) {
            y[j] = yValues.get(j);
        }
        for (int j = 0; j < zValues.size(); j++) {
            z[j] = zValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }
        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfX = interpolator.interpolate(dummy,x);
        PolynomialSplineFunction ecdfOfY = interpolator.interpolate(dummy,y);
        PolynomialSplineFunction ecdfOfZ = interpolator.interpolate(dummy,z);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_DISC_X_BIN_" + (j+1) + "_OF_" + bins, ecdfOfX.value((double) j / bins * ecdfOfX.getKnots().length)));
            listOfFeatures.add(new Pair<>("ECDF_DISC_Y_BIN_" + (j+1) + "_OF_" + bins, ecdfOfY.value((double) j / bins * ecdfOfY.getKnots().length)));
            listOfFeatures.add(new Pair<>("ECDF_DISC_Z_BIN_" + (j+1) + "_OF_" + bins, ecdfOfZ.value((double) j / bins * ecdfOfZ.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationUpAndY(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> upValues = new ArrayList<>();
        ArrayList<Double> yValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            upValues.add(featureLine.getAccUp());
            yValues.add(featureLine.getAccY());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(upValues);
        Collections.sort(yValues);
        double[] up = new double[upValues.size()];
        double[] y = new double[yValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < upValues.size(); j++) {
            up[j] = upValues.get(j);
        }
        for (int j = 0; j < yValues.size(); j++) {
            y[j] = yValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }
        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfUp = interpolator.interpolate(dummy,up);
        PolynomialSplineFunction ecdfOfY = interpolator.interpolate(dummy,y);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_UP_BIN_" + (j+1) + "_OF_" + bins, ecdfOfUp.value((double) j / bins * ecdfOfUp.getKnots().length)));
            listOfFeatures.add(new Pair<>("ECDF_RAW_Y_BIN_" + (j+1) + "_OF_" + bins, ecdfOfY.value((double) j / bins * ecdfOfY.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationUpDown(int bins){
        int size = listOfFeatureLines.size();
        ArrayList<Double> upValues = new ArrayList<>();
        ArrayList<Double> restValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            upValues.add(featureLine.getAccUp());
            restValues.add(featureLine.getAccRest());
            xAxisDummy.add(i);
            i++;
        }
        Collections.sort(upValues);
        Collections.sort(restValues);

        double[] up = new double[upValues.size()];
        double[] rest = new double[restValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < upValues.size(); j++) {
            up[j] = upValues.get(j);
        }
        for (int j = 0; j < restValues.size(); j++) {
            rest[j] = restValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfUp = interpolator.interpolate(dummy,up);
        PolynomialSplineFunction ecdfOfRest = interpolator.interpolate(dummy,rest);

        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_UP_BIN_" + (j+1) + "_OF_" + bins, ecdfOfUp.value((double) j / bins * ecdfOfUp.getKnots().length)));
            listOfFeatures.add(new Pair<>("ECDF_REST_BIN_" + (j+1) + "_OF_" + bins, ecdfOfRest.value((double) j / bins * ecdfOfRest.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationRaw(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> xValues = new ArrayList<>();
        ArrayList<Double> yValues = new ArrayList<>();
        ArrayList<Double> zValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            xValues.add(featureLine.getAccX());
            yValues.add(featureLine.getAccY());
            zValues.add(featureLine.getAccZ());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(xValues);
        Collections.sort(yValues);
        Collections.sort(zValues);

        double[] x = new double[xValues.size()];
        double[] y = new double[yValues.size()];
        double[] z = new double[zValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < xValues.size(); j++) {
            x[j] = xValues.get(j);
        }
        for (int j = 0; j < yValues.size(); j++) {
            y[j] = yValues.get(j);
        }
        for (int j = 0; j < zValues.size(); j++) {
            z[j] = zValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfX = interpolator.interpolate(dummy,x);
        PolynomialSplineFunction ecdfOfY = interpolator.interpolate(dummy,y);
        PolynomialSplineFunction ecdfOfZ = interpolator.interpolate(dummy,z);
            //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_RAW_X_BIN_" + (j+1) + "_OF_" + bins, ecdfOfX.value((double) j / bins * ecdfOfX.getKnots().length)));
            listOfFeatures.add(new Pair<>("ECDF_RAW_Y_BIN_" + (j+1) + "_OF_" + bins, ecdfOfY.value((double) j / bins * ecdfOfY.getKnots().length)));
            listOfFeatures.add(new Pair<>("ECDF_RAW_Z_BIN_" + (j+1) + "_OF_" + bins, ecdfOfZ.value((double) j / bins * ecdfOfZ.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationRawX(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> xValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            xValues.add(featureLine.getAccX());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(xValues);

        double[] x = new double[xValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < xValues.size(); j++) {
            x[j] = xValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfX = interpolator.interpolate(dummy,x);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_RAW_X_BIN_" + (j+1) + "_OF_" + bins, ecdfOfX.value((double) j / bins * ecdfOfX.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationRawY(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> yValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            yValues.add(featureLine.getAccY());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(yValues);

        double[] y = new double[yValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < yValues.size(); j++) {
            y[j] = yValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfY = interpolator.interpolate(dummy,y);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_RAW_Y_BIN_" + (j+1) + "_OF_" + bins, ecdfOfY.value((double) j / bins * ecdfOfY.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationRawZ(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> zValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            zValues.add(featureLine.getAccZ());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(zValues);

        double[] z = new double[zValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < zValues.size(); j++) {
            z[j] = zValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfZ = interpolator.interpolate(dummy,z);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_RAW_Z_BIN_" + (j+1) + "_OF_" + bins, ecdfOfZ.value((double) j / bins * ecdfOfZ.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationDiscX(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> xValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            xValues.add(featureLine.getEffAccX());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(xValues);
        double[] x = new double[xValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < xValues.size(); j++) {
            x[j] = xValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }
        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfX = interpolator.interpolate(dummy,x);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_DISC_X_BIN_" + (j+1) + "_OF_" + bins, ecdfOfX.value((double) j / bins * ecdfOfX.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationDiscY(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> yValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            yValues.add(featureLine.getEffAccY());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(yValues);
        double[] y = new double[yValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < yValues.size(); j++) {
            y[j] = yValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }
        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfY = interpolator.interpolate(dummy,y);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_DISC_Y_BIN_" + (j+1) + "_OF_" + bins, ecdfOfY.value((double) j / bins * ecdfOfY.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationDiscZ(int bins){
        //for each axis
        int size = listOfFeatureLines.size();
        ArrayList<Double> zValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            zValues.add(featureLine.getEffAccZ());
            xAxisDummy.add(i);
            i++;
        }
        //sort the values
        Collections.sort(zValues);
        double[] z = new double[zValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < zValues.size(); j++) {
            z[j] = zValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }
        //convert to function
        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfZ = interpolator.interpolate(dummy,z);
        //get the bin values
        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_DISC_Z_BIN_" + (j+1) + "_OF_" + bins, ecdfOfZ.value((double) j / bins * ecdfOfZ.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationUp(int bins){
        int size = listOfFeatureLines.size();
        ArrayList<Double> upValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            upValues.add(featureLine.getAccUp());
            xAxisDummy.add(i);
            i++;
        }
        Collections.sort(upValues);

        double[] up = new double[upValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < upValues.size(); j++) {
            up[j] = upValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfUp = interpolator.interpolate(dummy,up);

        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_UP_BIN_" + (j+1) + "_OF_" + bins, ecdfOfUp.value((double) j / bins * ecdfOfUp.getKnots().length)));
        }
    }

    public void calculateECDFRepresentationRest(int bins){
        int size = listOfFeatureLines.size();
        ArrayList<Double> restValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            restValues.add(featureLine.getAccRest());
            xAxisDummy.add(i);
            i++;
        }
        Collections.sort(restValues);

        double[] rest = new double[restValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < restValues.size(); j++) {
            rest[j] = restValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction ecdfOfRest = interpolator.interpolate(dummy,rest);

        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("ECDF_REST_BIN_" + (j+1) + "_OF_" + bins, ecdfOfRest.value((double) j / bins * ecdfOfRest.getKnots().length)));
        }
    }

    //TODO: this can be done faster.
    public void calculateAreaUnderFirstOfTwoLargestBumps(){
        ArrayList<Pair<Double,Integer>> bumps = new ArrayList<>();
        int bumpCounter = 0;
        double area = 0;
        boolean upwards_previous = true;
        for (FeatureLine fl : listOfFeatureLines) {
            double accup = fl.getAccUp();
            boolean upwards_current = accup > 0;
            if(upwards_current ==  upwards_previous)
                area+= accup;
            else {
                upwards_previous = upwards_current;
                bumps.add(new Pair<Double, Integer>(area,bumpCounter));
                area = 0;
                bumpCounter++;
            }
        }
        if(area != 0)
            bumps.add(new Pair<Double, Integer>(area,bumpCounter));

        Collections.sort(bumps, new Comparator<Pair<Double, Integer>>() {
            @Override
            public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                double v1 = Math.abs(o1.getKey());
                double v2 = Math.abs(o2.getKey());
                if(v1 > v2)
                    return -1;
                if(v2 > v1)
                    return 1;
                return 0;
            }
        });
        if(bumpCounter<2) {
            listOfFeatures.add(new Pair<Object, Object>("AreaOfFirstOfTop2Bumps", 0));
            return;
        }
        //biggest bump was first bump

        if(bumps.get(0).getValue() < bumps.get(1).getValue()) {
            //double pos = (bumps.get(0).getFirst() > 0)? 1 : -1;
            //listOfFeatures.add(new Pair<Object, Object>("AreaOfFirstOfTop2Bumps", pos));
            listOfFeatures.add(new Pair<Object, Object>("AreaOfFirstOfTop2Bumps", bumps.get(0).getKey()));
        }
        //biggest bump was not first
        else {
            //double pos = (bumps.get(1).getFirst() > 0)? 1 : -1;
            //listOfFeatures.add(new Pair<Object, Object>("AreaOfFirstOfTop2Bumps", pos));
            listOfFeatures.add(new Pair<Object, Object>("AreaOfFirstOfTop2Bumps", bumps.get(1).getKey()));
        }
    }

    public void calculateOverallAcceleration(){

    }

    public void calculateVerticalTimedDistribution(int bins){
        ArrayList<Double> upValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            upValues.add(featureLine.getAccUp());
            xAxisDummy.add(i);
            i++;
        }

        double[] up = new double[upValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < upValues.size(); j++) {
            up[j] = upValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction timedVerticalDist = interpolator.interpolate(dummy,up);

        for (int j = 0; j < bins; j++) {
            listOfFeatures.add(new Pair<>("TIMED_VERTICAL_BIN_" + (j+1) + "_OF_" + bins, timedVerticalDist.value((double) j / bins * timedVerticalDist.getKnots().length)));
        }
    }

    public void calculateOrientationChange(){
        double graX = 0;
        double graY = 0;
        double graZ = 0;
        int samples = 20;
        for (int i = 0; i < samples; i++) {
            graX += listOfFeatureLines.get(i).getGraX();
            graY += listOfFeatureLines.get(i).getGraY();
            graZ += listOfFeatureLines.get(i).getGraZ();
        }

        double graX_start= Math.abs(graX/samples);
        double graY_start= Math.abs(graY/samples);
        double graZ_start= Math.abs(graZ/samples);

        graX = 0;
        graY = 0;
        graZ = 0;
        int size = listOfFeatureLines.size();
        for (int i = 0; i < samples; i++) {
            graX += listOfFeatureLines.get(size-i-1).getGraX();
            graY += listOfFeatureLines.get(size-i-1).getGraY();
            graZ += listOfFeatureLines.get(size-i-1).getGraZ();
        }

        double graX_end= Math.abs(graX/samples);
        double graY_end= Math.abs(graY/samples);
        double graZ_end= Math.abs(graZ/samples);

        /*ArrayList<Pair<String, Double>> start_orientations = new ArrayList<>();
        start_orientations.add(new Pair<>("x", graX_start));
        start_orientations.add(new Pair<>("y", graY_start));
        start_orientations.add(new Pair<>("z", graZ_start));

        Collections.sort(start_orientations, new Comparator<Pair<String, Double>>() {
                    @Override
                    public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                        if(o1.getValue() > o2.getValue())
                            return -1;
                        if(o2.getValue() > o1.getValue())
                            return 1;
                        return 0;
                    }
                });

        double[] after = new double[]{0,0,0};
        int i = 0;
        for (Pair<String, Double> start_orientation : start_orientations) {
            if(start_orientation.getKey().equals("x"))
                after[i] = graX_end;
            if(start_orientation.getKey().equals("y"))
                after[i] = graY_end;
            if(start_orientation.getKey().equals("z"))
                after[i] = graZ_end;

            i++;
        }

        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_1ST", start_orientations.get(0).getValue()));
        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_2ND", start_orientations.get(1).getValue()));
        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_3RD", start_orientations.get(2).getValue()));

        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_1ST", after[0]));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_2ND", after[1]));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_3RD", after[2]));*/

        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_X", graX_start));
        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_Y", graY_start));
        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_Z", graZ_start));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_X", graX_end));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_Y", graY_end));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_Z", graZ_end));
    }

    public void calculateStartingOrientation(){
        ArrayList<double[]> measurementsFromStart = new ArrayList<>();
        double graX = 0;
        double graY = 0;
        double graZ = 0;
        int samples = 20;
        for (int i = 0; i < samples; i++) {
            graX += listOfFeatureLines.get(i).getGraX();
            graY += listOfFeatureLines.get(i).getGraY();
            graZ += listOfFeatureLines.get(i).getGraZ();
        }

        graX= graX/samples;
        graY= graY/samples;
        graZ= graZ/samples;

        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_X", graX));
        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_Y", graY));
        listOfFeatures.add(new Pair<Object, Object>("START_ORIENTATION_GRAVITY_Z", graZ));
    }

    public void calculateVerticalAccelerationDerivative(){
        ArrayList<Double> upValues = new ArrayList<>();
        ArrayList<Double> xAxisDummy = new ArrayList<>();
        Double i = 0.0;
        for (FeatureLine featureLine : listOfFeatureLines) {
            upValues.add(featureLine.getAccUp());
            xAxisDummy.add(i);
            i++;
        }

        double[] up = new double[upValues.size()];
        double[] dummy = new double[xAxisDummy.size()];
        for (int j = 0; j < upValues.size(); j++) {
            up[j] = upValues.get(j);
        }
        for (int j = 0; j < xAxisDummy.size(); j++) {
            dummy[j] = xAxisDummy.get(j);
        }

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction timedVerticalDist = interpolator.interpolate(dummy,up);
        UnivariateFunction direvative = timedVerticalDist.derivative();

    }

    public void calculateSumOfUpwardsAcceleration(){
        double sum_up = 0;
        for (FeatureLine fl : listOfFeatureLines) {
            double acc_up = fl.getAccUp();
            if(acc_up> 0)
                sum_up+= acc_up;
        }
        listOfFeatures.add(new Pair<Object, Object>("SUM_OF_UPWARDS_ACCELERATION", sum_up));
    }

    public void calculateSumOfDownwardsAcceleration(){
        double sum_down = 0;
        for (FeatureLine fl : listOfFeatureLines) {
            double acc_down = fl.getAccUp();
            if(acc_down < 0)
                sum_down+= acc_down;
        }
        listOfFeatures.add(new Pair<Object, Object>("SUM_OF_DOWNWARDS_ACCELERATION", sum_down));
    }

    public void calculateUpCorrelationWithDiscounted(){
        double rms_sum_X = 0;
        double rms_sum_Y = 0;
        double rms_sum_Z = 0;

        for (FeatureLine fl: listOfFeatureLines) {
            double accUp = fl.getAccUp();
            double rms_dif_x;
            rms_dif_x = fl.getEffAccX() - accUp;
            rms_sum_X += Math.pow(rms_dif_x, 2);
            double rms_dif_y = fl.getEffAccX() - accUp;
            rms_sum_Y += Math.pow(rms_dif_y, 2);
            double rms_dif_z = fl.getEffAccX() - accUp;
            rms_sum_Z += Math.pow(rms_dif_z, 2);
        }
        int size = listOfFeatureLines.size();
        double rms_correlation_x = Math.sqrt(rms_sum_X/size);
        listOfFeatures.add(new Pair<Object, Object>("RMS_CORRELATION_X_UP",rms_correlation_x));
        double rms_correlation_y = Math.sqrt(rms_sum_Y/size);
        listOfFeatures.add(new Pair<Object, Object>("RMS_CORRELATION_Y_UP",rms_correlation_y));
        double rms_correlation_z = Math.sqrt(rms_sum_Z/size);
        listOfFeatures.add(new Pair<Object, Object>("RMS_CORRELATION_Z_UP",rms_correlation_z));
    }

    public void calculateEndingOrientation(){
        ArrayList<double[]> measurementsFromStart = new ArrayList<>();
        double graX = 0;
        double graY = 0;
        double graZ = 0;
        int size = listOfFeatureLines.size();
        int samples = 20;
        for (int i = 0; i < samples; i++) {
            graX += listOfFeatureLines.get(size-i-1).getGraX();
            graY += listOfFeatureLines.get(size-i-1).getGraY();
            graZ += listOfFeatureLines.get(size-i-1).getGraZ();
        }

        graX= graX/samples;
        graY= graY/samples;
        graZ= graZ/samples;

        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_X", graX));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_Y", graY));
        listOfFeatures.add(new Pair<Object, Object>("END_ORIENTATION_GRAVITY_Z", graZ));
    }

    public void calculateOrientationJitter(){
        double changeX = 0;
        double lastGraX = 0;
        double changeY = 0;
        double lastGraY = 0;
        double changeZ = 0;
        double lastGraZ = 0;
        for (FeatureLine fl : listOfFeatureLines) {
            if(!(lastGraX == 0)) {
                changeX += fl.getGraX() - lastGraX;
                changeY += fl.getGraY() - lastGraY;
                changeZ += fl.getGraZ() - lastGraZ;
            }
            lastGraX = fl.getGraX();
            lastGraY = fl.getGraY();
            lastGraZ = fl.getGraZ();
        }
        double totalChange = (changeX + changeY + changeZ) / listOfFeatureLines.size();
        listOfFeatures.add(new Pair<Object, Object>("ORIENTATION_JITTER", totalChange));
    }

    public List<Pair<?, ?>> getListOfFeatures() {
        return listOfFeatures;
    }

    public List<FeatureLine> getListOfFeatureLines() {
        return listOfFeatureLines;
    }
}
