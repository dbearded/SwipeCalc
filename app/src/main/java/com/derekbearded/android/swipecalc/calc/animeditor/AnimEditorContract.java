package com.derekbearded.android.swipecalc.calc.animeditor;

import com.derekbearded.android.swipecalc.BasePresenter;
import com.derekbearded.android.swipecalc.BaseView;

/**
 * Created by Sputnik on 2/16/2018.
 */

interface AnimEditorContract {
    interface View extends BaseView<Presenter> {
        void updateDisplay(String expression);
        void updatePreview(String expression);
        void showDevOpts();
    }

    interface Presenter extends BasePresenter{
        void addNewValue(String symbol);
        void clear();
    }
}