LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := nativeutils

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions
LOCAL_CFLAGS += -fvisibility=hidden -O3
LOCAL_CFLAGS	:= -std=gnu++11 -DDEBUG -O0

LOCAL_SRC_FILES := NativeHelper.cpp \

LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)