package com.example.sputnik.gesturecalc.util;

import android.view.View;
import android.view.ViewGroup;

import com.example.sputnik.gesturecalc.anim.PathAnimator;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface ButtonGrid {
    public interface ButtonListener {
        void buttonPressed(String input);
    }

    public void setPathAnimator(PathAnimator animator);

    public void setPathActivator(final PathActivator activator);

    public void setupSize();

    public void setResetButton(View clear);

    public void registerButtonListener(ButtonListener listener);

    public void unregisterButtonListener(ButtonListener listener);
}