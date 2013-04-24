package ece1778.mozartsear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AcquireTempoActivity extends Activity {
	private int Tempo = 0;
	private String TempoType = "No type identified";
	private long StartTime_ms = 0;
	private long FinishTime_ms = 0;
	private long CurrentTime_ms = 0;
	private int NumberSamples = 0;
	private TextView TempoDisplay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_acquire_tempo);

		TempoDisplay = (TextView)findViewById(R.id.tempoTextView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_acquire_tempo, menu);
		return true;
	}

	/*****************************************************************/
	/*                 TAP TEMPO Button user click method            */
	/*****************************************************************/
	public void calculateTempo(View view) {

		CurrentTime_ms = System.currentTimeMillis();

		/* Calculate Clicks/seconds */
		if (NumberSamples == 0) {
			StartTime_ms = CurrentTime_ms;
		}

		NumberSamples++;
		FinishTime_ms = CurrentTime_ms;

		if (NumberSamples >= 2) {   /* at least two samples are needed */
			Tempo = ((NumberSamples -1) * 1000 * 60) / (int) (FinishTime_ms - StartTime_ms);

			/* Setting TempoType type */
			if (Tempo >= 83) {
				if (Tempo >= 132) {
					if (Tempo >= 150) {
						if (Tempo >= 168) {
							if (Tempo >= 178) {
								TempoType = "Prestissimo";
							}
							else {
								TempoType = "Presto";
							}
						}
						else {
							TempoType = "Allegrissimo";
						}
					}
					else {
						if (Tempo >= 140) {
							TempoType = "Vivacissimo";
						}
						else {
							TempoType = "Vivace";
						}
					}
				}
				else {
					if (Tempo >= 98) {
						if (Tempo >= 109) {
							TempoType = "Allegro";
						}
						else {
							TempoType = "Allegretto";
						}
					}
					else {
						if (Tempo >= 86) {
							TempoType = "Moderato";
						}
						else {
							TempoType = "Marcia moderato";
						}
					}
				}
			}
			else {   /* Tempo is less than 83 BPM */
				if (Tempo >= 55) {
					if (Tempo >= 73) {
						if (Tempo >= 78) {
							TempoType = "Andantino";
						}
						else {
							TempoType = "Andante";
						}
					}
					else {
						if (Tempo >= 69) {
							TempoType = "Andante moderato";
						}
						else {
							if (Tempo >= 65) {
								TempoType = "Adagietto";
							}
							else {
								TempoType = "Adagio";
							}
						}
					}
				}
				else {
					if (Tempo >= 45) {
						if (Tempo >= 50) {
							TempoType = "Larghetto";
						}
						else {
							TempoType = "Largo";
						}
					}
					else {
						if (Tempo >= 20) {
							if (Tempo >= 40) {
								TempoType = "Lento";
							}
							else {
								TempoType = "Grave";
							}
						}
						else {
							TempoType = "Larghissimo";
						}
					}
				}
			}

			TempoDisplay.setText("TEMPO = " + Tempo + " BPM" + "\n( " + TempoType + " )");
		}

	}

	/*****************************************************************/
	/*                 TAP TEMPO Button user click method            */
	/*****************************************************************/
	public void resetTempo(View view) {
		Tempo = 0;
		StartTime_ms = 0;
		FinishTime_ms = 0;
		NumberSamples = 0;
		TempoDisplay.setText(R.string.default_tempo_msg);
	}

	/*****************************************************************/
	/*                 DONE TEMPO Button user click method           */
	/*****************************************************************/
	public void doneTempo(View view) {
		if (Tempo != 0) {
			/* Go to LISTEN activity */
			finish();    	
			Intent listenIntent = new Intent(this, ListenActivity.class);
			
			Bundle bundle = new Bundle();
			
			bundle.putString("tempo_type", TempoType);
			bundle.putInt("tempo_value", Tempo);
			
			EditText minFreq = (EditText)findViewById(R.id.editTextMinFreq);
			EditText maxFreq = (EditText)findViewById(R.id.editTextMaxFreq);			
			bundle.putInt("min_freq", Integer.parseInt(minFreq.getText().toString()));
			bundle.putInt("max_freq", Integer.parseInt(maxFreq.getText().toString()));
			
			listenIntent.putExtras(bundle);
			startActivity(listenIntent);
		}
	}

	/*****************************************************************/
	/*                 MENU ITEM Buttons                             */
	/*****************************************************************/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_home_tempo:
			Intent homeIntent = new Intent(this, Main.class);
			startActivity(homeIntent);
			return true;

		case R.id.menu_transcribe_tempo:
			//Intent transcribeIntent = new Intent(this, AcquireTempoActivity.class);
			//startActivity(transcribeIntent);
			return true;

		case R.id.menu_library_tempo:
			Intent libraryIntent = new Intent(this, LibraryActivity.class);
			startActivity(libraryIntent);
			return true;

		case R.id.menu_info_tempo:
			Intent infoIntent = new Intent(this, InfoActivity.class);
			startActivity(infoIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
