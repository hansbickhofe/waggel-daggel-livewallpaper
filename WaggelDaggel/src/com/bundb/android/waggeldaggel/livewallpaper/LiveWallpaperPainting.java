package com.bundb.android.waggeldaggel.livewallpaper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
//import android.util.Log;

/**
 * 
 * A sample class that handle the painting of an Android LiveWallpaper in a
 * proper manner, without consumming resources when there is no need to modifiy
 * the wallpaper appearence : - when the wallpaper is hidden (no more visible) -
 * when there is nothing to update / animate
 * 
 * Sample from <a href="http://blog.androgames.net">Androgames tutorials
 * blog</a> Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey modified by hans bickhofe
 * 
 */
public class LiveWallpaperPainting extends Thread {

	/** Reference to the View and the context */
	private SurfaceHolder surfaceHolder;

	/** State */
	private boolean wait;
	private boolean run;

	/** Dimensions */
	public int width;
	public int height;
	public int sense = 5;
	public int sensor;
	private double hvPos;
	private double hhPos;
	private double angleV = 0;
	public double angleH = 0;
	public double cor = 1;
	private double angleR = 0;
	private double velV;
	public double velH;
	private double velR;
	private double decV;
	public double decH;
	private double decR;
	private long counter;

	public float oldX, oldY = 0f;

	/** Time tracking */
	private long previousTime;

	/** The drawable to use as Dackel Body */
	private Bitmap mBackgroundImage;

	/** The drawable to use as Dackel Head */
	private Bitmap mdaggelImage;
	public float diffX;
	public float diffY;
	public int triggered;
	public static final String LOG_TAG = "WD-Paint";

	public LiveWallpaperPainting(SurfaceHolder surfaceHolder, Context context,
			int sensor) {
		// keep a reference of the context and the surface
		// the context is needed is you want to inflate
		// some resources from your livewallpaper .apk
		this.surfaceHolder = surfaceHolder;
		// don't animate until surface is created and displayed
		this.wait = true;
		// initialize sensor
		this.sensor = sensor;
		Resources res = context.getResources();
		this.mdaggelImage = BitmapFactory.decodeResource(res,
				R.drawable.kopf_solo);
		this.mBackgroundImage = BitmapFactory.decodeResource(res,
				R.drawable.dackel_body);
	}

	/**
	 * Change the sensor preference
	 */
	public void setSensor(int sensor) {
		this.sensor = sensor;
	}

