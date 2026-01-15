package com.example.matrixdemo.impl;

import com.tencent.mrs.plugin.IDynamicConfig;

/**
 * @author: njb
 * @date: 2023/8/10 11:24
 * @desc:
 */
public class MatrixDynamicConfigImpl implements IDynamicConfig {
    public MatrixDynamicConfigImpl() {}

    public boolean isFPSEnable() { return true;}
    public boolean isTraceEnable() { return true; }
    public boolean isMatrixEnable() { return true; }
    public boolean isDumpHprof() {  return false;}

    @Override
    public String get(String key, String defStr) {
        return defStr;
    }

    @Override
    public int get(String key, int defInt) {
        return defInt;
    }

    @Override
    public long get(String key, long defLong) {
        return defLong;
    }

    @Override
    public boolean get(String key, boolean defBool) {
        return defBool;
    }

    @Override
    public float get(String key, float defFloat) {
        return defFloat;
    }
}
