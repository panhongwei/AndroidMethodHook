package com.panda.hook.javahook;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by panda on 17/8/8.
 */

public class HookUtil extends Object{
    int test;
    private static HashMap<String,Method> conMap=new HashMap();
    static {
        System.loadLibrary("nativemode");
        try {
            if(isArt()) {
                computeAccess(MethodDemo.class.getDeclaredMethod("m1"));
                computeSupperCls(Object.class.getDeclaredFields()[0], HookUtil.class.getDeclaredField("test"));
            }
        }catch (Exception e){e.printStackTrace();}
    }
    private static native long replaceNativeArt(Member src,Member new_,Method invoker);
    private static native long replaceNativeDavilk(Member src,MethodCallback callback);
    private static native void repair(Member src,long old);
    private static native void computeAccess(Method m);
    private static native void computeSupperCls(Field fld,Field test);
    private static native boolean setSupperCls(Field fld);
    private static native Object invokeDavConstructor(Member method,
                Class<?>[] parameterTypes, Class<?> returnType, Object thisObject, Object[] args)
            throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
//    private static native Method replaceNativeDavilk(Member src,Member new_);
    public static native void initMethod(Class cls,String name,String sig,boolean istatic);
    public native Object invoke(Object obj,Object[] args);
    public static native Object invokeReplace(Object thiz,Object obj,Object[] args);
    public static boolean isArt(){
        final String vmVersion = System.getProperty("java.vm.version");
        boolean isArt = vmVersion != null && vmVersion.startsWith("2");
        return isArt;
    }
    public static boolean setMadeClassSuper(Class cls){
        try{
            Field flag=cls.getField("flag");
            setSupperCls(flag);
            return true;
        }catch (Exception e){
            Log.e("panda","field not found!",e);
            return false;
        }
    }
    private static Method getOldConstructor(Constructor con,Method invoker,long addr){
        try {
            if(!isArt()){
                return null;
            }
            repair(invoker,addr);
            return invoker;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Method getOldMethod(Method con,Method invoker,long addr){
        try {
            if(!isArt()){
                return null;
            }
            repair(invoker,addr);
            return invoker;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args)
            throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (args == null) {
            args = new Object[0];
        }

        Class<?>[] parameterTypes;
        Class<?> returnType;
        if (method instanceof Method) {
            parameterTypes = ((Method) method).getParameterTypes();
            returnType = ((Method) method).getReturnType();

        } else if (method instanceof Constructor) {
            parameterTypes = ((Constructor<?>) method).getParameterTypes();
            returnType = null;
        } else {
            throw new IllegalArgumentException("method must be of type Method or Constructor");
        }
        return invokeDavConstructor(method, parameterTypes, returnType, thisObject, args);
    }
    public static Member repairMethod(Member method,Method invoker,long old){
//        Log.d("panda",Modifier.isStatic(invoker.getModifiers())+"");
//        return invoker;
        if(isArt()){
            return invoker;
        }else {
            return method;
        }
    }
    public static void generateMethodaArt(BackMethod old){
        try {
            long bak=replaceNativeArt(old.getOldMethod(), old.getNewMethod(),old.getInvoker())&0xffffffffffffffffl;
            old.setBackAddr(bak);
            return;
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
    public static void generateMethodDavilk(BackMethod old){
        try {
            long bak=replaceNativeDavilk(old.getOldMethod(),(MethodCallback) old.getCallback());
            old.setBackAddr(bak);
            return;
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
    public static int apiLevel(){
        return Build.VERSION.SDK_INT;
    }
    public static int compute(){
        if(!isArt()){
            return  0;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Class<?> abstractMethodClass = Class.forName("java.lang.reflect.Executable");
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                artMethodField.setAccessible(true);
                Method m1=MethodDemo.class.getDeclaredMethod("m1");
                Method m2=MethodDemo.class.getDeclaredMethod("m2");
                int res=(int)(artMethodField.getLong(m1)-artMethodField.getLong(m2));
                return Math.abs(res);
            } catch (Throwable e) {
                Log.d("panda","",e);
            }
        }else if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> abstractMethodClass = Class.forName("java.lang.reflect.AbstractMethod");
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                artMethodField.setAccessible(true);
                Method m1=MethodDemo.class.getDeclaredMethod("m1");
                Method m2=MethodDemo.class.getDeclaredMethod("m2");
                int res=(int)(artMethodField.getLong(m1)-artMethodField.getLong(m2));
                return Math.abs(res);
            } catch (Throwable e) {
                Log.d("panda","",e);
            }
        }else{
            try {
                Class<?> artMethodClass = Class.forName("java.lang.reflect.ArtMethod");
                Field f_objectSize = Class.class.getDeclaredField("objectSize");
                f_objectSize.setAccessible(true);
                return f_objectSize.getInt(artMethodClass);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
    public static String queryMethodShorty(Member m){
        String mShort="";
        Type[] types=null;
        if(m instanceof Method){
            types=((Method)m).getParameterTypes();
        }else if(m instanceof Constructor){
            types=((Constructor)m).getParameterTypes();
        }
        for(Type c:types){
            //Log.i("panda",c.toString());
            String name=c.toString();
            if(name.equals(int.class.getName())){
                mShort=mShort+"I";
            }else if(name.equals(byte.class.getName())){
                mShort=mShort+"B";
            }else if(name.equals(char.class.getName())){mShort=mShort+"C";}
            else if(name.equals(short.class.getName())){mShort=mShort+"S";}
            else if(name.equals(float.class.getName())){mShort=mShort+"F";}
            else if(name.equals(boolean.class.getName())){mShort=mShort+"Z";}
            else if(name.equals(long.class.getName())){mShort=mShort+"J";}
            else if(name.equals(double.class.getName())){mShort=mShort+"D";}
            else if(name.startsWith("[")){mShort=mShort+"L";}
            else {mShort=mShort+"L";}
        }
        return mShort;
    }
    public static String queryReturnType(Member m){
        String mShort="";
        if(m instanceof Method){
            Type rty=((Method)m).getReturnType();
            String name=rty.toString();
            if(name.equals(int.class.getName())){
                mShort=mShort+"I";
            }else if(name.equals(byte.class.getName())){
                mShort=mShort+"B";
            }else if(name.equals(char.class.getName())){mShort=mShort+"C";}
            else if(name.equals(short.class.getName())){mShort=mShort+"S";}
            else if(name.equals(float.class.getName())){mShort=mShort+"F";}
            else if(name.equals(boolean.class.getName())){mShort=mShort+"Z";}
            else if(name.equals(long.class.getName())){mShort=mShort+"J";}
            else if(name.equals(double.class.getName())){mShort=mShort+"D";}
            else {mShort=mShort+"L";}
            return mShort;
        }else if(m instanceof Constructor){
            //((Constructor)m).;
            return "L";
        }
        return  "L";
    }
    public static String sign(Member m){
        String shorty=queryMethodShorty(m);
        String ret=queryReturnType(m);
        if(m instanceof Method){
            return m.getName()+shorty+ret;
        }else {
            return shorty + ret;
        }
    }
    public static boolean isGenerateMethod(Member m){
        Iterator<Map.Entry<String, Method>> it = conMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Method> entry = it.next();
            if(entry.getValue().equals(m)){
                return true;
            }
        }
        return false;
    }
    public void generateMethodInvoke(Method m,long ptr)throws Throwable{
        Class<?> abstractMethodClass;
        try {
            abstractMethodClass = Class.forName("java.lang.reflect.AbstractMethod");
        }catch (Exception e){
            abstractMethodClass = Class.forName("java.lang.reflect.Executable");
        }
        Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");


    }
}
