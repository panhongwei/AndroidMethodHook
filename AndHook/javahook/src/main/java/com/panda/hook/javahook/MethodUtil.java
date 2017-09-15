package com.panda.hook.javahook;

import android.util.Log;

import com.android.dx.Code;
import com.android.dx.DexMaker;
import com.android.dx.Local;
import com.android.dx.MethodId;
import com.android.dx.TypeId;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by panda on 17/8/8.
 */

public class MethodUtil {
    private static TypeId<MethodUtil> utilType = TypeId.get(MethodUtil.class);
    private static TypeId<Integer> IntegerType = TypeId.get(Integer.class);
    private static TypeId<Long> LongType = TypeId.get(Long.class);
    private static TypeId<Short> ShortType = TypeId.get(Short.class);
    private static TypeId<Double> DoubleType = TypeId.get(Double.class);
    private static TypeId<Boolean> BooleanType = TypeId.get(Boolean.class);
    private static TypeId<Float> FloatType = TypeId.get(Float.class);
    private static TypeId<Byte> ByteType= TypeId.get(Byte.class);
    private static TypeId<Character> CharacterType= TypeId.get(Character.class);

    private static MethodId Integer_value=IntegerType.getMethod(IntegerType,"valueOf",TypeId.INT);
    private static MethodId Long_value=LongType.getMethod(LongType,"valueOf",TypeId.LONG);
    private static MethodId Short_value=ShortType.getMethod(ShortType,"valueOf",TypeId.SHORT);
    private static MethodId Double_value=DoubleType.getMethod(DoubleType,"valueOf",TypeId.DOUBLE);
    private static MethodId Boolean_value=BooleanType.getMethod(BooleanType,"valueOf",TypeId.BOOLEAN);
    private static MethodId Float_value=FloatType.getMethod(FloatType,"valueOf",TypeId.FLOAT);
    private static MethodId Byte_value=ByteType.getMethod(ByteType,"valueOf",TypeId.BYTE);
    private static MethodId Char_value=CharacterType.getMethod(CharacterType,"valueOf",TypeId.CHAR);

    private static MethodId int_value=IntegerType.getMethod(TypeId.INT,"intValue");
    private static MethodId long_value=LongType.getMethod(TypeId.LONG,"longValue");
    private static MethodId short_value=ShortType.getMethod(TypeId.SHORT,"shortValue");
    private static MethodId double_value=DoubleType.getMethod(TypeId.DOUBLE,"doubleValue");
    private static MethodId boolean_value=BooleanType.getMethod(TypeId.BOOLEAN,"booleanValue");
    private static MethodId float_value=FloatType.getMethod(TypeId.FLOAT,"floatValue");
    private static MethodId byte_value=ByteType.getMethod(TypeId.BYTE,"byteValue");
    private static MethodId char_value=CharacterType.getMethod(TypeId.CHAR,"charValue");

