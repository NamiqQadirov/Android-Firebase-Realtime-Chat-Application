package com.example.namiq.egisterpp.boommenu;

import com.example.namiq.egisterpp.R;

/**
 * Created by Weiping Huang at 23:44 on 16/11/21
 * For Personal Open Source
 * Contact me at 2584541288@qq.com or nightonke@outlook.com
 * For more projects: https://github.com/Nightonke
 */
public class BuilderManager {

    private static int[] imageResources = new int[]{
            R.drawable.camera,
            R.drawable.gallery,
            R.drawable.voice,
            R.drawable.location

    };

    private static int imageResourceIndex = 0;

    public static int getImageResource() {
        if (imageResourceIndex >= imageResources.length) imageResourceIndex = 0;
        return imageResources[imageResourceIndex++];
    }


     private static BuilderManager ourInstance = new BuilderManager();



    private BuilderManager() {
    }
}
