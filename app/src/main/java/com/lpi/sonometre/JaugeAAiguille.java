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
	private static final float TO_RADIAN = (float) (Math.PI / 180.0);
	float _minimum,   // Valeur min de la jauge
			_maximum,   // Valeur max de la jauge
			_valeur,    // Valeur reelle de la jauge
			_valeurCible, // Valeur cible pendant les animations
			_valeurDepart, // Valeur de depart de l'animation (ancienne valeur)
			_fractionAnimation; // Fraction de l'animation en cours (0.0..1.0)
	ValueAnimator _animator;
	int _couleurFondDebut;
	int _couleurFondFin;
	private Paint _paintFond;
	private Paint _paintAiguille;
	private TextPaint _textPaint1, _textPaint2,  _textPaintGraduations;
	private String _texte1, _texte2;
	int _echelleGraduations;
	float _tailleCentre = 12;
	final RectF _rContent = new RectF();
	final Rect _rText1 = new Rect();
	final Rect _rText2 = new Rect();
	final Rect _rGraduations = new Rect();
	static final float[] POSITIONS = {0.5f, 1};

	float _cx;
	float _cy;
	int _largeurBarre;
	int _paddingLeft;
	int _paddingTop;
	int _paddingRight;
	int _paddingBottom;
	int _contentWidth;
	int _contentHeight;

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
			float taille = a.getDimension(R.styleable.JaugeAAiguille_CircTextSize, 48);
			int couleur = a.getColor(R.styleable.JaugeAAiguille_CircTextCouleur, Color.BLACK);
			_texte1 = a.getString(R.styleable.JaugeAAiguille_CircTexte1);
			_texte2 = a.getString(R.styleable.JaugeAAiguille_CircTexte2);
			_textPaint1 = new TextPaint();
			_textPaint1.setAntiAlias(true);
			_textPaint1.setColor(couleur);
			_textPaint1.setTextSize(taille);

			_textPaint2 = new TextPaint();
			_textPaint2.setAntiAlias(true);
			_textPaint2.setColor(couleur);
			_textPaint2.setTextSize(taille);
		}

		// Graduations
		{
			float taille = a.getDimension(R.styleable.JaugeAAiguille_CircGraduationsTaille, 48);
			int couleur = a.getColor(R.styleable.JaugeAAiguille_CircGraduationCouleur, Color.WHITE);
			_echelleGraduations = a.getInt(R.styleable.JaugeAAiguille_CircGraduationEchelle, 10);
			_textPaintGraduations = new TextPaint();
			_textPaintGraduations.setTextSize(taille);
			_textPaintGraduations.setColor(couleur);
			_textPaintGraduations.setAntiAlias(true);
			_textPaintGraduations.setShadowLayer(12, 2, 2, Color.argb(128, 0, 0, 0));
		}
		a.recycle();

		calculeMesures();
	}

	/***********************************************************************************************
	 * Dessiner le controle
	 * @param canvas
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		super.onDraw(canvas);
		float taille = (Math.min(_contentHeight, _contentWidth) * 0.5f);

		_rContent.set(_cx - taille, _cy - taille, _cx + taille, _cy + taille);

		// Fond de la jauge
		int[] colors = {_couleurFondDebut, _couleurFondFin};
		_paintFond.setShader(new SweepGradient(_rContent.centerX(), _rContent.centerY(), colors, POSITIONS));
		canvas.drawArc(_rContent, -ANGLE_DEPART, ANGLE_TOTAL, false, _paintFond);

		for (float i = _minimum+_echelleGraduations; i <= _maximum-_echelleGraduations; i += _echelleGraduations)
		{
			final float arc = (i / (_maximum - _minimum) * ANGLE_TOTAL) - ANGLE_DEPART;
			float _x = _cx + (float) Math.cos(arc * TO_RADIAN) * taille;
			float _y = _cy + (float) Math.sin(arc * TO_RADIAN) * taille;
			String grad = String.format("%d", (int) i);
			_textPaintGraduations.getTextBounds(grad, 0, grad.length(), _rGraduations);
			drawTextCentre(canvas, grad, _x, _y, _rGraduations, _textPaintGraduations);
		}

		// Jauge
		final float valeurADessiner = _valeurDepart + (_valeurCible - _valeurDepart) * _fractionAnimation;
		final float arc = (valeurADessiner / (_maximum - _minimum) * ANGLE_TOTAL) - ANGLE_DEPART;

		float xAiguille = (float) Math.cos(arc * TO_RADIAN) * taille;
		float yAiguille = (float) Math.sin(arc * TO_RADIAN) * taille;
		canvas.drawLine(_cx, _cy, _cx + xAiguille, _cy + yAiguille, _paintAiguille);
		canvas.drawOval(_cx - _tailleCentre, _cy - _tailleCentre, _cx + _tailleCentre, _cy + _tailleCentre, _paintAiguille);

		// Texte
		drawTextCentre(canvas, _texte1, _cx, getBottom()- _rText1.height(), _rText1, _textPaint1);
		drawTextCentre(canvas, _texte2, _cx, getBottom() - (_rText1.height() * 2.0f) - _rText2.height(), _rText2, _textPaint2);
	}

	private void drawTextCentre(final Canvas canvas, @NonNull final String texte, final float x, final float y, final Rect taille, final TextPaint tp)
	{
		canvas.drawText(texte, x - taille.width() / 2.0f, y + taille.height() / 2.0f, tp);
	}

	/***********************************************************************************************
	 * Modifie la valeur affichee, avec une jolie animation
	 * @param value Valeur de la jauge
	 */
	public void setValeur(float value)
	{
		if ( _valeurCible!= value)
		{
			_valeurDepart = _valeurCible;
			if (value < _minimum)
				value = _minimum;
			if (value > _maximum)
				value = _maximum;

			_valeurCible = value;
			animer();
		}
	}

	public void setText1(@NonNull final String t1)
	{
		if (!t1.equals(_texte1))
		{
			_texte1 = t1;
			_textPaint1.getTextBounds(_texte1, 0, _texte1.length(), _rText1);

			invalidate();
		}
	}

	public void setText2(@NonNull final String t2)
	{
		if (!t2.equals(_texte2))
		{
			_texte2 = t2;
			calculeTailleTexte2();
			invalidate();
		}
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
				_fractionAnimation = (float) animation.getAnimatedValue();
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

	@Override protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		calculeMesures();
		_textPaint2.setTextSize(_textPaint1.getTextSize());
		calculeTailleTexte2();
	}

	private void calculeMesures()
	{
		_largeurBarre = (int) _paintFond.getStrokeWidth();

		_paddingLeft = getPaddingLeft() + _largeurBarre;
		_paddingTop = getPaddingTop() + _largeurBarre;
		_paddingRight = getPaddingRight() + _largeurBarre;
		_paddingBottom = getPaddingBottom() + _largeurBarre;

		_contentWidth = getWidth() - _paddingLeft - _paddingRight;
		_contentHeight = getHeight() - _paddingTop - _paddingBottom;

		_cx = _paddingLeft + (_contentWidth * 0.5f);
		_cy = _paddingTop + (_contentHeight * 0.5f);

	}

	private void calculeTailleTexte2()
	{
		// On ne fait que diminuer la taille du texte, pour eviter des agrandissements/reductions intempestifs
		_textPaint2.getTextBounds(_texte2, 0, _texte2.length(), _rText2);
		float textSize = _textPaint2.getTextSize();
		while (_rText2.width() > _contentWidth && _textPaint2.getTextSize() > 1)
		{
			textSize--;
			_textPaint2.setTextSize(textSize);
			_textPaint2.getTextBounds(_texte2, 0, _texte2.length(), _rText2);
		}
	}
}