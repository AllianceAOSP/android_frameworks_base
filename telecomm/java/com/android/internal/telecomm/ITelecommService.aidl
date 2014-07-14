/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.internal.telecomm;

import android.content.ComponentName;
import android.telecomm.PhoneAccount;
import android.telecomm.PhoneAccountMetadata;

/**
 * Interface used to interact with Telecomm. Mostly this is used by TelephonyManager for passing
 * commands that were previously handled by ITelephony.
 * {@hide}
 */
interface ITelecommService {
    /**
     * Brings the in-call screen to the foreground if there is an active call.
     *
     * @param showDialpad if true, make the dialpad visible initially.
     */
    void showCallScreen(boolean showDialpad);

    /**
     * @see TelecommManager#getEnabledPhoneAccounts
     */
    List<PhoneAccount> getEnabledPhoneAccounts();

    /**
     * @see TelecommManager#getPhoneAccountMetadata
     */
    PhoneAccountMetadata getPhoneAccountMetadata(in PhoneAccount account);

    /**
     * @see TelecommManager#registerPhoneAccount
     */
    void registerPhoneAccount(in PhoneAccount account, in PhoneAccountMetadata metadata);

    /**
     * @see TelecommManager#unregisterPhoneAccount
     */
    void unregisterPhoneAccount(in PhoneAccount account);

    /**
     * @see TelecommManager#clearAccounts
     */
    void clearAccounts(String packageName);

    /**
     * @see TelecommManager#getDefaultPhoneApp
     */
    ComponentName getDefaultPhoneApp();

    //
    // Internal system apis relating to call management.
    //

    /**
     * @see TelecommManager#silenceRinger
     */
    void silenceRinger();

    /**
     * @see TelecommManager#isInAPhoneCall
     */
    boolean isInAPhoneCall();

    /**
     * @see TelecomManager#isRinging
     */
    boolean isRinging();

    /**
     * @see TelecommManager#endCall
     */
    boolean endCall();

    /**
     * @see TelecommManager#acceptRingingCall
     */
    void acceptRingingCall();

    /**
     * @see PhoneManager#cancelMissedCallsNotification
     */
    void cancelMissedCallsNotification();

    /**
     * @see PhoneManager#handlePinMmi
     */
    boolean handlePinMmi(String dialString);
}
