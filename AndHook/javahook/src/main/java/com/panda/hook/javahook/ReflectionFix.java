package com.panda.hook.javahook;

import android.util.Log;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by panda on 17/10/17.
 */

public class ReflectionFix{
    static long oldPtr=0;
    static Method repairedInvoke=null;
    static Method oldInvoke=null;
    public static void fixMethodReflection(){
        try {
            if(oldPtr!=0){
                return;
            }
            oldInvoke=Class.class.getMethod("invoke",Object.class,Object[].class);
            Method invoke_new=HookUtil.class.getMethod("invoke",Object.class,Object[].class);
            BackMethod b=new BackMethod();
            b.setOldMethod(oldInvoke);
            b.setNewMethod(invoke_new);
            HookUtil.generateMethod(b);
            oldPtr=(long)b.getBackAddr();
            Log.d("panda","in fixMethodReflection="+oldPtr);
        } catch (NoSuchMethodException e) {
            Log.e("panda","",e);
        }
    }
    public static Object invoke(Object thiz,Object obj,Object[] args){
        try {
            if (oldPtr == 0||oldInvoke==null){
                throw new IllegalAccessException("didn't invoke ReflectionFix fixMethodReflection!");
            }
            if(!(thiz instanceof Method)){
                throw new IllegalAccessException("unknown instance!");
            }
            Method mm=(Method)thiz;
            if(!Modifier.isStatic(mm.getModifiers())&&!HookUtil.isGenerateMethod((Member)thiz)
                    &&HookManager.isHooked((Member) thiz)){
//                Log.d("panda","repairedInvoke="+thiz);
                return MethodUtil.invoke(HookManager.getCallerStr((Member) thiz),obj,args);
            }

            return HookUtil.invokeReplace(thiz,obj,args);
//            if(repairedInvoke==null){
//                repairedInvoke=(Method) HookUtil.repairMethod( oldInvoke,oldPtr);
//                return repairedInvoke.invoke(obj,args);
//            }else {
//                return repairedInvoke.invoke(obj,args);
//            }
        }catch (Throwable  e){
            Log.e("panda","",e);
            return null;
        }
    }
}
//    static long oldPtr=0;
//    static Method repairedInvoke=null;
//    static Method oldInvoke=null;
//    public static void fixMethodReflection(){
//        try {
//            if(oldPtr!=0){
//                return;
//            }
//            Method old=Method.class.getMethod("invoke",Object.class,Object[].class);
//            Method invoke_new=HookUtil.class.getMethod("invoke",Object.class,Object[].class);
//            BackMethod b=new BackMethod();
//            b.setOldMethod(old);
//            b.setNewMethod(invoke_new);
//            HookUtil.generateMethod(b);
//            oldPtr=(long)b.getBackAddr();
//            Log.d("panda","in fixMethodReflection");
//        } catch (NoSuchMethodException e) {
//            Log.e("panda","",e);
//        }
//    }
//    public static Object invoke(Object thiz,Object obj,Object[] args){
//        try {
//            if (oldPtr == 0||oldInvoke==null){
//                throw new IllegalAccessException("didn't invoke ReflectionFix fixMethodReflection!");
//            }
//            if(!(thiz instanceof Method)){
//                throw new IllegalAccessException("unknown instance!");
//            }
//            Log.d("panda","repairedInvoke="+thiz);
//            if(HookManager.isHooked((Member) thiz)){
//                return MethodUtil.invoke(HookManager.getCallerStr((Member) thiz),obj,args);
//            }
//            if(repairedInvoke==null){
//                repairedInvoke=(Method) HookUtil.repairMethod( oldInvoke,oldPtr);
//                return repairedInvoke.invoke(obj,args);
//            }else {
//                return repairedInvoke.invoke(obj,args);
//            }
//        }catch (Throwable  e){
//            Log.e("panda","",e);
//            return null;
//        }
//    }