/**
 * Copyright (c) 2016 AllianceROM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.statusbar;

/**
 * @hide
 * TODO: We need to somehow make these managers accessible via getSystemService
 */
public final class AllianceContextConstants {

    /**
     * @hide
     */
    private AllianceContextConstants() {
        // Empty constructor
    }

    /**
     * Use with {@link android.content.Context#getSystemService} to retrieve a
     * {@link com.android.internal.statusbar.CMStatusBarManager} for informing the user of
     * background events.
     *
     * @see android.content.Context#getSystemService
     * @see com.android.internal.statusbar.CMStatusBarManager
     */
    public static final String ALLIANCE_STATUS_BAR_SERVICE = "cmstatusbar";

    /**
     * Use with {@link android.content.Context#getSystemService} to retrieve a
     * {@link cyanogenmod.app.ProfileManager} for informing the user of
     * background events.
     *
     * @see android.content.Context#getSystemService
     * @see cyanogenmod.app.ProfileManager
     *
     * @hide
     */
    public static final String CM_PROFILE_SERVICE = "profile";

    /**
     * Use with {@link android.content.Context#getSystemService} to retrieve a
     * {@link cyanogenmod.app.PartnerInterface} interact with system settings.
     *
     * @see android.content.Context#getSystemService
     * @see cyanogenmod.app.PartnerInterface
     *
     * @hide
     */
    public static final String CM_PARTNER_INTERFACE = "cmpartnerinterface";

    /**
     * Use with {@link android.content.Context#getSystemService} to retrieve a
     * {@link cyanogenmod.app.CMTelephonyManager} to manage the phone and
     * data connection.
     *
     * @see android.content.Context#getSystemService
     * @see cyanogenmod.app.CMTelephonyManager
     *
     * @hide
     */
    public static final String CM_TELEPHONY_MANAGER_SERVICE = "cmtelephonymanager";

    /**
     * Use with {@link android.content.Context#getSystemService} to retrieve a
     * {@link cyanogenmod.hardware.CMHardwareManager} to manage the extended
     * hardware features of the device.
     *
     * @see android.content.Context#getSystemService
     * @see cyanogenmod.hardware.CMHardwareManager
     *
     * @hide
     */
    public static final String CM_HARDWARE_SERVICE = "cmhardware";

    /**
     * @hide
     */
    public static final String CM_APP_SUGGEST_SERVICE = "cmappsuggest";

    /**
     * Control device power profile and characteristics.
     *
     * @hide
     */
    public static final String CM_PERFORMANCE_SERVICE = "cmperformance";
}
