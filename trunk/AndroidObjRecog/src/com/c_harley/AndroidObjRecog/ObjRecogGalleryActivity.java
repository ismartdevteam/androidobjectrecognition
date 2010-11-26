/*
 * Author: Charles Norona
 * COT 5930 - Digital Image Processing
 * Professor Oge Marques
 * 
 * Most of this code has been reused by Ethan Rublee's porting of the OpenSURF library. 
 * Adaptations to this code have been made by the above mentioned author.
 */
package com.c_harley.AndroidObjRecog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.c_harley.AndroidObjRecog.SurfLib.SurfInfo;

/**
 * This activity is used to take an existing picture from memory and
 * calculate its SURF descriptors.
 * 
 * @author Modified by Charles Norona
 * @author Originally created by Ethan Rublee
 */
public class ObjRecogGalleryActivity extends Activity {

	@Override
	protected void onResume() {
		super.onResume();
		
		//Invoke Garbage Collector to clear memory.
		System.gc();
	}

	private static final int SURF_PROGRESS_BAR = 1;
	public static final int SURF_INTERUPTED = 2;
	private Bitmap surfedbitmap;
	private SurfLib surflib;

	static 
	{
		try 
		{
			System.loadLibrary("opencv");
			System.loadLibrary("OpenSURF");
		} 
		catch (UnsatisfiedLinkError e) 
		{
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
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

			return ProgressDialog.show(ObjRecogGalleryActivity.this, "",
					"Calculating SURF descriptors. Please wait...", true);
		}
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return surfedbitmap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Gallery g = (Gallery) findViewById(R.id.Gallery01);
		g.setAdapter(new ImageAdapter(this));

		g.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				showDialog(SURF_PROGRESS_BAR);
					new SurfTask().execute((int) id);
			}
		});

		ImageView imgview = (ImageView) findViewById(R.id.surfimage);
		surfedbitmap = (Bitmap) getLastNonConfigurationInstance();
		imgview.setImageBitmap(surfedbitmap);
		surflib = new SurfLib();
	}

	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;

		private Integer[] mImageIds = { R.drawable.test_image02 };

		public ImageAdapter(Context c) {
			mContext = c;

		}

		public int getCount() {
			return mImageIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return mImageIds[position];
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);

			i.setImageResource(mImageIds[position]);
			i.setLayoutParams(new Gallery.LayoutParams(150, 100));
			i.setScaleType(ImageView.ScaleType.FIT_XY);
			i.setBackgroundResource(mGalleryItemBackground);

			return i;
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public Bitmap surfify(int rid) {
		SurfInfo info = surflib.surfify(rid, ObjRecogGalleryActivity.this);
		return info.outputBitmap;
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
			startActivity(new Intent(this, ObjRecogCameraActivity.class));
			return true;
		case R.id.Gallery:
			//Do nothing, you're already there.
			return true;
		}
		return false;
	}

	private class SurfTask extends AsyncTask<Integer, Integer, Bitmap> 
	{
		@Override
		protected Bitmap doInBackground(Integer... id) {
			return surfify(id[0]);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			//Do nothing
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
		protected void onPostExecute(Bitmap result) {
			ImageView view = (ImageView) findViewById(R.id.surfimage);
			view.setImageBitmap(result);

			surfedbitmap = result;
			dismissDialog(SURF_PROGRESS_BAR);
		}
	}
}