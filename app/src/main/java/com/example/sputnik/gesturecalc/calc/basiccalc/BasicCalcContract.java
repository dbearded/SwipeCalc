package com.example.sputnik.gesturecalc.calc.basiccalc;

import com.example.sputnik.gesturecalc.BasePresenter;
import com.example.sputnik.gesturecalc.BaseView;

/**
 * Created by Sputnik on 2/16/2018.
 */

interface BasicCalcContract {

    interface View extends BaseView<Presenter>{
        void updateDisplay(String expression);
        void updatePreview(String expression);
    }

    interface Presenter extends BasePresenter{
        void addNewValue(String str);
        void clear();
        void delete();
        void setExpression(String expr);
    }
}
