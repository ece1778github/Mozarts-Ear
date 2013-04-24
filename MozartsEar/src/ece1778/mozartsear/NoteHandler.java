package ece1778.mozartsear;


public class NoteHandler {

	/* Frequencies less than 1/16 of a beat (tempo) are filtered out */
	final double NoiseDurationThreshold = 0.0625;
	final double NoiseFreqHighThreshold = 4500.5;
	final double NoiseFreqLowThreshold = 20.5;
	private String PreliminaryNote;
	
	final int TotalTableElements = 88;

	final double[] FrequencyArray = {
			4186.01, 3951.07, 3729.31, 3520,    3322.44, 3135.96, 2959.96, 2793.83,
			2637.02, 2489.02, 2349.32, 2217.46, 2093,    1975.53, 1864.66, 1760,
			1661.22, 1567.98, 1479.98, 1396.91, 1318.51, 1244.51, 1174.66, 1108.73,
			1046.5,  987.767, 932.328, 880,     830.609, 783.991, 739.989, 698.456,
			659.255, 622.254, 587.33,  554.365, 523.251, 493.883, 466.164, 440,
			415.305, 391.995, 369.994, 349.228, 329.628, 311.127, 293.665, 277.183,
			261.626, 246.942, 233.082, 220,     207.652, 195.998, 184.997, 174.614,
			164.814, 155.563, 146.832, 138.591, 130.813, 123.471, 116.541, 110,
			103.826, 97.9989, 92.4986, 87.3071, 82.4069, 77.7817, 73.4162, 69.2957,
			65.4064, 61.7354, 58.2705, 55,      51.913,  48.9995, 46.2493, 43.6536,
			41.2035, 38.8909, 36.7081, 34.6479, 32.7032, 30.8677, 29.1353, 27.5
			};
	
	final String[] PreliminaryNoteArray = {
			"C8", "B7", "A#7_or_Bb7", "A7", "G#7_or_Ab7", "G7", "F#7_or_Gb7", "F7",
			"E7", "D#7_or_Eb7", "D7", "C#7_or_Db7", "C7", "B6", "A#6_or_Bb6", "A6",
			"G#6_or_Ab6", "G6", "F#6_or_Gb6", "F6", "E6","D#6_or_Eb6", "D6", "C#6_or_Db6",
			"C6", "B5", "A#5_or_Bb5", "A5", "G#5_or_Ab5", "G5", "F#5_or_Gb5", "F5",
			"E5", "D#5_or_Eb5", "D5", "C#5_or_Db5", "C5", "B4", "A#4_or_Bb4", "A4",
			"G#4_or_Ab4", "G4", "F#4_or_Gb4", "F4", "E4", "D#4_or_Eb4", "D4", "C#4_or_Db4",
			"C4", "B3", "A#3_or_Bb3", "A3", "G#3_or_Ab3", "G3", "F#3_or_Gb3", "F3",
			"E3", "D#3_or_Eb3", "D3", "C#3_or_Db3", "C3", "B2", "A#2_or_Bb2", "A2",
			"G#2_or_Ab2", "G2", "F#2_or_Gb2", "F2", "E2", "D#2_or_Eb2", "D2", "C#2_or_Db2",
			"C2", "B1", "A#1_or_Bb1", "A1", "G#1_or_Ab1", "G1", "F#1_or_Gb1", "F1",
			"E1", "D#1_or_Eb1", "D1", "C#1_or_Db1", "C1", "B0", "A#0_or_Bb0", "A0"
			};
	
	final int TotalIntegerTableElements = 124;
	
	final String[] NoteArrayInteger = {
			"C8", "B7", "A#7", "Bb7", "A7", "G#7", "Ab7", "G7", "F#7", "Gb7", "F7",
			"E7", "D#7", "Eb7", "D7", "C#7", "Db7", "C7", "B6", "A#6", "Bb6", "A6",
			"G#6", "Ab6", "G6", "F#6", "Gb6", "F6", "E6","D#6", "Eb6", "D6", "C#6", "Db6",
			"C6", "B5", "A#5", "Bb5", "A5", "G#5", "Ab5", "G5", "F#5", "Gb5", "F5",
			"E5", "D#5", "Eb5", "D5", "C#5", "Db5", "C5", "B4", "A#4", "Bb4", "A4",
			"G#4", "Ab4", "G4", "F#4", "Gb4", "F4", "E4", "D#4", "Eb4", "D4", "C#4", "Db4",
			"C4", "B3", "A#3", "Bb3", "A3", "G#3", "Ab3", "G3", "F#3", "Gb3", "F3",
			"E3", "D#3", "Eb3", "D3", "C#3", "Db3", "C3", "B2", "A#2", "Bb2", "A2",
			"G#2", "Ab2", "G2", "F#2", "Gb2", "F2", "E2", "D#2", "Eb2", "D2", "C#2", "Db2",
			"C2", "B1", "A#1", "Bb1", "A1", "G#1", "Ab1", "G1", "F#1", "Gb1", "F1",
			"E1", "D#1", "Eb1", "D1", "C#1", "Db1", "C1", "B0", "A#0", "Bb0", "A0", "Rest" 
			};

