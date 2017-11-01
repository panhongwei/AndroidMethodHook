#include <jni.h>
#include <stdio.h>
#include <cassert>
#include "test.h"
#include <malloc.h>
#include <string.h>

static void repair(JNIEnv* env,jclass clazz,jobject thiz) {
	LOGD("panda");
	jclass cls=env->FindClass(JAVA_CLASS);
	jmethodID m=env->GetMethodID(cls,"test2","()I");
	env->CallIntMethod(thiz,m);
	return;
}
/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
{ "tt", "(Lcom/panda/hook/andhook/MainActivity;)V", (void*) repair },
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
//	LOGD("size offset=%d level=%d",size,level);
	return JNI_VERSION_1_4;
}
