package ece1778.mozartsear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class PatternRecognition extends View {	
	private static final String TAG = "PatternRecognition";	
	private static final int TEXT_OFFSET_Y = 58;
	private static final int ARROW_OFFSET_X = -26;

	int[] notePositionX;	// X coordinate list on the staff
	ArrayList<Integer> triad;
	ArrayList<Integer> scalarMotion;
	ArrayList<Integer> repetition;	
	ArrayList<Integer> motive;

	private Paint blackPaint;	
	private Paint blackFillPaint;

	private Bitmap arrow;

	public PatternRecognition(Context context) {
		super(context);

		blackFillPaint = new Paint();
		blackFillPaint.setColor(Color.BLACK);
		blackFillPaint.setStyle(Style.FILL);

		blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		blackPaint.setColor(Color.BLACK);
		blackPaint.setTextSize(32f);
		blackPaint.setTypeface(Typeface.DEFAULT_BOLD);
		blackPaint.setStyle(Style.STROKE);
		blackPaint.setTextAlign(Paint.Align.CENTER);

		arrow = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
	}

	public void setPositions(int[] positionList) {
		notePositionX = positionList;		
	}

	public void setContents(ArrayList<Integer> tri,
			ArrayList<Integer> sca,
			ArrayList<Integer> rep,	
			ArrayList<Integer> mot) {
		triad = tri;
		scalarMotion = sca;
		repetition = rep;
		motive = mot;
	}

	@Override
	protected void onDraw(Canvas c) {
		final int Y = 360;

		if (notePositionX != null) {
			if (triad != null) {
				for (int i=0; i<triad.size(); ++i) {
					int value = triad.get(i).intValue();
					if (value > 0) {
						drawTri(c, notePositionX[i], Y);
					}
				}
			}
			if (scalarMotion != null) {				
				for (int i=0; i<scalarMotion.size(); ++i) {
					int value = scalarMotion.get(i).intValue();
					if (value > 0) {
						drawSca(c, notePositionX[i], Y);
					}
				}
			}
			if (repetition != null) {
				for (int i=0; i<repetition.size(); ++i) {
					int value = repetition.get(i).intValue();
					if (value > 0) {
						drawRep(c, notePositionX[i], Y, value);
					}
				}
			}
			if (motive != null) {
				for (int i=0; i<motive.size(); ++i) {
					int value = motive.get(i).intValue();
					if (value > 0) {
						drawMot(c, notePositionX[i], Y, value);
					}
				}
			}
		} else {
			Log.e(TAG, "Attempted to draw before setting note position");
		}

		updateScrollWidth();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		switch (widthMode) {
		case MeasureSpec.EXACTLY:
			width = widthSize;
			break;	
		case MeasureSpec.AT_MOST:
			width = widthSize;
			break;
		default:
			width = 2000; //TODO nextStaffX;
			break;
		}

		int height;
		switch (heightMode) {
		case MeasureSpec.EXACTLY:
			height = heightSize;
			break;	
		case MeasureSpec.AT_MOST:
			height = heightSize;
			break;
		default:
			height = 500;
			break;
		}

		setMeasuredDimension(width, height);
	}

	// Auto adjust the scroll width to the end of the music score
	public void updateScrollWidth() {
		LayoutParams lp = getLayoutParams();
		if (notePositionX != null) {
			lp.width = notePositionX[notePositionX.length-1];
		} else {
			lp.width = 600;
		}
		lp.height = LayoutParams.WRAP_CONTENT;
		setLayoutParams(lp);
	}

	// Green arrow
	private void drawArrow(Canvas c, int x, int y) {
		c.drawBitmap(arrow, x+ARROW_OFFSET_X, y, null);
	}

	// Indicate triad
	private void drawTri(Canvas c, int x, int y) {
		drawArrow(c, x, y);
		c.drawText("TRI", x, y+TEXT_OFFSET_Y, blackPaint);
	}

	// Indicate scalar motion
	private void drawSca(Canvas c, int x, int y) {
		drawArrow(c, x, y);
		c.drawText("SCA", x, y+TEXT_OFFSET_Y, blackPaint);
	}

	// Indicate repetition
	private void drawRep(Canvas c, int x, int y, int id) {
		drawArrow(c, x, y);
		c.drawText("REP", x, y+TEXT_OFFSET_Y, blackPaint);
		c.drawText(Integer.toString(id), x, y+TEXT_OFFSET_Y+30, blackPaint);
	}

	// Indicate motive
	private void drawMot(Canvas c, int x, int y, int id) {
		drawArrow(c, x, y);
		c.drawText("MOT", x, y+TEXT_OFFSET_Y, blackPaint);
		c.drawText(Integer.toString(id), x, y+TEXT_OFFSET_Y+30, blackPaint);
	}

	/********************************************************************/
	/*                       GetTriads method                           */
	/********************************************************************/
	static ArrayList<Integer> GetTriads(int[] relativeIdList) {
		final int IS_TRIAD = 1;

		int length = relativeIdList.length;		
		int[] tri = new int[length];

		int diff1;
		int diff2;
		for (int i=0; i<length-3; ++i) {
			diff1 = relativeIdList[i+1]-relativeIdList[i];
			diff2 = relativeIdList[i+2]-relativeIdList[i+1];

			if (diff1==2 && diff2==2) {
				tri[i] = IS_TRIAD;
			} else if (diff1==-2 && diff2==-2) {
				tri[i] = IS_TRIAD;
			}
		}

		ArrayList<Integer> triList = new ArrayList<Integer>(length);
		for (int i=0; i<length; ++i) {
			triList.add(i, Integer.valueOf(tri[i]));
		}
		return triList;
	}

	/********************************************************************/
	/*                GetScalarMotions method                           */
	/********************************************************************/
	static ArrayList<Integer> GetScalarMotions(int[] relativeIdList) {
		final int IS_SCALAR_MOTION = 1;

		int length = relativeIdList.length;		
		int[] sca = new int[length];
		boolean[] used = new boolean[length];

		for (int i=0; i<length-1; ++i) {
			if (!used[i] && relativeIdList[i] != 0) {
				boolean direction;
				// Smaller number means higher on the staff
				direction = ((relativeIdList[i]-relativeIdList[i+1]) > 0);

				int numNotes = 0;
				int diff;
				for (int j=i; j<length-1; ++j) {
					diff = relativeIdList[j]-relativeIdList[j+1];

					// Make sure the note is exactly 1 higher or 1 lower
					if (diff==1 && direction==true) {
						++numNotes;
					} else if (diff==-1 && direction==false) {
						++numNotes;
					} else {
						break;
					}
				}

				// Require at least 4 ascending or descending consecutive notes
				if (numNotes >= 3) {
					sca[i] = IS_SCALAR_MOTION;
					for (int k=i; k<(i+numNotes); ++k) {
						used[k] = true;
					}
				}
			}
		}

		ArrayList<Integer> scaList = new ArrayList<Integer>(length);
		for (int i=0; i<length; ++i) {
			scaList.add(i, Integer.valueOf(sca[i]));
		}
		return scaList;
	}

	/********************************************************************/
	/*                GetRepetitions method                             */
	/********************************************************************/
	static ArrayList<Integer> GetRepetitions(int[] relativeIdList, ArrayList<Double> durationList, boolean[] usedIn) {
		int length = relativeIdList.length;
		int[] rep = new int[length];
		boolean[] used;

		if (relativeIdList.length != durationList.size()) {
			Log.e(TAG, "Repetition note id and duration list sizes don't match");
		}
		if (usedIn != null) {
			if (usedIn.length != durationList.size()) {
				Log.e(TAG, "Repetition used list and duration list sizes don't match");
				used = new boolean[length];
			} else {
				used = usedIn;
			}
		} else {
			used = new boolean[length];
		}

		for (int i=0; i<length-1; ++i) {
			if (!used[i] && relativeIdList[i] != 0) {
				for (int j=i+3; j<length; ++j) {
					if (!used[j] && relativeIdList[j] != 0) {
						int numNotes = 0;
						int k=0;
						while (true) {
							// Skip if reached end of score or note is 0
							if (j+k >= length || relativeIdList[i+k] == 0 || relativeIdList[j+k] == 0) {
								break;
							}

							int pitchDiff = relativeIdList[i+k]-relativeIdList[j+k];
							double durationDiff = durationList.get(i+k).doubleValue()-durationList.get(j+k).doubleValue();
							// Pitch and duration must be the same
							if (pitchDiff==0 && durationDiff==0) {
								++numNotes;
							} else {
								break;
							}

							++k;
						}

						// Requires at least 3 notes		
						if (numNotes >= 3) {
							if (!used[i]) {
								rep[i] = i+1;
								for (int l=i; l<(i+numNotes); ++l) {
									used[l] = true;
								}
							}
							rep[j] = rep[i];
							for (int l=j; l<(j+numNotes); ++l) {
								used[l] = true;
							}
						}
					}
				}
			}
		}

		ArrayList<Integer> repList = new ArrayList<Integer>(length);
		for (int i=0; i<length; ++i) {
			repList.add(i, Integer.valueOf(rep[i]));
		}
		return repList;
	}

	/********************************************************************/
	/*                       GetMotives method                          */
	/********************************************************************/
	// Expected value, with equal probability for each value (average)
	static double expectedValue(double[] data) {		
		double sum = 0;
		int length = data.length;
		for (int i=0; i<length; ++i) {
			sum += data[i];
		}
		return (sum/length);
	}

	// Standard deviation of data set
	static double stddev(double[] data) {
		double meanSqr = 0;
		double avg = expectedValue(data);
		int length = data.length;
		for (int i=0; i<data.length; ++i) {
			meanSqr += data[i]*data[i];
		}
		double variance = meanSqr/length-avg*avg;
		return Math.sqrt(variance);
	}

	// Autocorrelation of data set 1 and 2 with a shift of time 0
	static double autocorrelation(double[] data1, double[] data2, int len) {
		int length = Math.min(data1.length, data2.length);
		length = Math.min(length, len);

		if (length <= 0) {
			Log.e(TAG, "Zero/negative length for autocorrelation arguments");
			length = 0;
		}

		double sumError = 0;			
		double avg1 = expectedValue(data1);
		double avg2 = expectedValue(data2);
		for (int i=0; i<length; ++i) {
			sumError += (data1[i]-avg1)*(data2[i]-avg2);
		}

		double varSqr = stddev(data1)*stddev(data2);
		double autocor;
		if (varSqr != 0) {
			autocor = sumError/varSqr;
		} else {
			Log.e(TAG, "Variance is 0; risk of divide by 0");
			autocor = 0;
		}

		return autocor;
	}

	static int isMotive(double[] data1, double[] data2, int type) {
		final int PITCH = 0;
		final int MOTIVE_LENGTH_MIN = 4;
		final int MOTIVE_LENGTH_MAX = 10+1;
		final double MOTIVE_SCORE_THRESHOLD = 3.0d;

		int min = Math.min(data1.length, data2.length);

		double maxCorrelation = 0;
		int maxCorrelationLength = 0;
		for (int i=MOTIVE_LENGTH_MIN; i<MOTIVE_LENGTH_MAX; ++i) {
			double score = autocorrelation(data1, data2, i);
			if (score > maxCorrelation) {
				maxCorrelation = score;
				maxCorrelationLength = i;
			}
		}
		int result;
		if (maxCorrelation > MOTIVE_SCORE_THRESHOLD) {
			result = maxCorrelationLength;
		} else {
			result = 0;
		}

		if (type == PITCH) {
			// Ensure at least half the notes in a motive have the same first differences
			int firstDiffCount = 0;
			int half = maxCorrelationLength/2;
			int end = Math.min(maxCorrelationLength, min);
			for (int i=0; i<end-1; ++i) {
				double diff1 = data1[i+1]-data1[i];
				double diff2 = data2[i+1]-data2[i];
				if (diff1 == diff2) {
					firstDiffCount++;
				}
			}
			if (firstDiffCount < half) {
				result = 0;
			}
		}

		return result;
	}

	static ArrayList<Integer> GetMotives(int[] relativeIdList, ArrayList<Double> durationList, boolean[] usedIn) {
		int length = relativeIdList.length;
		int[] mot = new int[length];
		boolean[] used;

		if (length != durationList.size()) {
			Log.e(TAG, "Motive note id and duration list sizes don't match");
		}
		if (usedIn != null) {
			if (usedIn.length != durationList.size()) {
				Log.e(TAG, "Repetition used list and duration list sizes don't match");
				used = new boolean[length];
			} else {
				used = usedIn;
			}
		} else {
			used = new boolean[length];
		}

		double[] data1Pitch, data1Duration;
		double[] data2Pitch, data2Duration;

		// Window of notes being compared
		for (int i=0; i<length; ++i) {						
			if (!used[i] && relativeIdList[i] != 0) {
				// Fill in data for data1
				int mLength = Math.min(i+10, length)-i;
				data1Pitch = new double[mLength];
				data1Duration = new double[mLength];
				for (int m=0; m<mLength; ++m) {
					data1Pitch[m] = relativeIdList[i+m];
				}
				data1Duration = new double[mLength];
				for (int m=0; m<mLength; ++m) {
					data1Duration[m] = durationList.get(i+m).doubleValue();
				}

				// Window of notes compared to
				for (int j=i+4; j<length; ++j) {
					if (!used[j] && relativeIdList[j] != 0) {
						// Fill in data for data2
						int nLength = Math.min(j+10, length)-j;
						data2Pitch = new double[nLength];
						for (int n=0; n<nLength; ++n) {
							data2Pitch[n] = relativeIdList[j+n];
						}	
						data2Duration = new double[nLength];
						for (int n=0; n<nLength; ++n) {
							data2Duration[n] = durationList.get(j+n).doubleValue();
						}

						// Determine if the pitch and duration is motivic
						int lengthPitch = isMotive(data1Pitch, data2Pitch, 0);
						int lengthDuration = isMotive(data1Duration, data2Duration, 1);
						int numNotes = Math.min(lengthPitch, lengthDuration);

						if (numNotes >= 4) {
							if (!used[i]) {
								mot[i] = i+1;
								for (int l=i; l<(i+numNotes); ++l) {
									used[l] = true;
								}
							}
							mot[j] = mot[i];
							for (int l=j; l<(j+numNotes); ++l) {
								used[l] = true;
							}
						}
					}
				}
			}
		}

		ArrayList<Integer> motList = new ArrayList<Integer>(length);
		for (int i=0; i<length; ++i) {
			motList.add(i, Integer.valueOf(mot[i]));
		}
		return motList;
	}
}
