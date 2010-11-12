/*
 * Copyright (C) 2010 Google Inc.
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

import android.util.Log;

public class ModifierKeyState {
    protected static final String TAG = "ModifierKeyState";
    protected static final boolean DEBUG = KeyboardSwitcher.DEBUG_STATE;

    protected static final int RELEASING = 0;
    protected static final int PRESSING = 1;
    protected static final int MOMENTARY = 2;

    protected final String mName;
    protected int mState = RELEASING;

    public ModifierKeyState(String name) {
        mName = name;
    }

    public void onPress() {
        final int oldState = mState;
        mState = PRESSING;
        if (DEBUG)
            Log.d(TAG, mName + ".onPress: " + toString(oldState) + " > " + this);
    }

    public void onRelease() {
        final int oldState = mState;
        mState = RELEASING;
        if (DEBUG)
            Log.d(TAG, mName + ".onRelease: " + toString(oldState) + " > " + this);
    }

    public void onOtherKeyPressed() {
        final int oldState = mState;
        if (oldState == PRESSING)
            mState = MOMENTARY;
        if (DEBUG)
            Log.d(TAG, mName + ".onOtherKeyPressed: " + toString(oldState) + " > " + this);
    }

    public boolean isReleasing() {
        return mState == RELEASING;
    }

    public boolean isMomentary() {
        return mState == MOMENTARY;
    }

    @Override
    public String toString() {
        return toString(mState);
    }

    protected String toString(int state) {
        switch (state) {
        case RELEASING: return "RELEASING";
        case PRESSING: return "PRESSING";
        case MOMENTARY: return "MOMENTARY";
        default: return "UNKNOWN";
        }
    }
}
