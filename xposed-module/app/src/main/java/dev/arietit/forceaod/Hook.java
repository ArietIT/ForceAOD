package dev.arietit.forceaod;

import android.content.ContentResolver;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage {

    private static final String TAG = "ForceAOD";
    private static final String PKG_AOD = "com.samsung.android.app.aodservice";
    private static final String PKG_SYSUI = "com.android.systemui";

    /** Тумблер в Settings.System (переключается кнопкой Action в KSU-модуле). */
    private static final String TOGGLE = "force_aod";

    /** true = писать в logcat (tag ForceAOD) все ключи Settings.System, которые читает процесс. */
    private static final boolean PROBE = false;

    /** Ключ -> значение, которое подставляем, пока тумблер включён. */
    private static final Map<String, Integer> FORCE = new HashMap<>();
    static {
        FORCE.put("aod_mode", 1);
        FORCE.put("aod_tap_to_show_mode", 0);
        // Добавляй сюда ключи, которые покажет PROBE на One UI 9
    }

    private static final ThreadLocal<Boolean> BUSY = new ThreadLocal<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpp) {
        if (!PKG_AOD.equals(lpp.packageName) && !PKG_SYSUI.equals(lpp.packageName)) return;
        final String proc = lpp.packageName;

        hookSettings(proc);
        if (PKG_AOD.equals(proc)) hookPowerSave();
    }

    private static boolean enabled(ContentResolver cr, int user) {
        if (cr == null) return false;
        BUSY.set(true);
        try {
            return Settings.System.getIntForUser(cr, TOGGLE, 1, user) == 1;
        } catch (Throwable t) {
            return false;
        } finally {
            BUSY.remove();
        }
    }

    private void hookSettings(final String proc) {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                if (Boolean.TRUE.equals(BUSY.get())) return;
                String key = (String) p.args[1];
                if (PROBE) Log.d(TAG, proc + " read system:" + key);
                Integer v = FORCE.get(key);
                if (v == null) return;
                ContentResolver cr = (ContentResolver) p.args[0];
                int user = (int) p.args[p.args.length - 1];
                if (enabled(cr, user)) p.setResult(v);
            }
        };
        try {
            // getInt(cr, name, def) -> getIntForUser(cr, name, def, userId)
            XposedHelpers.findAndHookMethod(Settings.System.class, "getIntForUser",
                    ContentResolver.class, String.class, int.class, int.class, hook);
            // getInt(cr, name) -> getIntForUser(cr, name, userId)
            XposedHelpers.findAndHookMethod(Settings.System.class, "getIntForUser",
                    ContentResolver.class, String.class, int.class, hook);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": settings hook failed in " + proc + ": " + t);
        }
    }

    private void hookPowerSave() {
        try {
            XposedHelpers.findAndHookMethod(PowerManager.class, "isPowerSaveMode",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam p) {
                            android.app.Application app = android.app.AndroidAppHelper.currentApplication();
                            if (app == null) return;
                            int user = Process.myUid() / 100000;
                            if (enabled(app.getContentResolver(), user)) p.setResult(false);
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": powersave hook failed: " + t);
        }
    }
}
