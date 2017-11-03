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
#define MANAGER_CLASS "com/panda/hook/javahook/HookManager"
#define DEMO_CLASS "com/panda/hook/javahook/MethodDemo"
#define FIX_CLASS "com/panda/hook/javahook/ReflectionFix"
//#define OBJECT_CLASS "java/lang/Object"
#define OBJECT_PATH "java/lang/Object"
static constexpr uint32_t kAccPublic =       0x0001;  // class, field, method, ic
static constexpr uint32_t kAccPrivate =      0x0002;  // field, method, ic
static constexpr uint32_t kAccProtected =    0x0004;  // field, method, ic
static constexpr uint32_t kAccStatic =       0x0008;  // field, method, ic
static constexpr uint32_t kAccConstructor =           0x00010000;  // method (dex only) <(cl)init>
static constexpr uint32_t kAccNative =       0x0100;  // method
static constexpr uint32_t kAccFinal =        0x0010;
bool IsFinal(uint32_t access_flags){
    return (access_flags & kAccFinal) != 0;
}

class ArtField{
public:
    uint32_t declaring_class_;

    uint32_t access_flags_ = 0;

    // Dex cache index of field id
    uint32_t field_dex_idx_ = 0;

    // Offset of field within an instance or in the Class' static fields
    uint32_t offset_ = 0;
};
#endif