package ece1778.mozartsear;

import java.util.ArrayList;

public class DurationAsBeatFraction {
	
	/********************************************************************/
	/*          DetermineKey method - key detection algorithm           */
	/********************************************************************/
	public void DetermineDurationAsBeat(ArrayList<Double> DurationArray, int Tempo_ms) {
		
		for (int CurrentIndex=0; CurrentIndex < DurationArray.size(); CurrentIndex++) {
			double durationAsBeat = DurationArray.get(CurrentIndex) / Tempo_ms;
			if (durationAsBeat > 3.5) {
				DurationArray.set(CurrentIndex, 4.0);
			}
			else if ((durationAsBeat > 2.5) && (durationAsBeat <= 3.5)) {
				DurationArray.set(CurrentIndex, 3.0);
			}
			else if ((durationAsBeat > 1.75) && (durationAsBeat <= 2.5)) {
				DurationArray.set(CurrentIndex, 2.0);
			}
			else if ((durationAsBeat > 1.25) && (durationAsBeat <= 1.75)) {
				DurationArray.set(CurrentIndex, 1.5);
			}
			else if ((durationAsBeat > 0.875) && (durationAsBeat <= 1.25)) {
				DurationArray.set(CurrentIndex, 1.0);
			}
			else if ((durationAsBeat > 0.625) && (durationAsBeat <= 0.875)) {
				DurationArray.set(CurrentIndex, 0.75);
			}
			else if ((durationAsBeat > 0.375) && (durationAsBeat <= 0.625)) {
				DurationArray.set(CurrentIndex, 0.50);
			}
			else if ((durationAsBeat > 0.1875) && (durationAsBeat <= 0.375)) {
				DurationArray.set(CurrentIndex, 0.25);
			}
			else if (durationAsBeat <= 0.1875) {
				DurationArray.set(CurrentIndex, 0.125);
			} 	
		}	
	}
}
