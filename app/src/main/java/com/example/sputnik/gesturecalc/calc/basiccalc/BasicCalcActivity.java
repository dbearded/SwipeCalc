package com.example.sputnik.gesturecalc.calc.basiccalc;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sputnik.gesturecalc.R;

public class BasicCalcActivity extends AppCompatActivity{

    private BasicCalcPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_basic_calc);

        BasicCalcFragment fragment = new BasicCalcFragment();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.BasicCalcFragmentContainer, fragment).commit();

        presenter = new BasicCalcPresenter(fragment);
        fragment.setPresenter(presenter);
    }
}