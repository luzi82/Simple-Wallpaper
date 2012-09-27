package com.luzi82.simplewallpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Display;

public class Settings extends PreferenceActivity {

	final static int ACTIVITY_LANDSCAPE_CHOOSE = 1;
	final static int ACTIVITY_LANDSCAPE_CROP = 2;
	final static int ACTIVITY_PORTRAIT_CHOOSE = 3;
	final static int ACTIVITY_PORTRAIT_CROP = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LiveWallpaper.init();

		// Load the preferences from an XML resource
		getPreferenceManager().setSharedPreferencesName(LiveWallpaper.PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.preferences);

		// app ver
		Preference appVerPreference = findPreference("preference_info_about_version");
		PackageManager pm = getPackageManager();
		String pn = getPackageName();
		String appVerString = "unknown";
		try {
			PackageInfo pi = pm.getPackageInfo(pn, 0);
			appVerString = String.format("%1$s (%2$d)", pi.versionName, pi.versionCode);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		appVerPreference.setSummary(appVerString);

		// email intent
		Preference emailPreference = findPreference("preference_info_about_email");
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("message/rfc882");
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getResources().getString(R.string.preference_info_about_email_address) });
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
		emailPreference.setIntent(Intent.createChooser(emailIntent, emailPreference.getTitle()));

		Preference landscapePreference = findPreference("preference_setting_landscape");
		landscapePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, getString(R.string.activity_choose)), ACTIVITY_LANDSCAPE_CHOOSE);
				return true;
			}
		});

		Preference portraitPreference = findPreference("preference_setting_portrait");
		portraitPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, getString(R.string.activity_choose)), ACTIVITY_PORTRAIT_CHOOSE);
				return true;
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		// Log.v(LiveWallpaper.LOG_TAG, "onActivityResult " + requestCode);
		if ((requestCode == ACTIVITY_LANDSCAPE_CHOOSE) || (requestCode == ACTIVITY_PORTRAIT_CHOOSE)) {
			Rect rect = getSize(requestCode == ACTIVITY_LANDSCAPE_CHOOSE);
			int w = rect.width();
			int h = rect.height();

			// Log.v(LiveWallpaper.LOG_TAG, "w " + w + " h " + h);
			Uri uri = Uri.fromFile(tmpFile());
			// Log.v(LiveWallpaper.LOG_TAG, uri.getPath());

			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setType("image/*");
			intent.setData(data.getData());
			intent.putExtra("crop", "true");
			intent.putExtra("outputX", w);
			intent.putExtra("outputY", h);
			intent.putExtra("aspectX", w);
			intent.putExtra("aspectY", h);
			intent.putExtra("scale", true);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
			// intent.putExtra("return-data", true);
			intent.putExtra("return-data", false);
			startActivityForResult(Intent.createChooser(intent, getString(R.string.activity_crop)), (requestCode == ACTIVITY_LANDSCAPE_CHOOSE) ? ACTIVITY_LANDSCAPE_CROP : ACTIVITY_PORTRAIT_CROP);
		} else if ((requestCode == ACTIVITY_LANDSCAPE_CROP) || (requestCode == ACTIVITY_PORTRAIT_CROP)) {
			// Log.v(LiveWallpaper.LOG_TAG, data.getData().getPath());
			try {
				boolean landscape = (requestCode == ACTIVITY_LANDSCAPE_CROP);
				String outPath = (landscape ? LiveWallpaper.landscapeFile() : LiveWallpaper.portraitFile()).getAbsolutePath();

				File f = tmpFile();
				if (f.exists()) {
					Rect rect = getSize(landscape);
					int w = rect.width();
					int h = rect.height();

					Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
					// Log.v(LiveWallpaper.LOG_TAG, "w " + bitmap.getWidth() +
					// " h " + bitmap.getHeight());
					if (bitmap.getWidth() == w && bitmap.getHeight() == h) {
						File out = new File(outPath);
						out.delete();
						out = new File(outPath);
						f.renameTo(out);
					} else {
						Bitmap b2 = Bitmap.createScaledBitmap(bitmap, w, h, true);
						File out = new File(outPath);
						out.delete();
						FileOutputStream fos = new FileOutputStream(outPath);
						b2.compress(CompressFormat.PNG, 0, fos);
						fos.flush();
						fos.close();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// try {
			// Bundle extras = data.getExtras();
			// if (extras == null)
			// return;
			// Bitmap image = extras.getParcelable("data");
			// Log.v(LiveWallpaper.LOG_TAG,"w "+image.getWidth());
			// String filename = (requestCode == ACTIVITY_LANDSCAPE_CROP) ?
			// LiveWallpaper.LANDSCAPE_FILE : LiveWallpaper.PORTRAIT_FILE;
			// FileOutputStream fos = openFileOutput(filename,
			// Context.MODE_PRIVATE);
			// image.compress(Bitmap.CompressFormat.PNG, 100, fos);
			// fos.flush();
			// fos.close();
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}
	}

	Rect getSize(boolean aLandscape) {
		Display display = getWindowManager().getDefaultDisplay();

		int ww = display.getWidth();
		int hh = display.getHeight();

		try {
			Method getRawWidth = Display.class.getMethod("getRawWidth");
			Method getRawHeight = Display.class.getMethod("getRawHeight");
			ww = (Integer) getRawWidth.invoke(display);
			hh = (Integer) getRawHeight.invoke(display);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		int l = (ww > hh) ? ww : hh;
		int s = (ww > hh) ? hh : ww;

		int w = aLandscape ? l : s;
		int h = aLandscape ? s : l;

		return new Rect(0, 0, w, h);
	}

	static File tmpFile() {
		return new File(LiveWallpaper.folder(), "tmp");
	}

}
