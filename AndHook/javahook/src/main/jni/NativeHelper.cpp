#include <jni.h>
#include <stdio.h>
#include <cassert>
#include "NativeHelper.h"
#include "davilk.h"
#include <malloc.h>
#include <string.h>

int size=0;
static int access=-1;
static int level;
static bool isArt_;
static int supperOffset=-1;
typedef uint16_t u2;
typedef uint32_t u4;
static jmethodID hInvoke;

static void invokeDavConstructor(const u4* args, JValue* pResult, void* method, void* self);
static long replaceNativeArt(JNIEnv* env, jclass clazz, jobject src, jobject new_,jobject invoker) {
	void* mSrc=(void*)env->FromReflectedMethod(src);
	void* mNew_=(void*)env->FromReflectedMethod(new_);
	size_t* mInvoker=(size_t*)env->FromReflectedMethod(invoker);
	if(isArt_) {
		memcpy(mInvoker, mSrc, size);
		*(mInvoker + access) = *(mInvoker + access) | 0x0002;
		memcpy(mSrc, mNew_, size);
	}
	return  (size_t)mInvoker;
}
static void replaceNativeDavilk(JNIEnv* env, jclass clazz, jobject src, jobject callback) {
	Method* method=(Method*)env->FromReflectedMethod(src);
	LOGE("dalvik_hookMethodNative");
	if (callback == NULL) {
		LOGE("method and declaredClass must not be null");
		return ;
	}
	if (method == NULL) {
		LOGE("Could not get internal representation for method");
		return ;
	}
	if (isMethodHooked(method)) {
		// already hooked
		return;
	}
	XposedHookInfo* hookInfo = (XposedHookInfo*) calloc(1, sizeof(XposedHookInfo));
	memcpy(hookInfo, method, sizeof(hookInfo->originalMethodStruct));
	hookInfo->reflectedMethod = dvmDecodeIndirectRef(dvmThreadSelf(), env->NewGlobalRef(src));
	hookInfo->callback = dvmDecodeIndirectRef(dvmThreadSelf(), env->NewGlobalRef(callback));

	// Replace method with our own code

	method->accessFlags=method->accessFlags|kAccNative;
	method->func = (void *)&hookedMethodCallback;
	method->insns = (u2*) hookInfo;
	method->registersSize = method->insSize;
	method->outsSize = 0;

	return ;
}
static void repair(JNIEnv* env, jclass clazz, jobject src, jlong old) {
	size_t* mSrc=(size_t*)env->FromReflectedMethod(src);
	void* p=(void*) old;
	memcpy(mSrc,p,size);
	*(mSrc+access)=*(mSrc+access)|0x0002;
	free(p);
	return;
}
static void computeAccess(JNIEnv* env, jclass clazz, jobject src) {
	size_t * mSrc=(size_t *)env->FromReflectedMethod(src);
	size_t * com=(size_t *)env->GetStaticMethodID(env->FindClass(JAVA_CLASS),"compute","()I");
	for(int i=0;i<size/ sizeof(void *);++i){
		if(*(mSrc+i)==0x80019&&*(com+i)==0x80009){
			access=i;
			return;
		}
	}
	return ;
}
static jboolean setSupperCls(JNIEnv* env, jclass clazz, jobject flag) {
	if(!isArt_){
//		Field* field=(Field*)env->FromReflectedField(flag);
//		field->clazz->super=NULL;
	} else {
		ArtField* field=(ArtField*)env->FromReflectedField(flag);
		size_t *dCls=(size_t *)field->declaring_class_;
		if (supperOffset == -1) {
			return false;
		} else {
			*(dCls + supperOffset) = NULL;
			return true;
		}
	}
}
static void computeSupperCls(JNIEnv* env, jclass clazz,jobject fld,jobject test){
	ArtField* field=(ArtField*)env->FromReflectedField(fld);
	ArtField* demo=(ArtField*)env->FromReflectedField(test);
	size_t *dCls=(size_t *)field->declaring_class_;
	size_t *hCls=(size_t *)demo->declaring_class_;
	for(int i=0;i<50;++i){
		if(*(dCls+i)==NULL&&*(hCls+i)==(uint32_t)dCls){//compute SupperClass offset
			supperOffset=i;
			LOGD("find supperOffset=%d",i);
			return;
		}
	}
}
static void initMethod(JNIEnv* env, jclass clazz, jclass cls,jstring name,jstring sig, jboolean isstatic) {
	jmethodID m;
	const char* name_=env->GetStringUTFChars(name,0);;
	const char* sig_=env->GetStringUTFChars(sig,0);
	if(isstatic) {
		m=env->GetStaticMethodID(cls, name_, sig_);
	} else{
		m=env->GetMethodID(cls, name_, sig_);
	}

	if(env->ExceptionCheck()){
		env->ExceptionClear();
	}
	return;
}
void hookedMethodCallback(const u4* args, JValue* pResult, const Method* method, void* self) {
	if (!isMethodHooked(method)) {
		LOGE("Could not find Xposed original method - how did you even get here?");
		return;
	}
	XposedHookInfo* hookInfo = (XposedHookInfo*) method->insns;
	Method* original = (Method*) hookInfo;
	Object* originalReflected = hookInfo->reflectedMethod;
	Object* callback = hookInfo->callback;
	Object* thisObject = !dvmIsStaticMethod(original) ? (Object*)args[0]: NULL;
	ArrayObject* argTypes = dvmBoxMethodArgs(original, dvmIsStaticMethod(original) ? args : args + 1);
	JValue result;
	dvmCallMethod(self, (Method*) hInvoke, NULL, &result,
						originalReflected,  callback, thisObject, argTypes);
	dvmReleaseTrackedAlloc((Object *)argTypes, self);
// exceptions are thrown to the caller
//	if (dvmCheckException(self)) {
//		return;
//	}
	ClassObject* returnType = dvmGetBoxedReturnType(method);
	if (returnType->primitiveType == PRIM_VOID) {
		// ignored
	} else if (result.l == NULL) {
		if (dvmIsPrimitiveClass(returnType)) {
			LOGE("null result when primitive expected");
		}
		pResult->l = NULL;
	} else {
		if (!dvmUnboxPrimitive((Object *)result.l, returnType, pResult)) {
			//(result.l->clazz, returnType);
			LOGE("null result when primitive expected");
		}
	}
}
jobject invoke(JNIEnv* env, jobject clazz,jobject obj,jobject args){
	LOGD("in native invoke");
	jclass fix=env->FindClass(FIX_CLASS);
	jmethodID m1=env->GetStaticMethodID(fix,"invoke","(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
	jobject res=env->CallStaticObjectMethod(fix,m1,clazz,obj,args);
	return res;
}
/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
{ "replaceNativeArt", "(Ljava/lang/reflect/Member;Ljava/lang/reflect/Member;Ljava/lang/reflect/Method;)J", (void*) replaceNativeArt },
{ "replaceNativeDavilk", "(Ljava/lang/reflect/Member;Lcom/panda/hook/javahook/MethodCallback;)J", (void*) replaceNativeDavilk },
{ "repair", "(Ljava/lang/reflect/Member;J)V", (void*) repair },
{ "computeAccess", "(Ljava/lang/reflect/Method;)V", (void*) computeAccess },
{ "computeSupperCls", "(Ljava/lang/reflect/Field;Ljava/lang/reflect/Field;)V", (void*) computeSupperCls },
{ "setSupperCls", "(Ljava/lang/reflect/Field;)Z", (void*) setSupperCls },
{ "initMethod", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Z)V", (void*) initMethod },
{ "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", (void*) invoke },
};
/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

