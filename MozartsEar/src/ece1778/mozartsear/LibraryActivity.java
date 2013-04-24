package ece1778.mozartsear;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;

public class LibraryActivity extends Activity {
	
	private static final String TAG = "LibraryActivity";       /* Used in Log messages */
	
	/* Music Staff variables */
	private int TempoValue_bpm;
	private String KeyValue = "Unknown";
	private ArrayList<Integer> NoteListInteger  = new ArrayList<Integer>();   /* Final note list as integers 1 to 88 */
	private ArrayList<Double>  NoteDuration     = new ArrayList<Double>();    /* Final note duration as a fraction of beat list */
	
	/* File variables */
	private ListView lv;
	private File directory;
	private CSVReader reader;
	private String NameOfFile;
	private boolean boItemSelected = false;
	private int ItemSel;
	private ArrayList<String> FilesInFolder;
	private String[] FileFields = new String[2];   /* 2 columns in CSV File */
	
	/* Layout variables */
	private TextView TextViewAnalysisLib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		TextViewAnalysisLib = (TextView)findViewById(R.id.libraryTextView);
		
		boItemSelected = false;
		
		directory = Environment.getExternalStorageDirectory();
		
		lv = (ListView)findViewById(R.id.librarylistView);
	    FilesInFolder = GetFiles(directory.getAbsolutePath()+"/MozartsEar/");

	    if (FilesInFolder == null) {
	    	TextViewAnalysisLib.setText("Library is empty!");
	    }
	    else {
	    	lv.setAdapter(new ArrayAdapter<String>(this,
		    		android.R.layout.simple_list_item_1, FilesInFolder));
	    	
	    	lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		        	ItemSel = position;
		        	NameOfFile = FilesInFolder.get(position);
		        	TextViewAnalysisLib.setText(NameOfFile+" selected");
		        	boItemSelected = true;
		            }
		        });
	    }
	    
	    
	    
	}
	
	/*****************************************************************/
	/*                 onResume                                      */
	/*****************************************************************/
	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_library, menu);
		return true;
	}
	
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
        		//Intent libraryIntent = new Intent(this, LibraryActivity.class);
                //startActivity(libraryIntent);
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
	
	/*****************************************************************/
	/*                 DELETE Button user click method               */
	/*****************************************************************/
	public void DeleteFile(View view) {
		if (boItemSelected == true) {
			File file = new File(directory.getAbsolutePath()+"/MozartsEar/"+NameOfFile);
			file.delete();
			TextViewAnalysisLib.setText("File deleted!");
			boItemSelected = false;
			
			FilesInFolder = GetFiles(directory.getAbsolutePath()+"/MozartsEar/");
    	    
			if (FilesInFolder == null) {
		    	TextViewAnalysisLib.setText("Library is empty!");
		    	lv.setAdapter(null);
		    }
			else {
				lv.setAdapter(new ArrayAdapter<String>(this,
			    		android.R.layout.simple_list_item_1, FilesInFolder));
			}
		}
	}
	
	/*****************************************************************/
	/*                 VIEW Button user click method                 */
	/*****************************************************************/
	public void libraryAnalysis(View view) {
		if (boItemSelected == true) {
			new DisplayFile().execute();
		}
	}
	
	/*****************************************************************/
	/*                 EXIT Button user click method                 */
	/*****************************************************************/
	public void exitLibrary(View view) {
		finish();
	}
	
	/*****************************************************************/
    /*                 GetFiles method                               */
    /*****************************************************************/
	private ArrayList<String> GetFiles(String DirectoryPath) {
	    ArrayList<String> MyFiles = new ArrayList<String>();
	    File f = new File(DirectoryPath);

	    f.mkdirs();
	    File[] files = f.listFiles();
	    if (files.length == 0)
	        return null;
	    else {
	        for (int i=0; i<files.length; i++) 
	            MyFiles.add(files[i].getName());
	    }

	    return MyFiles;
	}
	
	/*****************************************************************/
	/*                 NoteAnalysis Thread                           */
	/*           AsyncTask<Params, Progress, Result>                 */
	/*****************************************************************/
	private class DisplayFile extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {

		}

		/***************** doInBackground START **********************/
		@Override
		protected Void doInBackground(Void... params) {
			
			int Index = 0;

			try {
				reader = new CSVReader(new FileReader(directory.getAbsolutePath()+"/MozartsEar/"+NameOfFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
            String [] nextLine;
            try {
				while ((nextLine = reader.readNext()) != null) {
					if (Index == 0) {
						KeyValue = nextLine[0];
						TempoValue_bpm = Integer.parseInt(nextLine[1]);
					}
					else {
						NoteListInteger.add(Integer.parseInt(nextLine[0]));
						NoteDuration.add(Double.parseDouble(nextLine[1]));
					}
					Index++;
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
            
            try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

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
			finish();
			Intent AnalysisIntent = new Intent(getBaseContext(), AnalysisActivity.class);
			AnalysisIntent.putExtra("activity_string", TAG);
			AnalysisIntent.putExtra("key_string", KeyValue);
			AnalysisIntent.putExtra("tempo_bpm", TempoValue_bpm);
			AnalysisIntent.putExtra("noteList", NoteListInteger);
			AnalysisIntent.putExtra("noteDuration", NoteDuration);
			startActivity(AnalysisIntent);		
		}
	}
	/***************** PreliminaryNoteAnalysis Thread   END **********/

}
