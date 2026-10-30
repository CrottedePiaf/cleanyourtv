
package com.gathin.tvhome;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

/** Lets the user change HOME through the system UI, without shell or privileged permissions. */
public class HomeSettingsActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        requestHome(this);
        finish();
    }

    /** Only runs after the user chooses Default home; Android always owns the decision. */
    static void requestHome(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roles = activity.getSystemService(RoleManager.class);
            try {
                if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME)
                        && !roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                    activity.startActivity(roles.createRequestRoleIntent(RoleManager.ROLE_HOME));
                    return;
                }
            } catch (ActivityNotFoundException | SecurityException unavailable) {
                // Some TV firmware exposes the role but omits or restricts its request UI.
            }
        }
        // TV firmware may omit the dedicated Home page. Let the user navigate its settings instead.
        String[] actions = {Settings.ACTION_HOME_SETTINGS,
                Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, Settings.ACTION_SETTINGS};
        for (String action : actions) {
            try {
                activity.startActivity(new Intent(action));
                Toast.makeText(activity, R.string.home_settings_help, Toast.LENGTH_LONG).show();
                return;
            } catch (ActivityNotFoundException | SecurityException unavailable) {
                // Try the next public settings entry point without changing any package.
            }
        }
        Toast.makeText(activity, R.string.not_available, Toast.LENGTH_LONG).show();
    }
}
