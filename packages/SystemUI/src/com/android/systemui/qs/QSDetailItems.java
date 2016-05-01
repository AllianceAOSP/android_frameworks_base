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

package com.android.systemui.qs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;

/**
 * Quick settings common detail view with line items.
 */
public class QSDetailItems extends FrameLayout {
    private static final String TAG = "QSDetailItems";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final Context mContext;
    private final H mHandler = new H();

    private String mTag;
    private Callback mCallback;
    private boolean mItemsVisible = true;
    private LinearLayout mItems;
    private View mEmpty;
    private View mMinHeightSpacer;
    private TextView mEmptyText;
    private ImageView mEmptyIcon;
    private int mMaxItems;
    private int mTextColor;
    private int mEmptyTextColor;
    private int mIconColor;
    private SettingsObserver mSettingsObserver;

    public QSDetailItems(Context context) {
        super(context);
        mContext = context;
        mSettingsObserver = new SettingsObserver(mHandler);
        mTag = TAG;
    }

    public QSDetailItems(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mSettingsObserver = new SettingsObserver(mHandler);
        mTag = TAG;
    }

    public static QSDetailItems convertOrInflate(Context context, View convert, ViewGroup parent) {
        if (convert instanceof QSDetailItems) {
            return (QSDetailItems) convert;
        }
        return (QSDetailItems) LayoutInflater.from(context).inflate(R.layout.qs_detail_items,
                parent, false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mItems = (LinearLayout) findViewById(android.R.id.list);
        mItems.setVisibility(GONE);
        mEmpty = findViewById(android.R.id.empty);
        mEmpty.setVisibility(GONE);
        mEmptyText = (TextView) mEmpty.findViewById(android.R.id.title);
        mEmptyIcon = (ImageView) mEmpty.findViewById(android.R.id.icon);
        mMinHeightSpacer = findViewById(R.id.min_height_spacer);

        // By default, a detail item view has fixed size.
        mMaxItems = getResources().getInteger(
                R.integer.quick_settings_detail_max_item_count);
        setMinHeightInItems(mMaxItems);
        updateColors();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(mEmptyText, R.dimen.qs_detail_empty_text_size);
        int count = mItems.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = mItems.getChildAt(i);
            FontSizeUtils.updateFontSize(item, android.R.id.title,
                    R.dimen.qs_detail_item_primary_text_size);
            FontSizeUtils.updateFontSize(item, android.R.id.summary,
                    R.dimen.qs_detail_item_secondary_text_size);
        }
    }

    public void setTagSuffix(String suffix) {
        mTag = TAG + "." + suffix;
    }

    public void setEmptyState(int icon, int text) {
        updateColors();
        mEmptyIcon.setImageResource(icon);
        mEmptyText.setText(text);
        mEmptyIcon.setColorFilter(mIconColor, Mode.MULTIPLY);
        mEmptyText.setTextColor(mEmptyTextColor);
    }

