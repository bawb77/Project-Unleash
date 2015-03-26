package ca.drsystems.unleash;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class Tutorial extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean("firstLaunch", false);
        if(!previouslyStarted){
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("firstLaunch", Boolean.TRUE);
            edit.commit();

            final EditText editText = new EditText(this);

            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setHint("NAME HERE");

            AlertDialog builder = new AlertDialog.Builder(this).create();
            builder.setView(editText);
            builder.setTitle("Enter Your Name");

            builder.setButton(AlertDialog.BUTTON_NEUTRAL, "Enter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("playerName", editText.getText().toString());
                    edit.commit();

                    Log.v("MYNAME", "THIS PLAYER'S NAME IS: " + prefs.getString("playerName", "default"));
                }
            });

            builder.show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
