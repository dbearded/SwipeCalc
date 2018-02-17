package com.example.sputnik.gesturecalc.anim;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class FactoryAnimator {
    public enum Type{
        Line, Circle
    }
    public static PathAnimator makeAnimator(FactoryAnimator.Type type){
        PathAnimator animator;
        switch (type){
            case Line:
                animator = new LineAnimator();
                break;
            case Circle:
                animator = new CircleAnimator();
                break;
            default:
                animator = null;
        }
        return animator;
    }
}
