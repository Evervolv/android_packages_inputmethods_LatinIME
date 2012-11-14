#
# Copyright (C) 2012 The Android Open Source Project
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
include $(CLEAR_VARS)

LATINIME_CORE_SOURCE_DIRECTORY := ../../java/src/com/android/inputmethod/latin
MAKEDICT_CORE_SOURCE_DIRECTORY := $(LATINIME_CORE_SOURCE_DIRECTORY)/makedict

LOCAL_MAIN_SRC_FILES := $(call all-java-files-under,$(MAKEDICT_CORE_SOURCE_DIRECTORY))
LOCAL_TOOL_SRC_FILES := $(call all-java-files-under,src)
LOCAL_SRC_FILES := $(LOCAL_TOOL_SRC_FILES) \
        $(filter-out $(addprefix %/, $(notdir $(LOCAL_TOOL_SRC_FILES))), $(LOCAL_MAIN_SRC_FILES)) \
        $(call all-java-files-under,tests) \
        $(LATINIME_CORE_SOURCE_DIRECTORY)/Constants.java

LOCAL_JAR_MANIFEST := etc/manifest.txt
LOCAL_MODULE := dicttool_aosp
LOCAL_JAVA_LIBRARIES := junit

include $(BUILD_HOST_JAVA_LIBRARY)
include $(LOCAL_PATH)/etc/Android.mk
