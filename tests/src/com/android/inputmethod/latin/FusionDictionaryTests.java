/*
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

package com.android.inputmethod.latin;

import android.test.AndroidTestCase;

import com.android.inputmethod.latin.makedict.FusionDictionary;
import com.android.inputmethod.latin.makedict.FusionDictionary.Node;

import java.util.HashMap;

/**
 * Unit test for FusionDictionary
 */
public class FusionDictionaryTests extends AndroidTestCase {
    public void testFindWordInTree() {
        FusionDictionary dict = new FusionDictionary(new Node(),
                new FusionDictionary.DictionaryOptions(new HashMap<String,String>(), false, false));

        dict.add("abc", 10, null, false /* isNotAWord */);
        assertNull(FusionDictionary.findWordInTree(dict.mRoot, "aaa"));
        assertNotNull(FusionDictionary.findWordInTree(dict.mRoot, "abc"));

        dict.add("aa", 10, null, false /* isNotAWord */);
        assertNull(FusionDictionary.findWordInTree(dict.mRoot, "aaa"));
        assertNotNull(FusionDictionary.findWordInTree(dict.mRoot, "aa"));

        dict.add("babcd", 10, null, false /* isNotAWord */);
        dict.add("bacde", 10, null, false /* isNotAWord */);
        assertNull(FusionDictionary.findWordInTree(dict.mRoot, "ba"));
        assertNotNull(FusionDictionary.findWordInTree(dict.mRoot, "babcd"));
        assertNotNull(FusionDictionary.findWordInTree(dict.mRoot, "bacde"));
    }
}
