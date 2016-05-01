package com.android.internal.util;

import java.util.ArrayList;

public class QSConstants {
    private QSConstants() {}

    public static final String TILE_WIFI = "wifi";
    public static final String TILE_BLUETOOTH = "bt";
    public static final String TILE_INVERSION = "inversion";
    public static final String TILE_CELLULAR = "cell";
    public static final String TILE_AIRPLANE = "airplane";
    public static final String TILE_ROTATION = "rotation";
    public static final String TILE_FLASHLIGHT = "flashlight";
    public static final String TILE_LOCATION = "location";
    public static final String TILE_CAST = "cast";
    public static final String TILE_HOTSPOT = "hotspot";
    public static final String TILE_PERFORMANCE = "performance";
    public static final String TILE_ADB_NETWORK = "adb_network";
    public static final String TILE_NFC = "nfc";
    public static final String TILE_COMPASS = "compass";
    public static final String TILE_LOCKSCREEN = "lockscreen";
    public static final String TILE_LTE = "lte";
    public static final String TILE_VOLUME = "volume_panel";
    public static final String TILE_SCREEN_TIMEOUT = "screen_timeout";
    public static final String TILE_USB_TETHER = "usb_tether";
    public static final String TILE_SYNC = "sync";
    public static final String TILE_EDIT = "edit";
    public static final String TILE_DND = "dnd";
    public static final String TILE_BRIGHTNESS = "brightness";
    public static final String TILE_SCREEN_OFF = "screen_off";
    public static final String TILE_SCREENSHOT = "screenshot";

    public static final String DYNAMIC_TILE_NEXT_ALARM = "next_alarm";
    public static final String DYNAMIC_TILE_IME_SELECTOR = "ime_selector";
    public static final String DYNAMIC_TILE_ADB = "adb";

    protected static final ArrayList<String> STATIC_TILES_AVAILABLE = new ArrayList<String>();
    protected static final ArrayList<String> DYNAMIC_TILES_AVAILBLE = new ArrayList<String>();
    protected static final ArrayList<String> TILES_AVAILABLE = new ArrayList<String>();

    static {
        STATIC_TILES_AVAILABLE.add(TILE_WIFI);
        STATIC_TILES_AVAILABLE.add(TILE_BLUETOOTH);
        STATIC_TILES_AVAILABLE.add(TILE_CELLULAR);
        STATIC_TILES_AVAILABLE.add(TILE_AIRPLANE);
        STATIC_TILES_AVAILABLE.add(TILE_ROTATION);
        STATIC_TILES_AVAILABLE.add(TILE_FLASHLIGHT);
        STATIC_TILES_AVAILABLE.add(TILE_LOCATION);
        STATIC_TILES_AVAILABLE.add(TILE_EDIT);
        STATIC_TILES_AVAILABLE.add(TILE_CAST);
        STATIC_TILES_AVAILABLE.add(TILE_HOTSPOT);
        STATIC_TILES_AVAILABLE.add(TILE_INVERSION);
        STATIC_TILES_AVAILABLE.add(TILE_DND);
//        STATIC_TILES_AVAILABLE.add(TILE_NOTIFICATIONS);
//        STATIC_TILES_AVAILABLE.add(TILE_DATA);
//        STATIC_TILES_AVAILABLE.add(TILE_ROAMING);
//        STATIC_TILES_AVAILABLE.add(TILE_DDS);
//        STATIC_TILES_AVAILABLE.add(TILE_APN);
        STATIC_TILES_AVAILABLE.add(TILE_ADB_NETWORK);
        STATIC_TILES_AVAILABLE.add(TILE_NFC);
        STATIC_TILES_AVAILABLE.add(TILE_COMPASS);
        STATIC_TILES_AVAILABLE.add(TILE_LOCKSCREEN);
//        STATIC_TILES_AVAILABLE.add(TILE_LTE);
//        STATIC_TILES_AVAILABLE.add(TILE_VISUALIZER);
        STATIC_TILES_AVAILABLE.add(TILE_VOLUME);
        STATIC_TILES_AVAILABLE.add(TILE_SCREEN_TIMEOUT);
        STATIC_TILES_AVAILABLE.add(TILE_USB_TETHER);
        STATIC_TILES_AVAILABLE.add(TILE_SYNC);
        STATIC_TILES_AVAILABLE.add(TILE_BRIGHTNESS);
        STATIC_TILES_AVAILABLE.add(TILE_SCREEN_OFF);
        STATIC_TILES_AVAILABLE.add(TILE_SCREENSHOT);

        TILES_AVAILABLE.addAll(STATIC_TILES_AVAILABLE);

        DYNAMIC_TILES_AVAILBLE.add(DYNAMIC_TILE_ADB);
        DYNAMIC_TILES_AVAILBLE.add(DYNAMIC_TILE_IME_SELECTOR);
        DYNAMIC_TILES_AVAILBLE.add(DYNAMIC_TILE_NEXT_ALARM);
    }
}
