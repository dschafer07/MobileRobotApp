package com.example.robovision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
/*import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;*/
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.example.robovision.bluetooth.BTBaseApplication;

public class VoiceControlActivity extends Activity implements
        RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView returnedText;
    private TextView returnedError;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private final String LOG_TAG = "VoiceRecognitionActivity";

    private BTBaseApplication mApplication;
    private final static String FORWARD = "1";
    private final static String REVERSE = "2";
    private final static String RIGHT   = "3";
    private final static String LEFT    = "4";
    private final static String STOP    = "0";
    private final static String ENTER   = "c";
    private final static String EXIT    = ";";


    private final Map<String, String> mCommands = new HashMap<String, String>();

    private void resetSpeechRecognizer() {

        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if(SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();
    }

    private void setRecogniserIntent() {

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    private void setMap() {
        mCommands.put("MOVE",FORWARD);
        mCommands.put("REVERSE",REVERSE);
        mCommands.put("RIGHT",RIGHT);
        mCommands.put("LEFT",LEFT);
        mCommands.put("STOP",STOP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_control);

        //Grab Global activity
        mApplication = (BTBaseApplication)getApplication();

        // UI initialisation
        returnedText = findViewById(R.id.textView1);
        returnedError = findViewById(R.id.errorView1);
        progressBar =  findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);


        // start speech recogniser
        resetSpeechRecognizer();

        // bluetooth setup
        mApplication = (BTBaseApplication)getApplication(); //getting application varaibles

        // start progress bar
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        // check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        // set map values
        setMap();

        setRecogniserIntent();
        speech.startListening(recognizerIntent);

        Enter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent);
            } else {
                Toast.makeText(VoiceControlActivity.this, "Permission Denied!", Toast
                        .LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public void onResume() {
        Log.i(LOG_TAG, "resume");
        super.onResume();
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "pause");
        super.onPause();
        speech.stopListening();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(EXIT);
        }
        Log.d("VoiceControl", "Exiting VC mode");
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        speech.stopListening();
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        StringBuilder text = new StringBuilder();
        for (String result : matches)
            text.append(result).append("\n");
        CheckCommands(text.toString());
        returnedText.setText(text.toString());
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(LOG_TAG, "FAILED " + errorMessage);
        returnedError.setText(errorMessage);
        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
    public void CheckCommands (String text) {
        for (String key : mCommands.keySet()) {
            if (text.toUpperCase().contains(key)) {
                if (text.toUpperCase().contains("SECOND")) {
                    String[] splitText = text.split("\n");
                    ArrayList<String> numList = new ArrayList<>(0);
                    for (String subString : splitText) {
                            numList.add(subString.replaceAll("[^0-9]",""));
                    }
                    for (String value : numList) {
                        if (!value.isEmpty()) {
                            TimedMove(key, value);
                            break;
                        }
                    }
                }
                else {
                    Move(key);
                }
                break;
            }
        }
    }

    private void Move(String command) {
        Log.d("VoiceControl","Move command initiated. Command code: " + command);
        if (mApplication.bluetoothThread!=null) {
            mApplication.bluetoothThread.write(Objects.requireNonNull(mCommands.get(command)));
        }
    }
    private void TimedMove(String command, String time) {
        Log.d("VoiceControl","Timed move command initiated. Command code: " + command + " Duration: " + time);
        if (mApplication.bluetoothThread!=null) {
            mApplication.bluetoothThread.write(Objects.requireNonNull(mCommands.get(command)));
        }
        Timer timer = new Timer("Timer");
        TimerTask stopTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("VoiceControl", "Timed task ended.");
                if (mApplication.bluetoothThread!=null) {
                    mApplication.bluetoothThread.write(STOP);
                }
            }
        };
        long delay = Long.parseLong(time) * 1000L;
        timer.schedule(stopTask,delay);
    }

    private void Enter(){
        Log.d("VoiceControl", "Entering VC Mode");
        if(mApplication.bluetoothThread!=null){
            mApplication.bluetoothThread.write(ENTER);
        }
    }

}