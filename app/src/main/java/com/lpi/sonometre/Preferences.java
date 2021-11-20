package com.lpi.sonometre;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

class Preferences
{
	public static final String PREFERENCES = Preferences.class.getName();
	public static final String PREF_CALIBRAGE = "calibrage";
	public static final String PREF_DELAI = "delai";
	private static Preferences _instance;
	@NonNull final SharedPreferences settings;
	@NonNull final SharedPreferences.Editor editor;

	// Parametres
	double _calibrage;
	int _delai;

	public static synchronized Preferences getInstance(@NonNull final Context context)
	{
		if (_instance == null)
			_instance = new Preferences(context);

		return _instance;

	}

	private Preferences(@NonNull final Context context)
	{
		settings = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		editor = settings.edit();

		_calibrage = settings.getFloat(PREF_CALIBRAGE, 1.1f);
		_delai = settings.getInt(PREF_DELAI, 200);
	}

	public double getCalibrage()
	{
		return _calibrage;
	}

	public void setCalibrage(double v)
	{
		_calibrage = v;
		editor.putFloat(PREF_CALIBRAGE, (float)v);
		editor.apply();
	}

	public int getDelai()
	{
		return _delai;
	}

	public void setDelai(int v)
	{
		_delai = v;
		editor.putInt(PREF_DELAI,v);
		editor.apply();
	}
}
