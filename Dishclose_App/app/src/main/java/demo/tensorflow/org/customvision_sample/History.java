package demo.tensorflow.org.customvision_sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class History extends AppCompatActivity {
    SQLiteHandler sqLiteHandler;
    private ListView historyListView ;
    ArrayList<String> arr = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sqLiteHandler = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> recipes;
        recipes  = sqLiteHandler.getRecipe();
        for ( String key : recipes.keySet() ) {
            arr.add(key);
        }

        if(arr.size()==0){
            arr.add("Start Scanning Dish...");
        }
        historyListView = (ListView) findViewById(R.id.history);
        ArrayAdapter ingAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arr);
        historyListView.setAdapter(ingAdapter);

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                //Toast.makeText(History.this, arr.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), ShowIngredientRecipe.class);
                intent.putExtra("EXTRA_SESSION_ID", arr.get(position));
                startActivity(intent);



            }
        });

    }





    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_view, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history:
                return true;
            case R.id.picture:
                Intent intent = new Intent(getBaseContext(), ClassifierActivity.class);
                startActivity(intent);
                return true;
            default:
                return true;
            //respond to menu item selection
        }}

}
