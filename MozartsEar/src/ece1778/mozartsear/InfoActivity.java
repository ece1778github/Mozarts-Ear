package ece1778.mozartsear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class InfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_info, menu);
		return true;
	}
	
	/*****************************************************************/
    /*                 MENU ITEM Buttons                             */
    /*****************************************************************/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    		case R.id.menu_home_info:
    			finish();
    			Intent homeIntent = new Intent(this, Main.class);
    			startActivity(homeIntent);
    			return true;

        	case R.id.menu_transcribe_info:
        		finish();
        		Intent transcribeIntent = new Intent(this, AcquireTempoActivity.class);
                startActivity(transcribeIntent);
        		return true;

        	case R.id.menu_library_info:
        		finish();
        		Intent libraryIntent = new Intent(this, LibraryActivity.class);
                startActivity(libraryIntent);
        		return true;

        	case R.id.menu_info_info:
        		//Intent infoIntent = new Intent(this, InfoActivity.class);
                //startActivity(infoIntent);
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
	}

}
