package io.github.nitsuya.aa.display.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.duzhaokun123.template.bases.BaseActivity
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.databinding.ActivitySettingsBinding
import io.github.nitsuya.aa.display.util.AADisplayConfig


class SettingsActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::class.java) {
    override fun initViews() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fl_root, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            requireContext().theme.applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true)
            preferenceManager.apply {
                sharedPreferencesName = AADisplayConfig.ConfigName
                sharedPreferencesMode = MODE_WORLD_READABLE
            }
            setPreferencesFromResource(R.xml.pref_aadisplay_config, rootKey)
        }
    }



}