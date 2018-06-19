package com.example.pratyush.speechrecognizer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pratyush on 16/4/18.
 */

public class MainActivity extends AppCompatActivity {

    private TextView input;
    private Button shareButton;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private SpeechRecognizer sr;
    private static final String TAG = "MainActivity";
    private Uri altDynamicLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = findViewById(R.id.input);
        shareButton = findViewById(R.id.shareButton);


        //------------------------speech

        requestRecordAudioPermission();

        Button detect = findViewById(R.id.detectButton);

        mDBHelper = new DatabaseHelper(this);

        //Get writable database
        try {
            mDb = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            Log.d(TAG, mSQLException.getLocalizedMessage());
        }

        //Cursor to get cocktail names
        Cursor resultSet = mDb.rawQuery("SELECT name FROM COCKTAIL;", null);
        resultSet.moveToFirst();
        final String[] names = new String[resultSet.getCount()];
        for (int i = 0; i < resultSet.getCount(); i++) {

            names[i] = resultSet.getString(0);
            Log.i(TAG, names[i]);
            resultSet.moveToNext();

        }

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);

                sr.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle bundle) {
                        askToRunQ();
                        input.setText("ready");
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        input.setText("started");
                    }

                    @Override
                    public void onRmsChanged(final float v) {
                        if (progressBar != null) {
                            progressBar.setProgress((int) v * 5);
//                            animateProgress(progressBar, );
                        }

                        //  input.setText("rms changed"+String.valueOf(v));
                    }

                    @Override
                    public void onBufferReceived(byte[] bytes) {

                    }

                    @Override
                    public void onEndOfSpeech() {
                        input.setText("speech ended");
                    }

                    @Override
                    public void onError(int i) {
                        Log.d(TAG, "error " + i);
                    }

                    @Override
                    public void onResults(Bundle bundle) {
                        String str = new String();
                        Log.d(TAG, "onResults " + bundle);
                        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        for (int i = 0; i < data.size(); i++) {
                            Log.d(TAG, "result " + data.get(i));
                            str += data.get(i);

                        }
                        //  input.setText("Top result: " + data.get(0));
                        List<SpeechResult> speechList = new ArrayList<SpeechResult>();

                        String keyword = data.get(0);
                        for (int i = 0; i < names.length; i++) {
                            int distance = distance(names[i], keyword);

                            speechList.add(new SpeechResult(names[i], distance));
                        }

                        Collections.sort(speechList, new SpeechResultComparator());
                        Log.d(TAG, "Did you mean: " + speechList.get(0).getName());


                        for (String string : data) {
                            if (string.matches("make\\s*7\\s*and\\s*7\\s*")) {
                                Toast.makeText(MainActivity.this, "Make drink detected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle bundle) {

                        Log.d(TAG, "onPartialResults " + bundle);
                        ArrayList data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        for (int i = 0; i < data.size(); i++) {
                            Log.d(TAG, "result " + data.get(i));

                        }
                        input.setText("Top partial result: " + data.get(0));
                    }

                    @Override
                    public void onEvent(int i, Bundle bundle) {

                    }
                });
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);      //Use LANGUAGE_MODEL_FREE_FORM for non-localized voice input
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.example.pratyush.speechrecognizer");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                sr.startListening(intent);
            }
        });

    }

    private void animateProgress(ProgressBar progressBar, int value) {


        progressBar.setProgress(0);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", value);
        animation.setDuration(500); // 0.5 second
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    ProgressBar progressBar;

    public void askToRunQ() {

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.speech_dialog, null);
        progressBar = (ProgressBar) dialogView.findViewById(R.id.speech_progress);
        progressBar.setMax(200);

        final android.app.AlertDialog alertDialog1 = alertDialogBuilder.create();
        alertDialog1.setView(dialogView);
        // alertDialog1.setCancelable(false);
        alertDialog1.show();

    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    public static int distance(String a, String keyword) {
        a = a.toLowerCase();
        keyword = keyword.toLowerCase();
        int[] costs = new int[keyword.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= keyword.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == keyword.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[keyword.length()];
    }
}