    /**
     * Set the minimum height of this detail view, in item count.
     */
    public void setMinHeightInItems(int minHeightInItems) {
        ViewGroup.LayoutParams lp = mMinHeightSpacer.getLayoutParams();
        lp.height = minHeightInItems * getResources().getDimensionPixelSize(
                R.dimen.qs_detail_item_height);
        mMinHeightSpacer.setLayoutParams(lp);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) Log.d(mTag, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) Log.d(mTag, "onDetachedFromWindow");
        mCallback = null;
    }

    public void setCallback(Callback callback) {
        mHandler.removeMessages(H.SET_CALLBACK);
        mHandler.obtainMessage(H.SET_CALLBACK, callback).sendToTarget();
    }

    public void setItems(Item[] items) {
        mHandler.removeMessages(H.SET_ITEMS);
        mHandler.obtainMessage(H.SET_ITEMS, items).sendToTarget();
    }

    public void setItemsVisible(boolean visible) {
        mHandler.removeMessages(H.SET_ITEMS_VISIBLE);
        mHandler.obtainMessage(H.SET_ITEMS_VISIBLE, visible ? 1 : 0, 0).sendToTarget();
    }

    private void handleSetCallback(Callback callback) {
        mCallback = callback;
    }

    private void handleSetItems(Item[] items) {
        final int itemCount = items != null ? Math.min(items.length, mMaxItems) : 0;
        mEmpty.setVisibility(itemCount == 0 ? VISIBLE : GONE);
        mItems.setVisibility(itemCount == 0 ? GONE : VISIBLE);
        for (int i = mItems.getChildCount() - 1; i >= itemCount; i--) {
            mItems.removeViewAt(i);
        }
        for (int i = 0; i < itemCount; i++) {
            bind(items[i], mItems.getChildAt(i));
        }
    }

    private void handleSetItemsVisible(boolean visible) {
        if (mItemsVisible == visible) return;
        mItemsVisible = visible;
        for (int i = 0; i < mItems.getChildCount(); i++) {
            mItems.getChildAt(i).setVisibility(mItemsVisible ? VISIBLE : INVISIBLE);
        }
    }

    private void bind(final Item item, View view) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.qs_detail_item, this, false);
            mItems.addView(view);
        }
        view.setVisibility(mItemsVisible ? VISIBLE : INVISIBLE);
        final ImageView iv = (ImageView) view.findViewById(android.R.id.icon);
        iv.setImageResource(item.icon);
        iv.setColorFilter(mIconColor, Mode.MULTIPLY);
        iv.getOverlay().clear();
        if (item.overlay != null) {
            item.overlay.setBounds(0, 0, item.overlay.getIntrinsicWidth(),
                    item.overlay.getIntrinsicHeight());
            iv.getOverlay().add(item.overlay);
            iv.setColorFilter(mIconColor, Mode.MULTIPLY);
        }
        final TextView title = (TextView) view.findViewById(android.R.id.title);
        title.setText(item.line1);
        updateColors();
        title.setTextColor(mTextColor);
        final TextView summary = (TextView) view.findViewById(android.R.id.summary);
        final boolean twoLines = !TextUtils.isEmpty(item.line2);
        title.setMaxLines(twoLines ? 1 : 2);
        summary.setVisibility(twoLines ? VISIBLE : GONE);
        summary.setText(twoLines ? item.line2 : null);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onDetailItemClick(item);
                }
            }
        });
        final ImageView disconnect = (ImageView) view.findViewById(android.R.id.icon2);
        disconnect.setVisibility(item.canDisconnect ? VISIBLE : GONE);
        disconnect.setColorFilter(mIconColor, Mode.MULTIPLY);
        disconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onDetailItemDisconnect(item);
                }
            }
        });
    }

    private void updateColors() {
        final ContentResolver resolver = mContext.getContentResolver();
        mTextColor = Settings.System.getInt(resolver, Settings.System.QUICK_SETTINGS_TILE_TEXT_COLOR, Color.WHITE);
        mEmptyTextColor = (153 << 24) | (mTextColor & 0x00ffffff);
        mIconColor = Settings.System.getInt(resolver, Settings.System.QUICK_SETTINGS_ICON_COLOR, Color.WHITE);
    }

    private class H extends Handler {
        private static final int SET_ITEMS = 1;
        private static final int SET_CALLBACK = 2;
        private static final int SET_ITEMS_VISIBLE = 3;

        public H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SET_ITEMS) {
                handleSetItems((Item[]) msg.obj);
            } else if (msg.what == SET_CALLBACK) {
                handleSetCallback((QSDetailItems.Callback) msg.obj);
            } else if (msg.what == SET_ITEMS_VISIBLE) {
                handleSetItemsVisible(msg.arg1 != 0);
            }
        }
    }

    public static class Item {
        public int icon;
        public Drawable overlay;
        public CharSequence line1;
        public CharSequence line2;
        public Object tag;
        public boolean canDisconnect;
    }

    public interface Callback {
        void onDetailItemClick(Item item);
        void onDetailItemDisconnect(Item item);
    }

    class SettingsObserver extends ContentObserver {

        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QUICK_SETTINGS_TILE_TEXT_COLOR), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QUICK_SETTINGS_ICON_COLOR), false, this, UserHandle.USER_ALL);
            update();
        }

        void unobserve() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            update();
        }

        public void update() {
            updateColors();
        }
    }
}
