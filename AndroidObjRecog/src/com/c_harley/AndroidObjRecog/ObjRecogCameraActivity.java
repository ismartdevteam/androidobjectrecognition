/*
 *Author: Charles Norona
 *COT 5930 - Digital Image Processing
 *Professor Oge Marques
 *
 *This file is an adapted version of the CameraPreview.java originally created by Android engineers. 
 *The original file can be found in the APIDemos project that comes with the Android SDK.
 */

package com.c_harley.AndroidObjRecog;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.ImageView;

import com.c_harley.AndroidObjRecog.SurfLib.SurfInfo;

// ----------------------------------------------------------------------

/**
 * @author Charles Norona
 *
 */
public class ObjRecogCameraActivity extends Activity {  

	private class SurfTask extends AsyncTask<Bitmap, Bitmap, Bitmap> 
	{
		@Override
		protected Bitmap doInBackground(Bitmap... mBitmap) {
			Log.d(TAG, "Executing background process!");
			return surfify(mBitmap[0]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			dismissDialog(SURF_PROGRESS_BAR);
			showDialog(SURF_INTERUPTED);
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Bitmap result) 
		{
			//ImageView view = (ImageView) findViewById(R.id.surfimage);
			ImageView view = new ImageView(ObjRecogCameraActivity.this);
			view.setImageBitmap(result);
			setContentView(view);

			//surfedbitmap = result;
			dismissDialog(SURF_PROGRESS_BAR);
			// removeDialog(SURF_PROGRESS_BAR);
		}
	}

	private Preview mPreview;
	private String TAG = "ObjRecogCameraActivity";
	private Bitmap bitmap;
	private SurfLib surflib;
	private static final int SURF_PROGRESS_BAR = 1;
	public static final int SURF_INTERUPTED = 2;
	public static final float scaleX = 0.40f;
	public static final float scaleY = 0.40f;
	Matrix aMatrix;
	
	ShutterCallback shutterCallback = new ShutterCallback() 
	{		
		public void onShutter() 
		{			
			Log.d(TAG, "onShutter'd");		
		}	
	};
	/** Handles data for raw picture */	
	PictureCallback rawCallback = new PictureCallback() 
	{		
		public void onPictureTaken(byte[] data, Camera camera) 
		{
			Log.d(TAG, "onPictureTaken - raw - ");		
		}	
	};

	/** Handles data for jpeg picture */	
	PictureCallback jpegCallback = new PictureCallback() 
	{		
		public void onPictureTaken(byte[] data, Camera camera) 
		{	
			//TODO:Use the data to "surfify"
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			 
			aMatrix = new Matrix();
			//aMatrix.postScale(scaleX, scaleY);
			aMatrix.setScale(scaleX, scaleY);
			
			width =  (int) ((float)scaleX * width);
			height =  (int) ((float)scaleY * height);
			 
//			Bitmap resizedBitmap = Bitmap.createBitmap(bitmap,
//					0,0,
//					width,
//					height, 
//					aMatrix, false);
			
			//Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
			bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
			
			//Get new 
//			width = resizedBitmap.getWidth();
//			height = resizedBitmap.getHeight();
			width = bitmap.getWidth();
			height = bitmap.getHeight();
			
			//int[] pixels = new int[width * height];

			showDialog(SURF_PROGRESS_BAR);
			//new SurfTask().execute(resizedBitmap);	
			new SurfTask().execute(bitmap);
			Log.d(TAG, "onPictureTaken - jpeg");		
			mPreview.mCamera.startPreview();
		}	
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new Preview(this);
		setContentView(mPreview);
		
		surflib = new SurfLib();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SURF_INTERUPTED: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You interupted the SURF process!")
			.setCancelable(false).setPositiveButton("Sorry...",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) 
				{
					//Do nothing
				}
			});
			AlertDialog alert = builder.create();
			return alert;
		}
		case SURF_PROGRESS_BAR: {

			return ProgressDialog.show(ObjRecogCameraActivity.this, "",
					"Calculating SURF descriptors. Please wait...", true);
		}
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_CAMERA:
			mPreview.mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
		return super.onKeyDown(keyCode, event);
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.Camera:
			//Do nothing, you're already there.
			return true;
		case R.id.Gallery:
			finish();
			return true;
		}
		return false;
	}	

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}

	public Bitmap surfify(Bitmap bitmap) {
		Log.d(TAG, "Preparing SurfInfo and calling the SURF library!");
		SurfInfo info = surflib.surfify(bitmap, ObjRecogCameraActivity.this, true);
		return info.outputBitmap;
	}
}

// ----------------------------------------------------------------------
class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	private static String TAG = "CameraPreview::Preview";

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Log.d(TAG, "surfaceChanged");
		Parameters parameters = mCamera.getParameters();

		//For API levels 5+
		//		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		//		parameters.setPreviewSize(sizes.get(0).width, sizes.get(0).height);

		//For API levels < 5
		parameters.setPreviewSize(w, h);

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		//Log.d(TAG, "surfaceCreated");
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			//mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		//Log.d(TAG, "surfaceDestroyed");
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
}