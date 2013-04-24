package ece1778.mozartsear;

/*********************************************************************************/
/*                      FILE STRUCTURE                                           */
/* _____________________________________________________________________________ */
/* |NoteListInteger	| NoteDuration	| Repetition	| ScalarMotion	| Triad    | */
/* |________________|_______________|_______________|_______________|__________| */
/* |   INTEGER      |   REAL        |  INTEGER		|  INTEGER		|  INTEGER | */
/* |________________|_______________|_______________|_______________|__________| */
/*                                                                               */
/* N.B. First Row:                                                               */
/* 				- NoteListInteger contains KEYValueIntegerID                     */
/*                              KEYValueIntegerID uses methods from KeyHandler:  */
/*                              GetKeyIntegerID() and Get KeyStringID() for      */
/*                              database handling purposes                       */
/* 				- Repetition contains TempoValue_bpm                             */
/* 				- ScalarMotion contains TempoValue_ms                            */                                                                        
/*********************************************************************************/

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;

public class SaveNotationActivity extends Activity {
	
	//private static final String TAG = "SaveActivity";       /* Used in Log messages */
	
	private ProgressBar progressBar;
	private Toast ToastMsg;
	private int Index;

	/* File values */
	private File directory;
	private CSVWriter writer;
	private int TempoValue_bpm;
	private String KeyValue;
	private String FileName;
	private ArrayList<Integer> NoteListInteger  = new ArrayList<Integer>();
	private ArrayList<Double>  NoteDuration     = new ArrayList<Double>();
	private EditText myEditText;
	private String[] FileFields = new String[2];   /* 2 columns in CSV File */


	/*****************************************************************/
    /*                 onCreate                                      */
    /*****************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_save_notation);
		
		myEditText   = (EditText)findViewById(R.id.NotationSaveName);
		progressBar = (ProgressBar)findViewById(R.id.progressBarSave);
		
		Bundle extras = getIntent().getExtras();
		KeyValue = extras.getString("key_string");
		TempoValue_bpm = extras.getInt("tempo_bpm");
		NoteListInteger = (ArrayList<Integer>) getIntent().getSerializableExtra("noteList");
		NoteDuration  = (ArrayList<Double>) getIntent().getSerializableExtra("noteDuration");

	}
	/***************** onCreate   END ********************************/

	/*****************************************************************/
    /*                 onStart                                       */
    /*****************************************************************/
    @Override
    protected void onStart() {
        super.onStart();
        
        //Log.d(getClass().getSimpleName(), "onStart()");

    }

	/*****************************************************************/
    /*                 onResume                                       */
    /*****************************************************************/
    @Override
    protected void onResume() {
        super.onResume();

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_save_notation, menu);
		return true;
	}
	
	/*****************************************************************/
	/*                 DONE Button user click method                 */
	/*****************************************************************/
	public void saveNotation(View view) throws IOException {
		FileName = myEditText.getText().toString();     /* Get table name */

		if (FileName.isEmpty()) {
			ToastMsg = Toast.makeText(getBaseContext(), "Enter a valid file name!", Toast.LENGTH_LONG);
	    	ToastMsg.show();
		}
		else {
			progressBar.setVisibility(View.VISIBLE);         /* Make progress bar visible */
			directory = new File(Environment.getExternalStorageDirectory() + "/MozartsEar");
			directory.mkdirs();
			
			writer = new CSVWriter(new FileWriter(directory.getAbsolutePath()+"/"+FileName+".csv"));
			new populateOperation().execute();
		}
	}
	
	/*****************************************************************/
	/*                 CANCEL SAVE Button user click method          */
	/*****************************************************************/
	public void cancelSaveNotation(View view) {
		finish();
	}
	
	/*****************************************************************/
    /*                 populateOperation Thread                      */
	/*           AsyncTask<Params, Progress, Result>                 */
    /*****************************************************************/
	private class populateOperation extends AsyncTask<Void, Void, Void> {
		

		@Override
		protected void onPreExecute() {

		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			/* Set KeyValue and TempoValue_bpm in first row of file */
			FileFields[0] = KeyValue;
			FileFields[1] = Integer.toString(TempoValue_bpm);
			
			writer.writeNext(FileFields);
			
			/* Set Note as integer and duration */
			for (Index = 0; Index < NoteListInteger.size(); Index++) {
				FileFields[0] = Integer.toString(NoteListInteger.get(Index));
				FileFields[1] = Double.toString(NoteDuration.get(Index));
				writer.writeNext(FileFields);
			}
			
			try {
				writer.close();
			}
			
			catch (IOException e) {
				e.printStackTrace();
			}

			return null;
        }

		@Override
		protected void onProgressUpdate(Void... values) {

		}

        @Override
        protected void onPostExecute(Void v) {
        	progressBar.setVisibility(View.INVISIBLE);   /* Make progress bar invisible */
        	Intent LibraryIntent = new Intent(getBaseContext(), LibraryActivity.class);
			startActivity(LibraryIntent);
			finish();
        }
	}
	/***************** populateOperation Thread   END ****************/

	/*****************************************************************/
    /*                 MENU ITEM Buttons                             */
    /*****************************************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    		case R.id.menu_home_library:
    			finish();
    			Intent homeIntent = new Intent(this, Main.class);
    			startActivity(homeIntent);
    			return true;

        	case R.id.menu_transcribe_library:
        		finish();
        		Intent transcribeIntent = new Intent(this, AcquireTempoActivity.class);
                startActivity(transcribeIntent);
        		return true;

        	case R.id.menu_library_library:
        		finish();
        		Intent libraryIntent = new Intent(this, LibraryActivity.class);
                startActivity(libraryIntent);
        		return true;

        	case R.id.menu_info_library:
        		Intent infoIntent = new Intent(this, InfoActivity.class);
                startActivity(infoIntent);
        		return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /*****************************************************************/
    /*                 onBackPressed                                 */
    /*****************************************************************/
	@Override
	public void onBackPressed() {
		finish();
		//Intent homeIntent = new Intent(this, Main.class);
		//startActivity(homeIntent);
	}

}
