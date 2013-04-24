package ece1778.mozartsear;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class ListenActivity extends Activity implements Runnable {
	private static final String TAG = "ListenActivity";	// Used in Log messages

	// Audio commands and states
	private enum AudioState {
		RECORDING, STOPPED;
	}

	// Main objects
	private AudioRecord ar = null;
	private AudioState recordingState;
	Thread audioRecordThread = null;	

	// Audio input sampling rates
	// (only 44.1khz is guaranteed to be supported on Android devices)
	private static final int SAMPLE_FREQ = 11025;

	// Audio input settings
	private static final int MUSIC_BUFFER_SIZE = 256;	// Short; for sampling data
	private static final int AUDIO_BUFFER_SIZE = 8192*2*2;	// Bytes; for microphone buffer
	private int audio_source;		// Audio input source
	private int audio_samplingFreq; // Hertz
	private int audio_channel;
	private int audio_encoding;
	private int audio_bufferSize;	// Bytes
	private ArrayList<Double> timeData;

	// FFT (for visualizer)
	private static final int VISUALIZER_FFT_SIZE = 2*MUSIC_BUFFER_SIZE;	// JTransform FFT has RE and IM parts, but we only use the RE	
	private DoubleFFT_1D visualizerFFT;
	private double[] visualizerFreqData;

	// Visualizer
	private static final int VISUALIZER_BARS = 128;
	private Visualizer visualizer;
	private double[] visualizerData;	

	// FFT (for frequency analysis)
	private static final int SPECTROGRAM_BUFFER_SIZE = 4096;
	private static final double FREQ_RESOLUTION = ((double)SAMPLE_FREQ)/((double)SPECTROGRAM_BUFFER_SIZE);
	private static final int ANALYSIS_FFT_SIZE = SPECTROGRAM_BUFFER_SIZE*2;	// JTransform FFT has RE and IM parts, but we only use the RE
	private ArrayList<Double> noteFrequency = new ArrayList<Double>(50);
	private ArrayList<Double> noteDuration = new ArrayList<Double>(50);

	private DoubleFFT_1D analysisFFT;
	private double[] analysisFreqData;
	private double[] magnitudeFFT = null;

	private int minFreq;
	private int maxFreq;

	// Hanning window precomputed
	private double[] visualizerHann = null;
	private double[] analysisHann = null;

	// File IO
	private static final String MUSIC_DATA_FILE = "musicData.mdf";
	public static String cacheDirectory;
	public static String fileDirectory;

	// Tempo
	private int TempoValue;
	private int TempoNum = 1;
	private int metroFrequency_ms;
	private int TimeOFFSET_ms;
	private String TempoType;
	private TextView TempoDisplay;
	private ImageView metroImage;
	private View root = null;

	/*****************************************************************/
	/*                 onCreate()                                    */
	/*****************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_listen);
		root=findViewById(android.R.id.content);

		TempoDisplay = (TextView)findViewById(R.id.tempoValueTextView);

		Bundle extras = getIntent().getExtras();
		TempoValue = extras.getInt("tempo_value");
		TempoType  = extras.getString("tempo_type");
		minFreq = extras.getInt("min_freq");
		maxFreq = extras.getInt("max_freq");

		metroFrequency_ms = (60 * 1000) / TempoValue;
		TimeOFFSET_ms = metroFrequency_ms / 50;   /* 20% time offset */

		TempoDisplay.setText(TempoValue + " BPM" + "\n( " + TempoType + " )");

		metroImage = (ImageView) findViewById(R.id.metroImageButton);

		// Visualizer
		visualizer = new Visualizer(this);

		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		visualizer.setLayoutParams(lp);		

		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.inProgressTextView);
		linearLayout.addView(visualizer);

		// File IO
		cacheDirectory = getCacheDir().toString()+"/";

		// Audio settings
		recordingState = AudioState.RECORDING;
		audio_source = MediaRecorder.AudioSource.MIC;		
		audio_samplingFreq = SAMPLE_FREQ;
		audio_channel = AudioFormat.CHANNEL_IN_MONO;
		audio_encoding = AudioFormat.ENCODING_PCM_16BIT;
		audio_bufferSize = AUDIO_BUFFER_SIZE; 


		// Buffer for music input
		timeData = new ArrayList<Double>(SPECTROGRAM_BUFFER_SIZE);

		// Setup FFT for analysis
		analysisFFT = new DoubleFFT_1D(ANALYSIS_FFT_SIZE);
		analysisFreqData = new double[ANALYSIS_FFT_SIZE];

		visualizerData = new double[VISUALIZER_BARS];
		visualizer.setVisualizerData(visualizerData);	

		// Setup FFT for visualizer
		visualizerFFT = new DoubleFFT_1D(VISUALIZER_FFT_SIZE);
		visualizerFreqData = new double[VISUALIZER_FFT_SIZE];
		magnitudeFFT = new double[SPECTROGRAM_BUFFER_SIZE];

		// Precompute hanning window values
		visualizerHann = new double[MUSIC_BUFFER_SIZE];
		for (int i=0; i<visualizerHann.length; i++) {
			visualizerHann[i] = hanningWindow((double)i, MUSIC_BUFFER_SIZE);
		}
		analysisHann = new double[SPECTROGRAM_BUFFER_SIZE];
		for (int i=0; i<analysisHann.length; i++) {
			analysisHann[i] = hanningWindow((double)i, SPECTROGRAM_BUFFER_SIZE);
		}
	}

	/*****************************************************************/
	/*                 onResume()                                    */
	/*****************************************************************/
	@Override
	public void onResume() {
		super.onResume();

		run();			
		startRecording();
	}

	/*****************************************************************/
	/*                 onPause()                                     */
	/*****************************************************************/
	@Override
	public void onPause() {
		stopRecording();
		root.removeCallbacks(this);

		super.onPause();
	}

	/*****************************************************************/
	/*                 postDelayed run()                             */
	/*****************************************************************/
	@Override
	public void run() {

		if (TempoNum == 1) {
			metroImage.setImageResource(R.drawable.metro_1);
		}
		else if (TempoNum == 2) {
			metroImage.setImageResource(R.drawable.metro_2);
		}
		else if (TempoNum == 3) {
			metroImage.setImageResource(R.drawable.metro_3);
		}
		else {
			metroImage.setImageResource(R.drawable.metro_4);
		}

		TempoNum++;

		if (TempoNum > 4) {
			TempoNum = 1;
		}

		root.postDelayed(this, metroFrequency_ms - TimeOFFSET_ms);
	}

	/*****************************************************************/
	/*                 onCreateOptionsMenu()                         */
	/*****************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_listen, menu);
		return true;
	}

	/*****************************************************************/
	/*                 SET NEW TEMPO Button user click method        */
	/*****************************************************************/
	public void setNewTempo(View view) {
		finish();
		Intent TempoIntent = new Intent(this, AcquireTempoActivity.class);
		startActivity(TempoIntent);
	}

	/*****************************************************************/
	/*                 MUTE/UNMUTE Button user click method          */
	/*****************************************************************/
	public void muteMetronome(View view) {

	}

	/*****************************************************************/
	/*           START/STOP LISTENING Button user click method       */
	/*****************************************************************/
	public void stopListening(View view) {
		stopRecording();

		new FreqConversion().execute();		
	}

	public void startAnalysis() {	
		Intent AnalysisIntent = new Intent(this, AnalysisActivity.class);
		AnalysisIntent.putExtra("activity_string", TAG);
		AnalysisIntent.putExtra("tempo_bpm", TempoValue);
		AnalysisIntent.putExtra("tempo_ms", metroFrequency_ms);
		AnalysisIntent.putExtra("noteFrequency", noteFrequency);
		AnalysisIntent.putExtra("noteDuration", noteDuration);
		startActivity(AnalysisIntent);

		root.removeCallbacks(this);
		finish();
	}

	/*****************************************************************/
	/*                 MENU ITEM Buttons                             */
	/*****************************************************************/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_home_listen:
			finish();
			Intent homeIntent = new Intent(this, Main.class);
			startActivity(homeIntent);
			return true;

		case R.id.menu_transcribe_listen:
			//Intent transcribeIntent = new Intent(this, AcquireTempoActivity.class);
			//startActivity(transcribeIntent);
			return true;

		case R.id.menu_library_listen:
			Intent libraryIntent = new Intent(this, LibraryActivity.class);
			startActivity(libraryIntent);
			return true;

		case R.id.menu_info_listen:
			Intent infoIntent = new Intent(this, InfoActivity.class);
			startActivity(infoIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*****************************************************************/
	/*                 Audio Recording                               */
	/*****************************************************************/
	public void startRecording() {	
		recordingState = AudioState.RECORDING;

		audioRecordThread = new Thread(new Runnable() {
			byte[] tempBuffer = new byte[MUSIC_BUFFER_SIZE*2];	
			short[] buf = new short[MUSIC_BUFFER_SIZE];

			@Override
			public void run() {
				setupMic();	
				BufferedOutputStream bos = null;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(cacheDirectory+MUSIC_DATA_FILE));
				} catch (FileNotFoundException e) {
					Log.e(TAG, "Failed to open "+MUSIC_DATA_FILE, e);
				}

				int numLoops = 0;
				while (recordingState == AudioState.RECORDING) {
					// Read from buffer			
					int err = 0;
					err = ar.read(buf, 0, MUSIC_BUFFER_SIZE);
					if (err == AudioRecord.ERROR_INVALID_OPERATION ||
							err == AudioRecord.ERROR_BAD_VALUE) {
						Log.wtf(TAG, "Failed to read buffer. Error: " + err);
					}

					int index = 0;
					for (int i=0; i<MUSIC_BUFFER_SIZE; i++) {
						// Setup visualizer FFT input						
						visualizerFreqData[index] = (double)buf[i]*visualizerHann[i];
						visualizerFreqData[index+1] = 0;						

						// buffer data				
						if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
							tempBuffer[index] = (byte)(buf[i]&0xFF);
							tempBuffer[index+1] = (byte)((buf[i]&0xFF00)>>8);
						} else {
							tempBuffer[index] = (byte)((buf[i]&0xFF00)>>8);
							tempBuffer[index+1] = (byte)(buf[i]&0xFF);
						}

						index += 2;
					}
					// Append the input data byte array to the music data file
					try {
						// Limit size to under 7mb
						// This converts to a bit over 5 minutes of recording time
						if (bos != null && 2*MUSIC_BUFFER_SIZE*numLoops < 7000000) {
							bos.write(tempBuffer, 0, MUSIC_BUFFER_SIZE*2);							
						} else {
							if (numLoops%400 == 0) {
								// Periodically displays message once 7mb limit is hit
								runOnUiThread(recordLengthExceeded);
							}
						}
					} catch (IOException e) {
						Log.e(TAG, "Error writing to "+MUSIC_DATA_FILE, e);
					}

					// Results are in-line with freq_data;
					visualizerFFT.realForward(visualizerFreqData);

					// Condense the frequency data to fit into the visualizer
					// We toss out the second half of the frequency data because we only
					// want the first half up to 4khz (sampling rate = 8khz) for music
					int j = 0;
					for (int i=0; i<MUSIC_BUFFER_SIZE; i+=2) {				
						// TODO: will want to double buffer visualizerData if graphics is to be smooth
						visualizerData[j] = 10*Math.log10(visualizerFreqData[i]*visualizerFreqData[i]+
								visualizerFreqData[i+1]*visualizerFreqData[i+1]);
						j++;
					}

					// Arbitrarily large number to limit the counter
					if (numLoops <= 99999999) {
						numLoops++;
					}
				}
				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
				} catch (IOException e) {
					Log.e(TAG, "Failed to flush and close "+MUSIC_DATA_FILE, e);
				}
				releaseMic();
			}
		});
		audioRecordThread.start();		
	}	

	Runnable recordLengthExceeded = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(getApplicationContext(), "Max recording length exceeded", Toast.LENGTH_LONG).show();
		}
	};

	public void stopRecording() {		
		recordingState = AudioState.STOPPED;
		try {
			audioRecordThread.join();
		} catch (InterruptedException e) {
			Log.e(TAG, "Interrupted while joining with audio recording thread", e);
		}
	}

	public void setupMic() {
		try {
			ar = new AudioRecord(audio_source,
					audio_samplingFreq,
					audio_channel,
					audio_encoding,
					audio_bufferSize);
		} catch (IllegalArgumentException e) {
			Log.wtf(TAG, "failed to initialize audio record object", e);
		}

		try {			
			ar.startRecording();
			if(ar.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
				Log.e(TAG, "failed to start recording");
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "start recording failed", e);
		}
	}

	public void releaseMic() {
		try {			
			ar.stop();				
			if(ar.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
				Log.e(TAG, "failed to stop recording");
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "stop recording failed", e);
		}

		ar.release();
	}

	/*****************************************************************/
	/*               Frequency Conversion for Analysis               */
	/*****************************************************************/

	// Populate the list by getting input from website and entering into database
	private class FreqConversion extends AsyncTask<Void, Void, Integer> {
		private static final String TAG = "AsyncTask:ComputeFreqAndTime";
		ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			progress = new ProgressDialog(ListenActivity.this);
			progress.setMessage("Processing input data...");
			progress.setCancelable(false);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.show();
		}

		// Load list of names from website
		@Override
		protected Integer doInBackground(Void... v) {
			int error = computeFreqAndTime();
			return Integer.valueOf(error);
		}

		// Finished loading database; remove spinner, enable button, show toast
		@Override
		protected void onPostExecute(Integer i) {		
			progress.dismiss();
			int error = i.intValue();

			switch (error) {
			case 0:
				startAnalysis();
				break;				
			case -2:
				Log.e(TAG, "Error reading/writing "+MUSIC_DATA_FILE);
				break;
			case -4:
				Log.e(TAG, "Tempo uninitialized");
				break;
			default:
				Log.e(TAG, "Unhandled error code");
				break;
			}	
		}
	}

	// Return values
	// 0 on success
	// -1 on not enough data values
	// otherwise, can we really say the music is any useful?
	// -2 on error reading/opening music data file
	// -3 on error writing/opening spectrogram data file
	// -4 Tempo is uninitialized
	//
	private int computeFreqAndTime() {
		// Setup IO
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(cacheDirectory+MUSIC_DATA_FILE));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to open "+MUSIC_DATA_FILE, e);
			return -2;
		}

		// Determine period of FFT computations (in number of samples)
		// Assuming the shortest note played is a sixteenth note.
		// Although there is a period shifting effect, by using double precision
		// and calculating when the next FFT should occur hopefully
		// has enough precision that it won't make a difference in results.
		// Assuming 4 beats per bar.		
		double periodFFT;	// samples
		double msFFT;	// milliseconds
		if (TempoValue != 0) {
			periodFFT = (SAMPLE_FREQ*15d)/TempoValue;
			msFFT = (1000*periodFFT)/SAMPLE_FREQ;
		} else {
			return -4;
		}

		// Gather the initial set of data for the FFT (fill it with 0)	
		for (int i=0; i<SPECTROGRAM_BUFFER_SIZE; i++) {
			timeData.add(Double.valueOf(0d));
		}

		short data = 0;
		int error = 0;
		byte[] buf = new byte[2];
		int done = 0;  
		int timesRepeated = 0;
		int windowDistance = 0;
		double prevFreq=0;
		double curFreq=0;
		double duration=0;

		while (done == 0) {
			// Read input required for the next FFT
			double current = periodFFT*timesRepeated;
			double next = periodFFT*(timesRepeated+1);
			windowDistance = (int)Math.round(next-current);
			for (int l=0; l<windowDistance; l++) {
				timeData.remove(0);
				try {
					if (bis != null) {
						error = bis.read(buf, 0, 2);
					}
				} catch (Exception e) {
					Log.e(TAG, "Error reading "+MUSIC_DATA_FILE, e);
					return -2;
				}
				// Pad the rest with 0s if reached EOF
				if (error == -1) {
					done = 1;
					timeData.add(Double.valueOf(0d));
				} else {			
					// Check endianness and reconstruct short
					if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
						data = (short)((buf[1]<<8)|(buf[0]&0xFF));
					} else {
						data = (short)((buf[0]<<8)|(buf[1]&0xFF));
					}

					// Add data to buffer
					timeData.add(Double.valueOf((double)data));
				}
			}

			int j = 0;
			int k = 0;			
			while (j < SPECTROGRAM_BUFFER_SIZE) {
				analysisFreqData[k] = timeData.get(j).doubleValue()*analysisHann[j];
				analysisFreqData[k+1] = 0d;
				j++;
				k += 2;
			}			
			// Run the FFT
			analysisFFT.realForward(analysisFreqData);

			// Calculate magnitude (dB)
			j=0;
			k=0;
			while (j < SPECTROGRAM_BUFFER_SIZE) {
				magnitudeFFT[j] = 10*Math.log10(analysisFreqData[k]*analysisFreqData[k]+
						analysisFreqData[k+1]*analysisFreqData[k+1]);
				j++;
				k += 2;
			}

			// Perform frequency and duration detection
			prevFreq = curFreq;
			double result = detectFrequency(magnitudeFFT);
			if (result >= 0) {
				curFreq = result;
				if (prevFreq == curFreq) {
					duration += msFFT;
				} else {
					// Check if previous note is a very short rest; remove if sixteenth or eighth
					if (noteFrequency.size() != 0 && 
							noteDuration.size() != 0 &&
							noteFrequency.get(noteFrequency.size()-1).doubleValue() == 0 &&
							noteDuration.get(noteDuration.size()-1).doubleValue() <= msFFT*2) {
						noteFrequency.remove(noteFrequency.size()-1);
						noteDuration.remove(noteDuration.size()-1);
					}

					// Add newest note
					noteFrequency.add(Double.valueOf(prevFreq));
					noteDuration.add(Double.valueOf(duration));
					duration = msFFT;
				}
			}

			timesRepeated++;
		}
		try {
			if (bis != null) {
				bis.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "Failed to close "+MUSIC_DATA_FILE, e);
		}

		// Display notes
		// test TODO		
		//for (int z=0; z<noteFrequency.size(); z++) {
		//	Log.v(TAG, "Note"+z+" - "+"Freq: "+noteFrequency.get(z).doubleValue()+" Dura: "+noteDuration.get(z).doubleValue()); 
		//}

		return 0;
	}

	// Detect and return the frequency in the sample data
	// 0 means a rest
	// other positive values represent frequencies
	private double detectFrequency(double[] freqData) {	
		final double PERCENTILE_START = 0.1;
		final double PERCENTILE_STOP = 0.6;
		final double THRESHOLD_SCALING_FACTOR = 1.7;
		final double FREQ_START = 80;
		final double FREQ_STOP = 2400;
		final int BIN_TOLERANCE = 5;		
		int length = freqData.length/2;

		int lowerBound = (int)(FREQ_START/FREQ_RESOLUTION);
		if (lowerBound < 1) {
			lowerBound = 1;
		}
		int upperBound = (int)(FREQ_STOP/FREQ_RESOLUTION);
		if(upperBound > length-1) {
			upperBound = length-1;
		}
		int range = upperBound-lowerBound;

		// Calculate average of values between the given percentile range
		// of the first half of the frequency data
		ArrayList<Double> data = new ArrayList<Double>(range);
		for (int i=lowerBound; i<upperBound; ++i) {
			data.add(Double.valueOf(freqData[i]));
		}
		Collections.sort(data);		
		int start = (int)(range*PERCENTILE_START);
		int stop = (int)(range*PERCENTILE_STOP);
		double sum = 0;
		for (int i=lowerBound+start; i<lowerBound+stop; ++i) {
			sum += data.get(i).doubleValue();
		}
		double avg = sum/(stop-start);

		// Determine cutoff threshold
		double threshold = avg*THRESHOLD_SCALING_FACTOR;

		// Build a list of local peaks using a gate/threshold
		ArrayList<Integer> peaks = new ArrayList<Integer>();
		peaks.add(Integer.valueOf(0));
		int prev=0;
		for (int i=lowerBound; i<upperBound; ++i) {
			if (freqData[i] > threshold) {
				double left = freqData[i-1];
				double right = freqData[i+1];
				if (left < freqData[i] && right < freqData[i]) {
					// If the 2 peaks are too close together take the taller peak
					if (prev + BIN_TOLERANCE < i) {
						// Add new peak
						peaks.add(Integer.valueOf(i));
						prev = i;
					} else if (freqData[i] > freqData[prev]) {	
						// Replace previous peak with new peak
						peaks.remove(peaks.size()-1);
						peaks.add(Integer.valueOf(i));
						prev = i;
					} // Keep previous peak				
				}				
			}
		}

		int freqBin = 0;
		int lengthPeaks = peaks.size()-1;
		if (lengthPeaks != 0) {
			// Calculate differences between peaks
			ArrayList<Integer> diffs = new ArrayList<Integer>(lengthPeaks);
			for (int i=0; i<lengthPeaks; ++i) {
				int index = peaks.get(i).intValue();
				int nextIndex = peaks.get(i+1).intValue();
				diffs.add(Integer.valueOf(nextIndex-index));
			}
			Collections.sort(diffs);

			int fundamental = diffs.get(0).intValue();
			int medianIndex = diffs.size()/2;
			int median = diffs.get(medianIndex).intValue();

			// Take the smaller of the fundamental and detected median
			// because sometimes, the fundamental data is lost under
			// noise and other times overtones are too weak
			freqBin = Math.min(fundamental, median); 
		} else {
			// Rest note
			freqBin = 0;
		}

		// Filter out frequency results we don't want
		int ignoreBelow = (int)(minFreq/FREQ_RESOLUTION);
		int ignoreAbove = (int)(maxFreq/FREQ_RESOLUTION);
		if (freqBin != 0 && (freqBin < ignoreBelow || freqBin > ignoreAbove)) {
			freqBin = -1;
		}

		return freqBin*FREQ_RESOLUTION;
	}

	// Hanning window
	private double hanningWindow(double n, int N) {
		double step1 = 2*Math.PI*n/(N-1);
		double step2 = 1-Math.cos(step1);
		return 0.5*step2;
	}
}