	final int[] NoteArrayIntegerValue = {
			88, 87, 86, 86, 85, 84, 84, 83, 82, 82, 81, 80, 79, 79, 78, 77, 77,
			76, 75, 74, 74, 73, 72, 72, 71, 70, 70, 69, 68, 67, 67, 66, 65, 65,
			64, 63, 62, 62, 61, 60, 60, 59, 58, 58, 57, 56, 55, 55, 54, 53, 53,
			52, 51, 50, 50, 49, 48, 48, 47, 46, 46, 45, 44, 43, 43, 42, 41, 41,
			40, 39, 38, 38, 37, 36, 36, 35, 34, 34, 33, 32, 31, 31, 30, 29, 29,
			28, 27, 26, 26, 25, 24, 24, 23, 22, 22, 21, 20, 19, 19, 18, 17, 17,
			16, 15, 14, 14, 13, 12, 12, 11, 10, 10, 9, 8, 7, 7, 6, 5, 5, 4, 3,
			2, 2, 1, 0
			};
	
	
	
	/* GetPreliminaryNote() returns a preliminary musical note (no key identified yet) for a */
	/* best approximation of a provided frequency input.                                     */
	/* Returns "Invalid" if frequency is outside valid limits or if duration is less than    */
	/* a fraction of a beat (Tempo).                                                         */
	public String GetPreliminaryNote(double Frequency_Hz, double Duration_ms, int Tempo_ms) {
		
		int index_high = 0;
		int index_low = TotalTableElements - 1;

		if ((Duration_ms/Tempo_ms) >= NoiseDurationThreshold) {
			
			if (Frequency_Hz == 0) {
				PreliminaryNote = "Rest";
			}
			else if ((Frequency_Hz > NoiseFreqHighThreshold) ||
				(Frequency_Hz < NoiseFreqLowThreshold)) {
				PreliminaryNote = "Invalid";
			}

			else if (Frequency_Hz >= FrequencyArray[0]) {
				PreliminaryNote = PreliminaryNoteArray[0];
			}
			else if (Frequency_Hz <= FrequencyArray[TotalTableElements - 1]) {
				PreliminaryNote = PreliminaryNoteArray[TotalTableElements - 1];
			}
			else {
				for(int i = 0; i < TotalTableElements - 1; i++) {
					if (Frequency_Hz < FrequencyArray[i+1]) {
						index_high++;
					}
				
					if (Frequency_Hz > FrequencyArray[TotalTableElements - 2 - i]) {
						index_low--;
					}

				}
			
				if ((index_low - index_high) > 1) {
					PreliminaryNote = PreliminaryNoteArray[index_high + 1];
				}
				else {
					if (Frequency_Hz >= (FrequencyArray[index_low] +
						((FrequencyArray[index_high] - FrequencyArray[index_low]) / 2))) {
						/* Frequency is closer to higher frequency */
						PreliminaryNote = PreliminaryNoteArray[index_high];
					}
					else {
						/* Frequency is closer to lower frequency */
						PreliminaryNote = PreliminaryNoteArray[index_low];
					}
				}
			}
		}
		else {
			PreliminaryNote = "Invalid";
		}
		
		return PreliminaryNote;
	}
	
	/* GetNoteIntegerVal() returns integer value representing a musical note for easy graph  */
	/* creation. Integer value are INVERTED from real piano key number as match 1 with the   */
	/* highest frequency note and 88 to match the lowest frequency note, again, for easy     */
	/* graph creation                                                                        */
	public int GetNoteIntegerVal(String NoteString) {
		int ReturnValue = 0;
		
		for(int i = 0; i < TotalIntegerTableElements - 1; i++) {
			if (NoteString == NoteArrayInteger[i]) {
				ReturnValue = NoteArrayIntegerValue[i];
			}
		}
		return ReturnValue;
	}
}
