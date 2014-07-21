package group.acm.bookshare;

import java.io.IOException;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zijunlin.Zxing.Demo.camera.CameraManager;
import com.zijunlin.Zxing.Demo.decoding.CaptureActivityHandler;
import com.zijunlin.Zxing.Demo.decoding.InactivityTimer;
import com.zijunlin.Zxing.Demo.view.ViewfinderView;

public class CaptureActivity extends Activity implements Callback {

	private CaptureActivityHandler handler;
	public CameraManager cameraManager;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private static boolean is_flash;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan);
		// 初始化 CameraManager

		is_flash = false;
		cameraManager = new CameraManager(getApplication());
		hasSurface = false;
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		inactivityTimer = new InactivityTimer(this);

		ImageView flash_img = (ImageView) findViewById(R.id.flash);
		/*
		 * int bmpW = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.open_w).getWidth();// 获取图片宽度 int bmpH =
		 * BitmapFactory.decodeResource(getResources(),
		 * R.drawable.open_w).getHeight();// 获取图片宽度 DisplayMetrics dm = new
		 * DisplayMetrics();
		 * getWindowManager().getDefaultDisplay().getMetrics(dm); int screenW =
		 * dm.widthPixels;// 获取分辨率宽度 int screenH = dm.heightPixels;// 获取分辨率宽度
		 * Matrix matrix1 = new Matrix(); matrix1.setScale(screenW/(10*bmpW),
		 * screenH/(10*bmpH)); matrix1.postTranslate(screenW/10*4,
		 * screenH/10*7); flash_img.setImageMatrix(matrix1);
		 */

		/*
		 * ImageView turnback_img = (ImageView) findViewById(R.id.turnback);
		 * bmpW = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.turnback_b).getWidth();// 获取图片宽度 bmpH =
		 * BitmapFactory.decodeResource(getResources(),
		 * R.drawable.turnback_b).getHeight();// 获取图片宽度
		 * 
		 * Matrix matrix2 = new Matrix(); matrix2.setScale(screenW/(10*bmpW),
		 * screenH/(10*bmpH)); matrix2.postTranslate(screenW/10*4,
		 * screenH/10*7); matrix2.postTranslate(screenW/10*6, screenH/10*7);
		 * turnback_img.setImageMatrix(matrix2);
		 */

	}

	public void Turnback(View v)// 返回按钮
	{
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		CaptureActivity.this.finish();
	}

	public void Flash(View v)// 控制闪光灯按钮
	{
		if (false == is_flash) {
			Parameters params = cameraManager.getCamera().getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			cameraManager.getCamera().setParameters(params);
			is_flash = true;
			ImageView flash_image = (ImageView) findViewById(R.id.flash);
			flash_image.setImageResource(R.drawable.close_b);
		} else {
			Parameters params = cameraManager.getCamera().getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			cameraManager.getCamera().setParameters(params);
			is_flash = false;
			ImageView flash_image = (ImageView) findViewById(R.id.flash);
			flash_image.setImageResource(R.drawable.open_b);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {

			initCamera(surfaceHolder);

		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;

	}

	@Override
	protected void onPause() {

		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		cameraManager.closeDriver();
		super.onPause();
	}

	@Override
	protected void onDestroy() {

		inactivityTimer.shutdown();
		super.onDestroy();

	}

	private void initCamera(SurfaceHolder surfaceHolder) {

		try {
			cameraManager.openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();

		Toast.makeText(this,
				obj.getBarcodeFormat().toString() + ":" + obj.getText(),
				Toast.LENGTH_LONG).show();

		Bundle data = new Bundle();
		data.putString("isbn", obj.getText());
		Intent intent = new Intent();
		intent.putExtras(data);
		CaptureActivity.this.setResult(RESULT_OK, intent);
		
		finish();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}