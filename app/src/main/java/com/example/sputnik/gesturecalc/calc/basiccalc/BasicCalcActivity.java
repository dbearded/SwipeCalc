package com.example.sputnik.gesturecalc.calc.basiccalc;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sputnik.gesturecalc.R;

public class BasicCalcActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_basic_calc);

        if (savedInstanceState == null) {

            BasicCalcFragment fragment = new BasicCalcFragment();

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.BasicCalcFragmentContainer, fragment).commit();

            BasicCalcPresenter presenter = new BasicCalcPresenter(fragment);
            fragment.setPresenter(presenter);
        }
    }
}