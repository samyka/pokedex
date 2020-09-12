package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private ImageView imageView;
    private  TextView Description;
    Button button;
    private String url;
    private RequestQueue requestQueue;
    boolean is_catched = false;
    public SharedPreferences sharedPref;
    public String id = "0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        button = findViewById(R.id.button);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        imageView = findViewById(R.id.imageView);
        Description = findViewById(R.id.Description);

       // Toast.makeText(PokemonActivity.this, sharedPref.getString("id","0"), Toast.LENGTH_SHORT).show();




        load();
        loadimage();
        loaddescription();


    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    imageView.setImageResource(R.drawable.ic_launcher_background);

                    sharedPref = getSharedPreferences(nameTextView.getText().toString(),Context.MODE_PRIVATE);
                    if(sharedPref.contains(nameTextView.getText().toString())) {
                        is_catched = sharedPref.getBoolean(nameTextView.getText().toString(), false);
                        if (is_catched == true)
                        {
                            button.setText("Release");
                        }
                        else
                        {
                            button.setText("Catch");

                        }
                    }
                    else
                    {
                        is_catched = false;
                        button.setText("Catch");

                    }



                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }



                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }

    public void toggleCatch(View view) {

        if ((is_catched))
        {
            button.setText("Catch");
            is_catched = false;

        }
        else
        {
            button.setText("Release");
            is_catched = true;

        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(nameTextView.getText().toString(), is_catched).commit();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((is_catched))
                {
                    button.setText("Catch");
                    is_catched = false;

                }
                else
                {
                    button.setText("Release");
                    is_catched = true;

                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(nameTextView.getText().toString(), is_catched).commit();
            }
        });
        // gotta catch 'em all!
    }


    public void loadimage() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    imageView.setImageResource(R.drawable.ic_launcher_background);

                    JSONObject sprites = response.getJSONObject("sprites");
                    String image_url = sprites.getString("front_default");
                    new DownloadSpriteTask().execute(image_url);

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }
    public void loaddescription() {

        String info_url = "https://pokeapi.co/api/v2/pokemon-species/"+url.subSequence(33,url.length()-1).toString().replace("/","")+"/" ;
        Toast.makeText(PokemonActivity.this, url , Toast.LENGTH_SHORT).show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, info_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    JSONArray results = response.getJSONArray("flavor_text_entries");
                    JSONObject result = results.getJSONObject(1);
                    String name = result.getString("flavor_text");
                    Description.setText(name);

                }


                catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }


    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            // load the bitmap into the ImageView!
            imageView.setImageBitmap(bitmap);
        }
    }
}
