package com.lpi.sonometre;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * TODO: document your custom view class.
 */
public class JaugeAAiguille extends View
{
	private static final int ANGLE_DEPART = -180;
	private static final int ANGLE_TOTAL = 180;
	private static final float TO_RADIAN = (float)(Math.PI / 180.0) ;
	float _minimum,   // Valeur min de la jauge
			_maximum,   // Valeur max de la jauge
			_valeur,    // Valeur reelle de la jauge
			_valeurCible, // Valeur cible pendant les animations
			_valeurDepart, // Valeur de depart de l'animation (ancienne valeur)
			_fractionAnimation; // Fraction de l'animation en cours (0..1)
	ValueAnimator _animator;
	int _couleurFondDebut;
	int _couleurFondFin ;
	private Paint _paintFond;
	private Paint _paintAiguille;
	private TextPaint _textPaint;
	private String _texte;
	float _tailleCentre = 12;
	final RectF r = new RectF();          // Pour eviter d'allouer un objet dans onDraw
	final Rect rText = new Rect();          // Pour eviter d'allouer un objet dans onDraw


	public JaugeAAiguille(Context context)
	{
		super(context);
		init(null, 0);
	}

	public JaugeAAiguille(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}

	public JaugeAAiguille(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	/***********************************************************************************************
	 * Initialisation du controle, lecture des attributs Styleable
	 * @param attrs
	 * @param defStyle
	 */
	private void init(AttributeSet attrs, int defStyle)
	{
		// Load attributes
		final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.JaugeAAiguille, defStyle, 0);

		_minimum = a.getFloat(R.styleable.JaugeAAiguille_CircMinimum, 0);
		_maximum = a.getFloat(R.styleable.JaugeAAiguille_CircMaximum, 100);
		_valeur = a.getFloat(R.styleable.JaugeAAiguille_CircValeur, 60);
		_valeurDepart = _valeur;


		// Style graphique du fond de la jauge
		{
			_couleurFondDebut = a.getColor(R.styleable.JaugeAAiguille_CircCouleurFondDebut, Color.GREEN);
			_couleurFondFin = a.getColor(R.styleable.JaugeAAiguille_CircCouleurFondFin, Color.RED);
			float largeurFond = a.getDimension(R.styleable.JaugeAAiguille_CircLargeurFond, 4);
			int style = a.getInt(R.styleable.JaugeAAiguille_CircBoutsFond, 0);
			Paint.Cap bouts;
			switch (style)
			{
				case 0:
					bouts = Paint.Cap.ROUND;
					break;
				case 1:
					bouts = Paint.Cap.SQUARE;
					break;
				default:
					bouts = Paint.Cap.BUTT;
			}

			_paintFond = new Paint();

			_paintFond.setStrokeWidth(largeurFond);
			_paintFond.setStrokeCap(bouts);
			_paintFond.setStyle(Paint.Style.STROKE);
			_paintFond.setShadowLayer(12, 2, 2, Color.argb(128, 0, 0, 0));
		}

		// Style graphique de l'aiguille
		{
			int _couleurMax = a.getColor(R.styleable.JaugeAAiguille_CircCouleurAiguille, Color.RED);
			float _largeurMax = a.getDimension(R.styleable.JaugeAAiguille_CircLargeurAiguille, 4);
			int style = a.getInt(R.styleable.JaugeAAiguille_CircBoutsAiguille, 0);
			_tailleCentre = a.getDimension(R.styleable.JaugeAAiguille_CircCentreAiguille, 36);
			Paint.Cap bouts;
			switch (style)
			{
				case 0:
					bouts = Paint.Cap.ROUND;
					break;
				case 1:
					bouts = Paint.Cap.SQUARE;
					break;
				default:
					bouts = Paint.Cap.BUTT;
			}

			_paintAiguille = new Paint();
			_paintAiguille.setColor(_couleurMax);
			_paintAiguille.setStrokeWidth(_largeurMax);
			_paintAiguille.setStrokeCap(bouts);
			_paintAiguille.setStyle(Paint.Style.FILL_AND_STROKE);
			_paintAiguille.setShadowLayer(12, 2, 2, Color.argb(128, 0, 0, 0));
		}

		// Texte
		{
			float taille = a.getDimension(R.styleable.JaugeAAiguille_CircTextSize, 16);
			int couleur = a.getColor(R.styleable.JaugeAAiguille_CircTextCouleur, Color.BLACK);
			_texte = a.getString(R.styleable.JaugeAAiguille_CircTexte);

			_textPaint = new TextPaint();
			_textPaint.setColor(couleur);
			_textPaint.setTextSize(taille);
		}
		a.recycle();
	}



