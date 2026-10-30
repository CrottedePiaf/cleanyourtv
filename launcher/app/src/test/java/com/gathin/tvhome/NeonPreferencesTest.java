package com.gathin.tvhome;

import org.junit.Test;
import android.content.SharedPreferences;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/** Neon updates and rollback must retain the owner's layout, wallpaper and motion choices. */
public class NeonPreferencesTest {
    @Test
    public void visualUpdatesAndRollbackPreserveChoices() {
        assertTrue(Config.preserveThemeChoices(true, false));
        assertTrue(Config.preserveThemeChoices(true, true));
        assertTrue(Config.preserveThemeChoices(false, true));
    }

    @Test
    public void ordinaryThemesKeepTheirExistingResetPolicy() {
        assertFalse(Config.preserveThemeChoices(false, false));
    }

    @Test
    public void savedSettingsSurviveActivationUpdateAndRollback() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("columns", 6);
        values.put("animations", false);
        values.put("advanced_effects", false);
        values.put("wallpaper_choice", "none");
        values.put("dock_opacity", 65);
        values.put("clock_format", "24");
        values.put("rows_below_dock", 2);
        Map<String, Object> saved = new HashMap<>(values);
        SharedPreferences preferences = preferences(values);
        Method sync = Config.class.getDeclaredMethod("syncThemeChoices", SharedPreferences.class,
                File.class, File.class, boolean.class);
        sync.setAccessible(true);
        for (boolean neon : new boolean[]{true, true, false}) {
            values.put("theme_signature", "previous-theme-revision");
            sync.invoke(null, preferences, null, null, neon);
            for (Map.Entry<String, Object> entry : saved.entrySet()) {
                assertEquals(entry.getKey(), entry.getValue(), values.get(entry.getKey()));
            }
            assertEquals(neon, values.get("neon_theme"));
        }
        // A later ordinary theme still uses its original explicit reset policy.
        values.put("theme_signature", "another-theme-revision");
        sync.invoke(null, preferences, null, null, false);
        assertFalse(values.containsKey("columns"));
        assertFalse(values.containsKey("animations"));
    }

    /** In-memory Android interface implementation; no device or Android runtime needed. */
    private static SharedPreferences preferences(Map<String, Object> values) {
        SharedPreferences.Editor editor = (SharedPreferences.Editor) Proxy.newProxyInstance(
                SharedPreferences.Editor.class.getClassLoader(), new Class<?>[]{SharedPreferences.Editor.class},
                (proxy, method, args) -> {
                    if (method.getName().startsWith("put")) values.put((String) args[0], args[1]);
                    else if (method.getName().equals("remove")) values.remove(args[0]);
                    else if (method.getName().equals("commit")) return true;
                    return proxy;
                });
        return (SharedPreferences) Proxy.newProxyInstance(SharedPreferences.class.getClassLoader(),
                new Class<?>[]{SharedPreferences.class}, (proxy, method, args) -> {
                    if (method.getName().equals("edit")) return editor;
                    if (method.getName().startsWith("get")) return values.getOrDefault(args[0], args[1]);
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
