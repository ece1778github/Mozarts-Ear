package ece1778.mozartsear;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

// Background thread used to draw to the canvas in the surfaceview
class SpectrumPanel extends Thread {
		private static final String TAG = "SpectrumPanel";
		
		private Visualizer visualizer;
		private SurfaceHolder surfaceHolder;
		private boolean isRunning = false;		// true: thread is running
		private int delay;

		public SpectrumPanel(Visualizer v) {
			surfaceHolder = v.getHolder();
			visualizer = v;
		}

		public void setRunning(boolean state) {
			isRunning = state;
		}
		
		public void setDelay(int d) {
			delay = d;
		}

		@Override
		public void run() {
			Canvas canvas = null;
			while (isRunning) {  
				try {
					sleep(delay);
				} catch (InterruptedException e) {
					Log.e(TAG, "Interrupted exception calling sleep");
				}

				try {
					canvas = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {						
						visualizer.onDraw(canvas);
						visualizer.postInvalidate();
					}
				} finally {
					if (canvas != null) {
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}