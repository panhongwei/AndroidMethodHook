package com.panda.hook.andhook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.android.dx.command.Main;
import com.panda.hook.javahook.BackMethod;
import com.panda.hook.javahook.HookManager;
import com.panda.hook.javahook.MethodCallback;
import com.panda.hook.javahook.MethodHookParam;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by panda on 17/8/19.
 */

public class APP extends Application {
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            Method m=MainActivity.class.getDeclaredMethod("test",Object.class,int.class,int.class,char.class);
            Constructor conn=Test.class.getDeclaredConstructor(int.class,int.class);
            List<BackMethod> list= new ArrayList();
            BackMethod b=new BackMethod();
            b.setOldMethod(m);
            list.add(b);
            HookManager.findAndHookMethod(MainActivity.class, "test", Object.class, int.class, int.class, char.class, new MethodCallback() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
//                    Log.d("panda", "i'm in method beforeHookedMethod"+());
                    Log.d("panda", "i'm in method beforeHookedMethod");
                    Log.d("panda", param.thisObject+"");
//                    param.setResult(111.0);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Log.d("panda", param.getResult()+"");
                    param.setResult(112233.0);
                }
            });
//            HookManager.findAndHookMethod(MainActivity.class, "onCreate", Bundle.class, new MethodCallback() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    super.beforeHookedMethod(param);
//                    Log.d("panda", "onCreate:"+param.thisObject.getClass().getName());
//                }
//
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//                    Log.d("panda", "i'm in method " +param.method.getName()+" afterHookedMethod");
//                }
//            });
            HookManager.findAndHookConstructor(Test.class, int.class, int.class, new MethodCallback() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.d("panda", "i'm in method " +param.method.getName()+" beforeHookedMethod");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Log.d("panda", "i'm in method " +param.method.getName()+" afterHookedMethod");
                }
            });
            HookManager.startHooks(base);
        }catch (Exception e){
            Log.d("panda","",e);
        }
    }
    public void onCreate() {
        super.onCreate();
    }

}
