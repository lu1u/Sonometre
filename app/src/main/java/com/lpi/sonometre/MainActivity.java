package com.lpi.sonometre;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.lpi.sonometre.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
{

	private Sonometre _sonometre;
	private JaugeAAiguille _jauge;
	private GraphCustomView _graphe;
	private String[] _nomsNiveaux;
	private int[] _niveauxSonores;

	@Override protected void onResume()
	{
		super.onResume();
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
		}
		else
		{

			_sonometre.start(this, level ->
			{
				_jauge.setValeur((float) level);
				_graphe.ajouteValeur((float) level);
				_jauge.setText1((int) Math.round(level) + " dB");
				_jauge.setText2(getNomNiveau((int) level));
			});
		}
	}

	private String getNomNiveau(final int level)
	{
		for (int i = _niveauxSonores.length - 1; i >= 0; i--)
			if (level > _niveauxSonores[i])
				return _nomsNiveaux[i];

		return _nomsNiveaux[0];
	}

	@Override protected void onPause()
	{
		super.onPause();
		if (_sonometre != null)
			_sonometre.stop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.toolbar);

		_sonometre = new Sonometre();
		_jauge = findViewById(R.id.jauge);
		_graphe = findViewById(R.id.graphCustomView);

		Resources res = getResources();
		_nomsNiveaux = res.getStringArray(R.array.NomsNiveaux);
		_niveauxSonores = res.getIntArray(R.array.NiveauxSonores);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_settings)
		{
			ParametresActivity.start(this);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}