/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.systemui.DemoMode;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TimeZone;

import libcore.icu.LocaleData;

/**
 * Digital clock for the status bar.
 */
public class Clock extends TextView implements DemoMode {

    protected boolean mAttached;
    protected Calendar mCalendar;
    protected String mClockFormatString;
    protected SimpleDateFormat mClockFormat;
    protected Locale mLocale;

    public static final int AM_PM_STYLE_NORMAL  = 0;
    public static final int AM_PM_STYLE_SMALL   = 1;
    public static final int AM_PM_STYLE_GONE    = 2;

    public static final int DATE_NORMAL = 0;
    public static final int DATE_SMALL = 1;
    public static final int DATE_GONE = 2;

    public static final int DATE_NORMAL_CASE = 0;
    public static final int DATE_LOWER_CASE = 1;
    public static final int DATE_UPPER_CASE = 2;

    protected int mAmPmStyle = AM_PM_STYLE_GONE;
    protected int mDateStyle = DATE_GONE;
    protected int mDateCase = DATE_NORMAL_CASE;

    private int mColor;
    private int mClockAndDateWidth;

    private SettingsObserver mSettingsObserver;
    private PhoneStatusBar mStatusBar;
    private Context mContext;
    private Handler mHandler;
    private TimerTask mSeconds;

    public Clock(Context context) {
        this(context, null);
    }

    public Clock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Clock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mHandler = new Handler();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            filter.addAction(Intent.ACTION_USER_SWITCHED);

            getContext().registerReceiverAsUser(mIntentReceiver, UserHandle.ALL, filter,
                    null, getHandler());
        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mCalendar = Calendar.getInstance(TimeZone.getDefault());

