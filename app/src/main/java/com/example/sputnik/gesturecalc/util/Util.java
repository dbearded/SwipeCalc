package com.example.sputnik.gesturecalc.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sputnik.gesturecalc.anim.Animator;
import com.example.sputnik.gesturecalc.anim.Settings;

/**
 * Created by Sputnik on 2/22/2018.
 */

public class Util {

    private static final String SETTINGS_URI =  "com.example.sputnik.gesutrecalc.Settings";
    private static final String SPACING = "spacing";
    private static final String START_SIZE = "startSize";
    private static final String END_SIZE = "endSize";
    private static final String DURATION = "duration";
    private static final String OPACITY = "opacity";
    private static final String TYPE = "type";


    public static Settings loadAnimatorSettings(Context context){
        Settings settings = new Settings();
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_URI, Context.MODE_PRIVATE);
        settings.setSpacing(prefs.getFloat(SPACING, settings.getSpacing()));
        settings.setStartSize(prefs.getFloat(START_SIZE, settings.getStartSize()));
        settings.setEndSize(prefs.getFloat(END_SIZE, settings.getEndSize()));
        settings.setAnimationDuration(prefs.getLong(DURATION, settings.getAnimationDuration()));
        settings.setOpacity(prefs.getInt(OPACITY, settings.getOpacity()));
        settings.setType(Animator.Type.fromString(prefs.getString(TYPE, settings.getType().toString())));

        return settings;
    }

    public static void saveAnimatorSettings(Context context, Settings settings){
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_URI, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(SPACING, settings.getSpacing());
        editor.putFloat(START_SIZE, settings.getStartSize());
        editor.putFloat(END_SIZE, settings.getEndSize());
        editor.putLong(DURATION, settings.getAnimationDuration());
        editor.putInt(OPACITY, settings.getOpacity());
        editor.putString(TYPE, settings.getType().toString());
        editor.apply();
    }

    public static SharedPreferences getSharedPrefSettings(Context context){
        return context.getSharedPreferences(SETTINGS_URI, Context.MODE_PRIVATE);
    }

    public static Animator updateAnimatorSettingsOnChange(Animator animator, SharedPreferences prefs, String key){
        switch (key){
            case SPACING:
                animator.setSpacing(prefs.getFloat(key, animator.getSpacing()));
                break;
            case START_SIZE:
                animator.setStartSize(prefs.getFloat(key, animator.getStartSize()));
                break;
            case END_SIZE:
                animator.setEndSize(prefs.getFloat(key, animator.getEndSize()));
                break;
            case DURATION:
                animator.setAnimationDuration(prefs.getLong(key, animator.getAnimationDuration()));
                break;
            case TYPE:
                animator = Animator.changeType(animator, Animator.Type.fromString(prefs.getString(key, animator.getType().toString())));
                break;
            case OPACITY:
                animator.setOpacity(prefs.getInt(key, animator.getOpacity()));
                break;
        }
        return animator;
    }
}
