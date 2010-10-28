/*
 *Author: Charles Norona
 *COT 5930 - Digital Image Processing
 *Professor Oge Marques
 *
 *This file is an adapted version of the CameraPreview.java originally created by Android engineers. The original
 *file can be found in the APIDemos project that comes with the Android SDK.
 */

package com.c_harley.AndroidObjRecog;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

// ----------------------------------------------------------------------

public class ObjRecogCameraActivity extends Activity {    
	private Preview mPreview;
	private String TAG = "ObjRecogCameraActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new Preview(this);
		setContentView(mPreview);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_CAMERA:
			//mCamera.takePicture(shutter, raw, jpeg)
			mPreview.mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
		return super.onKeyDown(keyCode, event);
	}

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
			FileOutputStream outStream = null;			
			try 
			{				
				// write to local sandbox file system				
				// outStream =				
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",				
				// System.currentTimeMillis()), 0);				
				// Or write to sdcard				
				outStream = new FileOutputStream(String.format(						
						"/sdcard/%d.jpg", System.currentTimeMillis()));				
				outStream.write(data);				
				outStream.close();				
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);			
			} 
			catch (FileNotFoundException e) 
			{				
				e.printStackTrace();			
			} 
			catch (IOException e) 
			{				
				e.printStackTrace();			
			} 
			finally 
			{			

			}			
			Log.d(TAG, "onPictureTaken - jpeg");		
			mPreview.mCamera.startPreview();
		}	
	};
}



// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	//private static String TAG = "CameraPreview::Preview";

	Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		//Log.d(TAG, "surfaceChanged");
		Parameters parameters = mCamera.getParameters();

		//For API levels 5+
		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		parameters.setPreviewSize(sizes.get(0).width, sizes.get(0).height);

		//For API levels <5
		//parameters.setPreviewSize(w, h);

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
}