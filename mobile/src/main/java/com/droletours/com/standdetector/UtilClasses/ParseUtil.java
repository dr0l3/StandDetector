package com.droletours.com.standdetector.UtilClasses;

import android.util.Log;

import com.droletours.com.standdetector.Correction;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import Core.Window;

/**
 * Created by Rune on 27-04-2016.
 */
public class ParseUtil {

    public static void sendMostLikelyWindowToParseServerWithRichCorrection(Window window, Correction correction, String biasConfiguration, String classifierObjectID){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(window);
            byte[] windowBytes = bos.toByteArray();
            ParseFile windowParseFile = new ParseFile("Window",windowBytes);
            windowParseFile.saveInBackground();

            ParseObject windowObject = new ParseObject("WronglyClassifiedWindow");
            windowObject.put("UserClassifierObjectID", classifierObjectID);
            windowObject.put("BiasConfiguration", biasConfiguration);
            windowObject.put("Classified_event", correction.getClassified_event());
            windowObject.put("Classified_sitstand", correction.getClassified_sitstand());
            windowObject.put("Corrected_event", correction.getCorrected_event());
            windowObject.put("Corrected_sitstand", correction.getCorrection_sitstand());
            windowObject.put("Window",windowParseFile);
            windowObject.saveInBackground();
            Log.d("stuff", "sent window to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMostLikelyWindowToParseServer(Window max_probability_window, String groupname) {
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(max_probability_window);
            byte[] windowBytes = bos.toByteArray();
            ParseFile windowParseFile = new ParseFile("Window",windowBytes);
            windowParseFile.saveInBackground();

            ParseObject windowObject = new ParseObject("WronglyClassifiedWindow");
            windowObject.put("Group", groupname);
            windowObject.put("Window",windowParseFile);
            windowObject.saveInBackground();
            Log.d("stuff", "sent window to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
