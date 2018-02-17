package com.example.sputnik.gesturecalc.calc.animeditor;

import com.example.sputnik.gesturecalc.BasePresenter;
import com.example.sputnik.gesturecalc.BaseView;

/**
 * Created by Sputnik on 2/16/2018.
 */

interface AnimEditorContract {
    interface View extends BaseView<Presenter> {
        void updateDisplay(String expression);
        void updatePreview(String expression);
    }

    interface Presenter extends BasePresenter{
        void addNewValue(String symbol);
        void clear();
    }
}