static int registerNatives(JNIEnv* env) {
	if (!registerNativeMethods(env, JAVA_CLASS, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0])))
		return JNI_FALSE;
	return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		return -1;
	}
	assert(env != NULL);
	if (!registerNatives(env)) { //注册
		return -1;
	}
	jclass demo=env->FindClass(DEMO_CLASS);
	jmethodID m1=env->GetStaticMethodID(demo,"m1","()V");
	jmethodID m2=env->GetStaticMethodID(demo,"m2","()V");
	size= reinterpret_cast<size_t >(m2)-reinterpret_cast<size_t >(m1);
	jmethodID isArt=env->GetStaticMethodID(env->FindClass(JAVA_CLASS),"isArt","()Z");
	isArt_=env->CallStaticBooleanMethod(env->FindClass(JAVA_CLASS),isArt);
	jmethodID apiLevel=env->GetStaticMethodID(env->FindClass(JAVA_CLASS),"apiLevel","()I");
	level=env->CallStaticIntMethod(env->FindClass(JAVA_CLASS),apiLevel);
	if(isArt_){
		jmethodID com=env->GetStaticMethodID(env->FindClass(JAVA_CLASS),"compute","()I");
		size=env->CallStaticIntMethod(env->FindClass(JAVA_CLASS),com);

	} else{
		void* dvm=dlopen("libdvm.so",0);
		if(dvm) {
			dvmSetNativeFunc = (void *(*)(void *, void *, void *)) (dlsym(dvm,
																		  "_Z16dvmSetNativeFuncP6MethodPFvPKjP6JValuePKS_P6ThreadEPKt"));
			if (!dvmSetNativeFunc) {
				LOGD("dvmSetNativeFunc is null");
				return JNI_FALSE;
			}
			dvmGetMethodFromReflectObj = (void *(*)(void *)) (dlsym(dvm,
																		  "_Z26dvmGetMethodFromReflectObjP6Object"));
			if (!dvmGetMethodFromReflectObj) {
				LOGD("dvmGetMethodFromReflectObj is null");
				return JNI_FALSE;
			}
			dvmInvokeMethod=(void* (*)(void* obj, void* method,
									 void* argList, void* params, void* returnType,
									 bool noAccessCheck))(dlsym(dvm,"_Z15dvmInvokeMethodP6ObjectPK6MethodP11ArrayObjectS5_P11ClassObjectb"));
			if (!dvmInvokeMethod) {
				LOGD("dvmCallMethod_fnPtr is null");
				return JNI_FALSE;
			}
			dvmDecodeIndirectRef=(Object* (* )(void* self, jobject jobj))(dlsym(dvm,"_Z20dvmDecodeIndirectRefP6ThreadP8_jobject"));
			if (!dvmDecodeIndirectRef) {
				LOGD("dvmDecodeIndirectRef is null");
				return JNI_FALSE;
			}
			dvmThreadSelf=(void* (*)())(dlsym(dvm,"_Z13dvmThreadSelfv"));
			if (!dvmThreadSelf) {
				LOGD("dvmThreadSelf is null");
				return JNI_FALSE;
			}
			dvmCallMethod=(void (*)(void* self, const Method* method, Object* obj,void* pResult, ...))(dlsym(dvm,"_Z13dvmCallMethodP6ThreadPK6MethodP6ObjectP6JValuez"));
			if (!dvmCallMethod) {
				LOGD("dvmCallMethod is null");
				return JNI_FALSE;
			}
			dvmReleaseTrackedAlloc=(void (*)(Object* obj, void* self))(dlsym(dvm,"dvmReleaseTrackedAlloc"));
			if (!dvmReleaseTrackedAlloc) {
				LOGD("dvmReleaseTrackedAlloc is null");
				return JNI_FALSE;
			}
			dvmUnboxPrimitive=(bool (*)(Object* value, ClassObject* returnType,void* pResult))(dlsym(dvm,"_Z17dvmUnboxPrimitiveP6ObjectP11ClassObjectP6JValue"));
			if (!dvmUnboxPrimitive) {
				LOGD("dvmUnboxPrimitive is null");
				return JNI_FALSE;
			}
			dvmGetBoxedReturnType=(ClassObject* (*)(const Method* meth))(dlsym(dvm,"_Z21dvmGetBoxedReturnTypePK6Method"));
			if (!dvmGetBoxedReturnType) {
				LOGD("dvmGetBoxedReturnType is null");
				return JNI_FALSE;
			}
			dexProtoGetParameterCount=(size_t (*)(const DexProto* pProto))(dlsym(dvm,"_Z25dexProtoGetParameterCountPK8DexProto"));
			if (!dexProtoGetParameterCount) {
				LOGD("dexProtoGetParameterCount is null");
				return JNI_FALSE;
			}
			dvmAllocArrayByClass=(ArrayObject* (*)(ClassObject* arrayClass,size_t length, int allocFlags))(dlsym(dvm,"dvmAllocArrayByClass"));
			if (!dvmAllocArrayByClass) {
				LOGD("dvmAllocArrayByClass is null");
				return JNI_FALSE;
			}
			dvmFindSystemClass=(ClassObject* (*)(const char* descriptor))(dlsym(dvm,"_Z18dvmFindSystemClassPKc"));
			if (!dvmFindSystemClass) {
				LOGD("dvmFindSystemClass is null");
				return JNI_FALSE;
			}
			dvmFindPrimitiveClass=(ClassObject* (*)(char type))(dlsym(dvm,"_Z21dvmFindPrimitiveClassc"));
			if (!dvmFindPrimitiveClass) {
				LOGD("dvmFindPrimitiveClass is null");
				return JNI_FALSE;
			}
			dvmBoxPrimitive=(void* (*)(JValue value, ClassObject* returnType))(dlsym(dvm,"_Z15dvmBoxPrimitive6JValueP11ClassObject"));
			if (!dvmBoxPrimitive) {
				LOGD("dvmBoxPrimitive is null");
				return JNI_FALSE;
			}
			jmethodID dexposedInvokeOriginalMethodNative = env->GetStaticMethodID(env->FindClass(JAVA_CLASS), "invokeDavConstructor",
																						  "(Ljava/lang/reflect/Member;[Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");

			hInvoke = env->GetStaticMethodID(env->FindClass(MANAGER_CLASS), "invoke",
																				  "(Ljava/lang/reflect/Member;Lcom/panda/hook/javahook/MethodCallback;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");

			LOGD("hInvoke is %p",hInvoke);
			dvmSetNativeFunc((void*)dexposedInvokeOriginalMethodNative,(void*)invokeDavConstructor,NULL);

		} else{
			LOGD("cant dlopen libdvm");
		}
	}
//	LOGD("size offset=%d level=%d",size,level);
	return JNI_VERSION_1_4;
}
static void invokeDavConstructor(const u4* args, JValue* pResult, void* method, void* self) {
//	LOGE("XposedBridge_invokeOriginalMethodNative");
	Method* meth = NULL;
	if (meth == NULL) {
		meth = (Method *)dvmGetMethodFromReflectObj((void*) args[0]);
		if (isMethodHooked(meth)) {
			meth = (Method*) meth->insns;
		}
	}
	void* params = (void*) args[1];
	void* returnType = (void*) args[2];
	void* thisObject = (void*) args[3]; // null for static methods
	void* argList = (void*) args[4];
	// invoke the method
	pResult->l=dvmInvokeMethod(thisObject, meth, argList, params, returnType, true);
	return ;
}
