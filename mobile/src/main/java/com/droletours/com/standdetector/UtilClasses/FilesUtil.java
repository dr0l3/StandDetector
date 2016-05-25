package com.droletours.com.standdetector.UtilClasses;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by Rune on 27-04-2016.
 */
public class FilesUtil {

    public static File getDownloadsDir(){
        File file = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)));
        if (!file.mkdirs()){
            Log.i("file not present", "the file was not present");
        }
        return file;
    }


    public static boolean isSdReadable() {

        boolean mExternalStorageAvailable = false;
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mExternalStorageAvailable = true;
                Log.i("isSdReadable", "External storage card is readable.");
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                Log.i("isSdReadable", "External storage card is readable.");
                mExternalStorageAvailable = true;
            } else {
                mExternalStorageAvailable = false;
                Log.i("isSdReadable", "External storage card is not readable");
            }
        } catch (Exception ex) {
        }
        return mExternalStorageAvailable;
    }

    public static void exportToFile(Object object, String filename){
        int time = (int) System.currentTimeMillis();
        File path = FilesUtil.getDownloadsDir();
        //File file = new File(path+"/WindowExport"+time);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/"+filename);

        //write to file
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if(FilesUtil.isSdReadable()) {
                FileOutputStream fos = new FileOutputStream(file,true);
                ObjectOutputStream oss = new ObjectOutputStream(fos);
                oss.writeObject(object);
                oss.flush();
                oss.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
