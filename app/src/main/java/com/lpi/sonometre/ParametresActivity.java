package com.lpi.sonometre;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ParametresActivity extends AppCompatActivity
{

	public static void start(@NonNull final Activity context)
	{
		context.startActivity( new Intent(context, ParametresActivity.class));
	}

	@Override protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parametres);


		// Controles de la fenetre
		final EditText etCalibrage = findViewById(R.id.editCalibrage);
		final EditText etDelai = findViewById(R.id.editDelai);

		final Button btnOk = findViewById(R.id.buttonOk);
		final Button btnAnnuler = findViewById(R.id.buttonAnnuler);

		// Initialiser les controles
		final Preferences p = Preferences.getInstance(this);
		etCalibrage.setText(String.format("%.1f", p.getCalibrage()));
		etDelai.setText(""+ p.getDelai());

		// Gestionnaire du bouton OK
		// Controler les valeurs, enregistrer les preferences
		btnOk.setOnClickListener(view ->
		{
			// Sauver les valeurs
			String calibrage = etCalibrage.getText().toString();
			p.setCalibrage(getDouble(calibrage));

			String delai = etDelai.getText().toString();
			p.setDelai(getInt(delai));
			ParametresActivity.this.finish();
		});

		// Gestionnaire du bouton Annuler: juste fermer la fenetre
		btnAnnuler.setOnClickListener(view -> ParametresActivity.this.finish());

	}


	private static double getDouble( @NonNull String value)
	{
		try
		{
			return Double.parseDouble(value);
		}
		catch(Exception e)
		{
			value = value.replace(",",".");
			return Double.parseDouble(value);
		}
	}



	private static int getInt( @NonNull String value)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch(Exception e)
		{
			return 0;
		}
	}
}