package com.c_harley.AndroidObjRecog;

/*
 * This code was created by Ethan Rublee who ported the OpenSURF library to Android.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.theveganrobot.OpenASURF.swig.IpPairVector;
import com.theveganrobot.OpenASURF.swig.Ipoint;
import com.theveganrobot.OpenASURF.swig.IpointVector;
import com.theveganrobot.OpenASURF.swig.SURFjni;
import com.theveganrobot.OpenASURF.swig.surfjnimodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.util.Log;

public class SurfLib {
	
	public static IpointVector surfPoints = null;
	
	public static class SurfInfo{
		public SURFjni surf;
		
		public Bitmap orignalBitmap;
		public Bitmap outputBitmap;
		public int id;		
		
		/**
		 * SurfInfo constructor adapted to add byte[] imageData as an attribute of the class.
		 * @param surf
		 * @param bitmap
		 * @param data - image byte data.
		 * @author Charles Norona, cnorona1@fau.edu
		 */
		public SurfInfo(SURFjni surf, Bitmap bitmap) 
		{
			this.surf = surf;
			this.orignalBitmap = bitmap;
		}
		
		public SurfInfo(SURFjni surf, Bitmap bitmap, int id) 
		{
			this.surf = surf;
			this.orignalBitmap = bitmap;
			this.id = id;
		}
	}
	
	//TODO: Use this to populate a list of points and their attributes
	/**
	 * Modified to acquire information on descriptors in a data set, surfPoints.
	 * @param surfinfo
	 * @param which
	 * @param matches
	 * @author Modified by Charles Norona
	 */
	public static void DrawSurfPoints( SurfInfo surfinfo, int which, IpPairVector matches){
		
		IpointVector points = surfinfo.surf.getIpts();
		IpPairVector matchVector = new IpPairVector();
		//compareDescriptors(surfPoints, points, matchVector);//Get matched descriptors
		surfPoints = points;//Update surfPoints
		
		surfinfo.outputBitmap = surfinfo.orignalBitmap.copy(Config.RGB_565, true);
		Canvas canvas = new Canvas(surfinfo.outputBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.STROKE);

		Paint orientpaint = new Paint();
		orientpaint.setAntiAlias(true);
		orientpaint.setColor(Color.BLUE);
		orientpaint.setStrokeWidth(2);
		orientpaint.setStyle(Style.STROKE);
		
		Paint dxdypaint = new Paint();
		dxdypaint.setAntiAlias(true);
		dxdypaint.setColor(Color.YELLOW);
		dxdypaint.setStrokeWidth(2);
		dxdypaint.setStyle(Style.STROKE);

		Ipoint point = null;
		
		for (int i = 0; i < points.size(); i++) {

			point = points.get(i);
			
			float orientation = point.getOrientation();
			
			canvas.drawLine(point.getX(), point.getY(), point.getX()
					+ (float) Math.cos(orientation) * point.getScale(), point
					.getY()
					+ (float) Math.sin(orientation) * point.getScale(),
					orientpaint);
			canvas.drawCircle(point.getX(), point.getY(), point.getScale(),
					paint);
		}
		
		if(matches != null){
			for(int i = 0; i < matches.size(); i++){
				point = which == 1 ? matches.get(i).getSecond(): matches.get(i).getFirst();
				canvas.drawLine(point.getX(), point.getY(), point.getX()
						+ point.getDx(), point
						.getY()
						+ point.getDy(),
						dxdypaint);
			}
		}	
	}
	public static final Bitmap loadBitmapFromStream(InputStream stream,
			BitmapFactory.Options ops) {
		Bitmap bitmap = null;

		try {

			bitmap = BitmapFactory.decodeStream(stream, null, ops);

			stream.close();

		} catch (IOException e) {
			Log.e("bad file read", e.toString());
		}

		return bitmap;

	}
	
	int width;

	int height;
	
	int[] pixels;
	
	private static final String TAG = "SurfInfo";
	
	private HashMap<Integer, SurfInfo> surfmap;

	private SURFjni surf;
	private SurfInfo info;
	
	public SurfLib(){
		surfmap = new HashMap<Integer, SurfInfo>();
	}
	
	public IpPairVector findMatchs(int id1, int id2){
	
		if(!surfmap.containsKey(id1)|| !surfmap.containsKey(id2)){
			return null;
		}
		IpointVector ipts1 = surfmap.get(id1).surf.getIpts();
		IpointVector  ipts2 = surfmap.get(id2).surf.getIpts();
		IpPairVector matches = new IpPairVector();
	
		surfjnimodule.getMatches(ipts1, ipts2, matches);
		return matches;
	}
	
	public SurfInfo getSurfInfo(int rid){
		return surfmap.get(rid);
	}
	
	public Bitmap loadBitmapFromResource(int resourceId, Context ctx) {

		BitmapFactory.Options ops = new BitmapFactory.Options();

		InputStream is = ctx.getResources().openRawResource(resourceId);
		Bitmap bitmap = loadBitmapFromStream(is, ops);
		return bitmap;
	}

	/**
	 * Acquires the SURF descriptors of the image. Adapted from 
	 * surfify(int rid, Context ctx, boolean draw).
	 * @param data - Byte array of image taken
	 * @param ctx
	 * @param draw
	 * @return SurfInfo object
	 * @author Charles Norona, cnorona1@fau.edu
	 */
	public SurfInfo surfify(Bitmap bitmap, Context ctx, boolean draw){
		//Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		Log.d(TAG, "surfifying!");
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		pixels = new int[width * height];
		
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		
		surf = new SURFjni(pixels, width, height);
		//The last parameter is the threshold. Increase it for less POIs.
		surf.surfDetDes(false, 4, 4, 2, 0.001f);

		info = new SurfInfo(surf, bitmap);
	
		if(draw)
			DrawSurfPoints(info,0,null);
		
		surfmap.put(0, info);
		Log.d(TAG, "surfifying successful!");
		return info;	
	}

	public SurfInfo surfify(int rid, Context ctx){
		return surfify(rid, ctx, true);
	}

	public SurfInfo surfify(int rid, Context ctx, boolean draw){
		Bitmap bmp = loadBitmapFromResource(rid,ctx);
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int[] pixels = new int[width * height];
		
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		
		SURFjni surf = new SURFjni(pixels, width, height);
		
		//The last parameter is the threshold. Increase it for less POIs.
		surf.surfDetDes(false, 4, 4, 2, 0.002f);
		
		SurfInfo info = new SurfInfo(surf, bmp, rid);
	
		if(draw)
			DrawSurfPoints(info,0,null);
		
		surfmap.put(rid, info);
		return info;	
	}
	
	/**
	 * Takes two vectors of descriptor points and populates the third vector, matchPoints, with matched 
	 * points from both vectors.
	 * @param oldPoints
	 * @param newPoints
	 * @param matchPoints
	 * @author Charles Norona
	 */
	public static void compareDescriptors(IpointVector firstSet, IpointVector secondSet, IpPairVector matchSet)
	{
		Log.d(TAG,"Comparing Descriptors");
		
		if(firstSet != null)
			surfjnimodule.getMatches(firstSet, secondSet, matchSet);
		else
		{
			Log.d(TAG, "Old set does not exist, yet!");
		}
	}
}
