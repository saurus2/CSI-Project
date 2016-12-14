package csi.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/*
 * Portions (c) 2009 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Coby Plain coby.plain@gmail.com, Ali Muzaffar ali@muzaffar.me
 */

public class DrawSurfaceView extends View {
	Point me = new Point(37.451037, 126.656499, "Me");
	Paint mPaint = new Paint();
	private double OFFSET = 0d;
	private double screenWidth, screenHeight = 0d;
	private Bitmap mfSpot1, mfSpot2, mfSpot3, mfSpot4, mfSpot5, turnleft, turnright, mBlips;
	private Bitmap mRadar;

	//목표물이 좌측, 정면, 우측 중 어디에 있는지를 나타내는 플래그
	//0이면 좌측, 1이면 정면, 2이면 우측을 나타낸다
	public static int directionFlag = 1;

	//AR에서의 자신 위치를 지정하는 변수
	public static Point props = new Point(0,0,"null");

	public DrawSurfaceView(Context c, Paint paint) {
		super(c);
	}

	public DrawSurfaceView(Context context, AttributeSet set) {
		super(context, set);
		mPaint.setColor(Color.GREEN);
		mPaint.setTextSize(50);
		mPaint.setStrokeWidth(DpiUtils.getPxFromDpi(getContext(), 2));
		mPaint.setAntiAlias(true);
		
		mRadar = BitmapFactory.decodeResource(context.getResources(), R.drawable.radar);

		mfSpot1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.forward1);
		mfSpot2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.forward2);
		mfSpot3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.forward3);
		mfSpot4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.forward4);
		mfSpot5 = BitmapFactory.decodeResource(context.getResources(), R.drawable.forward5);
		turnleft = BitmapFactory.decodeResource(context.getResources(), R.drawable.turnleft);
		turnright = BitmapFactory.decodeResource(context.getResources(), R.drawable.turnright);
		mBlips = BitmapFactory.decodeResource(context.getResources(), R.drawable.blip);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.d("onSizeChanged", "in here w=" + w + " h=" + h);
		screenWidth = (double) w;
		screenHeight = (double) h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawBitmap(mRadar, 0, 0, mPaint);
		
		int radarCentreX = mRadar.getWidth() / 2;
		int radarCentreY = mRadar.getHeight() / 2;

		for (int i = 0; i < 1; i++) {
			Bitmap blip = mBlips;//[i];
			Bitmap fspot1 = mfSpot1;
			Bitmap fspot2 = mfSpot2;
			Bitmap fspot3 = mfSpot3;
			Bitmap fspot4 = mfSpot4;
			Bitmap fspot5 = mfSpot5;
			Bitmap lspot = turnleft;
			Bitmap rspot = turnright;
			Point u = props;//.get(i);
			double dist = distInMetres(me, u);
			
			if (blip == null || fspot1 == null)
				continue;
			
			if(dist > 70)
				dist = 70; //we have set points very far away for demonstration
			
			double angle = bearing(me.latitude, me.longitude, u.latitude, u.longitude) - OFFSET;
			double xPos, yPos;
			
			if(angle < 0)
				angle = (angle+360)%360;
			
			xPos = Math.sin(Math.toRadians(angle)) * dist;
			yPos = Math.sqrt(Math.pow(dist, 2) - Math.pow(xPos, 2));

			if (angle > 90 && angle < 270)
				yPos *= -1;
			
			double posInPx = angle * (screenWidth / 90d);
			
			int blipCentreX = blip.getWidth() / 2;
			int blipCentreY = blip.getHeight() / 2;
			
			xPos = xPos - blipCentreX;
			yPos = yPos + blipCentreY;
			canvas.drawBitmap(blip, (radarCentreX + (int) xPos), (radarCentreY - (int) yPos), mPaint); //radar blip
			
			//reuse xPos
			int spotCentreX = fspot3.getWidth() / 2;
			int spotCentreY = fspot3.getHeight() / 2;
			xPos = posInPx - spotCentreX;

			u.y = (float)screenHeight/5 + spotCentreY;
			
			if (angle <= 5) {
				u.x = (float) ((screenWidth / 2) + xPos);
				canvas.drawBitmap(fspot3, u.x, u.y, mPaint); //camera spot
				directionFlag = 1;
			}
			else if (angle < 335 &&	angle >= 315) {
				u.x = (float) ((screenWidth / 2) - ((screenWidth * 4) - xPos));
				canvas.drawBitmap(fspot1, u.x, u.y, mPaint); //camera spot
				directionFlag = 1;
			}
			else if (angle <= 25 &&	angle > 5) {
				u.x = (float) ((screenWidth / 2) + xPos);
				canvas.drawBitmap(fspot4, u.x, u.y, mPaint); //camera spot
				directionFlag = 1;
			}
			else if (angle <= 45 &&	angle > 25) {
				u.x = (float) ((screenWidth / 2) + xPos);
				canvas.drawBitmap(fspot5, u.x, u.y, mPaint); //camera spot
				directionFlag = 1;
			}
			else if (angle >= 355) {
				u.x = (float) ((screenWidth / 2) - ((screenWidth * 4) - xPos));
				canvas.drawBitmap(fspot3, u.x, u.y, mPaint); //camera spot
				directionFlag = 1;
			}
			else if (angle < 355 &&	angle >= 335) {
				u.x = (float) ((screenWidth / 2) - ((screenWidth * 4) - xPos));
				canvas.drawBitmap(fspot2, u.x, u.y, mPaint); //camera spot
				directionFlag = 1;
			}

			else if (angle > 45 && angle <= 180) {
				u.x = (float) screenWidth - lspot.getWidth();
				u.y = (float) screenHeight/10 + spotCentreY;
				canvas.drawBitmap(rspot, u.x, u.y, mPaint); //camera spot
				directionFlag = 2;
			}

			else if (angle > 180 && angle < 315) {
				u.x = (float) 0d;
				u.y = (float) screenHeight/10 + spotCentreY;
				canvas.drawBitmap(lspot, u.x, u.y, mPaint); //camera spot
				directionFlag = 0;
			}

			if(directionFlag == 1) {
				u.x += spotCentreX - u.description.length()*50 / 2;
				u.y = (float) screenHeight / 2 + spotCentreY;
				canvas.drawText(u.description, u.x, u.y, mPaint); //text
			}

			// 테스트용 문구 표시
//			float y = u.y;
//			for (String line: MainActivity.msg.split("\n")) {
//				canvas.drawText(line, u.x - (float)(screenWidth / 90d)*20, y- 3 * spotCentreY, mPaint);
//				y += mPaint.descent() - mPaint.ascent();
//			}

			//남은 거리 표시
			Paint rPaint = new Paint();
			rPaint.setColor(Color.WHITE);
			rPaint.setTextSize(50);
			rPaint.setAntiAlias(true);

			Paint rsPaint = new Paint();
			rsPaint.setColor(Color.BLACK);
			rsPaint.setTextSize(50);
			rsPaint.setAntiAlias(true);
			rsPaint.setStyle(Paint.Style.STROKE);
			rsPaint.setStrokeWidth(1);

			canvas.drawText(MainActivity.remainDistanceMsg, 0, (float)screenHeight-50, rPaint); //text
			canvas.drawText(MainActivity.remainDistanceMsg, 0, (float)screenHeight-50, rsPaint); //text
		}
	}

	public void setOffset(float offset) {
		this.OFFSET = offset;
	}

	public void setMyLocation(double latitude, double longitude) {
		me.latitude = latitude;
		me.longitude = longitude;
	}

	protected double distInMetres(Point me, Point u) {

		double lat1 = me.latitude;
		double lng1 = me.longitude;

		double lat2 = u.latitude;
		double lng2 = u.longitude;

		double earthRadius = 6371;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return dist * 1000;
	}

	protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
		double longDiff = Math.toRadians(lon2 - lon1);
		double la1 = Math.toRadians(lat1);
		double la2 = Math.toRadians(lat2);
		double y = Math.sin(longDiff) * Math.cos(la2);
		double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

		double result = Math.toDegrees(Math.atan2(y, x));
		return (result+360.0d)%360.0d;
	}
}
