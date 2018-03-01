package com.derekbearded.android.swipecalc.calc.animeditor;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.derekbearded.android.swipecalc.R;

/**
 * Created by Sputnik on 2/9/2018.
 */

public class AnimEditorActivity extends AppCompatActivity {

    AnimEditorPresenter presenter;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_anim_editor);

        AnimEditorFragment fragment = (AnimEditorFragment) getFragmentManager().findFragmentById(R.id.AnimEditorFragmentContainer);

        if (fragment == null) {
            fragment = new AnimEditorFragment();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.AnimEditorFragmentContainer, fragment).commit();
        }
        presenter = new AnimEditorPresenter(fragment);
    }
}
