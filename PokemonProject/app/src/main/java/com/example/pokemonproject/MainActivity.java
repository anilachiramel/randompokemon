package com.example.pokemonproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    static final String DATA_NOT_FOUND = "Data Not Found!!";
    final int min = 0;
    final int max = 898;

    String pokeUrl = "https://pokeapi.co/api/v2/pokemon/";
    TextView txtPokemonName;
    ImageButton btnRefresh;
    ImageView ivFrontImage;
    ImageView ivBackImage;
    TextView textViewMoves;
    TextView textViewStats;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtPokemonName = findViewById(R.id.textView);
        btnRefresh = findViewById(R.id.button);
        ivFrontImage =  findViewById(R.id.frontImage);
        ivBackImage =  findViewById(R.id.backImage);
        textViewMoves =  findViewById(R.id.textViewMoves);
        textViewStats =  findViewById(R.id.textViewStats);

        textViewMoves.setMovementMethod(new ScrollingMovementMethod());
        textViewStats.setMovementMethod(new ScrollingMovementMethod());
        fetchRandomPokemon();
        btnRefresh.setOnClickListener(v -> fetchRandomPokemon());
    }

    void fetchRandomPokemon() {
        int random = new Random().nextInt((max - min) + 1) + min;
        new JsonTask().execute(pokeUrl+random+"/");
    }

    private class JsonTask extends AsyncTask<String, String, String> {
        String resultPokemonName = "";
        Bitmap resultFrontImage;
        Bitmap resultBackImage;
        StringBuffer moveName = new StringBuffer();
        StringBuffer statsName = new StringBuffer();

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading...");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    JSONObject baseJSONResponse = new JSONObject(line);
                    resultPokemonName = baseJSONResponse.getString("name");
                    JSONObject sprites = baseJSONResponse.getJSONObject("sprites");
                    url = new URL(sprites.getString("front_shiny"));
                    resultFrontImage = BitmapFactory
                            .decodeStream(url.openConnection().getInputStream());
                    url = new URL(sprites.getString("back_shiny"));
                    resultBackImage = BitmapFactory
                            .decodeStream(url.openConnection().getInputStream());
                    JSONArray moves = baseJSONResponse.getJSONArray("moves");
                    for (int i=0;i<moves.length();i++) {
                        JSONObject move = moves.getJSONObject(i).getJSONObject("move");
                        moveName.append(move.getString("name")).append("\n");
                    }
                    JSONArray stats = baseJSONResponse.getJSONArray("stats");
                    for (int i=0;i<stats.length();i++) {
                        JSONObject stat = stats.getJSONObject(i).getJSONObject("stat");
                        statsName.append(stat.getString("name")).append("\n");
                    }
                }
                return null;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            if (Objects.isNull(resultPokemonName) || resultPokemonName.isEmpty()) txtPokemonName.setText(DATA_NOT_FOUND);
            else txtPokemonName.setText(resultPokemonName);

            if (Objects.isNull(moveName) || moveName.length() == 0) textViewMoves.setText(DATA_NOT_FOUND);
            else textViewMoves.setText(moveName);

            if (Objects.isNull(statsName) || statsName.length() == 0) textViewStats.setText(DATA_NOT_FOUND);
            else textViewStats.setText(statsName);

            if (Objects.isNull(resultFrontImage)) ivFrontImage.setImageResource(R.drawable.datanotfound);
            else ivFrontImage.setImageBitmap(resultFrontImage);

            Log.d("ABC", "onPostExecute: " + resultBackImage);
            Log.d("ABC", "onPostExecute: " + Objects.isNull(resultBackImage));
            if (Objects.isNull(resultBackImage)) ivBackImage.setImageResource(R.drawable.datanotfound);
            else ivBackImage.setImageBitmap(resultBackImage);
        }
    }
}