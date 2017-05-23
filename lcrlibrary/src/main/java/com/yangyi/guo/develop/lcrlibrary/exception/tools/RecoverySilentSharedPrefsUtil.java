package com.yangyi.guo.develop.lcrlibrary.exception.tools;

import com.yangyi.guo.develop.lcrlibrary.exception.core.CrashData;
import com.yangyi.guo.develop.lcrlibrary.exception.core.Recovery;

/**
 * Created by GYH on 2017/5/22.
 */

public class RecoverySilentSharedPrefsUtil {

    private static final long DEFAULT_TIME_INTERVAL = 30 * 1000;

    private static final int DEFAULT_MAX_COUNT = 2;

    private static final String SHOULD_CLEAR_APP_AND_NOT_RESTART = "should_clear_app_and_not_restart";
    private static final String SHARED_PREFS_NAME = "recovery_silent_info";

    private static final String CRASH_COUNT = "crash_count";

    private static final String CRASH_TIME = "crash_time";

    /**
     * 记录缓存数据
     *
     * 并且判断是否能够能够重启
     */
    public static void recordCrashData() {
        int count = 0;
        long time = 0L;
        boolean shouldRestart = false;
        try {
            count = Integer.parseInt(get(CRASH_COUNT, String.valueOf(0)));
            time = Long.parseLong(get(CRASH_TIME, String.valueOf(0L)));
        } catch (Exception e) {
            count = 0;
            time = 0L;
            RecoveryLog.e(e.getMessage());
        }
        count = Math.max(count, 0);
        time = Math.max(time, 0L);

        count += 1;
        time = time == 0L ? System.currentTimeMillis() : time;

        long interval = System.currentTimeMillis() - time;
        if (count >= DEFAULT_MAX_COUNT && interval <= DEFAULT_TIME_INTERVAL) {
            shouldRestart = true;
            count = 0;
            time = 0L;
        } else {
            if (count >= DEFAULT_MAX_COUNT || interval > DEFAULT_TIME_INTERVAL) {
                count = 1;
                time = System.currentTimeMillis();
            }
        }
        CrashData data = CrashData.newInstance()
                .count(count)
                .time(time)
                .restart(shouldRestart);
        RecoveryLog.e(data.toString());
        saveCrashData(data);
    }

    private static void saveCrashData(CrashData data) {
        if (data == null)
            return;
        SharedPreferencesCompat.newBuilder(Recovery.getInstance().getContext(), SHARED_PREFS_NAME)
                .put(CRASH_COUNT, String.valueOf(data.crashCount))
                .put(CRASH_TIME, String.valueOf(data.crashTime))
                .put(SHOULD_CLEAR_APP_AND_NOT_RESTART, String.valueOf(data.shouldRestart))
                .apply();
    }

    /**
     * 判断是否能够清除并且重启应用
     * @return
     */
    public static boolean shouldClearAppNotRestart() {
        return Boolean.parseBoolean(get(SHOULD_CLEAR_APP_AND_NOT_RESTART, String.valueOf(false)));
    }

    /**
     * 清除
     */
    public static void clear() {
        SharedPreferencesCompat.clear(Recovery.getInstance().getContext(), SHARED_PREFS_NAME);
    }


    /**
     * 获取share数据
     * @param key
     * @param defValue
     * @return
     */
    private static String get(String key, String defValue) {
        return SharedPreferencesCompat.get(Recovery.getInstance().getContext(), SHARED_PREFS_NAME, key, defValue);
    }
}