        // Make sure we update to the current time
        if (mSettingsObserver == null) {
            mSettingsObserver = new SettingsObserver(new Handler());
        }
        mSettingsObserver.observe();
        updateCustomizations();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
        if (mSettingsObserver != null) {
            mSettingsObserver.unobserve();
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
                TimeZone.setDefault(mCalendar.getTimeZone());
                if (mClockFormat != null) {
                    mClockFormat.setTimeZone(mCalendar.getTimeZone());
                }
            } else if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                final Locale newLocale = getResources().getConfiguration().locale;
                if (!newLocale.equals(mLocale)) {
                    mLocale = newLocale;
                }
                updateCustomizations();
                return;
            }
            updateClock();
        }
    };

    final void updateClock() {
        if (mDemoMode || mCalendar == null) return;
        mColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_COLOR, Color.WHITE);
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        setTextColor(mColor);
        setText(getSmallTime());
        setTextColor(mColor);
    }

    private final CharSequence getSmallTime() {
        boolean is24 = DateFormat.is24HourFormat(mContext, ActivityManager.getCurrentUser());
        LocaleData d = LocaleData.get(mContext.getResources().getConfiguration().locale);

        final char MAGIC1 = '\uEF00';
        final char MAGIC2 = '\uEF01';

        SimpleDateFormat sdf;
        String format = is24 ? d.timeFormat_Hm : d.timeFormat_hm;
        if (!format.equals(mClockFormatString)) {
            /*
             * Search for an unquoted "a" in the format string, so we can
             * add dummy characters around it to let us find it again after
             * formatting and change its size.
             */
            if (mAmPmStyle != AM_PM_STYLE_NORMAL) {
                int a = -1;
                boolean quoted = false;
                for (int i = 0; i < format.length(); i++) {
                    char c = format.charAt(i);

                    if (c == '\'') {
                        quoted = !quoted;
                    }
                    if (!quoted && c == 'a') {
                        a = i;
                        break;
                    }
                }

                if (a >= 0) {
                    // Move a back so any whitespace before AM/PM is also in the alternate size.
                    final int b = a;
                    while (a > 0 && Character.isWhitespace(format.charAt(a-1))) {
                        a--;
                    }
                    format = format.substring(0, a) + MAGIC1 + format.substring(a, b)
                        + "a" + MAGIC2 + format.substring(b + 1);
                }
            }
            mClockFormat = sdf = new SimpleDateFormat(format);
            mClockFormatString = format;
        } else {
            sdf = mClockFormat;
        }
        CharSequence dateString = null;
        String result = sdf.format(mCalendar.getTime());

        if (Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_SECONDS_CLOCK, 0) == 1) {
            String temp = result;
        result = String.format("%s:%02d", temp, new GregorianCalendar().get(Calendar.SECOND));
        }

        if (mDateStyle != DATE_GONE) {
            Date now = new Date();
            String dateFormat = Settings.System.getString(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_DATE_FORMAT);
            if (dateFormat == null || dateFormat.isEmpty()) {
                dateString = DateFormat.format("EEE", now) + " ";
            } else {
                switch (dateFormat) {
                    case "0":
                        dateFormat = "dd/MM/yy";
                        break;
                    case "1":
                        dateFormat = "MM/dd/yy";
                        break;
                    case "2":
                        dateFormat = "yyyy-MM-dd";
                        break;
                    case "3":
                        dateFormat = "yyyy-dd-MM";
                        break;
                    case "4":
                        dateFormat = "dd-MM-yyyy";
                        break;
                    case "5":
                        dateFormat = "MM-dd-yyyy";
                        break;
                    case "6":
                        dateFormat = "MMM dd";
                        break;
                    case "7":
                        dateFormat = "MMM dd, yyyy";
                        break;
                    case "8":
                        dateFormat = "MMMM dd, yyyy";
                        break;
                    case "9":
                    default:
                        dateFormat = "EEE";
                        break;
                    case "10":
                        dateFormat = "EEE dd";
                        break;
                    case "11":
                        dateFormat = "EEE dd/MM";
                        break;
                    case "12":
                        dateFormat = "EEE MM/dd";
                        break;
                    case "13":
                        dateFormat = "EEE dd MMM";
                        break;
                    case "14":
                        dateFormat = "EEE MMM dd";
                        break;
                    case "15":
                        dateFormat = "EEE MMMM dd";
                        break;
                    case "16":
                        dateFormat = "EEEE dd/MM";
                        break;
                    case "17":
                        dateFormat = "EEEE MM/dd";
                        break;
                }
                dateString = DateFormat.format(dateFormat, now) + " ";
            }
            if (mDateCase == DATE_LOWER_CASE) {
                result = dateString.toString().toLowerCase() + result;
            } else if (mDateCase == DATE_UPPER_CASE) {
                result = dateString.toString().toUpperCase() + result;
            } else {
                result = dateString.toString() + result;
            }
        }

        SpannableStringBuilder formatted = new SpannableStringBuilder(result);

        if (mAmPmStyle != AM_PM_STYLE_NORMAL) {
            int magic1 = result.indexOf(MAGIC1);
            int magic2 = result.indexOf(MAGIC2);
            if (magic1 >= 0 && magic2 > magic1) {
                if (mAmPmStyle == AM_PM_STYLE_GONE) {
                    formatted.delete(magic1, magic2+1);
                } else {
                    if (mAmPmStyle == AM_PM_STYLE_SMALL) {
                        CharacterStyle style = new RelativeSizeSpan(0.7f);
                        formatted.setSpan(style, magic1, magic2,
                                          Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    formatted.delete(magic2, magic2 + 1);
                    formatted.delete(magic1, magic1 + 1);
                }
            }
        }

        if (mDateStyle != DATE_NORMAL) {
            if (dateString != null) {
                int dateStringLen = dateString.length();
                if (mDateStyle == DATE_GONE) {
                    formatted.delete(0, dateStringLen);
                } else {
                    if (mDateStyle == DATE_SMALL) {
                        CharacterStyle style = new RelativeSizeSpan(0.7f);
                        formatted.setSpan(style, 0, dateStringLen, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                }
            }
        }
        return formatted;
    }

    private boolean mDemoMode;

    @Override
    public void dispatchDemoCommand(String command, Bundle args) {
        if (!mDemoMode && command.equals(COMMAND_ENTER)) {
            mDemoMode = true;
        } else if (mDemoMode && command.equals(COMMAND_EXIT)) {
            mDemoMode = false;
            updateClock();
        } else if (mDemoMode && command.equals(COMMAND_CLOCK)) {
            String millis = args.getString("millis");
            String hhmm = args.getString("hhmm");
            if (millis != null) {
                mCalendar.setTimeInMillis(Long.parseLong(millis));
            } else if (hhmm != null && hhmm.length() == 4) {
                int hh = Integer.parseInt(hhmm.substring(0, 2));
                int mm = Integer.parseInt(hhmm.substring(2));
                boolean is24 = DateFormat.is24HourFormat(
                        getContext(), ActivityManager.getCurrentUser());
                if (is24) {
                    mCalendar.set(Calendar.HOUR_OF_DAY, hh);
                } else {
                    mCalendar.set(Calendar.HOUR, hh);
                }
                mCalendar.set(Calendar.MINUTE, mm);
            }
            setText(getSmallTime());
        }
    }

    public void setAmPmStyle(int style) {
        mAmPmStyle = style;
        mClockFormatString = "";
        updateClock();
    }

    protected void updateCustomizations() {
        ContentResolver resolver = mContext.getContentResolver();
        boolean is24Hour = DateFormat.is24HourFormat(mContext);
        int amPm = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_CLOCK_AM_PM, AM_PM_STYLE_GONE, UserHandle.USER_CURRENT);
        mAmPmStyle = is24Hour ? AM_PM_STYLE_GONE : amPm;
        mClockFormatString = "";

        mDateStyle = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_DATE, DATE_GONE, UserHandle.USER_CURRENT);
        mDateCase = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_DATE_CASE, DATE_NORMAL_CASE, UserHandle.USER_CURRENT);

        mSeconds = new TimerTask() {
            @Override
            public void run() {
                Runnable updater = new Runnable() {
                    public void run() {
                        updateClock();
                    }
                };
                mHandler.post(updater);
            }
        };
        Timer timer = new Timer();
        timer.schedule(mSeconds, 0, 1001);

        if (mAttached) {
            updateClock();
        }
    }

    protected class SettingsObserver extends ContentObserver {

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_CLOCK_AM_PM), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_DATE), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_DATE_CASE), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_DATE_FORMAT), false, this, UserHandle.USER_ALL);
            updateCustomizations();
        }

        void unobserve() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateCustomizations();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateCustomizations();
        }
    }
}

