package csi.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
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
	private Bitmap mfSpots, mlSpots, mrSpots, mBlips;
	private Bitmap mRadar;

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

		mfSpots = BitmapFactory.decodeResource(context.getResources(), R.drawable.forwardarrow);
		mlSpots = BitmapFactory.decodeResource(context.getResources(), R.drawable.leftarrow);
		mrSpots = BitmapFactory.decodeResource(context.getResources(), R.drawable.rightarrow);
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
			Bitmap fspot = mfSpots;//[i];
			Bitmap lspot = mlSpots;
			Bitmap rspot = mrSpots;
			Point u = props;//.get(i);
			double dist = distInMetres(me, u);
			
			if (blip == null || fspot == null)
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
			int spotCentreX = fspot.getWidth() / 2;
			int spotCentreY = fspot.getHeight() / 2;
			xPos = posInPx - spotCentreX;
			
			if (angle <= 45) {
				u.x = (float) ((screenWidth / 2) + xPos);
				canvas.drawBitmap(fspot, u.x, u.y, mPaint); //camera spot
			}
			
			else if (angle >= 315) {
				u.x = (float) ((screenWidth / 2) - ((screenWidth * 4) - xPos));
				canvas.drawBitmap(fspot, u.x, u.y, mPaint); //camera spot
			}

			else if (angle > 45 && angle <= 180) {
				u.x = (float) ((screenWidth / 90d) * 80);
				canvas.drawBitmap(rspot, u.x, u.y, mPaint); //camera spot
			}

			else if (angle > 180 && angle < 315) {
				u.x = (float) (screenWidth / 90d);
				canvas.drawBitmap(lspot, u.x, u.y, mPaint); //camera spot
			}
			
			u.y = (float)screenHeight/2 + spotCentreY;
			canvas.drawText(u.description, u.x, u.y, mPaint); //text

			// 테스트용 문구 표시
			float y = u.y;
			for (String line: MainActivity.msg.split("\n")) {
				canvas.drawText(line, u.x - (float)(screenWidth / 90d)*20, y- 3 * spotCentreY, mPaint);
				y += mPaint.descent() - mPaint.ascent();
			}

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
