LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE := OpenSURF

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -L$(NDK_APP_OUT)/OpenCV-Android/armeabi -lopencv -lcxcore -lcv -lcvaux -lcvml -lcvhighgui \
				-llog  \



OPENCV := apps/OpenCV-Android

LOCAL_C_INCLUDES += \
        $(OPENCV)/cv/src \
        $(OPENCV)/cv/include \
        $(OPENCV)/cxcore/include \
        $(OPENCV)/cvaux/src \
        $(OPENCV)/cvaux/include \
        $(OPENCV)/ml/include \
        $(OPENCV)/otherlibs/highgui \
        $(OPENCV)
        
LOCAL_CFLAGS += -fpic 
MY_SRCS += fasthessian.cpp  integral.cpp  ipoint.cpp \
			 surf.cpp surfutils.cpp surfjni.cpp surfjni_wrap.cpp

LOCAL_SRC_FILES := $(MY_SRCS)



include $(BUILD_SHARED_LIBRARY)
