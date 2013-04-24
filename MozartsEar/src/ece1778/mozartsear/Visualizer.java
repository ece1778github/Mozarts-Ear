package ece1778.mozartsear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Visualizer extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Visualizer";

	private static final int BAR_SPACING = 5;
	private static final float VISUALIZER_WIDTH = 640;
	private SpectrumPanel spectrumPanel;
	private Paint paint;
	private double[] visualizerData = null;

	Visualizer(Context context) {
		super(context);
		getHolder().addCallback(this);		
		paint = new Paint();	
		paint.setColor(Color.GREEN);
		Shader shader = new LinearGradient(0, 40, 0, 80, Color.CYAN, Color.BLUE, TileMode.MIRROR);
		paint.setShader(shader);		
	}

	@Override 
	public void onDraw(Canvas canvas) {		
		if (canvas == null) {
			Log.e(TAG, "Null canvas");
		}
		else if (visualizerData == null) {
			Log.e(TAG, "Uninitialized visualizer data in Visualizer class");
		} else {
			canvas.drawColor(Color.BLACK);
			canvas.scale((float)getWidth()/VISUALIZER_WIDTH, -1f, 0f, getHeight()* 0.5f);
			for (int i=0; i<visualizerData.length; ++i) {
				int j=i*BAR_SPACING;

				float magnitude = (float)(visualizerData[i]+visualizerData[i]);	// scale by 2 (double height)
				canvas.drawRect(j, 0, j+3, magnitude, paint);
			}
		}
	} 

	// SurfaceHolder callbacks
	@Override
	public void surfaceCreated(SurfaceHolder holder) {		
		spectrumPanel = new SpectrumPanel(this); 
		spectrumPanel.setRunning(true);  
		spectrumPanel.setDelay(50);
		spectrumPanel.start();                        
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		spectrumPanel.setRunning(false); 

		boolean retry = true;
		while (retry) {
			try {				             
				spectrumPanel.join();         
				retry = false;
			} catch (InterruptedException e) {
				//Log.e(TAG, "Failed to stop spectrum panel", e);
			}
		}
	}

	@Override 
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) { 
	}

	public void setVisualizerData(double[] vd) {
		visualizerData = vd;
	}

}
