package com.example.sputnik.gesturecalc.calc.animeditor;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.util.ButtonGridCompat;

/**
 * Created by Sputnik on 2/9/2018.
 */

public class AnimEditorActivity extends AppCompatActivity {
    private AnimEditorPresenter presenter;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_anim_editor);

        AnimEditorFragment fragment = new AnimEditorFragment();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.AnimEditorFragmentContainer, fragment).commit();

        presenter = new AnimEditorPresenter(fragment);
        fragment.setPresenter(presenter);
    }
}
