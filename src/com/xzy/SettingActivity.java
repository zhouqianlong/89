package com.xzy;

import com.ramy.minervue.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * ����
 * @author Ф����
 *
 */
public class SettingActivity extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
	}
}
