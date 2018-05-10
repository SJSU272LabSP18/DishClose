package demo.tensorflow.org.customvision_sample;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import demo.tensorflow.org.customvision_sample.env.Logger;

public class Feedback extends AppCompatActivity  implements AsyncResponse  {
    private static final Logger LOGGER = new Logger();
    String tag="";

    public void processFinish(String r){

        LOGGER.i("From Process Finish %s", r);
        try {
            Toast.makeText(Feedback.this, "Thank You for the feedback", Toast.LENGTH_LONG).show();
            Thread.sleep(1500);
            Intent intent = new Intent(getBaseContext(), History.class);
            startActivity(intent);
            finish();

        }catch (Exception e){
            LOGGER.i("Exception");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tag = getIntent().getStringExtra("EXTRA_SESSION_ID");

        final Button submitButton = findViewById(R.id.submit);
        submitButton .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                final Spinner feedbackSpinner1 = (Spinner) findViewById(R.id.SpinnerFeedbackType1);
                String image = feedbackSpinner1.getSelectedItem().toString();
                LOGGER.i("Image: %s", image);

                final Spinner feedbackSpinner2 = (Spinner) findViewById(R.id.SpinnerFeedbackType2);
                String ingredient = feedbackSpinner2.getSelectedItem().toString();
                LOGGER.i("Ing: %s", ingredient);

                final Spinner feedbackSpinner3 = (Spinner) findViewById(R.id.SpinnerFeedbackType3);
                String recipe = feedbackSpinner3.getSelectedItem().toString();
                LOGGER.i("Recipe: %s", recipe);
                LOGGER.i("Tag: %s", tag);

                final EditText feedbackField = (EditText) findViewById(R.id.EditTextFeedbackBody);
                String text = feedbackField.getText().toString();


                LOGGER.i("Feedback Text: %s",text );


                try{

                    FeedbackLongRunningGetIO longRunningGetIO = new FeedbackLongRunningGetIO();
                    longRunningGetIO.delegete=Feedback.this;
                    longRunningGetIO.execute(image,recipe,ingredient,text,tag);
                }
                catch (Exception e){
                    LOGGER.i("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$: %s", e.getMessage());
                }

            }
        });
    }



}



class FeedbackLongRunningGetIO extends AsyncTask<String, Void, String> {
    private static final Logger LOGGER = new Logger();

    public AsyncResponse delegete=null;

    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        InputStream in = entity.getContent();
        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n > 0) {
            byte[] b = new byte[4096];
            n = in.read(b);
            if (n > 0) out.append(new String(b, 0, n));
        }
        return out.toString();
    }


    @Override
    protected String doInBackground(String... params) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost("http://13.57.176.22:3001/handleFeedback");
        JSONObject tag = new JSONObject();
        String text = null;

        try {
            tag.put("image", params[0]);
            tag.put("recipe", params[1]);
            tag.put("ingredient", params[2]);
            tag.put("text", params[3]);
            tag.put("tag", params[4]);


            StringEntity jsonEntity = new StringEntity(tag.toString());
            httpPost.setEntity(jsonEntity);
            httpPost.addHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(httpPost, localContext);
            HttpEntity entity = response.getEntity();
            text = getASCIIContentFromEntity(entity);

        } catch (Exception e) {
            return e.getLocalizedMessage();

        }
        return text;
    }


    protected void onPostExecute(String results) {

//        DataHolder.getInstance().setData(results);
//        DataHolder.getInstance().setShowData(true);
        LOGGER.i("############: %s", results);
        if (results!=null) {

            delegete.processFinish(results);
//            EditText et = (EditText)findViewById(R.id.my_edit);
//            et.setText(results);
//        }
//        Button b = (Button)findViewById(R.id.my_button);
//        b.setClickable(true);

        }



    }
}