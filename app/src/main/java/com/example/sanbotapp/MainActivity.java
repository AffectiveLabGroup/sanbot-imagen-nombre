package com.example.sanbotapp;


import static com.qihancloud.opensdk.function.beans.SpeakOption.LAG_ENGLISH_US;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.LogWriter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sanbotapp.robotControl.FaceRecognitionControl;
import com.example.sanbotapp.robotControl.HardwareControl;
import com.example.sanbotapp.robotControl.SpeechControl;
import com.qihancloud.opensdk.base.TopBaseActivity;
import com.qihancloud.opensdk.beans.FuncConstant;
import com.qihancloud.opensdk.function.beans.EmotionsType;
import com.qihancloud.opensdk.function.beans.LED;
import com.qihancloud.opensdk.function.beans.SpeakOption;
import com.qihancloud.opensdk.function.beans.handmotion.AbsoluteAngleHandMotion;
import com.qihancloud.opensdk.function.beans.headmotion.AbsoluteAngleHeadMotion;
import com.qihancloud.opensdk.function.beans.headmotion.RelativeAngleHeadMotion;
import com.qihancloud.opensdk.function.beans.speech.Grammar;
import com.qihancloud.opensdk.function.beans.wheelmotion.RelativeAngleWheelMotion;
import com.qihancloud.opensdk.function.unit.HandMotionManager;
import com.qihancloud.opensdk.function.unit.HardWareManager;
import com.qihancloud.opensdk.function.unit.HeadMotionManager;
import com.qihancloud.opensdk.function.unit.MediaManager;
import com.qihancloud.opensdk.function.unit.SpeechManager;
import com.qihancloud.opensdk.function.unit.SystemManager;
import com.qihancloud.opensdk.function.unit.WheelMotionManager;
import com.qihancloud.opensdk.function.unit.interfaces.speech.RecognizeListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class MainActivity extends TopBaseActivity {


    public Boolean reconocimientoFacial = false;
    private Button btnImagen;

    private ImageButton imagen;

    private FaceRecognitionControl faceRecognitionControl;
    private SpeechManager speechManager;
    private MediaManager mediaManager;
    private SystemManager systemManager;
    private HandMotionManager handMotionManager;
    private WheelMotionManager wheelMotionManager;
    private HeadMotionManager headMotionManager;
    private HardWareManager hardwareManager;
    private SpeechControl speechControl;

    List<String> palabras = Arrays.asList("banana", "apple", "dog");
    int indiceActual = 0;


    @Override
    protected void onMainServiceConnected() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        onMainServiceConnected();
        setContentView(R.layout.activity_main);

        speechManager = (SpeechManager) getUnitManager(FuncConstant.SPEECH_MANAGER);
        mediaManager = (MediaManager) getUnitManager(FuncConstant.MEDIA_MANAGER);
        systemManager = (SystemManager) getUnitManager(FuncConstant.SYSTEM_MANAGER);
        handMotionManager = (HandMotionManager) getUnitManager(FuncConstant.HANDMOTION_MANAGER);
        wheelMotionManager = (WheelMotionManager) getUnitManager(FuncConstant.WHEELMOTION_MANAGER);
        headMotionManager = (HeadMotionManager) getUnitManager(FuncConstant.HEADMOTION_MANAGER);
        hardwareManager = (HardWareManager) getUnitManager(FuncConstant.HARDWARE_MANAGER);

        speechControl = new SpeechControl(speechManager);

        faceRecognitionControl = new FaceRecognitionControl(speechManager, mediaManager);

        btnImagen = findViewById(R.id.btnImagen);

        imagen = findViewById(R.id.imagen);

        faceRecognitionControl.stopFaceRecognition();

        setonClicks();

        SpeakOption speakOption = new SpeakOption();
        speakOption.setSpeed(50);
        speakOption.setIntonation(50);
        speakOption.setLanguageType(LAG_ENGLISH_US);

        speechManager.setOnSpeechListener(new RecognizeListener() {

            @Override
            public boolean onRecognizeResult(Grammar grammar) {

                // 4. Validar
                boolean correcto = grammar.getText() != null &&
                        grammar.getText().toLowerCase().contains(palabras.get(indiceActual).toLowerCase());

                runOnUiThread(() -> {
                    if (correcto) {
                        speechManager.startSpeak("Great!", speakOption);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Cambiar imagen
                        indiceActual++;
                        if (indiceActual >= palabras.size()) {
                            indiceActual = 0;
                        }

                        actualizarImagen();
                    } else {
                        speechManager.startSpeak("Try again!", speakOption);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                });

                return true;
            }

            @Override
            public void onRecognizeVolume(int i) {

            }
        });
    }


    public void setonClicks() {

        SpeakOption speakOption = new SpeakOption();
        speakOption.setSpeed(50);
        speakOption.setIntonation(50);
        speakOption.setLanguageType(LAG_ENGLISH_US);


        btnImagen.setOnClickListener(v -> {

            new Thread(() -> {

                speechManager.doWakeUp();

            }).start();
        });

    }

    private void actualizarImagen() {
        String nombreImagen = palabras.get(indiceActual);

        int resId = getResources().getIdentifier(
                nombreImagen,
                "drawable",
                getPackageName()
        );

        imagen.setImageResource(resId);
    }



    @Override
    public void onResume() {
        SpeakOption speakOption = new SpeakOption();
        speakOption.setSpeed(50);
        speakOption.setIntonation(50);

        super.onResume();
        // Inicializamos el sistema
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                speechManager.startSpeak("Clica el botón y dí el nombre de la imagen en inglés", speakOption );

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }, 200);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
