package com.lpi.sonometre;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * TODO: document your custom view class.
 */
public class GraphCustomView extends View
{
	private int _NbMaxValeurs ;
	private static final String TAG = "GraphCustomView";
	private int _couleurFond, _couleurAxe, _couleurGrapheMax, _couleurGrapheMin, _couleurTexte;
	private float _tailleFonte;
	private Paint _paintAxe, _paintGraphe;
	private float[] _points = new float[0];
	private final ArrayList<Float> _valeurs = new ArrayList<>();
	private int paddingLeft;
	private int paddingTop;
	private int paddingRight;
	private int paddingBottom;
	private int contentWidth;
	private int contentHeight;
	private TextPaint _paintTexte;
	private int _idResMaxDb, _idResMinDb, _idResMoyDb;
	private String _sMaxDB = "";
	private String _sMinDB = "";
	private String _sMoyDB = "";
	private float _max;
	private float _min;
	private float _moy;
	private float _epaisseurGraphe;

	public GraphCustomView(Context context)
	{
		super(context);
		init(null, 0);
	}

	public GraphCustomView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public GraphCustomView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle)
	{
		// Load attributes
		try
		{
			final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GraphCustomView, defStyle, 0);
			_couleurFond = a.getColor(R.styleable.GraphCustomView_CouleurFond, Color.WHITE);
			_couleurAxe = a.getColor(R.styleable.GraphCustomView_CouleurAxe, Color.BLACK);
			_epaisseurGraphe = a.getFloat(R.styleable.GraphCustomView_EpaisseurGraphe, 3);
			_couleurGrapheMax = a.getColor(R.styleable.GraphCustomView_CouleurGraphMax, Color.RED);
			_couleurGrapheMin = a.getColor(R.styleable.GraphCustomView_CouleurGraphMin, Color.GREEN);
			_couleurTexte = a.getColor(R.styleable.GraphCustomView_CouleurTexte, Color.BLACK);
			_tailleFonte = a.getDimension(R.styleable.GraphCustomView_TailleFonte, 10);
			_idResMaxDb = a.getInt(R.styleable.GraphCustomView_IdStringMaxDB, R.string.formatMaxDB);
			_idResMoyDb = a.getInt(R.styleable.GraphCustomView_IdStringMoyDB, R.string.formatMoyDB);
			_idResMinDb = a.getInt(R.styleable.GraphCustomView_IdStringMinDB, R.string.formatMinDB);
			_NbMaxValeurs = a.getInt(R.styleable.GraphCustomView_NbMaxValeurs, 1000);
			a.recycle();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// Update TextPaint and text measurements from attributes
		creerAttributs();

		// Pour tests seulement
		if ( isInEditMode())
		{
			Random r = new Random(System.currentTimeMillis());
			for (int i = 0; i < _NbMaxValeurs; i++)
				_valeurs.add(new Float(35 + r.nextInt(20)));
			ajouteValeur(35 + r.nextInt(20));
		}
	}


	private void creerAttributs()
	{
		_paintAxe = new Paint();
		_paintAxe.setStyle(Paint.Style.STROKE);
		_paintAxe.setAntiAlias(true);
		_paintAxe.setStrokeWidth(1);
		_paintAxe.setColor(_couleurAxe);

		_paintGraphe = new Paint();
		_paintGraphe.setStyle(Paint.Style.STROKE);
		_paintGraphe.setAntiAlias(true);
		_paintGraphe.setStrokeWidth(_epaisseurGraphe);
		Shader shader = new LinearGradient(0, 0, 0, 100 /*canvas height*/, _couleurGrapheMin,  _couleurGrapheMax, Shader.TileMode.CLAMP /*or REPEAT*/);
		_paintGraphe.setShader(shader);

		_paintTexte = new TextPaint();
		_paintTexte.setAntiAlias(true);
		_paintTexte.setTextSize(_tailleFonte);
		_paintTexte.setColor(_couleurTexte);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.drawColor(_couleurFond);

		// Dessine l'axe
		if (_valeurs.size() > 0)
		{
			canvas.save();
			canvas.scale(1, -1);
			canvas.translate(0, -getHeight());
			canvas.drawLine(paddingLeft, _max, paddingLeft + contentWidth,  _max, _paintAxe);
			canvas.drawLine(paddingLeft, _moy, paddingLeft + contentWidth,  _moy, _paintAxe);
			canvas.drawLine(paddingLeft, _min, paddingLeft + contentWidth,  _min, _paintAxe);

			canvas.drawLines(_points, _paintGraphe);


			canvas.restore();

			Rect bounds = new Rect();
			_paintTexte.getTextBounds(_sMaxDB, 0, _sMaxDB.length(), bounds);
			int height = bounds.height();
			canvas.drawText(_sMaxDB, paddingLeft, paddingTop + height, _paintTexte);
			canvas.drawText(_sMinDB, paddingLeft, paddingTop + contentHeight, _paintTexte);
			canvas.drawText(_sMoyDB, paddingLeft, paddingTop + (contentHeight / 2.0f) + height, _paintTexte);
		}
	}

	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		paddingLeft = getPaddingLeft();
		paddingTop = getPaddingTop();
		paddingRight = getPaddingRight();
		paddingBottom = getPaddingBottom();

		contentWidth = getWidth() - paddingLeft - paddingRight;
		contentHeight = getHeight() - paddingTop - paddingBottom;

		Shader shader = new LinearGradient(0, 0, 0, getHeight() /*canvas height*/, _couleurGrapheMin,  _couleurGrapheMax, Shader.TileMode.CLAMP /*or REPEAT*/);
		_paintGraphe.setShader(shader);
		recalculePoints();
	}

	public void ajouteValeur(float valeur)
	{
		if (valeur > Float.MAX_VALUE || (int)valeur <= 0/*Float.MIN_VALUE*/)
			return;

		Log.d(TAG, "Ajoute valeur " + valeur);
		if (_valeurs.size() >= _NbMaxValeurs)
			_valeurs.remove(0);

		_valeurs.add(valeur);
		recalculePoints();
	}

	private void recalculePoints()
	{
		// Trouver max
		_max = Float.MIN_VALUE;
		_min = Float.MAX_VALUE;
		_moy = 0;

		for (Float f : _valeurs)
		{
			if (f.floatValue() > _max)
				_max = f.floatValue();

			if (f.floatValue() < _min)
				_min = f.floatValue();

			_moy += f.floatValue();
		}
		if (_valeurs.size() > 0)
			_moy /= _valeurs.size();

		_sMaxDB = getContext().getString(_idResMaxDb, (int) _max);
		_sMinDB = getContext().getString(_idResMinDb, (int) _min);
		_sMoyDB = getContext().getString(_idResMoyDb, (int) _moy);

		_max++;
		_min--;
		float range = _max - _min;

		// Refaire le tableau de points
		_points = new float[_valeurs.size() * 4];
		for (int i = 1; i < _valeurs.size(); i++)
		{
			_points[i * 4] = paddingLeft + (i * contentWidth) / (float) _valeurs.size();
			_points[i * 4 + 1] = paddingBottom + ((_valeurs.get(i) - _min) / range) * contentHeight;
			_points[i * 4 + 2] = paddingLeft + ((i - 1) * contentWidth) / (float) _valeurs.size();
			_points[i * 4 + 3] = paddingBottom + ((_valeurs.get(i - 1) - _min) / range) * contentHeight;
		}
		_max = paddingBottom + ((_max - _min) / range) * contentHeight;
		_moy = paddingBottom + ((_moy - _min) / range) * contentHeight;
		_min = paddingBottom;
		invalidate();
	}

}