	/**
	 * Pauses the livewallpaper animation
	 */
	public void pausePainting() {
		this.wait = true;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Resume the livewallpaper animation
	 */
	public void resumePainting() {
		this.wait = false;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Stop the livewallpaper animation
	 */
	public void stopPainting() {
		this.run = false;
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void run() {
		this.run = true;
		Canvas c = null;
		while (run) {
			try {
				c = this.surfaceHolder.lockCanvas(null);
				synchronized (this.surfaceHolder) {
					doDraw(c);
				}
			} finally {
				if (c != null) {
					this.surfaceHolder.unlockCanvasAndPost(c);
				}
			}
			// pause if no need to animate
			synchronized (this) {
				if (wait) {
					try {
						wait();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	/**
	 * Invoke when the surface dimension change
	 * 
	 * @param width
	 * @param height
	 */
	public void setSurfaceSize(int width, int height) {
		this.width = width;
		this.height = height;
		switch (height ) {
		case 320:
			hvPos = height * 0.60;
			cor = 0.3;
			break;
		case 400:
			hvPos = height * 0.51;
			cor = 0.5 ;
			break;
		case 432:
			hvPos = height * 0.48;
			cor = 0.6 ;
			break;
		case 480:
			hvPos = height * 0.53;
			cor = 0.7 ;
			break;
		case 800:
			hvPos = height * 0.53;
			cor = 1 ;
			break;
		case 854:
			hvPos = height * 0.49;
			cor = 1.1 ;
			break;
		default:
			hvPos = height * 0.53;
			cor = 1 ;
			break;	
		}
		
		
		hhPos = width * 0.5 ;
//		Log.v(LOG_TAG, "Pos"+ hvPos + "x" + hhPos + " w: " + width + " h:" + height) ;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Invoke while the screen is touched
	 * 
	 * @param event
	 */
	public void doTouchEvent(MotionEvent event) {
		sensor = 1;
		triggered = 1;
		counter = 7000;
		velV = Math.random() * 20 + ( 25 * cor );
		velH = Math.random() * 20 + ( 10 * cor );
		velR = Math.random() * 5 + ( 5 * cor );
		decV = 0.12;
		decH = 0.08;
		decR = 0.08;
//		Log.v(LOG_TAG, "Touched");

		this.wait = false;
		synchronized (this) {
			notify();
//			Log.v(LOG_TAG, "Touched-notify");
		}
	}

	/**
	 * Invoke while the phone is shaked
	 * 
	 * @param event
	 */

	public void updateTV(float x, float y) {
		sensor = 2;
		diffX = x - oldX;
		diffY = y - oldY;
//		Log.v(LOG_TAG, "Shaked");
		if (diffX >= sense) {
//			Log.v(LOG_TAG, "Shaked-trigger ");
			triggered = 1;
			counter = 7000;
			velV = Math.random() * 20 + ( 25 * cor );
			velH = Math.random() * 20 + ( 10 * cor );
			velR = Math.random() * 5 + ( 5 * cor );
			decV = 0.12;
			decH = 0.08;
			decR = 0.08;
			this.wait = false;
			synchronized (this) {
				notify();
//				Log.v(LOG_TAG, "Shaked-notify");
			}
		}
		oldX = x;
		oldY = y;
	}

	/**
	 * Do the actual drawing stuff
	 * 
	 * @param canvas
	 */
	private void doDraw(Canvas canvas) {
		long currentTime = System.currentTimeMillis();
		long elapsed = currentTime - previousTime;
		Paint bodyPaint = new Paint();
		Paint headPaint = new Paint();
		
		canvas.drawBitmap(mBackgroundImage, 0, 0, bodyPaint);	
		canvas.drawBitmap(mdaggelImage, (int) hhPos - mdaggelImage.getWidth()
				/ 2, (int) hvPos - mdaggelImage.getHeight() / 2, headPaint);

		if (triggered == 1) {
			if (elapsed > 12) {

				if (angleV < 360)
					angleV += decV;
				else
					angleV = 0;
				if (velV > 0)
					velV -= .1;
				float kopfy = (float) (hvPos + Math.sin(angleV) * velV);

				// if (angleH < 360) angleH += decH;
				// else angleH = 0;
				// if (velH > 0) velH -= .1;
				// float kopfx = (float) (hhPos+Math.sin(angleH)*velH);
				float kopfx = (float) hhPos;

				if (angleR < 360)
					angleR += decR;
				else
					angleR = 0;
				if (velR > 0)
					velR -= 0.1;
				float kopfrotation = (float) (Math.sin(angleR) * velR);

				counter -= elapsed;

				if (counter >= 0) {
					canvas.drawBitmap(mBackgroundImage, 0, 0, bodyPaint);
					canvas.save();
					canvas.rotate(kopfrotation, kopfx - mdaggelImage.getWidth()
							/ 2, kopfy - mdaggelImage.getHeight() / 2);
					canvas.drawBitmap(mdaggelImage, kopfx
							- mdaggelImage.getWidth() / 2, kopfy
							- mdaggelImage.getHeight() / 2, headPaint);
					canvas.restore();
				} else {
					triggered = 0;
					counter = 0;
					velV = Math.random() * 20 + ( 25 * cor ) ;
					velH = Math.random() * 20 + ( 10 * cor );
					velR = Math.random() * 5 + ( 5 * cor );
					decV = 0.12;
					decH = 0.08;
					decR = 0.08;

				}
				previousTime = currentTime;
			}

		} else {
			wait = true;
		}

	}
}