    public static void generateMethodFromMethod(DexMaker dexMaker, TypeId<?> declaringType,Method m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId proxy = declaringType.getMethod(TypeId.get(m.getReturnType()), m.getName(), params);
        Code code;
        if(Modifier.isStatic(m.getModifiers())) {
             code = dexMaker.declare(proxy, Modifier.STATIC | Modifier.PUBLIC);
        }else{
             int mode=Modifier.isPrivate(m.getModifiers())?Modifier.PRIVATE:Modifier.PUBLIC;
             code = dexMaker.declare(proxy, mode);
        }
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local i_ = code.newLocal(TypeId.INT);
        Local arg_ = code.newLocal(TypeId.OBJECT);

        Local localResult = code.newLocal(TypeId.OBJECT);
        Local caller = code.newLocal(TypeId.STRING);
        Local res = code.newLocal(TypeId.get(m.getReturnType()));
        Local cast = m.getReturnType().equals(void.class)?null:code.newLocal(getClassTypeFromClass(m.getReturnType()));
        Local<?> thisRef ;
        if(Modifier.isStatic(m.getModifiers())){
            thisRef=code.newLocal(declaringType);
            code.loadConstant(thisRef,null);
        }else{
            thisRef=code.getThis(declaringType);
        }
        code.loadConstant(caller,m.getDeclaringClass().getName().replace(".","_")+"_"+HookUtil.sign(m));
        code.loadConstant(a, proxy.getParameters().size() );
        code.newArray(args, a);
        for (int i = 0; i < pTypes.length; ++i) {
            code.loadConstant(i_, i);
            MethodId mId = getValueFromClass(pTypes[i]);
            if (mId != null) {
                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
                code.aput(args, i_, arg_);
            } else {
                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
            }
        }
        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke",TypeId.STRING, TypeId.OBJECT, TypeId.get(Object[].class));
        code.invokeStatic(invoke, localResult,caller, thisRef, args);
        if(m.getReturnType().equals(void.class)){
            code.returnVoid();
            return;
        }
        if(getValueFromClass(m.getReturnType())!=null){
            MethodId mId = toValueFromClass(m.getReturnType());
            code.cast(cast,localResult);
            code.invokeVirtual(mId,res, cast);
            code.returnValue(res);
            return;
        }else{
            code.cast(res,localResult);
            code.returnValue(res);
            return;
        }

    }
    public static void generateMethodFromConstructor(DexMaker dexMaker, TypeId<?> declaringType, Constructor m) {
        Class<?>[] pTypes = m.getParameterTypes();
        TypeId<?> params[] = new TypeId[pTypes.length ];
//            params[0] = TypeId.OBJECT;
        for (int i = 0; i < pTypes.length; ++i) {
            params[i ] = getTypeIdFromClass(pTypes[i]);
        }
        MethodId proxy = declaringType.getConstructor( params);
        Code code = dexMaker.declare(proxy, Modifier.PUBLIC);
        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local i_ = code.newLocal(TypeId.INT);
        Local arg_ = code.newLocal(TypeId.OBJECT);
        Local localResult = code.newLocal(TypeId.OBJECT);
//        Local<Object> obj=code.newLocal(TypeId.OBJECT);
        Local<?> thisRef = code.getThis(declaringType);
        Local caller = code.newLocal(TypeId.STRING);
        code.loadConstant(caller,m.getDeclaringClass().getName().replace(".","_")+"_"+HookUtil.sign(m));
        code.invokeDirect(TypeId.OBJECT.getConstructor(), null, thisRef);
//        code.loadConstant(thisRef,code.getThis(declaringType));
        code.loadConstant(a, proxy.getParameters().size() );
        code.newArray(args, a);
        for (int i = 0; i < pTypes.length; ++i) {
            code.loadConstant(i_, i);
            MethodId mId = getValueFromClass(pTypes[i]);
            if (mId != null) {
                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
                code.aput(args, i_, arg_);
            } else {
                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
            }
        }
        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke",TypeId.STRING, TypeId.OBJECT, TypeId.get(Object[].class));
        code.invokeStatic(invoke, localResult,caller, thisRef, args);
        code.returnVoid();

    }
    public static void addDefaultConstructor(DexMaker dexMaker, TypeId<?> declaringType) {
        Code code = dexMaker.declare(declaringType.getConstructor(), Modifier.PUBLIC);
        Local<?> thisRef = code.getThis(declaringType);
        code.invokeDirect(TypeId.OBJECT.getConstructor(), null, thisRef);
        code.returnVoid();
    }
    private static TypeId getTypeIdFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return TypeId.INT;
        }else if(cls.getName().equals(long.class.getName())){
            return TypeId.LONG;
        }else if(cls.getName().equals(short.class.getName())){
            return TypeId.SHORT;
        }else if(cls.getName().equals(double.class.getName())){
            return TypeId.DOUBLE;
        }else if(cls.getName().equals(boolean.class.getName())){
            return TypeId.BOOLEAN;
        }else if(cls.getName().equals(float.class.getName())){
            return TypeId.FLOAT;
        }else if(cls.getName().equals(byte.class.getName())){
            return TypeId.BYTE;
        }else if(cls.getName().equals(char.class.getName())){
            return TypeId.CHAR;
        }else if(cls.getName().equals(void.class.getName())){
            return TypeId.VOID;
        }else{
            return  TypeId.get(cls);
        }
    }
    private static TypeId getClassTypeFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return IntegerType;
        }else if(cls.getName().equals(long.class.getName())){
            return LongType;
        }else if(cls.getName().equals(short.class.getName())){
            return ShortType;
        }else if(cls.getName().equals(double.class.getName())){
            return DoubleType;
        }else if(cls.getName().equals(boolean.class.getName())){
            return BooleanType;
        }else if(cls.getName().equals(float.class.getName())){
            return FloatType;
        }else if(cls.getName().equals(byte.class.getName())){
            return BooleanType;
        }else if(cls.getName().equals(char.class.getName())){
            return CharacterType;
        }
        return TypeId.get(cls);
    }
    private static MethodId getValueFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return Integer_value;
        }else if(cls.getName().equals(long.class.getName())){
            return Long_value;
        }else if(cls.getName().equals(short.class.getName())){
            return Short_value;
        }else if(cls.getName().equals(double.class.getName())){
            return Double_value;
        }else if(cls.getName().equals(boolean.class.getName())){
            return Boolean_value;
        }else if(cls.getName().equals(float.class.getName())){
            return Float_value;
        }else if(cls.getName().equals(byte.class.getName())){
            return Byte_value;
        }else if(cls.getName().equals(char.class.getName())){
            return Char_value;
        }else{
            return  null;
        }
    }
    private static MethodId toValueFromClass(Class cls){
        if(cls.getName().equals(int.class.getName())){
            return int_value;
        }else if(cls.getName().equals(long.class.getName())){
            return long_value;
        }else if(cls.getName().equals(short.class.getName())){
            return short_value;
        }else if(cls.getName().equals(double.class.getName())){
            return double_value;
        }else if(cls.getName().equals(boolean.class.getName())){
            return boolean_value;
        }else if(cls.getName().equals(float.class.getName())){
            return float_value;
        }else if(cls.getName().equals(byte.class.getName())){
            return byte_value;
        }else if(cls.getName().equals(char.class.getName())){
            return char_value;
        }else{
            return  null;
        }
    }
    public static Object invoke(String caller,Object thiz,Object[] args)throws Throwable{
        Log.d("panda","invoke!");
        return HookManager.invoke(caller,thiz,args);
    }
}
//generate constructor
//        Class<?>[] pTypes = m.getParameterTypes();
//        TypeId<?> params[] = new TypeId[pTypes.length ];
////            params[0] = TypeId.OBJECT;
//        for (int i = 0; i < pTypes.length; ++i) {
//            params[i ] = getTypeIdFromClass(pTypes[i]);
//        }
//        MethodId proxy = declaringType.getConstructor( params);
//        Code code = dexMaker.declare(proxy, Modifier.PUBLIC);
//        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
//        Local<Integer> a = code.newLocal(TypeId.INT);
//        Local i_ = code.newLocal(TypeId.INT);
//        Local arg_ = code.newLocal(TypeId.OBJECT);
//        Local localResult = code.newLocal(TypeId.OBJECT);
////        Local<Object> obj=code.newLocal(TypeId.OBJECT);
//        Local<?> thisRef = code.getThis(declaringType);
//        code.invokeDirect(TypeId.OBJECT.getConstructor(), null, thisRef);
////        code.loadConstant(thisRef,code.getThis(declaringType));
//        code.loadConstant(a, proxy.getParameters().size() );
//        code.newArray(args, a);
//        for (int i = 0; i < pTypes.length; ++i) {
//            code.loadConstant(i_, i);
//            MethodId mId = getValueFromClass(pTypes[i]);
//            if (mId != null) {
//                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
//                code.aput(args, i_, arg_);
//            } else {
//                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
//            }
//        }
//        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke", TypeId.OBJECT, TypeId.get(Object[].class));
//        code.invokeStatic(invoke, localResult, thisRef, args);
//        code.returnVoid();

