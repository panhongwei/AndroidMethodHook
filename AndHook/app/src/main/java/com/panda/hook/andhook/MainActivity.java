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
    public static MainActivity instance=new MainActivity();
    static {
        System.loadLibrary("test");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Log.d("panda","before invoke test =======>>>>>>");
            Log.d("panda", test(this,123,2444,'Z')+"");
            Log.d("panda","before invoke test1 =======>>>>>>");
            Log.d("panda","test1 res="+test1(this,333,4444,'E')+"");
            Log.d("panda","before invoke contrustor Test =======>>>>>>");
            new Test(11111,22222).log();
            Log.d("panda","before invoke reflect tt =======>>>>>>");
            tt(instance);
            Log.d("panda","before invoke reflect Test =======>>>>>>");
            Method t=MainActivity.class.getDeclaredMethod("test1",Object.class,int.class,int.class,char.class);
            t.invoke(this,this,111,333,'D');
        } catch (Exception e) {
            Log.e("panda","",e);
        }
    }
    public  static MainActivity test(Object thiz,int a,int b,char cr){
        return null;
    }
    public double test1(Object thiz,int a,int b,char cr){
        Log.d("panda","in new test1");
        Log.d("panda",this+"");
        return 111.001;
    }

    public int test2(){
        Log.d("panda","in new test2");
        return 111;
    }
    public static native void tt(MainActivity instance);
}
