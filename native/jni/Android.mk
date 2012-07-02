# Copyright (C) 2011 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

############ some local flags
# If you change any of those flags, you need to rebuild both libjni_latinime_static
# and the shared library.
FLAG_DBG ?= false
FLAG_DO_PROFILE ?= false

######################################
include $(CLEAR_VARS)

LATIN_IME_SRC_DIR := src
LATIN_IME_SRC_FULLPATH_DIR := $(LOCAL_PATH)/$(LATIN_IME_SRC_DIR)

LOCAL_C_INCLUDES += $(LATIN_IME_SRC_FULLPATH_DIR) $(LATIN_IME_SRC_FULLPATH_DIR)/gesture

LOCAL_CFLAGS += -Werror -Wall

# To suppress compiler warnings for unused variables/functions used for debug features etc.
LOCAL_CFLAGS += -Wno-unused-parameter -Wno-unused-function

LATIN_IME_JNI_SRC_FILES := \
    com_android_inputmethod_keyboard_ProximityInfo.cpp \
    com_android_inputmethod_latin_BinaryDictionary.cpp \
    com_android_inputmethod_latin_NativeUtils.cpp \
    jni_common.cpp

LATIN_IME_CORE_SRC_FILES := \
    additional_proximity_chars.cpp \
    basechars.cpp \
    bigram_dictionary.cpp \
    char_utils.cpp \
    correction.cpp \
    dictionary.cpp \
    proximity_info.cpp \
    proximity_info_state.cpp \
    unigram_dictionary.cpp \
    gesture/incremental_decoder_interface.cpp

LOCAL_SRC_FILES := \
    $(LATIN_IME_JNI_SRC_FILES) \
    $(addprefix $(LATIN_IME_SRC_DIR)/, $(LATIN_IME_CORE_SRC_FILES))

ifeq ($(FLAG_DO_PROFILE), true)
    $(warning Making profiling version of native library)
    LOCAL_CFLAGS += -DFLAG_DO_PROFILE
else # FLAG_DO_PROFILE
ifeq ($(FLAG_DBG), true)
    $(warning Making debug version of native library)
    LOCAL_CFLAGS += -DFLAG_DBG
endif # FLAG_DBG
endif # FLAG_DO_PROFILE

LOCAL_MODULE := libjni_latinime_common_static
LOCAL_MODULE_TAGS := optional

ifdef HISTORICAL_NDK_VERSIONS_ROOT # In the platform build system
include external/stlport/libstlport.mk
else # In the NDK build system
LOCAL_C_INCLUDES += external/stlport/stlport bionic
endif

include $(BUILD_STATIC_LIBRARY)
######################################
include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(LATIN_IME_SRC_FULLPATH_DIR) \
    $(addprefix $(LATIN_IME_SRC_FULLPATH_DIR)/, gesture gesture/impl)

LOCAL_CFLAGS += -Werror -Wall

# To suppress compiler warnings for unused variables/functions used for debug features etc.
LOCAL_CFLAGS += -Wno-unused-parameter -Wno-unused-function

LATIN_IME_GESTURE_IMPL_SRC_FILES := \
    gesture/impl/gesture_decoder_impl.cpp \
    gesture/impl/incremental_decoder_impl.cpp \
    gesture/impl/token_beam_impl.cpp \
    gesture/impl/token_impl.cpp

LOCAL_SRC_FILES := $(addprefix $(LATIN_IME_SRC_DIR)/, $(LATIN_IME_GESTURE_IMPL_SRC_FILES))

ifeq ($(FLAG_DO_PROFILE), true)
    $(warning Making profiling version of native library)
    LOCAL_CFLAGS += -DFLAG_DO_PROFILE
else # FLAG_DO_PROFILE
ifeq ($(FLAG_DBG), true)
    $(warning Making debug version of native library)
    LOCAL_CFLAGS += -DFLAG_DBG
endif # FLAG_DBG
endif # FLAG_DO_PROFILE

# TODO: Can remove this static library from AOSP completely?
LOCAL_MODULE := libjni_latinime_gesture_impl_aosp_static
LOCAL_MODULE_TAGS := optional

ifdef HISTORICAL_NDK_VERSIONS_ROOT # In the platform build system
include external/stlport/libstlport.mk
else # In the NDK build system
LOCAL_C_INCLUDES += external/stlport/stlport bionic
endif

include $(BUILD_STATIC_LIBRARY)
######################################
include $(CLEAR_VARS)

# All code in LOCAL_WHOLE_STATIC_LIBRARIES will be built into this shared library.
LOCAL_WHOLE_STATIC_LIBRARIES := \
    libjni_latinime_common_static libjni_latinime_gesture_impl_aosp_static

ifdef HISTORICAL_NDK_VERSIONS_ROOT # In the platform build system
LOCAL_SHARED_LIBRARIES := libstlport
else # In the NDK build system
LOCAL_SHARED_LIBRARIES := libstlport_static
endif

ifeq ($(FLAG_DO_PROFILE), true)
    $(warning Making profiling version of native library)
    LOCAL_SHARED_LIBRARIES += libcutils libutils
else # FLAG_DO_PROFILE
ifeq ($(FLAG_DBG), true)
    $(warning Making debug version of native library)
    LOCAL_SHARED_LIBRARIES += libcutils libutils
endif # FLAG_DBG
endif # FLAG_DO_PROFILE

LOCAL_MODULE := libjni_latinime
LOCAL_MODULE_TAGS := optional

ifdef HISTORICAL_NDK_VERSIONS_ROOT # In the platform build system
include external/stlport/libstlport.mk
endif

include $(BUILD_SHARED_LIBRARY)

#################### Clean up the tmp vars
LATIN_IME_CORE_SRC_FILES :=
LATIN_IME_JNI_SRC_FILES :=
LATIN_IME_GESTURE_IMPL_SRC_FILES :=
LATIN_IME_SRC_DIR :=
LATIN_IME_SRC_FULLPATH_DIR :=
