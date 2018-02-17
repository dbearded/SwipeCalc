package com.example.sputnik.gesturecalc;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}