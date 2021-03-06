package com.projects.oleg.viewtotextureconverter.Views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.projects.oleg.viewtotextureconverter.Utils;

import java.util.ArrayList;

/**
 * Created by momo-chan on 7/13/15.
 */
public class VoiceEditText extends EditText implements RecognitionListener, View.OnClickListener, View.OnFocusChangeListener {
    private SpeechStatusListener listener;
    private boolean voiceDetectOn = false;
    private boolean ready = false;
    private SpeechRecognizer voiceTT;
    private Intent voiceIntent;

    public VoiceEditText(Context context) {
        super(context);
        getSpeechRecognizer();
    }

    public VoiceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        getSpeechRecognizer();
    }

    public VoiceEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getSpeechRecognizer();
    }

    public VoiceEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getSpeechRecognizer();
    }

    private void startVoice(){
        if(!voiceDetectOn){
            voiceTT.startListening(voiceIntent);
            Utils.print("staring to listen");
        }
    }

    @Override
    public void onClick(View v) {
        Utils.print("I am touched");
        startVoice();
    }

    private boolean first = true;
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus && !first){
            startVoice();
        }
        first = false;
    }


    private void getSpeechRecognizer(){
        voiceTT = SpeechRecognizer.createSpeechRecognizer(getContext());
        voiceTT.setRecognitionListener(this);

        voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
        voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        setOnFocusChangeListener(this);
        setOnClickListener(this);
    }



    public void setResultListener(SpeechStatusListener listener){
        this.listener = listener;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Utils.print("ready for speech");
        ready = true;
        Utils.print("Speech has started");
        voiceDetectOn = true;
        if(listener != null){
            listener.onRecognitionStarted();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        if(listener != null){
            listener.onWordStarted();
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        if(listener != null){
            listener.onWordEnded();
        }
    }

    @Override
    public void onError(int error) {
        voiceDetectOn = false;
        if(listener != null){
            listener.onRecognitionEnded();
        }
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> recogResult = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Utils.print("Voice recognition result: " + recogResult.get(0));
        if(listener != null){
            if(recogResult.get(0) != null) {
                listener.onSpeechResult(recogResult.get(0));
                setText(recogResult.get(0));
            }
        }

        Utils.print("Speech has ended");
        voiceDetectOn = false;
        if(listener != null){
            listener.onRecognitionEnded();
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        onResults(partialResults);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public interface SpeechStatusListener{
        public void onRecognitionStarted();
        public void onRecognitionEnded();
        public void onWordStarted();
        public void onWordEnded();
        public void onSpeechResult(String result);
    }
}
