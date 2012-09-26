package com.luzi82.simplewallpaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.FileObserver;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class LiveWallpaper extends WallpaperService {

	public static final String LOG_TAG = "LiveWallpaper";
	public static final String PREFERENCE_NAME = "PREF";

	// public static final String LANDSCAPE_FILE = "landscape";
	// public static final String PORTRAIT_FILE = "portrait";

	@Override
	public Engine onCreateEngine() {
		return new LiveWallpaperEngine();
	}

	class LiveWallpaperEngine extends Engine {

		Bitmap mBitmapL = null;
		Bitmap mBitmapP = null;
		long mLastModL = -1;
		long mLastModP = -1;
		FileObserver fo = null;

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			// Log.v(LOG_TAG,"onCreate");
			init();
			// fo = new Fo();
			// fo.startWatching();
			loadBitmap();
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			// Log.v(LOG_TAG,"onSurfaceChanged");
			redraw(holder);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			// Log.v(LOG_TAG,"onSurfaceCreated");
			redraw(holder);
		}

		private synchronized void loadBitmap() {
			try {
				File f = landscapeFile();
				if (f.exists()) {
					long m = f.lastModified();
					if (m != mLastModL) {
						FileInputStream fis = new FileInputStream(f);
						mBitmapL = BitmapFactory.decodeStream(fis);
						fis.close();
						mLastModL = m;
					}
				} else {
					mBitmapL = null;
					mLastModL = -1;
				}
			} catch (FileNotFoundException e) {
				mBitmapL = null;
				mLastModL = -1;
				e.printStackTrace();
			} catch (IOException e) {
				mBitmapL = null;
				mLastModL = -1;
				e.printStackTrace();
			}
			
			try {
				File f = portraitFile();
				if (f.exists()) {
					long m = f.lastModified();
					if (m != mLastModP) {
						FileInputStream fis = new FileInputStream(f);
						mBitmapP = BitmapFactory.decodeStream(fis);
						fis.close();
						mLastModP = m;
					}
				} else {
					mBitmapP = null;
					mLastModP = -1;
				}
			} catch (FileNotFoundException e) {
				mBitmapP = null;
				mLastModP = -1;
				e.printStackTrace();
			} catch (IOException e) {
				mBitmapP = null;
				mLastModP = -1;
				e.printStackTrace();
			}
		}

		private synchronized void redraw(SurfaceHolder holder) {
			if (holder == null)
				return;
			Canvas c = holder.lockCanvas();
			if (c == null)
				return;
			int width = c.getWidth();
			int height = c.getHeight();
			// Log.v(LOG_TAG, "size "+width+" "+height);
			boolean landscape = width > height;
			if (landscape) {
				if (mBitmapL != null) {
					c.drawBitmap(mBitmapL, 0, 0, null);
				}
			} else {
				if (mBitmapP != null) {
					c.drawBitmap(mBitmapP, 0, 0, null);
				}
			}
			holder.unlockCanvasAndPost(c);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			if (visible) {
				loadBitmap();
				redraw(getSurfaceHolder());
			}
		}

		// class Fo extends FileObserver {
		// public Fo() {
		// super(folder().getAbsolutePath(),CLOSE_WRITE,CREATE,DELETE,MODIFY,MOVED_FROM);
		// }
		//
		// @Override
		// public void onEvent(int event, String path) {
		// Log.v(LOG_TAG, "event " + Integer.toHexString(event) + " path " +
		// path);
		// loadBitmap();
		// }
		// }

	}

	static void init() {
		folder().mkdirs();
	}

	static File folder() {
		return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/simple_wallpaper/");
	}

	static File landscapeFile() {
		return new File(folder(), "landscape");
	}

	static File portraitFile() {
		return new File(folder(), "portrait");
	}

}
