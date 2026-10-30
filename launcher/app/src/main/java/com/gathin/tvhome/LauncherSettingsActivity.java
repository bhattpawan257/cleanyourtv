package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Remote-friendly settings owned by the launcher; system settings stay one row away. */
public final class LauncherSettingsActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }

    static final String EXTRA_REORDER = "com.gathin.tvhome.extra.REORDER_FROM_SETTINGS";
    private SharedPreferences choices;
    private final TextView[] columnChoices = new TextView[4];
    private final TextView[] rowChoices = new TextView[4];
    private static final int[] DOCK_OPACITIES = {100, 70, 50, 30};
    private final TextView[] dockChoices = new TextView[DOCK_OPACITIES.length];
    private View reorderChoice;
    private TextView wallpaperChoice;
    private TextView animationsChoice;
    private TextView advancedChoice;
    private TextView followThemeChoice;
    private TextView openOnStartChoice;
    private TextView moreOptionsChoice;
    private LinearLayout moreOptionsContainer;
    private int columns;
    private int rowsBelowDock;
    private int dockOpacity;
    private boolean animations;
    private boolean advancedEffects;
    /** On-TV switches: the saved key(s) each one writes, its row id and its label. */
    private static final String[][] TOGGLE_KEYS = {
            {"wifi_status_color", "wifi_status_warnings"},
            {"dock_glass"},
            {"always_show_names"},
            {"focus_overshoot"},
            {"dock_row"},
            {"dock_band"},
            {"dock_title"},
            {"dock_even_padding"},
            {"fill_dock"},
            {"single_row"},
            {"dock_band_all"},
            {"cinematic_home"},
            {"cinematic_dots"},
            {"cinematic_white_focus"},
            {"ambient_glow"},
            {"app_glow"},
            {"fade_rows"},
            {"uniform_tiles"},
            {"wallpaper_true_color"},
            {"wallpaper_captions"},
            {"reduced_motion"},
            {"top_menu"},
            {"launcher_settings_button"},
            {"reorder_button"},
            {"show_network_pill"},
            {"show_inputs_pill"},
            {"icon_only_buttons"},
            {"icon_only_inputs"},
            {"vpn_pill"},
            {"icon_only_vpn"},
            {"show_greeting"},
            {"header_white"},
            {"date_below_clock"},
            {"shamsi_date"},
            {"lunar_date"},
            {"show_stats"},
            {"clock_wallpaper"},
            {"clock_glow"},
            {"tile_menu"},
            {"inputs_connected_only"},
            {"coocaa_sources"},
            {"module_cards"},
            {"show_home_settings_shortcut"},
            {"open_on_start_choice"}
    };
    private static final int[] TOGGLE_IDS = {
            R.id.wifiStatusChoice, R.id.dockGlassChoice, R.id.namesChoice, R.id.overshootChoice,
            R.id.dockRowChoice, R.id.dockBandChoice, R.id.dockTitleChoice, R.id.dockEvenPaddingChoice,
            R.id.fillDockChoice, R.id.singleRowChoice, R.id.dockBandAllChoice,
            R.id.cinematicHomeChoice, R.id.cinematicDotsChoice, R.id.cinematicWhiteFocusChoice,
            R.id.ambientGlowChoice, R.id.appGlowChoice, R.id.fadeRowsChoice, R.id.uniformTilesChoice,
            R.id.wallpaperTrueColorChoice, R.id.wallpaperCaptionsChoice, R.id.reducedMotionChoice,
            R.id.topMenuChoice, R.id.launcherSettingsButtonChoice, R.id.reorderButtonChoice,
            R.id.showNetworkPillChoice, R.id.showInputsPillChoice, R.id.iconOnlyButtonsChoice,
            R.id.iconOnlyInputsChoice, R.id.vpnPillChoice, R.id.iconOnlyVpnChoice,
            R.id.showGreetingChoice, R.id.headerWhiteChoice, R.id.dateBelowClockChoice,
            R.id.shamsiDateChoice, R.id.lunarDateChoice, R.id.showStatsChoice,
            R.id.clockWallpaperChoice, R.id.clockGlowChoice,
            R.id.tileMenuChoice, R.id.inputsConnectedOnlyChoice, R.id.coocaaSourcesChoice,
            R.id.moduleCardsChoice, R.id.showHomeSettingsShortcutChoice, R.id.openOnStartToggleChoice
    };
    private static final int[] TOGGLE_LABELS = {
            R.string.wifi_status_label, R.string.dock_glass_label, R.string.names_label, R.string.overshoot_label,
            R.string.dock_row_label, R.string.dock_band_label, R.string.dock_title_label, R.string.dock_even_padding_label,
            R.string.fill_dock_label, R.string.single_row_label, R.string.dock_band_all_label,
            R.string.cinematic_home_label, R.string.cinematic_dots_label, R.string.cinematic_white_focus_label,
            R.string.ambient_glow_label, R.string.app_glow_label, R.string.fade_rows_label, R.string.uniform_tiles_label,
            R.string.wallpaper_true_color_label, R.string.wallpaper_captions_label, R.string.reduced_motion_label,
            R.string.top_menu_label, R.string.launcher_settings_button_label, R.string.reorder_button_label,
            R.string.show_network_pill_label, R.string.show_inputs_pill_label, R.string.icon_only_buttons_label,
            R.string.icon_only_inputs_label, R.string.vpn_pill_label, R.string.icon_only_vpn_label,
            R.string.show_greeting_label, R.string.header_white_label, R.string.date_below_clock_label,
            R.string.shamsi_date_label, R.string.lunar_date_label, R.string.show_stats_label,
            R.string.clock_wallpaper_label, R.string.clock_glow_label,
            R.string.tile_menu_label, R.string.inputs_connected_only_label, R.string.coocaa_sources_label,
            R.string.module_cards_label, R.string.show_home_settings_shortcut_label, R.string.open_on_start_toggle_label
    };
    private final TextView[] toggleChoices = new TextView[TOGGLE_IDS.length];
    private final boolean[] toggleOn = new boolean[TOGGLE_IDS.length];

    private void loadToggles(Config c) {
        toggleOn[0] = c.wifiStatusColor;
        toggleOn[1] = c.dockGlass;
        toggleOn[2] = c.alwaysShowNames;
        toggleOn[3] = c.focusOvershoot;
        toggleOn[4] = c.dockRow;
        toggleOn[5] = c.dockBand;
        toggleOn[6] = c.dockTitle;
        toggleOn[7] = c.dockEvenPadding;
        toggleOn[8] = c.fillDock;
        toggleOn[9] = choices.getBoolean("single_row", c.singleRow);
        toggleOn[10] = c.dockBandAll;
        toggleOn[11] = c.cinematicHome;
        toggleOn[12] = c.cinematicDots;
        toggleOn[13] = c.cinematicWhiteFocus;
        toggleOn[14] = c.ambientGlow;
        toggleOn[15] = c.appGlow;
        toggleOn[16] = c.fadeRows;
        toggleOn[17] = c.uniformTiles;
        toggleOn[18] = c.wallpaperTrueColor;
        toggleOn[19] = c.wallpaperCaptions;
        toggleOn[20] = c.reducedMotion;
        toggleOn[21] = c.topMenu;
        toggleOn[22] = c.launcherSettingsButton;
        toggleOn[23] = c.reorderButton;
        toggleOn[24] = c.showNetworkPill;
        toggleOn[25] = c.showInputsPill;
        toggleOn[26] = c.iconOnlyButtons;
        toggleOn[27] = c.iconOnlyInputs;
        toggleOn[28] = c.vpnPill;
        toggleOn[29] = c.iconOnlyVpn;
        toggleOn[30] = c.showGreeting;
        toggleOn[31] = c.headerWhite;
        toggleOn[32] = c.dateBelowClock;
        toggleOn[33] = c.shamsiDate;
        toggleOn[34] = c.lunarDate;
        toggleOn[35] = c.showStats;
        toggleOn[36] = c.clockWallpaper;
        toggleOn[37] = c.clockGlow;
        toggleOn[38] = c.tileMenu;
        toggleOn[39] = c.inputsConnectedOnly;
        toggleOn[40] = c.coocaaSources;
        toggleOn[41] = c.moduleCards;
        toggleOn[42] = c.showHomeSettingsShortcut;
        toggleOn[43] = c.openOnStartChoice;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_launcher_settings);
        choices = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        Config config = Config.load(this);
        reorderChoice = findViewById(R.id.reorderChoice);
        reorderChoice.setVisibility(config.launcherSettingsButton ? View.VISIBLE : View.GONE);
        reorderChoice.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_REORDER, true));
            finish();
        });
        columns = config.columns;
        rowsBelowDock = config.rowsBelowDock;
        dockOpacity = config.dockOpacity;
        animations = config.animations;
        advancedEffects = config.animations && config.advancedEffects;
        loadToggles(config);

        int[] ids = {R.id.columns4, R.id.columns5, R.id.columns6, R.id.columns7};
        for (int i = 0; i < ids.length; i++) {
            final int value = i + 4;
            columnChoices[i] = findViewById(ids[i]);
            columnChoices[i].setOnClickListener(v -> {
                if (columns == value) return;
                columns = value;
                choices.edit().putInt("columns", value).apply();
                changed();
                refresh();
            });
        }
        int[] rowIds = {R.id.rows0, R.id.rows1, R.id.rows2, R.id.rows3};
        for (int i = 0; i < rowIds.length; i++) {
            final int value = i;
            rowChoices[i] = findViewById(rowIds[i]);
            rowChoices[i].setOnClickListener(v -> {
                if (rowsBelowDock == value) return;
                rowsBelowDock = value;
                choices.edit().putInt(Config.ROWS_BELOW_DOCK_KEY, value).apply();
                changed();
                refresh();
            });
        }
        int[] dockIds = {R.id.dock100, R.id.dock70, R.id.dock50, R.id.dock30};
        for (int i = 0; i < dockIds.length; i++) {
            final int value = DOCK_OPACITIES[i];
            dockChoices[i] = findViewById(dockIds[i]);
            dockChoices[i].setOnClickListener(v -> {
                if (dockOpacity == value) return;
                dockOpacity = value;
                choices.edit().putInt(Config.DOCK_OPACITY_KEY, value).apply();
                changed();
                refresh();
            });
        }
        // Mirror text alignment without globally opting the launcher into RTL. Numeric rows
        // still need explicit ordering in Android's no-supportsRtl compatibility mode.
        if (getResources().getBoolean(R.bool.rtl_menu_bar))
            findViewById(android.R.id.content).setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (getResources().getBoolean(R.bool.rtl_app_grid)) {
            reverseRow(columnChoices);
            reverseRow(rowChoices);
            reverseRow(dockChoices);
        }
        wallpaperChoice = findViewById(R.id.wallpaperChoice);
        wallpaperChoice.setOnClickListener(v -> showWallpaperPicker());
        animationsChoice = findViewById(R.id.animationsChoice);
        animationsChoice.setOnClickListener(v -> {
            animations = !animations;
            if (!animations) advancedEffects = false;
            choices.edit().putBoolean("animations", animations)
                    .putBoolean("advanced_effects", advancedEffects).apply();
            changed();
            refresh();
        });
        advancedChoice = findViewById(R.id.advancedChoice);
        advancedChoice.setOnClickListener(v -> {
            if (advancedEffects) {
                setAdvanced(false);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.advanced_effects)
                        .setMessage(R.string.advanced_effects_warning)
                        .setPositiveButton(R.string.enable, (dialog, which) -> setAdvanced(true))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        for (int i = 0; i < TOGGLE_IDS.length; i++) {
            final int index = i;
            toggleChoices[i] = findViewById(TOGGLE_IDS[i]);
            toggleChoices[i].setOnClickListener(v -> {
                toggleOn[index] = !toggleOn[index];
                SharedPreferences.Editor editor = choices.edit();
                for (String key : TOGGLE_KEYS[index]) editor.putBoolean(key, toggleOn[index]);
                editor.apply();
                changed();
                refresh();
            });
        }
        openOnStartChoice = findViewById(R.id.openOnStartChoice);
        openOnStartChoice.setOnClickListener(v -> showOpenOnStartPicker());

        moreOptionsContainer = findViewById(R.id.moreOptionsContainer);
        moreOptionsChoice = findViewById(R.id.moreOptionsChoice);
        moreOptionsChoice.setOnClickListener(v -> {
            boolean open = moreOptionsContainer.getVisibility() != View.VISIBLE;
            moreOptionsContainer.setVisibility(open ? View.VISIBLE : View.GONE);
            refresh();
            if (open) {
                toggleChoices[4].requestFocus();
            } else {
                moreOptionsChoice.requestFocus();
            }
        });

        followThemeChoice = findViewById(R.id.followThemeChoice);
        followThemeChoice.setOnClickListener(v -> {
            if (!hasOnTvChoices()) return;
            choices.edit().remove("columns").remove(Config.ROWS_BELOW_DOCK_KEY).remove("animations")
                    .remove("advanced_effects").remove(Config.WALLPAPER_CHOICE_KEY)
                    .remove(Config.DOCK_OPACITY_KEY).remove(OpenOnStart.KEY).commit();
            SharedPreferences.Editor reset = choices.edit();
            for (String[] keys : TOGGLE_KEYS) for (String key : keys) reset.remove(key);
            reset.commit();
            Config current = Config.load(this);
            loadToggles(current);
            columns = current.columns;
            rowsBelowDock = current.rowsBelowDock;
            dockOpacity = current.dockOpacity;
            animations = current.animations;
            advancedEffects = current.animations && current.advancedEffects;
            changed();
            refresh();
        });
        findViewById(R.id.defaultHomeChoice).setOnClickListener(v -> {
            Toast.makeText(this, R.string.home_settings_help, Toast.LENGTH_LONG).show();
            open(new Intent(Settings.ACTION_HOME_SETTINGS));
        });
        findViewById(R.id.tvSettingsChoice).setOnClickListener(v ->
                open(SystemSettings.intent(this, Settings.ACTION_SETTINGS)));
        refresh();
        if (config.launcherSettingsButton) reorderChoice.requestFocus();
        else columnChoices[Math.max(0, Math.min(columns - 4, 3))].requestFocus();
    }

    private void reverseRow(TextView[] views) {
        LinearLayout row = (LinearLayout) views[0].getParent();
        row.removeAllViews();
        for (int i = views.length - 1; i >= 0; i--) row.addView(views[i]);
    }

    private void showWallpaperPicker() {
        File dir = Config.dir(this);
        List<File> images = new java.util.ArrayList<>(WallpaperRotation.images(
                dir == null ? null : new File(dir, "wallpapers")));
        // A theme with a single wallpaper.jpg offers that picture too (Config maps the choice back to it).
        File single = dir == null ? null : new File(dir, Config.WALLPAPER_NAME);
        if (single != null && single.isFile()) images.add(0, single);
        String[] labels = new String[images.size() + 1];
        for (int i = 0; i < images.size(); i++) labels[i] = images.get(i).getName();
        labels[images.size()] = getString(R.string.wallpaper_none);
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        int checked = "none".equals(current) ? images.size()
                : indexOf(images, current);
        new AlertDialog.Builder(this)
                .setTitle(R.string.wallpaper_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == images.size()) {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, "none").apply();
                    } else {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY,
                                images.get(which).getName()).apply();
                    }
                    changed();
                    Toast.makeText(this, R.string.wallpaper_applied, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNeutralButton(R.string.wallpaper_theme_default, (dialog, which) -> {
                    choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Off, then every app with a TV entry point by name. Saved at once; it is read on the next boot. */
    private void showOpenOnStartPicker() {
        PackageManager pm = getPackageManager();
        List<String[]> apps = new ArrayList<>();
        Intent tv = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(tv, 0)) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            boolean seen = false;
            for (String[] a : apps) seen |= a[1].equals(pkg);
            if (!seen) apps.add(new String[]{r.loadLabel(pm).toString(), pkg});
        }
        Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        String[] labels = new String[apps.size() + 1];
        labels[0] = getString(R.string.off);
        String current = choices.getString(OpenOnStart.KEY, "");
        int checked = 0;
        for (int i = 0; i < apps.size(); i++) {
            labels[i + 1] = apps.get(i)[0];
            if (apps.get(i)[1].equals(current)) checked = i + 1;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.open_on_start_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) choices.edit().remove(OpenOnStart.KEY).apply();
                    else {
                        OpenOnStart.skipThisBoot(this);
                        choices.edit().putString(OpenOnStart.KEY, apps.get(which - 1)[1]).apply();
                    }
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** The picked app's name, its package when it is gone, or Off. */
    private String openOnStartLabel() {
        String pkg = choices.getString(OpenOnStart.KEY, "");
        if (pkg.isEmpty()) return getString(R.string.off);
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    private static int indexOf(List<File> images, String name) {
        if (name == null) return -1;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    /**
     * A choice was saved. Home rebuilds when it next resumes because the change count moved, whether these
     * settings are left with Back or with the remote's Home button (which cancels the activity result).
     */
    private void changed() {
        setResult(RESULT_OK);
        choices.edit().putLong(Config.SETTINGS_REVISION_KEY,
                choices.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
    }

    private void setAdvanced(boolean enabled) {
        advancedEffects = enabled;
        choices.edit().putBoolean("advanced_effects", enabled).apply();
        changed();
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < columnChoices.length; i++) {
            columnChoices[i].setSelected(columns == i + 4);
        }
        for (int i = 0; i < rowChoices.length; i++) {
            rowChoices[i].setSelected(rowsBelowDock == i);
        }
        for (int i = 0; i < dockChoices.length; i++) {
            dockChoices[i].setSelected(dockOpacity == DOCK_OPACITIES[i]);
        }
        animationsChoice.setText(getString(R.string.animations_state,
                getString(animations ? R.string.on : R.string.off)));
        advancedChoice.setText(getString(R.string.advanced_effects_state,
                getString(advancedEffects ? R.string.on : R.string.off)));
        for (int i = 0; i < toggleChoices.length; i++) {
            toggleChoices[i].setText(getString(R.string.toggle_state, getString(TOGGLE_LABELS[i]),
                    getString(toggleOn[i] ? R.string.on : R.string.off)));
        }
        advancedChoice.setEnabled(animations);
        advancedChoice.setAlpha(animations ? 1f : 0.5f);
        String wallpaperCurrent = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        wallpaperChoice.setText(getString(R.string.wallpaper_state,
                "none".equals(wallpaperCurrent) ? getString(R.string.wallpaper_none)
                        : wallpaperCurrent != null ? wallpaperCurrent
                        : getString(R.string.wallpaper_theme_default)));
        reorderChoice.setVisibility(toggleOn[22] ? View.VISIBLE : View.GONE);
        openOnStartChoice.setVisibility(toggleOn[43] ? View.VISIBLE : View.GONE);
        openOnStartChoice.setText(getString(R.string.open_on_start_state, openOnStartLabel()));
        boolean moreOpen = moreOptionsContainer.getVisibility() == View.VISIBLE;
        moreOptionsChoice.setText(moreOpen ? R.string.more_options_hide : R.string.more_options);
        moreOptionsChoice.setSelected(moreOpen);
        followThemeChoice.setSelected(!hasOnTvChoices());
    }

    private boolean hasOnTvChoices() {
        for (String[] keys : TOGGLE_KEYS) for (String key : keys) if (choices.contains(key)) return true;
        return choices.contains("columns") || choices.contains(Config.ROWS_BELOW_DOCK_KEY)
                || choices.contains("animations") || choices.contains("advanced_effects")
                || choices.contains(Config.WALLPAPER_CHOICE_KEY) || choices.contains(Config.DOCK_OPACITY_KEY)
                || choices.contains(OpenOnStart.KEY);
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        }
    }
}
