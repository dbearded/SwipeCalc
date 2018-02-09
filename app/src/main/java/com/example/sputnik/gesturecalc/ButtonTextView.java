package com.example.sputnik.gesturecalc;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Sputnik on 2/7/2018.
 */
public class ButtonTextView extends android.support.v7.widget.AppCompatTextView {

    public ButtonTextView(Context context) {
        super(context);
        setupClickListener();
    }

    public ButtonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupClickListener();
    }

    public ButtonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupClickListener();
    }

    private void setupClickListener() {
        this.setClickable(true);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(10);
            }
        });
    }
}
