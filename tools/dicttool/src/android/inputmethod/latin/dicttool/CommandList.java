/**
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.inputmethod.latin.dicttool;

public class CommandList {
    public static void populate() {
        Dicttool.addCommand("info", Info.class);
        Dicttool.addCommand("compress", Compress.Compressor.class);
        Dicttool.addCommand("uncompress", Compress.Uncompressor.class);
        Dicttool.addCommand("makedict", Makedict.class);
    }
}
