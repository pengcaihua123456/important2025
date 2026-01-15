package com.example.matrixdemo.utils;

import java.util.Map;

public class TheadElement {

    public void test(){
        Map<Thread, StackTraceElement[]>  map=Thread.getAllStackTraces();
    }

}
