package ece1778.mozartsear;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AnalysisActivity extends Activity {

	private static final String TAG = "AnalysisActivity";       /* Used in Log messages */
	private static final String TAG2 = "PreAnalysisActivity";   /* Used in Log messages */
	private static final String TAG3 = "PostAnalysisActivity";  /* Used in Log messages */
	private static final String LIBRARY_ACTIVITY = "LibraryActivity";
	private static final String LISTEN_ACTIVITY = "ListenActivity";

	/* Layout variables */
	private ProgressBar progressBar;
	private TextView TextViewAnalysis;
	private String CallActivity;

	/* Algorithm variables */
	private int TempoValue_bpm;
	private int TempoValue_ms;
	private String KeyValue = "Unknown";
	private String AccidentalValue = "Unknown"; 
	private ArrayList<Double>  FrequencyList    = new ArrayList<Double>();    /* Frequencies [Hz] received from ListenActivity */
	private ArrayList<Double>  DurationList     = new ArrayList<Double>();    /* Duration [ms] received from ListenActivity */
	private ArrayList<String>  NoteList         = new ArrayList<String>();    /* Final note list as strings */
	private ArrayList<Integer> NoteListInteger  = new ArrayList<Integer>();   /* Final note list as integers 1 to 88 */
	private ArrayList<Integer> RepetitionList;   			/* Final REPETITION recognition list */
	private ArrayList<Integer> ScalarMotionList;   			/* Final SCALAR MOTION recognition list */
	private ArrayList<Integer> TriadList;   				/* Final TRIAD recognition list */
	private ArrayList<Integer> MotiveList;	 				/* Final MOTIVE recognition list */
	private ArrayList<Double>  NoteDuration     = new ArrayList<Double>();    /* Final note duration as a fraction of beat list */
	private NoteHandler myNoteHandler;
	private KeyHandler myKeyHandler;
	private DurationAsBeatFraction myDurationAsBeat;
	// TODO remove later private PatternRecognition myPatternRecognition;
	private boolean boNoteAnalysisCompleted;

	/* Status flags for async task handling and status report */
	AtomicBoolean boTotalAnalysisDone     = new AtomicBoolean(false);
	AtomicBoolean boUnknownKEY            = new AtomicBoolean(false);

	/*****************************************************************/
	/*                 onCreate                                      */
	/*****************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis);

		Bundle extras = getIntent().getExtras();
		CallActivity = extras.getString("activity_string");

		if (CallActivity.equals(LISTEN_ACTIVITY)) {
			TempoValue_ms = extras.getInt("tempo_ms");
			FrequencyList = (ArrayList<Double>) getIntent().getSerializableExtra("noteFrequency");
			DurationList  = (ArrayList<Double>) getIntent().getSerializableExtra("noteDuration");
		}
		if (CallActivity.equals(LIBRARY_ACTIVITY)) {
			KeyValue = extras.getString("key_string");
			NoteListInteger = (ArrayList<Integer>) getIntent().getSerializableExtra("noteList");
			NoteDuration  = (ArrayList<Double>) getIntent().getSerializableExtra("noteDuration");
		}

		TempoValue_bpm = extras.getInt("tempo_bpm");

		myNoteHandler = new NoteHandler();
		myKeyHandler = new KeyHandler(KeyValue, AccidentalValue);
		myDurationAsBeat = new DurationAsBeatFraction();
		//TODO remove? myPatternRecognition = new PatternRecognition();

		progressBar = (ProgressBar)findViewById(R.id.analysisProgressBar);
		TextViewAnalysis = (TextView)findViewById(R.id.analysisTextView);

		boNoteAnalysisCompleted = false;

		boTotalAnalysisDone.set(false);
		boUnknownKEY.set(false); 

	}

	/*****************************************************************/
	/*                 onResume                                      */
	/*****************************************************************/
	@Override
	protected void onResume() {
		super.onResume();

		if (boNoteAnalysisCompleted == false) {
			if (CallActivity.equals(LISTEN_ACTIVITY)) {
				/* Start analysis */
				progressBar.setVisibility(View.VISIBLE);   /* Make progress bar visible */
				new NoteAnalysis().execute();
			}
			if (CallActivity.equals(LIBRARY_ACTIVITY)) {
				TextViewAnalysis.setText("MUSICAL KEY identified: " + KeyValue + " major");
				drawMusicScore();
			}
			boNoteAnalysisCompleted = true;
		}
	}

	/*****************************************************************/
	/*                 onCreateOptionsMenu                           */
	/*****************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_analysis, menu);
		return true;
	}

	/*****************************************************************/
	/*                 MENU ITEM Buttons                             */
	/*****************************************************************/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_home_analysis:
			finish();
			Intent homeIntent = new Intent(this, Main.class);
			startActivity(homeIntent);
			return true;

		case R.id.menu_transcribe_analysis:
			finish();
			Intent transcribeIntent = new Intent(this, AcquireTempoActivity.class);
			startActivity(transcribeIntent);
			return true;

		case R.id.menu_library_analysis:
			finish();
			Intent libraryIntent = new Intent(this, LibraryActivity.class);
			startActivity(libraryIntent);
			return true;

		case R.id.menu_info_analysis:
			finish();
			Intent infoIntent = new Intent(this, InfoActivity.class);
			startActivity(infoIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}	

	/*****************************************************************/
	/*                 SAVE Button user click method                 */
	/*****************************************************************/
	public void saveAnalysis(View view) {

		if (boTotalAnalysisDone.get() == true) {

			Intent SaveNotationIntent = new Intent(this, SaveNotationActivity.class);
			SaveNotationIntent.putExtra("key_string", KeyValue);
			SaveNotationIntent.putExtra("tempo_bpm", TempoValue_bpm);
			SaveNotationIntent.putExtra("noteList", NoteListInteger);
			SaveNotationIntent.putExtra("noteDuration", NoteDuration);
			startActivity(SaveNotationIntent);
			finish();

		}

	}

	/*****************************************************************/
	/*                 DONE Button user click method                 */
	/*****************************************************************/
	public void doneAnalysis(View view) {
		finish();
	}

	/*****************************************************************/
	/*               ANALYZE Button user click method                */
	/*****************************************************************/
	int state = 0;
	public void patternAnalysis(View view) {
		final int NOT_DONE = 0;
		final int SHOW = 1;
		final int HIDE = 2;

		Button b = (Button)findViewById(R.id.analyzePatternButton);		
		if (state == NOT_DONE) {				
			drawPatternAnalysis();
			b.setText(R.string.pattern_analysis_msg_hide);
			state = SHOW;

		} else if (state == SHOW) {
			PatternRecognition pr = (PatternRecognition)findViewById(R.id.patternRecognition);
			pr.setVisibility(View.INVISIBLE);	
			b.setText(R.string.pattern_analysis_msg_show);
			state = HIDE;
		} else {
			PatternRecognition pr = (PatternRecognition)findViewById(R.id.patternRecognition);
			pr.setVisibility(View.VISIBLE);
			b.setText(R.string.pattern_analysis_msg_hide);
			state = SHOW;
		}		
	}

	/*****************************************************************/
	/*                 onBackPressed                                 */
	/*****************************************************************/
	@Override
	public void onBackPressed() {
		finish();
	}

	/*****************************************************************/
	/*                 NoteAnalysis Thread                           */
	/*           AsyncTask<Params, Progress, Result>                 */
	/*****************************************************************/
	private class NoteAnalysis extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			TextViewAnalysis.setText("Analyzing fundamental frequencies for \na preliminary note analysis");
		}

		/***************** doInBackground START **********************/
		@Override
		protected Void doInBackground(Void... params) {
			String TempNoteString = null;
			Double TempNoteDuration;
			int Index = 0;

			/* Take FrequencyList and DurationList as received from          */
			/* ListenActivity, and calculate NoteList and NoteDuration       */
			/* merging repeated notes.                                       */

			for (Index = 0; Index < FrequencyList.size(); Index++) {

				TempNoteString = myNoteHandler.GetPreliminaryNote(FrequencyList.get(Index), 
						DurationList.get(Index), TempoValue_ms);
				if(TempNoteString == "Invalid") {
					/* Do nothing - discard note */
				}
				else {
					NoteList.add(TempNoteString);
					NoteDuration.add(DurationList.get(Index));
				}
			}

			/* Merging repeated notes (adding duration) in NoteList          */
			for (Index = 1; Index < NoteList.size(); Index++) {
				if (NoteList.get(Index) == NoteList.get(Index-1)) {
					TempNoteDuration = NoteDuration.get(Index) + NoteDuration.get(Index-1);
					NoteDuration.set(Index-1,TempNoteDuration);
					NoteList.remove(Index);
					NoteDuration.remove(Index);
					Index--;
				}
			}

			/* TEST only */
			//for (int z=0; z<NoteList.size(); z++) {
			//	Log.v(TAG2, "PreliminaryNoteMerged"+z+" - "+"Freq: "+NoteList.get(z)+" Dura: "+NoteDuration.get(z).doubleValue()); 
			//}

			/* Setting time duration as a fraction of beat */
			myDurationAsBeat.DetermineDurationAsBeat(NoteDuration, TempoValue_ms);

			//Log.v(TAG3, "Duration as a fraction of beat - PASS");

			/* Determining KEY                                                */
			/* KeyValue will contain either a valid KEY detected or "Unknown" */
			/* AccidentalValue will contain detected value or either "None"   */
			/* meaning no black notes are present, or "Unknown" (algorithm    */
			/* was unable to detect it                                        */
			myKeyHandler.DetermineKey(NoteList);
			KeyValue = myKeyHandler.getKEYval();
			AccidentalValue = KeyHandler.getAccidentalVal();

			if (KeyValue == "Unknown") {
				/* Algorithm unable to detect KEY - this will be noted in a flag  */
				/* and the KEY is forced to be C Major                            */
				boUnknownKEY.set(true);
				KeyValue = "C";
			}

			if (AccidentalValue == "Unknown") {
				/* Black note keys detected and unable to identify the accidental */
				/* value - last resource is to force it to #                      */
				AccidentalValue = "#";
			}

			/* Removing ambiguity in black notes */
			if ((AccidentalValue == "b") || (AccidentalValue == "#")) {
				myKeyHandler.SetAccidental(NoteList, AccidentalValue);
			}

			//Log.v(TAG3, "Determine musical key - PASS");

			/* Adding elements to Integer Note List */
			for (Index = 0; Index < NoteList.size(); Index++) {
				NoteListInteger.add(myNoteHandler.GetNoteIntegerVal(NoteList.get(Index)));
			}

			//Log.v(TAG3, "NoteListInteger build - PASS");	

			return (null);
		}
		/***************** doInBackground END ************************/

		/***************** onProgressUpdate START ********************/
		@Override
		protected void onProgressUpdate(Void... values) {

		}
		/***************** onProgressUpdate END **********************/

		@Override
		protected void onPostExecute(Void v) {

			//Log.v(TAG3, "onPostExecute() start - PASS");

			if (boUnknownKEY.get() == false) {
				TextViewAnalysis.setText("MUSICAL KEY identified: " + KeyValue + " major");
			}
			else {
				TextViewAnalysis.setText("Wowsers! This app is called Mozart'sEar not Schoenberg'sScribe!\n" +
						"Unable to determine Key. Play more notes or cut out some notes from outside of the key\n" +
						"Forcing KEY to be C major");
			}

			progressBar.setVisibility(View.INVISIBLE);   /* Make progress bar invisible */

			//Log.v(TAG3, "onPostExecute() set progressBar - PASS");

			boTotalAnalysisDone.set(true);

			/* TEST only */
			//for (int z=0; z<NoteList.size(); z++) {
			//	Log.v(TAG, "NoteList"+z+" - "+"Freq: "+NoteList.get(z)+" Dura: "+NoteDuration.get(z).doubleValue()); 
			//}

			/* TEST only */
			//for (int z=0; z<NoteListInteger.size(); z++) {
			//	Log.v(TAG3, "Note"+z+" - "+"Integer Note: "+NoteListInteger.get(z)+" Dura: "+NoteDuration.get(z).doubleValue()); 
			//}

			drawMusicScore();		
		}
	}
	/***************** PreliminaryNoteAnalysis Thread   END **********/

	// Draws the music score and attaches the layout to the GUI
	private void drawMusicScore() {
		// Setup the music score		
		MusicScore ms = new MusicScore(this);
		ms.setId(R.id.musicScore);
		ms.setLayoutParams(new RelativeLayout.LayoutParams(500, LayoutParams.WRAP_CONTENT));
		ms.setTempo(TempoValue_bpm);
		ms.setKey(KeyValue);
		ms.setMusicContents(NoteListInteger, NoteDuration);

		RelativeLayout rl = new RelativeLayout(this);
		rl.setId(R.id.musicScoreRelativeLayout);
		rl.setLayoutParams(new HorizontalScrollView.LayoutParams(
				HorizontalScrollView.LayoutParams.WRAP_CONTENT,
				HorizontalScrollView.LayoutParams.WRAP_CONTENT));
		rl.addView(ms);

		HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.musicScoreLayout);
		hsv.addView(rl);

		Button b = (Button)findViewById(R.id.analyzePatternButton);
		if (NoteListInteger != null && NoteDuration != null &&
				NoteListInteger.size() != 0 && NoteDuration.size() != 0) {
			b.setEnabled(true);
		} else {
			b.setEnabled(false);
		}
	}

	// Draw the music characterization/pattern analysis results
	// This must be performed AFTER the music score is drawn
	private void drawPatternAnalysis() {
		// Setup the pattern recognition		
		PatternRecognition pr = new PatternRecognition(this);
		pr.setId(R.id.patternRecognition);
		pr.setLayoutParams(new RelativeLayout.LayoutParams(500, LayoutParams.WRAP_CONTENT));

		MusicScore ms = (MusicScore)findViewById(R.id.musicScore);

		int[] positionList = ms.getNotePositionList();

		pr.setPositions(positionList);
		int[] relativeNoteList = ms.getRelativeNoteList();
		boolean[] usedList = new boolean[relativeNoteList.length];
		
		TriadList = PatternRecognition.GetTriads(relativeNoteList);
		ScalarMotionList = PatternRecognition.GetScalarMotions(relativeNoteList);
		MotiveList = PatternRecognition.GetMotives(relativeNoteList, NoteDuration, usedList);
		RepetitionList = PatternRecognition.GetRepetitions(relativeNoteList, NoteDuration, usedList);		
		pr.setContents(TriadList, ScalarMotionList, RepetitionList, MotiveList);			

		RelativeLayout rl = (RelativeLayout)findViewById(R.id.musicScoreRelativeLayout);
		rl.addView(pr);
	}
}
