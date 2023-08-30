package com.img2imp.voiceselection;

import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextToSpeech mttsAr, mttsEn;
    EditText inputEt;
    CheckBox arabicCb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputEt = findViewById(R.id.input_edt);
        arabicCb = findViewById(R.id.arabic_cb);
        mttsAr = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mttsAr.setLanguage(Locale.forLanguageTag("ar"));

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                }
            } else {
            }
        });

        mttsEn = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mttsEn.setLanguage(Locale.ENGLISH);
                for (Voice tmpVoice : mttsEn.getVoices()) {
                    System.out.println("VVV "+tmpVoice.getName());
                }
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                }
            } else {
            }
        });



    }


    public void SpeakText(View view) {
        float baseTone = 0.5f;
        String tagString = (String)view.getTag();
        String sType = tagString.split(",")[0];
        System.out.println(sType+"sType"+sType.length()+sType);
        baseTone = 0.4f + Float.parseFloat(tagString.split(",")[1]);
        if(arabicCb.isChecked()){
            mttsAr.setPitch(baseTone);
            mttsAr.setSpeechRate(baseTone);
            Voice voiceobj;
            if(sType.charAt(0) == 'M'){
                System.out.println("TTT AR MALE"+" en-us-x-sfg#male_2-local");
                Set<String> a=new HashSet<>();
                a.add("male");
                voiceobj =new Voice("en-us-x-iom-network",Locale.getDefault(),400,200,true,a);
            }
            else{
                System.out.println("TTT AR FEMALE"+" ar-XA-Standard-A");
                voiceobj = new Voice("en-us-x-iom-local", new Locale("en","IN"), 1, 1, true, null);
            }
            mttsAr.setVoice(voiceobj);
            mttsAr.speak(inputEt.getText(),TextToSpeech.QUEUE_FLUSH,null,null);
        }
        else{
            mttsEn.setPitch(baseTone);
            mttsEn.setSpeechRate(baseTone);
            Voice voiceobj;
            if(sType.charAt(0) == 'M'){
                System.out.println("TTT MALE"+" en-IN-Standard-B");
                voiceobj = new Voice("en-us-x-iol-network", Locale.getDefault(), 1, 1, true, null);
            }
            else{
                System.out.println("TTT FEMALE"+" en-IN-Standard-A");
                voiceobj = new Voice("en-us-x-iol-local", Locale.getDefault(), 1, 1, true, null);
            }
            mttsEn.setVoice(voiceobj);
            mttsEn.speak(inputEt.getText(),TextToSpeech.QUEUE_FLUSH,null,null);
        }
    }
}