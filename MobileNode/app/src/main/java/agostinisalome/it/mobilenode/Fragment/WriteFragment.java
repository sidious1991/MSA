package agostinisalome.it.mobilenode.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import agostinisalome.it.mobilenode.R;
import agostinisalome.it.mobilenode.Utils.AWSSimpleQueueServiceUtil;
import agostinisalome.it.mobilenode.Utils.Util;

/**
 * Created by Paolo on 26/04/2017.
 */

public class WriteFragment extends Fragment  {
    private Button publish;
    private Button clear;
    //private Button create;
    private FloatingActionButton create;
    private ListView topics;
    private EditText textMulti;
    private  ProgressDialog p;
    private TextView text;
    public String akey;
    public String skey;
    private AWSSimpleQueueServiceUtil test;
    private String unique_id ;

    public WriteFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.activity_write,container,false);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }
        Util util = new Util();
        try {
            akey = util.getProperty("accessKey", this.getContext());
            skey = util.getProperty("secretKey", this.getContext());
            test = new AWSSimpleQueueServiceUtil(akey, skey);
            unique_id= Settings.Secure.getString(getContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            new AsyncTaskReader().execute();

        }catch (IOException e) {
            e.printStackTrace();
        }
        /* setting listener */

            topics=(ListView) view.findViewById(R.id.topics);

            text=(TextView)view.findViewById(R.id.textViewWrite) ;
            create=(FloatingActionButton) view.findViewById(R.id.create_topic);
            create.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add topic");

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT );
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                builder.setView(input,50,50,50,50);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //test.createQueue(input.getText().toString());
                        test.sendMessageToQueue("creationQueue",Util.createDelete("CREATE",unique_id,input.getText().toString()).toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });


        topics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {

               // Toast.makeText(getContext(),adapterView.getItemAtPosition(i).toString(),Toast.LENGTH_SHORT ).show();
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext());
                final EditText input = new EditText(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                input.setLayoutParams(lp);
                alert.setView(input,50,50,50,50);

                final String temp = adapterView.getItemAtPosition(i).toString();
                alert.setTitle("Write topic "+adapterView.getItemAtPosition(i).toString());
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Toast.makeText(getContext(),"OK",Toast.LENGTH_SHORT ).show();
                        //String queueUrl  = test.getQueueUrl(temp);
                        test.sendMessageToQueue("notificationQueue", Util.publish(unique_id,temp,String.valueOf(input.getText())).toString());
                        //test.sendMessageToQueue(queueUrl, String.valueOf(input.getText()));
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //   Toast.makeText(getContext(),"Cancel",Toast.LENGTH_SHORT ).show();
                            }
                        });
                alert.show();            }
        });
        return view;
    }



    private class AsyncTaskReader extends AsyncTask<String, String, String> {

        private String resp;


        @Override
        protected String doInBackground(String... params) {

            List<String> filtered = new ArrayList<String>();
            JSONObject json = new JSONObject();

            try {

               // List<String> lista = test.listQueues().getQueueUrls();
                filtered= Util.getTopicList();
                for(int i=0;i<filtered.size();i++) {
                    //filtered.add(lista.get(i).substring(lista.get(i).lastIndexOf("/") + 1, lista.get(i).length()));

                    json.put(""+i+"", filtered.get(i));
                }

            }catch(NullPointerException e){
                e.printStackTrace();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Context context= getContext();
            List<String> temp= new ArrayList<>();
            try {
                JSONObject json = new JSONObject(result);
                List<String> t = new ArrayList<>();
                for(int j=0;j<json.length();j++)
                    temp.add(json.getString(""+j+""));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getContext(),
                        android.R.layout.simple_list_item_1,
                        temp
                );
                text.setText("List Of Topics");
                topics.setAdapter(adapter);
                p.dismiss();
            }catch(NullPointerException e){
                e.printStackTrace();

            }
        }


        @Override
        protected void onPreExecute() {
            Context context= getContext();
            p = new ProgressDialog(context);
            p.setMessage(Html.fromHtml("<b>Waiting Loading....</b>"));
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }


        @Override
        protected void onProgressUpdate(String... text) {


        }
    }
}

