package com.panda.hook.andhook;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;
import com.panda.hook.javahook.BackMethod;
import com.panda.hook.javahook.HookManager;
import com.panda.hook.javahook.HookUtil;
import com.panda.hook.javahook.MethodUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Log.d("panda","res="+ test(this,123,2444,'Z')+"");
            Log.d("panda","res="+test(this,333,4444,'E')+"");
            new Test(11111,22222);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     public static double test(Object thiz,int a,int b,char cr){
        return (a+0.0)/b;
    }
}
