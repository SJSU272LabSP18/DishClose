package demo.tensorflow.org.customvision_sample;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import demo.tensorflow.org.customvision_sample.env.Logger;

public class ShowIngredientRecipe extends AppCompatActivity implements AsyncResponse {
    private static final Logger LOGGER = new Logger();
    private ListView ingListView ;
    private ListView stepsListView ;
    private ProgressBar spinner;
    private TextView title,ingredientText,recipe;
    private String [] data1 ={"H", "P", "D", "N", "P", "P"};
    private String [] data2 ={"K", "M", "B", "K", "A", "K"};
    SQLiteHandler sqLiteHandler;
    String s="";

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_view, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history:
                Intent intentHistory = new Intent(getBaseContext(), History.class);
                startActivity(intentHistory);
                return true;

            case R.id.picture:
                Intent intentClassifier = new Intent(getBaseContext(), ClassifierActivity.class);
                startActivity(intentClassifier);
                return true;


            default:
                return true;
                //respond to menu item selection
    }}

    public void processFinish(String r){
        LOGGER.i("From Process Finish %s", r);
        try {
            JSONObject jsonObject = new JSONObject(r);
            JSONArray steps = jsonObject.getJSONArray("steps");
            String[] arrSteps = new String[steps.length()];
            for (int i = 0; i < steps.length(); i++){
                arrSteps[i] = steps.getString(i);
        }

            JSONArray ingredient = jsonObject.getJSONArray("ingredients");
            String[] arrIngredient = new String[ingredient.length()];
            for(int i = 0; i < ingredient.length(); i++) {
                arrIngredient[i] = ingredient.getString(i);
            }
            sqLiteHandler.addRecipe(s,arrIngredient.toString(),arrSteps.toString());

            title = (TextView) findViewById(R.id.title);
            title.setText(jsonObject.getString("title"));

            ingListView = (ListView) findViewById(R.id.ingredient_list_view);
            ArrayAdapter ingAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrIngredient);
            ingListView.setAdapter(ingAdapter);

            stepsListView = (ListView) findViewById(R.id.recipe_list_view);
            ArrayAdapter stepsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrSteps);
            stepsListView.setAdapter(stepsAdapter);

            spinner=(ProgressBar)findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            ingredientText = (TextView) findViewById(R.id.ingredient);
            recipe = (TextView) findViewById(R.id.recipe);
            recipe.setVisibility(View.VISIBLE);
            ingredientText.setVisibility(View.VISIBLE);

    }catch (Exception e){
        LOGGER.i("Exception");
    }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sqLiteHandler = new SQLiteHandler(getApplicationContext());
        setContentView(R.layout.activity_show_ingredient_recipe3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        spinner=(ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.VISIBLE);


        ingredientText = (TextView) findViewById(R.id.ingredient);
        recipe = (TextView) findViewById(R.id.recipe);
        recipe.setVisibility(View.GONE);
        ingredientText.setVisibility(View.GONE);


        setSupportActionBar(toolbar);
        s = getIntent().getStringExtra("EXTRA_SESSION_ID");
            try{

                LongRunningGetIO longRunningGetIO = new LongRunningGetIO();
                longRunningGetIO.delegete=this;
                longRunningGetIO.execute(s);
            }
            catch (Exception e){
                LOGGER.i("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$: %s", e.getMessage());
            }


    }
    public void startFeedbackActivity(View v){
        Intent intent = new Intent(getBaseContext(), Feedback.class);
        intent.putExtra("EXTRA_SESSION_ID", s);
        startActivity(intent);
    }

}

class LongRunningGetIO extends AsyncTask<String, Void, String> {
    private static final Logger LOGGER = new Logger();
    private ListView list1, list2;

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
        HttpPost httpPost = new HttpPost("http://13.57.176.22:3001/processTag");
        JSONObject tag = new JSONObject();
        String text = null;

        try {
            tag.put("tag", params[0]);
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