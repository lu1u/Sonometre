package com.lpi.sonometre;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

/***
 * Classe pour ecouter le niveau sonore
 */
class Sonometre
{
	// Listener pour recevoir les niveaux sonores
	public interface SoundLevelListener
	{
		void soundLevel(double level);
	}

	Preferences _preferences;
	private Thread thread;
	private boolean _continuer = true;
	SoundLevelListener _listener;
	private static double mEMA = 0.0;
	static final private double EMA_FILTER = 0.6;

	final Handler handler = new Handler()
	{
		@Override
		public void handleMessage(@NonNull Message msg)
		{
			super.handleMessage(msg);
			if (_listener != null)
			{
				double v = (Double) msg.obj;
				_listener.soundLevel(v);
			}
		}
	};

	private MediaRecorder _recorder = null;

	private void stopListenAudio()
	{
		if (thread != null)
		{
			_continuer = false;
			thread = null;
		}
	}

	private void startListenAudio(@NonNull Context context)
	{
		_continuer = true;
		thread = new Thread(() ->
		{
			while (_continuer)
			{
				try
				{
					double decibels = soundDb(getAmplitudeEMA());  //Get the sound pressure value
					Message message = new Message();
					message.what = 1;
					message.obj = Double.valueOf(decibels);
					handler.sendMessage(message);

					Thread.sleep(_preferences.getDelai());
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	public void start(@NonNull Context context, @NonNull SoundLevelListener listener)
	{
		if (_recorder == null)
		{
			try
			{
				_preferences = Preferences.getInstance(context);
				_recorder = new MediaRecorder();
				_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				_recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				//_recorder.setOutputFile("/dev/null");
				_recorder.setOutputFile(context.getExternalCacheDir() + "/test.3gp");
				_recorder.prepare();
				Thread.sleep(1000); // Bug qui provoque une exception "Start failed"
				_recorder.start();

				_listener = listener;
				// Demarrer le 'listener'
				startListenAudio(context);

			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void stop()
	{
		if (_recorder != null)
		{
			stopListenAudio();
			_recorder.stop();
			_recorder.release();
			_recorder = null;
		}
	}

	public double soundDb(double ampl)
	{
		return 20 * Math.log10(ampl / _preferences.getCalibrage());
	}

	public double getAmplitude()
	{
		if (_recorder != null)
			return (_recorder.getMaxAmplitude());
		else
			return 0;

	}

	public double getAmplitudeEMA()
	{
		double amp = getAmplitude();
		mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
		return mEMA;
	}
}
