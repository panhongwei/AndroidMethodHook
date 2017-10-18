#include <jni.h>
#include <stdio.h>
#include <cassert>
#include "NativeHelper.h"
#include <malloc.h>
#include <string.h>
#include "ArtMethod_8_0.h"

int size=0;
int access;
int level;
bool isArt_;
typedef uint16_t u2;
typedef uint32_t u4;
void* (*dvmSetNativeFunc)(void*,void*,void*);
void* (*dvmInvokeMethod)(void* obj, void* method,
						 void* argList, void* params, void* returnType,
						 bool noAccessCheck);
void* (*dvmGetMethodFromReflectObj)(void*);

static void invokeDavConstructor(const u4* args, void* pResult, void* method, void* self);
static long replaceNative(JNIEnv* env, jclass clazz, jobject src, jobject new_) {
	void* mSrc=(void*)env->FromReflectedMethod(src);
	void* mNew_=(void*)env->FromReflectedMethod(new_);
	void* p=malloc(size);
	if(p){
		memcpy(p,mSrc,size);

	}
	if(level>=26&&!IsDirect(((ArtMethod *) mSrc)->access_flags_)) {
		ArtMethod *aSrc = (ArtMethod *) mSrc;
		ArtMethod *aNew_ = (ArtMethod *) mNew_;
		aNew_->method_index_ = aSrc->method_index_;
	}
	memcpy(mSrc, mNew_, size);
	return  (size_t)p;
}
static void repair(JNIEnv* env, jclass clazz, jobject src, jlong old) {
	size_t* mSrc=(size_t*)env->FromReflectedMethod(src);
	void* p=(void*) old;
//	*(mSrc+access)=*(mSrc+access)|0x0002;
//	if(old==-1){
//		*(mSrc+access)=*(mSrc+access)|0x0002;
//		return;
//	}
	memcpy(mSrc,p,size);
	*(mSrc+access)=*(mSrc+access)|0x0002;
	if(!isArt_) {
		free(p);
	}
	return;
}
static void computeAccess(JNIEnv* env, jclass clazz, jobject src) {
	size_t * mSrc=(size_t *)env->FromReflectedMethod(src);
	size_t * com=(size_t *)env->GetStaticMethodID(env->FindClass(JAVA_CLASS),"compute","()I");
	for(int i=0;i<size/ sizeof(void *);++i){
		if(*(mSrc+i)==0x80019&&*(com+i)==0x80009){
			access=i;
//			LOGD("access offset=%d",access);
			return;
		}
	}
	return ;
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
{ "replaceNative", "(Ljava/lang/reflect/Member;Ljava/lang/reflect/Member;)J", (void*) replaceNative },
{ "repair", "(Ljava/lang/reflect/Member;J)V", (void*) repair },
{ "computeAccess", "(Ljava/lang/reflect/Method;)V", (void*) computeAccess },
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
			dvmGetMethodFromReflectObj = (void *(*)(void *)) (dlsym(dvm,
																		  "_Z26dvmGetMethodFromReflectObjP6Object"));
			dvmInvokeMethod=(void* (*)(void* obj, void* method,
									 void* argList, void* params, void* returnType,
									 bool noAccessCheck))(dlsym(dvm,"_Z15dvmInvokeMethodP6ObjectPK6MethodP11ArrayObjectS5_P11ClassObjectb"));
			if (!dvmInvokeMethod) {
				LOGD("dvmCallMethod_fnPtr is null");
				return JNI_FALSE;
			}
			LOGD("dvmSetNativeFunc=%p dvmGetMethodFromReflectObj=%p dvmInvokeMethod=%p",dvmSetNativeFunc,dvmGetMethodFromReflectObj,dvmInvokeMethod);
			jmethodID dexposedInvokeOriginalMethodNative = env->GetStaticMethodID(env->FindClass(JAVA_CLASS), "invokeDavConstructor",
																						  "(Ljava/lang/reflect/Member;[Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Object;[Ljava/lang/Object;)V");

			dvmSetNativeFunc((void*)dexposedInvokeOriginalMethodNative,(void*)invokeDavConstructor,NULL);
		} else{
			LOGD("cant dlopen libdvm");
		}
	}
//	LOGD("size offset=%d level=%d",size,level);
	return JNI_VERSION_1_4;
}
static void invokeDavConstructor(const u4* args, void* pResult, void* method, void* self) {
	void* meth = NULL;
	if (meth == NULL) {
		meth = dvmGetMethodFromReflectObj((void*) args[0]);
	}
	void* params = (void*) args[1];
	void* returnType = (void*) args[2];
	void* thisObject = (void*) args[3]; // null for static methods
	void* argList = (void*) args[4];

	// invoke the method
	dvmInvokeMethod(thisObject, meth, argList, params, returnType, true);
	return;
}
