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

/**
 * Created by panda on 17/8/8.
 */

public class HookUtil {
    private static HashMap<String,Method> conMap=new HashMap();
    static {
        System.loadLibrary("nativeutils");
        try {
            computeAccess(MethodDemo.class.getDeclaredMethod("m1"));
        }catch (Exception e){e.printStackTrace();}
    }
    private static native long replaceNative(Member src,Member new_);
    private static native void repair(Member src,long old);
    private static native void computeAccess(Method m);
    private static native void invokeDavConstructor(Member method,
                Class<?>[] parameterTypes, Class<?> returnType, Object thisObject, Object[] args)
            throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
//    private static native Method replaceNativeDavilk(Member src,Member new_);
    public static native void initMethod(Class cls,String name,String sig,boolean istatic);
    public static boolean isArt(){
        final String vmVersion = System.getProperty("java.vm.version");
        boolean isArt = vmVersion != null && vmVersion.startsWith("2");
        return isArt;
    }
//    public static void invokeArtConstructor(Constructor con,Object thiz,Object[] args)
//            throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
//    {
//        String name=con.getDeclaringClass().getSimpleName()+"_"+sign(con);
//        Method m=conMap.get(name);
//        if(m==null){
//            m=generateConstructor(con);
//            if(m==null)
//                throw new NullPointerException("");
//            conMap.put(name,m);
//        }
//        m.invoke(thiz,args);
//    }
//    public static Method getArtMethod(Method con)
//            throws NullPointerException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
//    {
//        String name=con.getDeclaringClass().getSimpleName()+"_"+sign(con);
//        Method m=conMap.get(name);
//        if(m==null){
//            m=generateMethod(con);
//            if(m==null)
//                throw new NullPointerException("");
//            conMap.put(name,m);
//        }
//        return m;
//    }
    private static Method getOldConstructor(Constructor con,long addr){
        try {
            if(!isArt()){
                return null;
            }
            Class<?> abstractMethodClass = Class.forName("java.lang.reflect.AbstractMethod");
            if(Build.VERSION.SDK_INT<23){
                Class<?> artMethodClass = Class.forName("java.lang.reflect.ArtMethod");
                //Get the original artMethod field
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                if (!artMethodField.isAccessible()) {
                    artMethodField.setAccessible(true);
                }
                Object srcArtMethod = artMethodField.get(con);

                Constructor<?> constructor = artMethodClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object destArtMethod = constructor.newInstance();

                //Fill the fields to the new method we created
                for (Field field : artMethodClass.getDeclaredFields()) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(destArtMethod, field.get(srcArtMethod));
                }
                Field access=artMethodClass.getDeclaredField("accessFlags");
                access.setAccessible(true);
                Method newCon = Method.class.getConstructor(artMethodClass).newInstance(destArtMethod);
                newCon.setAccessible(true);
                repair(newCon, addr);
                return newCon;
            }else {
                Constructor<Method> constructor = Method.class.getDeclaredConstructor();
                // we can't use constructor.setAccessible(true); because Google does not like it
                AccessibleObject.setAccessible(new AccessibleObject[]{constructor}, true);
                Method m = constructor.newInstance();
                m.setAccessible(true);
                for (Field field : abstractMethodClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(m, field.get(con));
                }
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                artMethodField.setAccessible(true);
                artMethodField.set(m, addr);
                m.setAccessible(true);
                return m;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Method getOldMethod(Method con,long addr){
        try {
            if(!isArt()){
                return null;
            }
            Class<?> abstractMethodClass = Class.forName("java.lang.reflect.AbstractMethod");
            if(Build.VERSION.SDK_INT<23){
                Class<?> artMethodClass = Class.forName("java.lang.reflect.ArtMethod");
                //Get the original artMethod field
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                if (!artMethodField.isAccessible()) {
                    artMethodField.setAccessible(true);
                }
                Object srcArtMethod = artMethodField.get(con);

                Constructor<?> constructor = artMethodClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object destArtMethod = constructor.newInstance();
//
//                //Fill the fields to the new method we created
//                for (Field field : artMethodClass.getDeclaredFields()) {
//                    if (!field.isAccessible()) {
//                        field.setAccessible(true);
//                    }
//                    field.set(destArtMethod, field.get(srcArtMethod));
//                }
                Method newMethod = Method.class.getConstructor(artMethodClass).newInstance(destArtMethod);
                newMethod.setAccessible(true);
                repair(newMethod, addr);
                Field access=artMethodClass.getDeclaredField("accessFlags");
                access.setAccessible(true);
                int flags=(int)access.get(destArtMethod);
                flags=flags|0x0002;
                access.set(destArtMethod,flags);
                return newMethod;
            }else {
                Constructor<Method> constructor = Method.class.getDeclaredConstructor();
                // we can't use constructor.setAccessible(true); because Google does not like it
                AccessibleObject.setAccessible(new AccessibleObject[]{constructor}, true);
                Method m = constructor.newInstance();
                m.setAccessible(true);
                for (Field field : abstractMethodClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(m, field.get(con));
                }
                Field artMethodField = abstractMethodClass.getDeclaredField("artMethod");
                artMethodField.setAccessible(true);
                artMethodField.set(m, addr);
                m.setAccessible(true);
                repair(m,-1);
                return m;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void invokeOriginalMethod(Member method, Object thisObject, Object[] args)
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
        invokeDavConstructor(method, parameterTypes, returnType, thisObject, args);
    }
    public static Member repairMethod(Member method,long old){
        if(isArt()){
            String name=method.getDeclaringClass().getSimpleName()+"_"+sign(method);
            Method m=conMap.get(name);
            if(m!=null){
                return m;
            }
            if(method instanceof Method){
                Method m_=getOldMethod((Method)method,old);
                Log.d("panda",m_+"");
                conMap.put(name,m_);
                return m_;
            }else{
                Method m_=getOldConstructor((Constructor) method,old);
                conMap.put(name,m_);
                return m_;
            }
        }else {
            repair(method, old);
            return method;
        }
    }
    public static void generateMethod(BackMethod old){
        try {
            long bak=replaceNative(old.getOldMethod(), old.getNewMethod());
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
        if (Build.VERSION.SDK_INT >= 23) {
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
}
