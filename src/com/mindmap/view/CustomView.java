package com.mindmap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class CustomView extends View {

	Paint paint = new Paint();
	float start_x = 0;
	float start_y = 0;
	float end_x = 0;
	float end_y = 0;

	public CustomView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint.setColor(Color.BLACK);
		canvas.drawLine(start_x, start_y, end_x, end_y, paint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int heightMeasured = 0;
		/*
		 * for each child get height and heightMeasured += childHeight;
		 */

		// If I am in a scrollview i got heightmeasurespec == 0, so
		if (heightMeasureSpec == 0) {
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeasured,
					MeasureSpec.AT_MOST);
		}

		setMeasuredDimension(
				getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
				getDefaultSize(this.getSuggestedMinimumHeight(),
						heightMeasureSpec));
	}

	public float getStart_x() {
		return start_x;
	}

	public void setStart_x(float start_x) {
		this.start_x = start_x;
	}

	public float getStart_y() {
		return start_y;
	}

	public void setStart_y(float start_y) {
		this.start_y = start_y;
	}

	public float getEnd_x() {
		return end_x;
	}

	public void setEnd_x(float end_x) {
		this.end_x = end_x;
	}

	public float getEnd_y() {
		return end_y;
	}

	public void setEnd_y(float end_y) {
		this.end_y = end_y;
	}
}
