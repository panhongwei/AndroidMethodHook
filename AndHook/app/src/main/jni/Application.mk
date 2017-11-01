# The ARMv7 is significanly faster due to the use of the hardware FPU

APP_STL :=

APP_CPPFLAGS += -fpermissive  #此项有效时表示宽松的编译形式，比如没有用到的代码中有错误也可以通过编译；

ifeq ($(NDK_DEBUG), 1)
APP_CPPFLAGS += -DNDK_DEBUG
else
APP_CPPFLAGS += -fvisibility=hidden -O3
endif

APP_ABI := armeabi armeabi-v7a
APP_PLATFORM := android-14
APP_STL := gnustl_static