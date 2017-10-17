/*
 *
 * Copyright (c) 2015, alipay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * 	common.h
 *
 *  @author : sanping.li@alipay.com
 *
 */

#ifndef NativeHelper_H_
#define NativeHelper_H_

#include <jni.h>
#include "dlfcn.h"
#include "stdio.h"
#include <elf.h>
#include <android/log.h>
#define TAG "panda"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,  TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define JAVA_CLASS "com/panda/hook/javahook/HookUtil"
#define DEMO_CLASS "com/panda/hook/javahook/MethodDemo"
#define OBJECT_PATH "java/lang/Object"

#endif