	/***********************************************************************************************
	 * Dessiner le controle
	 * @param canvas
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);
		int largeurBarre = (int) _paintFond.getStrokeWidth();
		int paddingLeft = getPaddingLeft() + largeurBarre;
		int paddingTop = getPaddingTop() + largeurBarre;
		int paddingRight = getPaddingRight() + largeurBarre;
		int paddingBottom = getPaddingBottom() + largeurBarre;

		int contentWidth = getWidth() - paddingLeft - paddingRight;
		int contentHeight = getHeight() - paddingTop - paddingBottom;

		float taille = (Math.min(contentHeight, contentWidth) * 0.5f);
		float cx = paddingLeft + (contentWidth * 0.5f);
		float cy = paddingTop + (contentHeight * 0.5f);

		r.set(cx - taille, cy - taille, cx + taille, cy + taille);

		// Fond de la jauge
		int[] colors = {_couleurFondDebut, _couleurFondFin};
		float[] positions = {0.5f, 1.0f};//{(_minimum- ANGLE_DEPART)*TO_RADIAN,(_maximum - ANGLE_DEPART)*TO_RADIAN};
		_paintFond.setShader(new SweepGradient(r.centerX(), r.centerY(), colors , positions));
		canvas.drawArc(r, -ANGLE_DEPART, ANGLE_TOTAL, false, _paintFond);

		// Jauge
		final float valeurADessiner = _valeurDepart + (_valeurCible-_valeurDepart) * _fractionAnimation;
		final float arc = (valeurADessiner /  (_maximum - _minimum) * ANGLE_TOTAL) - ANGLE_DEPART;

		float xAiguille = (float)Math.cos( arc * TO_RADIAN ) * taille;
		float yAiguille = (float)Math.sin( arc * TO_RADIAN ) * taille ;
		canvas.drawLine(r.centerX(), r.centerY(), r.centerX()+xAiguille, r.centerY() + yAiguille, _paintAiguille);
		canvas.drawOval(r.centerX()-_tailleCentre, r.centerY()-_tailleCentre, r.centerX()+_tailleCentre, r.centerY()+_tailleCentre, _paintAiguille);

		// Texte
		_textPaint.getTextBounds(_texte, 0, _texte.length(), rText);
		canvas.drawText(_texte, this.getLeft() + (this.getWidth() - rText.width())/ 2.0f, this.getBottom() - rText.height(), _textPaint);

	}


	/***********************************************************************************************
	 * Modifie la valeur affichee, avec une jolie animation
	 * @param value
	 */
	public void setValeur(float value)
	{
		_valeurDepart = _valeurCible;
		if (value < _minimum)
			value = _minimum;
		if (value > _maximum)
			value = _maximum;

		_valeurCible = value;
		animer();
	}

	public void setText(@NonNull final String t)
	{
		_texte = t;
		invalidate();
	}

	/***
	 * Fait une animation a chaque changement des valeurs
	 */
	private void animer()
	{
		if (_animator != null)
		{
			_animator.cancel();

		}

		_animator = ValueAnimator.ofFloat(0, 1);
		_animator.setDuration(Math.abs(_valeurCible - _valeur) > (_maximum / 2) ? 200 : 100);
		_animator.addUpdateListener(animation ->
		{
			try
			{
				_fractionAnimation = (float)animation.getAnimatedValue();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			invalidate();
		});

		_animator.addListener(new Animator.AnimatorListener()
		{
			@Override public void onAnimationStart(final Animator animator)
			{

			}

			@Override public void onAnimationEnd(final Animator animator)
			{
				_valeur = _valeurCible;
				_fractionAnimation = 1.0f;
				_animator = null;
				invalidate();
			}

			@Override public void onAnimationCancel(final Animator animator)
			{

			}

			@Override public void onAnimationRepeat(final Animator animator)
			{

			}
		});
		_animator.start();
	}


}