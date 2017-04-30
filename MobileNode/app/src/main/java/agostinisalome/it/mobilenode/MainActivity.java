package agostinisalome.it.mobilenode;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.services.sqs.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import agostinisalome.it.mobilenode.Fragment.ReadFragment;
import agostinisalome.it.mobilenode.Fragment.SetFilterFragment;
import agostinisalome.it.mobilenode.Fragment.SettingsFragment;
import agostinisalome.it.mobilenode.Fragment.WriteFragment;
import agostinisalome.it.mobilenode.Utils.AWSSimpleQueueServiceUtil;
import agostinisalome.it.mobilenode.Utils.DBHelper;
import agostinisalome.it.mobilenode.Utils.Util;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Util util = new Util();
    AWSSimpleQueueServiceUtil test;
    private Button button;
    private ListView spinn;
    private ListView mylist;
    public String akey;
    public String skey;
    public String UserId;
    DBHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//Permette la connessione ad Internet con il Main Thread
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }
        /* connessione aws */
        try {
            akey = util.getProperty("accessKey", this.getApplicationContext());
            skey = util.getProperty("secretKey", this.getApplicationContext());
            test = new AWSSimpleQueueServiceUtil(akey, skey);
            db = new DBHelper(getApplicationContext());
            
//Esempi di funzioni di Get e Post Http verso il server
            String unique_id= Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            Log.e("UserID",unique_id);
            String get= util.GET("http://hmkcode.com/examples/index.php");
            Log.e("GET Response",get);

            String post = util.POST("http://hmkcode.appspot.com/jsonservlet",Util.publish(unique_id,"Server","ciao"));
            Log.e("POST",post);

//AsyncTask per la gestione di tutti i thread
           new AsyncPullTask().execute("0");

        }catch (IOException e) {
            e.printStackTrace();
        }


    }

    private class AsyncPullTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {

            try {
                String queueUrl = test.getQueueUrl("Server");

                List<Message>messageList = new ArrayList();

                do {
                    messageList = test.getMessagesFromQueue(queueUrl);

                    for (int i = 0; i < messageList.size(); i++) {


                        Date data = new Date();
                        db.insertTableFiltered(data,"Server",messageList.get(i).getBody());

                        test.deleteMessageFromQueue(queueUrl,messageList.get(i));


                    }
                }     while(messageList.size()!=0);


            }catch(NullPointerException e){
                e.printStackTrace();

            }
            try {
                Thread.sleep(Integer.valueOf(params[0])*100);
                //wait(Integer.valueOf(params[0])*100);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (util.getBatteryLevel(MainActivity.this)>=75.0) {
             //Minuti di attesa 
              return "5";
          }
          else if(util.getBatteryLevel(MainActivity.this)>= 50.0){
              return  "10";
          }
          else {
              return "30";
          }



        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Context context= getApplicationContext();
           new AsyncPullTask().execute(result);

            return;

        }


        @Override
        protected void onPreExecute() {
            Context context= getApplicationContext();
            Toast t= Toast.makeText(context,"Inizio esecuzione",Toast.LENGTH_LONG);
            t.show();
        }


        @Override
        protected void onProgressUpdate(String... text) {


        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
//elenco dei Fragment allegati al Menù laterale
        if (id == R.id.nav_camera) {
            // Handle the camera action
            fragment= new WriteFragment();

        } else if (id == R.id.nav_gallery) {
            fragment = new ReadFragment();

        } else if (id == R.id.nav_slideshow) {
            fragment = new SetFilterFragment();

        } else if (id == R.id.nav_manage) {
            fragment= new SettingsFragment();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if(fragment!=null){
            FragmentManager fm=getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame,fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
