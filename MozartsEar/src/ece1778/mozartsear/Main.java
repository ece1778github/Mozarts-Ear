package ece1778.mozartsear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*****************************************************************/
    /*                 START Button user click method                */
    /*****************************************************************/
    public void AcquireTempo(View view) {
    	/* Acquire Tempo activity call */
    	Intent intent = new Intent(this, AcquireTempoActivity.class);
        startActivity(intent);
    }
    
    /*****************************************************************/
    /*                 EXIT Button user click method                 */
    /*****************************************************************/
    public void exitMain(View view) {
    	finish();
    }
    
    /*****************************************************************/
    /*                 MENU ITEM Buttons                             */
    /*****************************************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    		case R.id.menu_home:
    			//finish();
    			//Intent homeIntent = new Intent(this, Main.class);
    			//startActivity(homeIntent);
    			return true;

        	case R.id.menu_transcribe:
        		Intent transcribeIntent = new Intent(this, AcquireTempoActivity.class);
                startActivity(transcribeIntent);
        		return true;

        	case R.id.menu_library:
        		//finish();
        		Intent libraryIntent = new Intent(this, LibraryActivity.class);
                startActivity(libraryIntent);
        		return true;

        	case R.id.menu_info:
        		Intent infoIntent = new Intent(this, InfoActivity.class);
                startActivity(infoIntent);
        		return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
