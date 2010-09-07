package com.bundb.android.waggeldaggel.livewallpaper;

import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
//import android.util.Log;

/**
 * 
 * A sample class that defines a LiveWallpaper an it's associated Engine. The
 * Engine delegates all the Wallpaper painting stuff to a specialized Thread.
 * 
 * Sample from <a href="http://blog.androgames.net">Androgames tutorials
 * blog</a> Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey modified by hans bickhofe
 * 
 */
public class LiveWallpaperService extends WallpaperService {

	public static final String PREFERENCES = "com.bundb.android.waggeldaggel.livewallpaper";
	public static final String PREFERENCE_SENSOR = "preference_sensor";

	private SensorManager myManager;
	private List<Sensor> sensors;
	private Sensor accSensor;
	private int sensor2Use ;

	public static final String LOG_TAG = "WD-Service";

	@Override
	public Engine onCreateEngine() {
		return new DaggelEngine();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class DaggelEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private LiveWallpaperPainting painting;
		private SharedPreferences prefs;

		DaggelEngine() {
			SurfaceHolder holder = getSurfaceHolder();
			prefs = LiveWallpaperService.this.getSharedPreferences(PREFERENCES,
					0);
			sensor2Use = Integer.parseInt(prefs.getString(PREFERENCE_SENSOR,
			"1"));
			if (sensor2Use == 1) {
				setTouchEventsEnabled(true);
				// Set Sensor + Manager
			} else {
				setTouchEventsEnabled(false);
				myManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				sensors = myManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			}
			prefs.registerOnSharedPreferenceChangeListener(this);

			painting = new LiveWallpaperPainting(holder,
					getApplicationContext(), Integer.parseInt(prefs.getString(
							PREFERENCE_SENSOR, "1")));
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			sensor2Use = Integer.parseInt(prefs.getString(PREFERENCE_SENSOR,
			"1"));
			painting.setSensor(Integer.parseInt(prefs.getString(
					PREFERENCE_SENSOR, "1")));
			
			if (sensor2Use == 1) {
				setTouchEventsEnabled(true);
				// Set Sensor + Manager
//				Log.v(LOG_TAG, "Sensor " + sensor2Use);
			} else {
				setTouchEventsEnabled(false);
//				Log.v(LOG_TAG, "Sensor " + sensor2Use);
				myManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				sensors = myManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			}
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			if (sensor2Use == 1) {
				setTouchEventsEnabled(true);
				// Set Sensor + Manager
			} else {
				setTouchEventsEnabled(false);
				myManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				sensors = myManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
				if (sensors.size() > 0) {
					accSensor = sensors.get(0);
				}
			}
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			// remove listeners and callbacks here
			painting.stopPainting();
			if (sensor2Use == 2) {
				myManager.unregisterListener(mySensorListener);
			}

		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			if (visible) {
				painting.resumePainting();
				if (sensor2Use == 2) {
					myManager.registerListener(mySensorListener, accSensor,
							SensorManager.SENSOR_DELAY_NORMAL);
				}

			} else {
				// remove listeners and callbacks here
				painting.pausePainting();
				if (sensor2Use == 2) {
					myManager.unregisterListener(mySensorListener);
				}
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			painting.setSurfaceSize(width, height);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			painting.start();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			boolean retry = true;
			painting.stopPainting();
			if (sensor2Use == 2) {
				myManager.unregisterListener(mySensorListener);
			}
			while (retry) {
				try {
					painting.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			if (sensor2Use == 1) {
				painting.doTouchEvent(event);
				// Log.v(LOG_TAG, "Sensor " + sensor2Use + " " +event);
			}
		}

		private final SensorEventListener mySensorListener = new SensorEventListener() {
			public void onSensorChanged(SensorEvent event) {
				if (sensor2Use == 2) {
					painting.updateTV(event.values[0], event.values[1]);
//					Log.v(LOG_TAG, "Sensor " + sensor2Use + " " + event);
				}
			}

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
	}

}