//generate method with same params
////        if(Modifier.isStatic(m.getModifiers())){
//        Class<?>[] pTypes = m.getParameterTypes();
//        TypeId<?> params[] = new TypeId[pTypes.length ];
////            params[0] = TypeId.OBJECT;
//        for (int i = 0; i < pTypes.length; ++i) {
//            params[i ] = getTypeIdFromClass(pTypes[i]);
//        }
//        MethodId proxy = declaringType.getMethod(TypeId.OBJECT, m.getDeclaringClass().getSimpleName()+"_"+HookUtil.sign(m), params);
//        Code code;
//        if(Modifier.isStatic(m.getModifiers())) {
//             code = dexMaker.declare(proxy, Modifier.STATIC | Modifier.PUBLIC);
//        }else{
//             code = dexMaker.declare(proxy, Modifier.PUBLIC);
//        }
//        Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
//        Local<Integer> a = code.newLocal(TypeId.INT);
//        Local i_ = code.newLocal(TypeId.INT);
//        Local arg_ = code.newLocal(TypeId.OBJECT);
//
//        Local localResult = code.newLocal(TypeId.OBJECT);
////        Local res = code.newLocal(TypeId.get(m.getReturnType()));
////        Local cast = code.newLocal(getClassTypeFromClass(m.getReturnType()));
//        Local<?> thisRef ;
//        if(Modifier.isStatic(m.getModifiers())){
//            thisRef=code.newLocal(declaringType);
//            code.loadConstant(thisRef,null);
//        }else{
//            thisRef=code.getThis(declaringType);
//        }
////        code.loadConstant(res,null);
////        code.loadConstant(obj,null);
//        code.loadConstant(a, proxy.getParameters().size() );
//        code.newArray(args, a);
//        for (int i = 0; i < pTypes.length; ++i) {
//            code.loadConstant(i_, i);
//            MethodId mId = getValueFromClass(pTypes[i]);
//            if (mId != null) {
//                code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
//                code.aput(args, i_, arg_);
//            } else {
//                code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
//            }
//        }
//        MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke", TypeId.OBJECT, TypeId.get(Object[].class));
//        code.invokeStatic(invoke, localResult, thisRef, args);
////        if(m.getReturnType().equals(void.class)){
////            code.returnVoid();
////            return;
////        }
////        if(getValueFromClass(m.getReturnType())!=null){
////            MethodId mId = toValueFromClass(m.getReturnType());
////            code.cast(cast,localResult);
////            code.invokeVirtual(mId,res, cast);
////            code.returnValue(res);
////            return;
////        }else{
////            code.cast(res,localResult);
////        }
////        code.cast(res,localResult);
//        code.returnValue(localResult);
//        return;
//        }else {
//            Class<?>[] pTypes = m.getParameterTypes();
//            TypeId<?> params[] = new TypeId[pTypes.length ];
////            params[0] = TypeId.OBJECT;
//            for (int i = 0; i < pTypes.length; ++i) {
//                params[i ] = getTypeIdFromClass(pTypes[i]);
//            }
//            MethodId proxy = declaringType.getMethod(TypeId.get(m.getReturnType()), m.getName(), params);
//            Code code = dexMaker.declare(proxy, Modifier.PUBLIC);
//            Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
//            Local<Integer> a = code.newLocal(TypeId.INT);
//            Local i_ = code.newLocal(TypeId.INT);
//            Local arg_ = code.newLocal(TypeId.OBJECT);
//            Local localResult = code.newLocal(TypeId.OBJECT);
//            Local res = code.newLocal(TypeId.get(m.getReturnType()));
//            //Local obj=code.newLocal(TypeId.OBJECT);
//            Local<?> thisRef = code.getThis(declaringType);
////            code.loadConstant(obj,code.getThis(declaringType));
//            code.loadConstant(a, proxy.getParameters().size() );
//            code.newArray(args, a);
//            for (int i = 0; i < pTypes.length; ++i) {
//                code.loadConstant(i_, i);
//                MethodId mId = getValueFromClass(pTypes[i]);
//                if (mId != null) {
//                    code.invokeStatic(mId, arg_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
//                    code.aput(args, i_, arg_);
//                } else {
//                    code.aput(args, i_, code.getParameter(i , (TypeId) proxy.getParameters().get(i )));
//                }
//            }
//            MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke", TypeId.OBJECT, TypeId.get(Object[].class));
//            code.invokeStatic(invoke, localResult, thisRef, args);
//            code.cast(res,localResult);
//            code.returnValue(res);
////            Class<?>[] pTypes = m.getParameterTypes();
////            TypeId<?> params[] = new TypeId[pTypes.length + 1];
////            params[0] = TypeId.OBJECT;
////            for (int i = 0; i < pTypes.length; ++i) {
////                params[i + 1] = getTypeIdFromClass(pTypes[i]);
////            }
////            MethodId proxy = declaringType.getMethod(TypeId.OBJECT, m.getDeclaringClass().getSimpleName() + "_" + HookUtil.sign(m), params);
////            Code code = dexMaker.declare(proxy, Modifier.STATIC | Modifier.PUBLIC);
////            Local<Object[]> args = code.newLocal(TypeId.get(Object[].class));
////            Local<Integer> a = code.newLocal(TypeId.INT);
////            Local i_ = code.newLocal(TypeId.INT);
////            Local arg_ = code.newLocal(TypeId.OBJECT);
////            Local localResult = code.newLocal(TypeId.OBJECT);
////            code.loadConstant(a, proxy.getParameters().size() - 1);
////            code.newArray(args, a);
////            for (int i = 0; i < pTypes.length; ++i) {
////                code.loadConstant(i_, i);
////                MethodId mId = getValueFromClass(pTypes[i]);
////                if (mId != null) {
////                    code.invokeStatic(mId, arg_, code.getParameter(i + 1, (TypeId) proxy.getParameters().get(i + 1)));
////                    code.aput(args, i_, arg_);
////                } else {
////                    code.aput(args, i_, code.getParameter(i + 1, (TypeId) proxy.getParameters().get(i + 1)));
////                }
////            }
////            MethodId invoke = utilType.getMethod(TypeId.OBJECT, "invoke", TypeId.OBJECT, TypeId.get(Object[].class));
////            code.invokeStatic(invoke, localResult, code.getParameter(0, (TypeId) proxy.getParameters().get(0)), args);
////            code.returnValue(localResult);
//        }