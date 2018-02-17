package com.example.sputnik.gesturecalc.util;

import android.view.View;
import android.view.ViewGroup;

import com.example.sputnik.gesturecalc.anim.PathAnimator;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface ButtonGrid {
    interface ButtonListener {
        void buttonPressed(String input);
    }

    void setPathAnimator(PathAnimator animator);

    void setPathActivator(final PathActivator activator);

    void setupSize();

    void setResetButton(View clear);

    void registerButtonListener(ButtonListener listener);

    void unregisterButtonListener(ButtonListener listener);
}