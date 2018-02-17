package com.example.sputnik.gesturecalc.util;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import java.util.jar.Attributes;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class ButtonGridFactory {
    public static ButtonGrid makeButtonGrid(Context context, int buildCode){
        if (buildCode <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            return new ButtonGridCompat(context);
        } else {
            return new ButtonGridBase(context);
        }
    }


    // Context context, AttributeSet attrs, int defStyleAttr
    public static ButtonGrid makeButtonGrid(Context context, AttributeSet attrs, int buildCode){
        if (buildCode <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            return new ButtonGridCompat(context, attrs);
        } else {
            return new ButtonGridBase(context, attrs);
        }
    }

    public static ButtonGrid makeButtonGrid(Context context, AttributeSet attrs, int defStyleAttr, int buildCode){
        if (buildCode <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            return new ButtonGridCompat(context, attrs, defStyleAttr);
        } else {
            return new ButtonGridBase(context, attrs, defStyleAttr);
        }
    }
}
