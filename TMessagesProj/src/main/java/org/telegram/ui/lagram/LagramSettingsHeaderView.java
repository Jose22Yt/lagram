package org.telegram.ui.lagram;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.GLIcon.GLIconRenderer;
import org.telegram.ui.Components.Premium.GLIcon.GLIconTextureView;
import org.telegram.ui.Components.Premium.GLIcon.Icon3D;

import java.util.Locale;

public class LagramSettingsHeaderView extends LinearLayout implements Theme.Colorable {

    private final Theme.ResourcesProvider resourcesProvider;
    private final GLIconTextureView iconTextureView;
    private final TextView titleTextView;
    private final TextView versionTextView;

    public LagramSettingsHeaderView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        iconTextureView = new GLIconTextureView(context, GLIconRenderer.DIALOG_STYLE, Icon3D.TYPE_DIAMOND);
        addView(iconTextureView, LayoutHelper.createLinear(165, 165, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 0));

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24.0f);
        titleTextView.setTypeface(AndroidUtilities.bold());
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setText(context.getString(R.string.AppName));
        addView(titleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 4, 20, 0));

        versionTextView = new TextView(context);
        versionTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        versionTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        versionTextView.setText(getVersionString());
        addView(versionTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 20, 4, 20, 0));

        updateColors();
    }

    public static String getVersionString() {
        try {
            PackageInfo packageInfo = ApplicationLoader.applicationContext
                    .getPackageManager()
                    .getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            return String.format(Locale.US, "v%s (%d) • Turbo", packageInfo.versionName, packageInfo.versionCode / 10);
        } catch (Exception e) {
            FileLog.e(e);
            return "v12.6.1 • Turbo";
        }
    }

    public void setPaused(boolean paused) {
        iconTextureView.setPaused(paused);
        iconTextureView.setDialogVisible(paused);
    }

    @Override
    public void updateColors() {
        if (iconTextureView.mRenderer != null) {
            iconTextureView.mRenderer.updateColors();
        }
        titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        versionTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
    }
}
