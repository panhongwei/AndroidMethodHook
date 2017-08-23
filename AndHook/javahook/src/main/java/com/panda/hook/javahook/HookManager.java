package com.panda.hook.javahook;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.android.dx.DexMaker;
import com.android.dx.TypeId;

//import junit.framework.TestMine;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by panda on 17/8/8.
 */

public class HookManager {
    static private HashMap<String,BackMethod> hooked=new HashMap();
    static private Context context=null;
    static private List<BackMethod> needHooks=new ArrayList<>();
    public static Context getSystemContext() {
        if (context == null) {
            try {
                Class at = Class.forName("android.app.ActivityThread");
                Application current = (Application)at.getDeclaredMethod("currentApplication").invoke(null);
                return current.getBaseContext();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return context;
        }
    }
    public static void beginHook(List<BackMethod> methods){
        try {
            Iterator<BackMethod> it = methods.iterator();
            while(it.hasNext()){
                BackMethod m = it.next();
                String sigName=m.getOldMethod().getDeclaringClass().getSimpleName()+"."+HookUtil.sign(m.getOldMethod());
                if(hooked.get(sigName)!=null){
                    Log.d("panda",m.getOldMethod().getName()+" is hooked!");
                    it.remove();
                }
            }
            if(methods.size()<=0){
                return;
            }
            DexMaker dexMaker = new DexMaker();
            Map<String,TypeId> classes=new HashMap<>();
            for(BackMethod m:methods){
//                m.getDeclaringClass().getName().equals()
                String name=m.getOldMethod().getDeclaringClass().getName().replace(".","_");
                if(classes.get(name)==null){
                    TypeId<?> cls = TypeId.get("L"+name+";");
                    dexMaker.declare(cls, "", Modifier.PUBLIC, TypeId.OBJECT);

                    MethodUtil.addDefaultConstructor(dexMaker, cls);
                    classes.put(name,cls);
                    if(m.getOldMethod() instanceof Method) {
                        MethodUtil.generateMethodFromMethod(dexMaker, cls, (Method) m.getOldMethod());
                    }else {
                        MethodUtil.generateMethodFromConstructor(dexMaker, cls, (Constructor) m.getOldMethod());
                    }
                }else{
                    if(m.getOldMethod() instanceof Method) {
                        MethodUtil.generateMethodFromMethod(dexMaker, classes.get(name), (Method) m.getOldMethod());
                    }else {
                        MethodUtil.generateMethodFromConstructor(dexMaker, classes.get(name), (Constructor) m.getOldMethod());
                    }
                }
            }
//            Log.d("panda",getSystemContext().getCacheDir().getPath());
            File outputDir = new File(context.getDir("path", Context.MODE_PRIVATE).getPath());
            if (outputDir.exists()) {
                File[] fs = outputDir.listFiles();
                for (File f : fs) {
                    f.delete();
                }
            }
            ClassLoader loader = dexMaker.generateAndLoad(context.getClassLoader(),
                    outputDir);
            for(BackMethod bak : methods){
                Member m=bak.getOldMethod();
                String name=m.getDeclaringClass().getName().replace(".","_");
                Class<?> cls = loader.loadClass(name);
                Constructor con=cls.getDeclaredConstructor();
                con.newInstance();
                Member mem=null;
                if(m instanceof Method){
                    mem=cls.getDeclaredMethod(m.getName(),((Method) m).getParameterTypes());
                }else{
                    mem=cls.getDeclaredConstructor(((Constructor) m).getParameterTypes());
                }
                if(mem==null)
                    throw new NullPointerException("mem is null");
                if(m instanceof Method){
                    Method getSig=Method.class.getDeclaredMethod("getSignature");
                    getSig.setAccessible(true);
                    String sig=(String)getSig.invoke(m);
                    sig=sig.replace(".","/");
                    HookUtil.initMethod(m.getDeclaringClass(),m.getName(),sig,Modifier.isStatic(m.getModifiers()));
                }else {
                    Method getSig=Constructor.class.getDeclaredMethod("getSignature");
                    getSig.setAccessible(true);
                    String sig=(String)getSig.invoke(m);
                    sig=sig.replace(".","/");
                    HookUtil.initMethod(m.getDeclaringClass(),"<init>",sig,Modifier.isStatic(m.getModifiers()));
                }
                bak.setNewMethod(mem);
                HookUtil.generateMethod(bak);
                hooked.put(mem.getDeclaringClass().getSimpleName()+"_"+HookUtil.sign(m),bak);
            }
        }catch (Exception e){
            Log.e("panda","",e);
        }
    }
    public static void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) throws NoSuchMethodException{
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length-1] instanceof MethodCallback))
            throw new IllegalArgumentException("no callback defined");
        MethodCallback callback = (MethodCallback) parameterTypesAndCallback[parameterTypesAndCallback.length-1];
        Method m = clazz.getDeclaredMethod( methodName, getParameterClasses(clazz.getClassLoader(), parameterTypesAndCallback));
        //XposedBridge.hookMethod(m, callback);
        BackMethod back=new BackMethod();
        back.setOldMethod(m);
        back.setCallback(callback);
        addNeedsMethod(null,back,false);
    }
    public static void findAndHookConstructor(Class<?> clazz,Object... parameterTypesAndCallback) throws NoSuchMethodException{
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length-1] instanceof MethodCallback))
            throw new IllegalArgumentException("no callback defined");
        MethodCallback callback = (MethodCallback) parameterTypesAndCallback[parameterTypesAndCallback.length-1];
        Constructor m = clazz.getDeclaredConstructor(getParameterClasses(clazz.getClassLoader(), parameterTypesAndCallback));
        //XposedBridge.hookMethod(m, callback);
        BackMethod back=new BackMethod();
        back.setOldMethod(m);
        back.setCallback(callback);
        addNeedsMethod(null,back,false);
    }
    public static void startHooks(Context context){
        addNeedsMethod(context,null,true);
    }
    private static void addNeedsMethod(Context con,BackMethod m,boolean end){
        if (!end){
            needHooks.add(m);
        }else {
            if(con==null)
                throw  new NullPointerException("context is null!");
            context=con;
            beginHook(needHooks);
            needHooks.clear();
        }
    }

    public static Class<?>[] getParameterClasses(ClassLoader classLoader, Object[] parameterTypesAndCallback) {
        Class<?>[] parameterClasses = null;
        for (int i = parameterTypesAndCallback.length - 1; i >= 0; i--) {
            Object type = parameterTypesAndCallback[i];
            if (type == null)
                throw new NullPointerException("parameter type must not be null");

            // ignore trailing callback
            if (type instanceof MethodCallback)
                continue;

            if (parameterClasses == null)
                parameterClasses = new Class<?>[i+1];

            if (type instanceof Class)
                parameterClasses[i] = (Class<?>) type;
            else if (type instanceof String) {
                try {
                    parameterClasses[i] = classLoader.loadClass((String) type); //((String) type, classLoader);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else
                throw new NullPointerException("parameter type must either be specified as Class or String");
        }

        // if there are no arguments for the method
        if (parameterClasses == null)
            parameterClasses = new Class<?>[0];

        return parameterClasses;
    }

    public static Object invoke(String method,Object thiz,Object[] args)throws  Throwable{
        Member old=null;
        Object res=null;
        MethodCallback callback=null;
        BackMethod back =(BackMethod) hooked.get(method);
        MethodHookParam param=new MethodHookParam();
        if(back==null){
            throw new  NullPointerException("find back null");
        }
        callback=(MethodCallback) back.getCallback();
        if(callback==null){
            throw new  NullPointerException("find old Method null");
        }
        old= HookUtil.repairMethod(back.getOldMethod(),(long)back.getBackAddr());
        if(old==null){
            throw new  NullPointerException("find old Method null");
        }
        if(old instanceof Method){
            ((Method) old).setAccessible(true);
        }else{
            ((Constructor) old).setAccessible(true);
        }
        param.method = old;
        param.thisObject = thiz;
        param.args = args;
        try {
            callback.beforeHookedMethod(param);
        } catch (Throwable t) {
            // reset result (ignoring what the unexpectedly exiting callback did)
            t.printStackTrace();
            param.setResult(null);
            param.returnEarly = false;
        }
        if (param.getThrowable()!=null) {
            if(!HookUtil.isArt()){
                HookUtil.generateMethod(back);
            }
            throw param.getThrowable();
        }
        if (param.returnEarly) {
            if(!HookUtil.isArt()){
                HookUtil.generateMethod(back);
            }
            return param.getResult();
        }
        if(old instanceof Method){
            res=((Method) old).invoke(thiz,args);
            param.setResult(res);
            try {
                callback.afterHookedMethod(param);
            } catch (Throwable t) {
                param.setResult(null);
            }
        }else {
            HookUtil.invokeOriginalMethod(old,thiz,args);
            param.setResult(null);
            try {
                callback.afterHookedMethod(param);
            } catch (Throwable t) {
                param.setResult(null);
            }
        }
        if(!HookUtil.isArt()){
            HookUtil.generateMethod(back);
        }
        if (param.getThrowable()!=null) {
            throw param.getThrowable();
        }
        return param.getResult();
    }
}
