# Android.mk for core and binding
MY_LOCAL_PATH := $(call my-dir)

include $(call all-subdir-makefiles)

LOCAL_PATH := $(MY_LOCAL_PATH)

include $(CLEAR_VARS)
LOCAL_MODULE    := core

LOCAL_SRC_FILES := common-c/src/AudioStream.c \
                   common-c/src/ByteBuffer.c \
                   common-c/src/Connection.c \
                   common-c/src/ConnectionTester.c \
                   common-c/src/ControlStream.c \
                   common-c/src/FakeCallbacks.c \
                   common-c/src/InputStream.c \
                   common-c/src/LinkedBlockingQueue.c \
                   common-c/src/Misc.c \
                   common-c/src/Platform.c \
                   common-c/src/PlatformCrypto.c \
                   common-c/src/PlatformSockets.c \
                   common-c/src/RtpAudioQueue.c \
                   common-c/src/RtpVideoQueue.c \
                   common-c/src/RtspConnection.c \
                   common-c/src/RtspParser.c \
                   common-c/src/SdpGenerator.c \
                   common-c/src/SimpleStun.c \
                   common-c/src/VideoDepacketizer.c \
                   common-c/src/VideoStream.c \
                   common-c/reedsolomon/rs.c \
                   common-c/enet/callbacks.c \
                   common-c/enet/compress.c \
                   common-c/enet/host.c \
                   common-c/enet/list.c \
                   common-c/enet/packet.c \
                   common-c/enet/peer.c \
                   common-c/enet/protocol.c \
                   common-c/enet/unix.c \
                   common-c/enet/win32.c \
                   simplejni.c \
                   callbacks.c \
                   minisdl.c \


LOCAL_C_INCLUDES := $(LOCAL_PATH)/common-c/enet/include \
                    $(LOCAL_PATH)/common-c/reedsolomon \
                    $(LOCAL_PATH)/common-c/src \

LOCAL_CFLAGS := -DHAS_SOCKLEN_T=1 -DLC_ANDROID -DHAVE_CLOCK_GETTIME=1

ifeq ($(NDK_DEBUG),1)
LOCAL_CFLAGS += -DLC_DEBUG
endif

LOCAL_LDLIBS := -llog

LOCAL_STATIC_LIBRARIES := libopus libssl libcrypto cpufeatures
LOCAL_LDFLAGS += -Wl,--exclude-libs,ALL

LOCAL_BRANCH_PROTECTION := standard

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)