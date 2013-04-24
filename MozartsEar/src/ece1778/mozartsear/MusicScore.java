package ece1778.mozartsear;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class MusicScore extends View {
	// Major key internal IDs
	enum KeyId {
		KID_C_MAJOR,
		KID_G_MAJOR,
		KID_D_MAJOR,
		KID_A_MAJOR,
		KID_E_MAJOR,
		KID_B_MAJOR,
		KID_F_SHARP_MAJOR,
		KID_C_SHARP_MAJOR,
		KID_F_MAJOR,
		KID_B_FLAT_MAJOR,
		KID_E_FLAT_MAJOR,
		KID_A_FLAT_MAJOR,
		KID_D_FLAT_MAJOR,
		KID_G_FLAT_MAJOR,
		KID_C_FLAT_MAJOR,
	};

	private static final String TAG = "MusicScore";

	// Notes and rests
	private static final int DOUBLE_WHOLE = 0;
	private static final int WHOLE = 1;
	private static final int HALF = 2;
	private static final int QUARTER = 3;
	private static final int EIGHTH = 4;
	private static final int SIXTEENTH = 5;
	private static final int NOTES_COUNT = 6;

	// Symbols (from images)
	private static final int TREBLE_CLEF = 0;
	private static final int BASE_CLEF = 1;
	private static final int TIME_SIGNATURE_44 = 2;
	private static final int NATURAL = 3;
	private static final int SHARP = 4;
	private static final int FLAT = 5;
	private static final int TEMPO = 6;
	private static final int STAFF = 7;
	private static final int LEDGER = 8;
	private static final int BAR = 9;
	private static final int END_BAR = 10;
	private static final int SYMBOLS_COUNT = 11;

	// More symbols (derived from multiple images)
	private static final int KEY = 100;

	// Draw dot for dotted notes
	private static final int DOT_RADIUS = 5;
	private static final int DOT_OFFSET_X = 58;
	private static final int DOT_OFFSET_Y = 126;

	private static final int DEFAULT_DISTANCE = 74;	// pixels
	private static final int X = 0;
	private static final int Y = 1;
	private static final int NOTE_DIFF = 12;	// pixel distance between a natural and a sharp/flat
	private static final int STAFF_DIFF = NOTE_DIFF*12;	// pixel distance between first treble and base clef

	public static final boolean DOTTED = true;
	public static final boolean NOT_DOTTED = false;
	public static final char ACCIDENTAL_NATURAL = 'n';
	public static final char ACCIDENTAL_SHARP = '#';
	public static final char ACCIDENTAL_FLAT = 'b';
	public static final char ACCIDENTAL_NONE = '0';

	// Music letters
	private static final int A = 0;
	private static final int B = 1;
	private static final int C = 2;
	private static final int D = 3;
	private static final int E = 4;
	private static final int F = 5;
	private static final int G = 6;	

	private Bitmap[] note;
	private Bitmap[] rest;
	private Bitmap[] symbol;
	private Canvas canvas;
	private Paint blackPaint;
	private Paint blackFillPaint;

	private int[] rel;	// relative X and Y coordinate for ALL items drawn
	private int nextNoteX;	// X coordinates for next note
	private int nextStaffX;	// X coordinates for next staff segment

	private float[] scale; // canvas scaling factor
	private float[] pivot; // pivot point for scaling the canvas

	private int[] notePositionX = null;	// X coordinate for where the note was placed
	private int[] noteRelativeId = null;	// Precomputed note Id
	private char[] noteAccidental = null;	// Precomputed accidental for the note

	private ArrayList<Integer> notePitch = null;
	private ArrayList<Double> noteDuration = null;
	private KeyId keyId = null;
	private char[] keySet = {'n','n','n','n','n','n','n'};	// notes in a key
	private int tempo = 0;

	public MusicScore(Context context) {
		super(context);

		rel = new int[2];
		rel[X] = 0;
		rel[Y] = 60;

		scale = new float[2];
		pivot = new float[2];
		scale[X] = 1f;
		scale[Y] = 1f;
		pivot[X] = 0f;
		pivot[Y] = 0f;

		blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		blackPaint.setColor(Color.BLACK);
		blackPaint.setTextSize(36f);
		blackPaint.setStyle(Style.STROKE);

		blackFillPaint = new Paint();
		blackFillPaint.setColor(Color.BLACK);
		blackFillPaint.setStyle(Style.FILL);

		note = new Bitmap[NOTES_COUNT];
		note[DOUBLE_WHOLE] = BitmapFactory.decodeResource(getResources(), R.drawable.doublewholenote);
		note[WHOLE] = BitmapFactory.decodeResource(getResources(), R.drawable.wholenote);
		note[HALF] = BitmapFactory.decodeResource(getResources(), R.drawable.halfnote);
		note[QUARTER] = BitmapFactory.decodeResource(getResources(), R.drawable.quarternote);
		note[EIGHTH] = BitmapFactory.decodeResource(getResources(), R.drawable.eighthnote);
		note[SIXTEENTH] = BitmapFactory.decodeResource(getResources(), R.drawable.sixteenthnote);

		rest = new Bitmap[NOTES_COUNT];
		rest[DOUBLE_WHOLE] = BitmapFactory.decodeResource(getResources(), R.drawable.doublewholerest);
		rest[WHOLE] = BitmapFactory.decodeResource(getResources(), R.drawable.wholerest);
		rest[HALF] = BitmapFactory.decodeResource(getResources(), R.drawable.halfrest);
		rest[QUARTER] = BitmapFactory.decodeResource(getResources(), R.drawable.quarterrest);
		rest[EIGHTH] = BitmapFactory.decodeResource(getResources(), R.drawable.eighthrest);
		rest[SIXTEENTH] = BitmapFactory.decodeResource(getResources(), R.drawable.sixteenthrest);

		symbol = new Bitmap[SYMBOLS_COUNT];
		symbol[TREBLE_CLEF] = BitmapFactory.decodeResource(getResources(), R.drawable.trebleclef);
		symbol[BASE_CLEF] = BitmapFactory.decodeResource(getResources(), R.drawable.baseclef);
		symbol[TIME_SIGNATURE_44] = BitmapFactory.decodeResource(getResources(), R.drawable.timesignature_44);
		symbol[NATURAL] = BitmapFactory.decodeResource(getResources(), R.drawable.natural);
		symbol[SHARP] = BitmapFactory.decodeResource(getResources(), R.drawable.sharp);
		symbol[FLAT] = BitmapFactory.decodeResource(getResources(), R.drawable.flat);
		symbol[TEMPO] = BitmapFactory.decodeResource(getResources(), R.drawable.tempo);
		symbol[STAFF] = BitmapFactory.decodeResource(getResources(), R.drawable.staff);
		symbol[LEDGER] = BitmapFactory.decodeResource(getResources(), R.drawable.ledger);
		symbol[BAR] = BitmapFactory.decodeResource(getResources(), R.drawable.bar);
		symbol[END_BAR] = BitmapFactory.decodeResource(getResources(), R.drawable.endbar);
	}

	@Override
	protected void onDraw(Canvas c) {
		canvas = c;
		canvas.scale(scale[X], scale[Y], pivot[X], pivot[Y]);
		drawMusicScore();

		// Auto size the scroll width
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
			width = nextStaffX;
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

	// Call this immediately after the class object is created!!!
	// Nothing will show up on screen without it.
	public void setMusicContents(ArrayList<Integer> notePitch, ArrayList<Double> noteDuration) {
		this.notePitch = notePitch;
		this.noteDuration = noteDuration;

		if (notePitch == null || noteDuration == null) {
			Log.e(TAG, "Music content arguments null - notePitch:"+(notePitch == null)+" noteDuration:"+(noteDuration == null));
		} else {
			int length = notePitch.size();
			if (length > 0) {
				notePositionX = new int[length];
				noteRelativeId = new int[length];
				noteAccidental = new char[length];

				// Precompute note positions on the staff and accidentals
				for (int i=0; i<length; i++) {
					int noteId = getNoteId(notePitch.get(i).intValue());

					// This tells us where to place the note on the staff
					RelativeNoteIdInfo noteInfo = getRelativeNoteInfo(noteId);										

					noteRelativeId[i] = noteInfo.relNoteId;
					noteAccidental[i] = noteInfo.accidental;		
				}
			} else {
				Log.v(TAG, "Input notePitch ArrayList has length: "+length);
			}

			if (noteDuration.size() <= 0) {
				Log.e(TAG, "Input noteDuration ArrayList has length: "+noteDuration.size());
			}
		}
	}

	// Returns the position of the note on the staff as an integer (line/space)
	public int[] getRelativeNoteList() {
		return noteRelativeId;
	}

	// Returns the X coordinates of each note; this array is filled after the score is drawn the first time
	public int[] getNotePositionList() {		
		return notePositionX;
	}

	private void drawMusicScore() {
		// Notes based on beat
		final double FULL_BAR = 4;	// 4 beats in a bar
		final double BEATS_DOUBLE_WHOLE = 8d;
		final double BEATS_WHOLE = 4d;
		final double BEATS_DOTTED_HALF = 3d;
		final double BEATS_HALF = 2d;
		final double BEATS_DOTTED_QUARTER = 1.5d;
		final double BEATS_QUARTER = 1d;
		final double BEATS_DOTTED_EIGHTH = 0.75d;		
		final double BEATS_EIGHTH = 0.5;
		final double BEATS_SIXTEENTH = 0.25d;

		if (tempo <= 0) {
			Log.w(TAG, "Tempo uninitialized");
		}

		if (notePitch == null || noteDuration == null) {
			Log.e(TAG, "Music content arguments null. Did you forget to call setMusicContents?");
		} else if (keyId == null) {
			Log.e(TAG, "Music key has not been set");
		} else if (tempo == 0) {
			Log.e(TAG, "Tempo has not been set");
		} else if (notePitch.size() != noteDuration.size()) {	
			Log.e(TAG, "ArrayList size mismatch: noteId size != noteDuration size");
		} else {
			double barGauge = 0;	// Number of beats we've filled a bar with; When this reaches exactly 4, we draw a bar

			// Reposition starting points
			nextNoteX = 0;
			nextStaffX = 0;	

			// Setup the score
			drawSymbol(TREBLE_CLEF);	// Draws BOTH treble and base clef
			drawSymbol(KEY);
			drawSymbol(TIME_SIGNATURE_44);
			drawSymbol(TEMPO);		
			drawSymbol(STAFF);
			drawSymbol(STAFF);

			// Draw the notes
			int size=notePitch.size();
			for (int i=0; i<size; i++) {
				if (barGauge >= FULL_BAR) {
					barGauge = 0d;
					drawSymbol(BAR);
				}			

				int note = notePitch.get(i).intValue();
				double duration = noteDuration.get(i).doubleValue();				

				if (duration == BEATS_DOUBLE_WHOLE) {					
					barGauge += BEATS_DOUBLE_WHOLE;
					drawNote(DOUBLE_WHOLE, note);
					// TODO setting this repeatedly is very inefficient
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_WHOLE) {					
					barGauge += BEATS_WHOLE;
					drawNote(WHOLE, note);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_DOTTED_HALF) {
					barGauge += BEATS_DOTTED_HALF;
					drawNote(HALF, note, DOTTED);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_HALF) {
					barGauge += BEATS_HALF;
					drawNote(HALF, note);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_DOTTED_QUARTER) {
					barGauge += BEATS_HALF;
					drawNote(QUARTER, note, DOTTED);	
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_QUARTER) {
					barGauge += BEATS_QUARTER;
					drawNote(QUARTER, note);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_DOTTED_EIGHTH) {
					barGauge += BEATS_EIGHTH;
					drawNote(EIGHTH, note, DOTTED);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_EIGHTH) {
					barGauge += BEATS_EIGHTH;
					drawNote(EIGHTH, note);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else if (duration == BEATS_SIXTEENTH) {
					barGauge += BEATS_SIXTEENTH;
					drawNote(SIXTEENTH, note);
					notePositionX[i] = nextNoteX-DEFAULT_DISTANCE+100;
				} else {
					Log.w(TAG, "Unsupported note detected. note="+note+" duration="+duration);
				}	
			}
			drawSymbol(END_BAR);
		}
	}

	// Auto adjust the scroll width to the end of the music score
	public void updateScrollWidth() {
		LayoutParams lp = getLayoutParams();	
		lp.width = nextStaffX;
		lp.height = LayoutParams.WRAP_CONTENT;
		setLayoutParams(lp);
	}

	// Set the music key and save it as an internal key ID
	// Initialize the keySet corresponding to the key
	public void setKey(String key) {
		this.keyId = getKeyId(key);

		switch (keyId) {
		case KID_C_SHARP_MAJOR:
			this.keySet[B] = ACCIDENTAL_SHARP;	// B#
		case KID_F_SHARP_MAJOR:
			this.keySet[E] = ACCIDENTAL_SHARP;	// E#
		case KID_B_MAJOR:
			this.keySet[A] = ACCIDENTAL_SHARP;	// A#
		case KID_E_MAJOR:
			this.keySet[D] = ACCIDENTAL_SHARP;	// D#
		case KID_A_MAJOR:
			this.keySet[G] = ACCIDENTAL_SHARP;	// G#		
		case KID_D_MAJOR:
			this.keySet[C] = ACCIDENTAL_SHARP;	// C#
		case KID_G_MAJOR:
			this.keySet[F] = ACCIDENTAL_SHARP;	// F#
		case KID_C_MAJOR:
			break;
		case KID_C_FLAT_MAJOR:			
			this.keySet[F] = ACCIDENTAL_FLAT;	// Fb
		case KID_G_FLAT_MAJOR:			
			this.keySet[C] = ACCIDENTAL_FLAT;	// Cb
		case KID_D_FLAT_MAJOR:			
			this.keySet[G] = ACCIDENTAL_FLAT;	// Gb
		case KID_A_FLAT_MAJOR:			
			this.keySet[D] = ACCIDENTAL_FLAT;	// Db
		case KID_E_FLAT_MAJOR:			
			this.keySet[A] = ACCIDENTAL_FLAT;	// Ab
		case KID_B_FLAT_MAJOR:			
			this.keySet[E] = ACCIDENTAL_FLAT;	// Eb
		case KID_F_MAJOR:			
			this.keySet[B] = ACCIDENTAL_FLAT;	// Bb
			break;		
		default:
			Log.e(TAG, "Cannot initialize keySet for unidentified key");
			break;
		}
	}

	// convert string key to key ID
	private KeyId getKeyId(String key) {
		// Music major key
		final String C_MAJOR = "C";
		final String G_MAJOR = "G";
		final String D_MAJOR = "D";
		final String A_MAJOR = "A";
		final String E_MAJOR = "E";
		final String B_MAJOR = "B";
		final String F_SHARP_MAJOR = "F#";
		final String C_SHARP_MAJOR = "C#";
		final String F_MAJOR = "F";
		final String B_FLAT_MAJOR = "Bb";
		final String E_FLAT_MAJOR = "Eb";
		final String A_FLAT_MAJOR = "Ab";
		final String D_FLAT_MAJOR = "Db";
		final String G_FLAT_MAJOR = "Gb";
		final String C_FLAT_MAJOR = "Cb";

		// Determine key id
		if (key.equals(C_MAJOR)) {
			keyId = KeyId.KID_C_MAJOR;
		} else if (key.equals(G_MAJOR)) {
			keyId = KeyId.KID_G_MAJOR;
		} else if (key.equals(D_MAJOR)) {
			keyId = KeyId.KID_D_MAJOR;
		} else if (key.equals(A_MAJOR)) {
			keyId = KeyId.KID_A_MAJOR;
		} else if (key.equals(E_MAJOR)) {
			keyId = KeyId.KID_E_MAJOR;
		} else if (key.equals(B_MAJOR)) {
			keyId = KeyId.KID_B_MAJOR;
		} else if (key.equals(F_SHARP_MAJOR)) {
			keyId = KeyId.KID_F_SHARP_MAJOR;
		} else if (key.equals(C_SHARP_MAJOR)) {
			keyId = KeyId.KID_C_SHARP_MAJOR;
		} else if (key.equals(F_MAJOR)) {
			keyId = KeyId.KID_F_MAJOR;
		} else if (key.equals(B_FLAT_MAJOR)) {
			keyId = KeyId.KID_B_FLAT_MAJOR;
		} else if (key.equals(E_FLAT_MAJOR)) {
			keyId = KeyId.KID_E_FLAT_MAJOR;
		} else if (key.equals(A_FLAT_MAJOR)) {
			keyId = KeyId.KID_A_FLAT_MAJOR;
		} else if (key.equals(D_FLAT_MAJOR)) {
			keyId = KeyId.KID_D_FLAT_MAJOR;
		} else if (key.equals(G_FLAT_MAJOR)) {
			keyId = KeyId.KID_G_FLAT_MAJOR;
		} else if (key.equals(C_FLAT_MAJOR)) {
			keyId = KeyId.KID_C_FLAT_MAJOR;
		} else {
			Log.e(TAG, "Unknown music key: "+key);
		}

		return keyId;
	}

	// Draw key signature
	// TODO too long; ugly
	// create images?
	private void drawKey() {		
		// Draw the key on both treble and base clef
		switch (keyId) {
		case KID_C_SHARP_MAJOR:			
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			drawAccidental(52, ACCIDENTAL_SHARP);	// C#
			drawAccidental(59, ACCIDENTAL_SHARP);	// G#
			drawAccidental(54, ACCIDENTAL_SHARP);	// D#
			drawAccidental(49, ACCIDENTAL_SHARP);	// A#
			drawAccidental(56, ACCIDENTAL_SHARP);	// E#
			drawAccidental(51, ACCIDENTAL_SHARP);	// B#
			break;
		case KID_F_SHARP_MAJOR:			
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			drawAccidental(52, ACCIDENTAL_SHARP);	// C#
			drawAccidental(59, ACCIDENTAL_SHARP);	// G#
			drawAccidental(54, ACCIDENTAL_SHARP);	// D#
			drawAccidental(49, ACCIDENTAL_SHARP);	// A#
			drawAccidental(56, ACCIDENTAL_SHARP);	// E#
			break;
		case KID_B_MAJOR:			
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			drawAccidental(52, ACCIDENTAL_SHARP);	// C#
			drawAccidental(59, ACCIDENTAL_SHARP);	// G#
			drawAccidental(54, ACCIDENTAL_SHARP);	// D#
			drawAccidental(49, ACCIDENTAL_SHARP);	// A#
			break;
		case KID_E_MAJOR:		
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			drawAccidental(52, ACCIDENTAL_SHARP);	// C#
			drawAccidental(59, ACCIDENTAL_SHARP);	// G#
			drawAccidental(54, ACCIDENTAL_SHARP);	// D#
			break;
		case KID_A_MAJOR:				
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			drawAccidental(52, ACCIDENTAL_SHARP);	// C#
			drawAccidental(59, ACCIDENTAL_SHARP);	// G#
			break;
		case KID_D_MAJOR:
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			drawAccidental(52, ACCIDENTAL_SHARP);	// C#
			break;
		case KID_G_MAJOR:			
			drawAccidental(57, ACCIDENTAL_SHARP);	// F#
			break;
		case KID_C_MAJOR:
			break;
		case KID_C_FLAT_MAJOR:		
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			drawAccidental(56, ACCIDENTAL_FLAT);	// Eb
			drawAccidental(49, ACCIDENTAL_FLAT);	// Ab
			drawAccidental(54, ACCIDENTAL_FLAT);	// Db
			drawAccidental(47, ACCIDENTAL_FLAT);	// Gb
			drawAccidental(52, ACCIDENTAL_FLAT);	// Cb
			drawAccidental(45, ACCIDENTAL_FLAT);	// Fb
			break;
		case KID_G_FLAT_MAJOR:		
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			drawAccidental(56, ACCIDENTAL_FLAT);	// Eb
			drawAccidental(49, ACCIDENTAL_FLAT);	// Ab
			drawAccidental(54, ACCIDENTAL_FLAT);	// Db
			drawAccidental(47, ACCIDENTAL_FLAT);	// Gb
			drawAccidental(52, ACCIDENTAL_FLAT);	// Cb
			break;
		case KID_D_FLAT_MAJOR:			
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			drawAccidental(56, ACCIDENTAL_FLAT);	// Eb
			drawAccidental(49, ACCIDENTAL_FLAT);	// Ab
			drawAccidental(54, ACCIDENTAL_FLAT);	// Db
			drawAccidental(47, ACCIDENTAL_FLAT);	// Gb
			break;
		case KID_A_FLAT_MAJOR:			
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			drawAccidental(56, ACCIDENTAL_FLAT);	// Eb
			drawAccidental(49, ACCIDENTAL_FLAT);	// Ab
			drawAccidental(54, ACCIDENTAL_FLAT);	// Db
			break;
		case KID_E_FLAT_MAJOR:		
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			drawAccidental(56, ACCIDENTAL_FLAT);	// Eb
			drawAccidental(49, ACCIDENTAL_FLAT);	// Ab
			break;
		case KID_B_FLAT_MAJOR:			
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			drawAccidental(56, ACCIDENTAL_FLAT);	// Eb
			break;
		case KID_F_MAJOR:			
			drawAccidental(51, ACCIDENTAL_FLAT);	// Bb
			break;		
		default:
			Log.e(TAG, "Cannot draw unsupported music key");
			break;
		}
	}

	// Sets the tempo; recommended to call this immediately after the class object is created
	public void setTempo(int t) {
		this.tempo = t;
	}

	// Draw the note (not dotted)
	public void drawNote(int type, int pitch) {
		drawNote(type, pitch, NOT_DOTTED);
	}

	// Draw the accidental at a specific height on the staff (for key signature use!!!)
	// Draws the accidental at the specified pitch and also 2 octaves below it
	private void drawAccidental(int pitch, char accidental) {
		// This tells us where to place the note on the staff
		int noteId = getNoteId(pitch);
		int offset = NOTE_DIFF*(getRelativeNoteInfo(noteId).relNoteId-23);

		// Draw the natural 'n', sharp '#', or flat 'b' accidental
		switch (accidental) {
		case ACCIDENTAL_NATURAL:
			canvas.drawBitmap(symbol[NATURAL], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
			canvas.drawBitmap(symbol[NATURAL], rel[X]+nextNoteX, rel[Y]+11*NOTE_DIFF+offset, null);
			nextNoteX += 24;
			break;
		case ACCIDENTAL_SHARP:
			canvas.drawBitmap(symbol[SHARP], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
			canvas.drawBitmap(symbol[SHARP], rel[X]+nextNoteX, rel[Y]+11*NOTE_DIFF+offset, null);
			nextNoteX += 24;
			break;
		case ACCIDENTAL_FLAT:
			canvas.drawBitmap(symbol[FLAT], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
			canvas.drawBitmap(symbol[FLAT], rel[X]+nextNoteX, rel[Y]+11*NOTE_DIFF+offset, null);
			nextNoteX += 24;
			break;
		default:
			Log.w(TAG, "Unsupported accidental "+accidental);
			break;
		}
	}

	// Convert from pitch number to note Id
	private int getNoteId(int pitch) {
		final int NOTES = 88;
		final int REST = 0;

		int noteId;
		if (pitch == 0) {
			noteId = REST;
		} else {
			noteId = NOTES-pitch+1;
		}
		return noteId;
	}		

	// Draw the note: of type/duration, pitch (ignores the accidental parameter)
	// OR draw the accidental alone (uses pitch and accidental parameter, type=NONE)
	// Boundaries where you start placing ledger lines
	private void drawNote(int type, int pitch, boolean dotted) {
		final int A5 = 28;
		final int C4 = 49;
		final int E2 = 69;
		int A5_REL = getRelativeNoteInfo(A5).relNoteId; 
		int C4_REL = getRelativeNoteInfo(C4).relNoteId;
		int E2_REL = getRelativeNoteInfo(E2).relNoteId;

		// Ledger line offsets
		final int LEDGER_X_OFFSET = 66;

		if (pitch < 0 || pitch > 88) {
			Log.w(TAG, "Invalid note pitch "+pitch+". Nothing will be drawn.");			
		} else if (pitch == 0) {
			// Draw rests
			drawRest(type, dotted);
		} else {
			// Invert noteID to make graphics positioning easier 
			int noteId = getNoteId(pitch);

			// This tells us where to place the note on the staff
			RelativeNoteIdInfo noteInfo = getRelativeNoteInfo(noteId);
			int offset = NOTE_DIFF*(noteInfo.relNoteId-23);
			char accidental = noteInfo.accidental;

			//Log.v(TAG, "Accidental: "+accidental);

			// Draw the natural 'n', sharp '#', or flat 'b' accidental
			// according to the key signature
			switch (accidental) {
			case ACCIDENTAL_NATURAL:
				canvas.drawBitmap(symbol[NATURAL], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
				nextNoteX += 14;
				break;
			case ACCIDENTAL_SHARP:
				canvas.drawBitmap(symbol[SHARP], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
				nextNoteX += 14;
				break;
			case ACCIDENTAL_FLAT:
				canvas.drawBitmap(symbol[FLAT], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
				nextNoteX += 14;
				break;
			case ACCIDENTAL_NONE:
				// Don't draw anything
				break;
			default:
				Log.w(TAG, "Unsupported accidental "+accidental+" used in music notes");
				break;
			}

			// Draw ledger lines if necessary (do this before drawing the notes)
			if (noteInfo.relNoteId == C4_REL) {
				canvas.drawBitmap(symbol[LEDGER], rel[X]+nextNoteX+LEDGER_X_OFFSET, rel[Y]+50+10*NOTE_DIFF, null);
			} else if (noteInfo.relNoteId <= A5_REL) {		
				int m = numStaffLinesBetween(A5+2, noteId);
				while (m > 0) {
					canvas.drawBitmap(symbol[LEDGER], rel[X]+nextNoteX+LEDGER_X_OFFSET, rel[Y]+50-m*2*NOTE_DIFF, null);
					m--;
				}				
			} else if (noteInfo.relNoteId >= E2_REL) {				
				int m = numStaffLinesBetween(E2-1, noteId);
				while (m > 0) {
					canvas.drawBitmap(symbol[LEDGER], rel[X]+nextNoteX+LEDGER_X_OFFSET, rel[Y]+50+(m*2+20)*NOTE_DIFF, null);
					m--;
				}		
			}	

			// Draw the note (also increment next note position)
			switch (type) {
			case DOUBLE_WHOLE:
			case WHOLE:
				canvas.drawBitmap(note[type], rel[X]+nextNoteX, rel[Y]+50+offset, null);
				nextNoteX += DEFAULT_DISTANCE;
				if (dotted == DOTTED) {
					canvas.drawOval(new RectF(rel[X]+nextNoteX+DOT_OFFSET_X-DOT_RADIUS,
							rel[Y]+DOT_OFFSET_Y-DOT_RADIUS+50+offset,
							rel[X]+nextNoteX+DOT_OFFSET_X+DOT_RADIUS,
							rel[Y]+DOT_OFFSET_Y+DOT_RADIUS+50+offset), blackFillPaint);
				}
				break;
			case HALF:
			case QUARTER:
			case EIGHTH:
			case SIXTEENTH:
				canvas.drawBitmap(note[type], rel[X]+nextNoteX, rel[Y]-3*NOTE_DIFF+offset, null);
				nextNoteX += DEFAULT_DISTANCE;
				if (dotted == DOTTED) {
					canvas.drawOval(new RectF(rel[X]+nextNoteX+DOT_OFFSET_X-DOT_RADIUS,
							rel[Y]+DOT_OFFSET_Y-DOT_RADIUS-3*NOTE_DIFF+offset,
							rel[X]+nextNoteX+DOT_OFFSET_X+DOT_RADIUS,
							rel[Y]+DOT_OFFSET_Y+DOT_RADIUS-3*NOTE_DIFF+offset), blackFillPaint);
				}
				break;
			default:
				Log.w(TAG, "Invalid note type "+type+". Nothing will be drawn.");
				break;
			}

			// Extend the staff
			if (nextNoteX > nextStaffX) {
				drawSymbol(STAFF);
			}		
		}
	}

	// Determines whether the input pitch has an enharmonic equivalent note
	// Pitch numbers start at 1(highest) and end at 88(lowest)
	// a 1 means the note is one that has an enharmonic equivalent; a 0 means it doesn't
	private boolean hasEnharmonicEquivalent(int noteId) {
		if (noteId < 0 || noteId > 87) {
			Log.w(TAG, "Note pitch out of valid range. Cannot determine enharmonic equivalence.");			
		}

		int modulus = (noteId-1)%12;
		boolean enhEqu;
		switch (modulus) {
		case 11:
		case 9:
		case 6:
		case 4:
		case 2:
			enhEqu = true;
			break;
		default:
			enhEqu = false;
			break;
		}

		return enhEqu;
	}

	// Count how many staff lines are between 2 notes
	private int numStaffLinesBetween(int noteIdA, int noteIdB) {
		int absDiff = Math.abs(getRelativeNoteInfo(noteIdA).relNoteId-getRelativeNoteInfo(noteIdB).relNoteId);
		int staffLines = absDiff/2 + absDiff%2;

		return staffLines;		
	}

	// Returns music letter (0-6 which represents A-G)
	private int getMusicLetter(int relativeNoteId) {
		int flip = 7-(relativeNoteId%7);
		return (flip+3)%7;
	}

	class RelativeNoteIdInfo {
		public int relNoteId;
		public char accidental;
	}

	// Determine the pitch number if we were to remove all enharmonic equivalent notes
	// Calculates it based on the key signature
	// Determines the proper accidental for the note based on the key signature
	private RelativeNoteIdInfo getRelativeNoteInfo(int noteId) {
		if (keyId == null) {
			Log.e(TAG, "Cannot determine relative note Id without music key");
			return null;
		}

		int modulus = (noteId-1)%12;
		int quotient = (noteId-1)/12;

		// By default assumes flat for enharmonic equivalent notes
		int offset;
		if (modulus >= 11) {
			offset = 5;
		} else if (modulus >= 9) {
			offset = 4;
		} else if (modulus >= 6) {
			offset = 3;
		} else if (modulus >= 4) {
			offset = 2;
		} else if (modulus >= 2) {
			offset = 1;
		} else {
			offset = 0;
		}
		int relNoteId = noteId-(5*quotient+offset);
		//Log.v(TAG, "noteId:"+noteId+" RelativeNoteId:"+relNoteId);


		// If enharmonic equivalent note, then look at key signature to determine if it
		// should be sharp instead of flat
		int adjustedRelNoteId = relNoteId;
		char accidental = ACCIDENTAL_NONE;
		if (hasEnharmonicEquivalent(noteId)) {		
			if (keySet[getMusicLetter(relNoteId)] == ACCIDENTAL_FLAT) {
				// Do nothing
			} else if (keySet[getMusicLetter(relNoteId+1)] == ACCIDENTAL_SHARP) {
				adjustedRelNoteId++;
			} else {
				// Default to #
				adjustedRelNoteId++;
				accidental = ACCIDENTAL_SHARP;
			}
		} else {
			if (keySet[getMusicLetter(relNoteId)] != ACCIDENTAL_NATURAL) {
				accidental = ACCIDENTAL_NATURAL;
			}
		}

		RelativeNoteIdInfo relNoteIdInfo = new RelativeNoteIdInfo();
		relNoteIdInfo.relNoteId = adjustedRelNoteId;
		relNoteIdInfo.accidental = accidental;
		return relNoteIdInfo;
	}

	private void drawRest(int type, boolean dotted) {
		switch (type) {
		case DOUBLE_WHOLE:
		case WHOLE:
		case HALF:
		case QUARTER:
		case EIGHTH:
		case SIXTEENTH:
			canvas.drawBitmap(rest[type], rel[X]+nextNoteX, rel[Y]+50, null);
			//canvas.drawBitmap(rest[type], rel[X]+nextNoteX, rel[Y]+50+STAFF_DIFF, null);
			nextNoteX += DEFAULT_DISTANCE;
			if (dotted == DOTTED) {
				canvas.drawOval(new RectF(rel[X]+nextNoteX+DOT_OFFSET_X-DOT_RADIUS,
						rel[Y]+DOT_OFFSET_Y-DOT_RADIUS-36,
						rel[X]+nextNoteX+DOT_OFFSET_X+DOT_RADIUS,
						rel[Y]+DOT_OFFSET_Y+DOT_RADIUS-36), blackFillPaint);
			}
			break;
		default:
			Log.w(TAG, "Invalid rest type "+type+". Nothing will be drawn.");
			break;
		}

		// Extend the staff
		if (nextNoteX > nextStaffX) {
			drawSymbol(STAFF);
		}	
	}

	private void drawSymbol(int type) {
		switch (type) {
		case TREBLE_CLEF:
		case BASE_CLEF:
			canvas.drawBitmap(symbol[TREBLE_CLEF], rel[X]+nextNoteX, rel[Y], null);
			canvas.drawBitmap(symbol[BASE_CLEF], rel[X]+nextNoteX, rel[Y]+50+STAFF_DIFF, null);
			nextNoteX += 70;
			break;
		case TIME_SIGNATURE_44:
			canvas.drawBitmap(symbol[TIME_SIGNATURE_44], rel[X]+nextNoteX, rel[Y]+50, null);
			canvas.drawBitmap(symbol[TIME_SIGNATURE_44], rel[X]+nextNoteX, rel[Y]+50+STAFF_DIFF, null);
			nextNoteX += 70;
			break;
		case NATURAL:
			Log.w(TAG, "Call to drawSymbol with argument NATURAL does nothing. Remove redundant call.");
		case SHARP:
			Log.w(TAG, "Call to drawSymbol with argument SHARP does nothing. Remove redundant call.");
		case FLAT:
			Log.w(TAG, "Call to drawSymbol with argument FLAT does nothing. Remove redundant call.");
			break;
		case TEMPO:
			canvas.drawBitmap(symbol[TEMPO], rel[X]+170, rel[Y]-60, null);
			canvas.drawText(Integer.toString(tempo), rel[X]+260, rel[Y]+2, blackPaint);
			break;
			// Draws the next staff segment
		case STAFF:
			canvas.drawBitmap(symbol[STAFF], rel[X]+nextStaffX, rel[Y]+50, null);
			canvas.drawBitmap(symbol[STAFF], rel[X]+nextStaffX, rel[Y]+50+STAFF_DIFF, null);
			nextStaffX += 200;	// increment next location by 200 pixels;
			break;
			// Draws all required ledger lines for the most recently placed note	
		case LEDGER:
			// Don't use this
			Log.w(TAG, "Call to drawSymbol with argument LEDGER does nothing. Remove redundant call.");
			break;
			// Draws a bar line to divide the bar
		case BAR:
			canvas.drawBitmap(symbol[BAR], rel[X]+nextNoteX, rel[Y]+50, null);
			canvas.drawBitmap(symbol[BAR], rel[X]+nextNoteX, rel[Y]+50+STAFF_DIFF, null);			
			nextNoteX += rel[X]+80;
			break;
			// Draws the end bar signifying the end of the music score
		case END_BAR:
			canvas.drawBitmap(symbol[END_BAR], rel[X]+nextStaffX-150, rel[Y]+50, null);
			canvas.drawBitmap(symbol[END_BAR], rel[X]+nextStaffX-150, rel[Y]+50+STAFF_DIFF, null);
			nextStaffX += 70;
			break;
		case KEY:
			// Draws the key signature
			drawKey();
			break;
		default:
			Log.w(TAG, "Invalid symbol type "+type+". Nothing will be drawn.");
			break;
		}
	}
}
