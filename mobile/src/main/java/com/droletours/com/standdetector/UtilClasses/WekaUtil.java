package com.droletours.com.standdetector.UtilClasses;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

import Core.ClassifierType;
import Core.Window;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by Rune on 27-04-2016.
 */
public class WekaUtil {

    public static Instance convertWindowToInstance(Window window, Instances header){
        List<Pair<?,?>> features = window.getListOfFeatures();
        int size = header.numAttributes();

        DenseInstance denst = new DenseInstance(size);
        for (Pair pair : features) {

            Attribute attribute = header.attribute((String) pair.getKey());
            Object val = pair.getValue();
            if(val instanceof Integer)
                denst.setValue( attribute,(Integer) val);
            if(val instanceof Double)
                denst.setValue( attribute,(Double) val);
        }
        return denst;
    }

    public static Instances getInstances(Window window, ClassifierType type){
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (Pair pair : window.getListOfFeatures()) {
            attributes.add(new Attribute((String) pair.getKey()));
        }

        ArrayList<String> labels = new ArrayList<>();
        if(type == ClassifierType.SIT_STAND_CLASSIFIER){
            labels.add("sit");
            labels.add("stand");
        } else if( type == ClassifierType.EVENT_SNIFFER){
            labels.add("null");
            labels.add("event");
        }

        Attribute classLabel = new Attribute("class", labels);
        attributes.add(classLabel);

        Instances dataset = new Instances("dataset", attributes, 1000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        return dataset;
    }

    public static Instances getInstancesEvent(Window window){
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (Pair pair : window.getListOfFeatures()) {
            attributes.add(new Attribute((String) pair.getKey()));
        }

        ArrayList<String> labels = new ArrayList<>();
        labels.add("null");
        labels.add("stand");


        Attribute classLabel = new Attribute("class", labels);
        attributes.add(classLabel);

        Instances dataset = new Instances("dataset", attributes, 1000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        return dataset;
    }

    public static Instances getInstacesStandSit(Window window){
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (Pair pair : window.getListOfFeatures()) {
            attributes.add(new Attribute((String) pair.getKey()));
        }

        ArrayList<String> labels = new ArrayList<>();
        labels.add("stand");
        labels.add("sit");

        Attribute classLabel = new Attribute("class", labels);
        attributes.add(classLabel);

        Instances dataset = new Instances("dataset", attributes, 1000);
        dataset.setClassIndex(dataset.numAttributes()-1);
        return dataset;
    }
}
