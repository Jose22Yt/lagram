package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            ComponentName cn = i.getComponentName(ctx);
            int targetState = i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            int currentState = pm.getComponentEnabledSetting(cn);
            if (currentState != targetState) {
                pm.setComponentEnabledSetting(cn, targetState, PackageManager.DONT_KILL_APP);
            }
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.drawable.lagram_icon_1_bg, R.drawable.lagram_icon_1_fg, R.string.LagramIconDefault),
        NOX("NoxIcon", R.drawable.lagram_icon_2_bg, R.drawable.lagram_icon_2_fg, R.string.LagramIconObsidian),
        TURBO("TurboIcon", R.drawable.lagram_icon_3_bg, R.drawable.lagram_icon_3_fg, R.string.LagramIconAurora),
        AQUA("AquaIcon", R.drawable.lagram_icon_4_bg, R.drawable.lagram_icon_4_fg, R.string.LagramIconEmerald),
        PREMIUM("PremiumIcon", R.drawable.lagram_icon_5_bg, R.drawable.lagram_icon_5_fg, R.string.LagramIconSunset),
        VINTAGE("VintageIcon", R.drawable.lagram_icon_6_bg, R.drawable.lagram_icon_6_fg, R.string.LagramIconTitanium);

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
        }
    }
}
