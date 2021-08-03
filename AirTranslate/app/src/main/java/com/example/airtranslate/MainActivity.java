package com.example.airtranslate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.airtranslate.API.APIRequestData;
import com.example.airtranslate.API.RetroServer;
import com.example.airtranslate.Model.ResponseModel;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private Spinner spinnerFrom, spinnerTo;
    private TextView hasil, tvFrom, tvTo;
    private EditText edtText;

    private String[] nama_negara, id_negara;
    private String hasilTerjemah, from, to, bahasa_dipilih, bahasa_dari, versiAplikasi;

    private ImageView btnChange, btnVoice, btnCopy, btnSpeak;

    private ClipboardManager clipboardManager;
    private ClipData clipData;
    private Snackbar snackbar;
    private TextToSpeech textToSpeech;

    private Toolbar toolBar;

    private static final int REQUEST_CODE_INPUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        insisialisasi();

        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Air");
        toolBar.setTitleTextColor(Color.WHITE);
        toolBar.setBackgroundColor(Color.parseColor("#0092c9"));

        textToSpeech = new TextToSpeech(getApplicationContext(), this);

        btnVoice.bringToFront();

        nama_negara = getResources().getStringArray(R.array.nama_negara);
        id_negara = getResources().getStringArray(R.array.id_negara);

        ArrayAdapter<String> adapterString = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, nama_negara);

        spinnerFrom.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        spinnerTo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        spinnerTo.setGravity(Gravity.CENTER);
        spinnerFrom.setGravity(Gravity.CENTER);
        spinnerFrom.setAdapter(adapterString);
        spinnerTo.setAdapter(adapterString);

        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#0092c9"));
                from = parent.getItemAtPosition(position).toString();
                bahasa_dari = id_negara[position];
                tvFrom.setText(from);

                fungsiTranslate(edtText.getText().toString(), bahasa_dari, bahasa_dipilih);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                to = parent.getItemAtPosition(position).toString();
                bahasa_dipilih = id_negara[position];
                tvTo.setText(to);

                fungsiTranslate(edtText.getText().toString(), bahasa_dari, bahasa_dipilih);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                ((TextView)parent.getChildAt(0)).setTextColor(Color.WHITE);
            }
        });

        btnChange.setOnClickListener(v -> {

            int spinner_from = spinnerFrom.getSelectedItemPosition();
            int spinner_to = spinnerTo.getSelectedItemPosition();

            spinnerTo.setSelection(spinner_from);
            spinnerFrom.setSelection(spinner_to);

            tvTo.setText(spinnerTo.getSelectedItem().toString());
            tvFrom.setText(spinnerFrom.getSelectedItem().toString());

            edtText.setText(hasil.getText().toString());
            hasil.setText(edtText.getText().toString());

            fungsiTranslate(edtText.getText().toString(), bahasa_dari, bahasa_dipilih);

        });

        tvTo.setText(spinnerTo.getSelectedItem().toString());
        tvFrom.setText(spinnerFrom.getSelectedItem().toString());

        edtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fungsiTranslate(s, bahasa_dari, bahasa_dipilih);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnCopy.setOnClickListener(v -> {
            clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipData = ClipData.newPlainText("text", hasil.getText().toString());
            clipboardManager.setPrimaryClip(clipData);


            snackbar = Snackbar
                    .make(v, "Text Berhasil Dicopy", Snackbar.LENGTH_SHORT);
            snackbar.show();
        });

        btnSpeak.setOnClickListener(v -> fungsiSpeak());

        btnVoice.setOnClickListener(v -> fungsiVoice());

    }

    private void fungsiVoice() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Mendengarkan...");

        try {
            startActivityForResult(intent, REQUEST_CODE_INPUT);
        } catch (Exception exception) {
            Toast.makeText(this, "" + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fungsiSpeak() {
        textToSpeech.speak(hasil.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

    }

    private void fungsiTranslate(CharSequence text, String fromCountry, String toCountry) {

        APIRequestData translateApi = RetroServer.konekRetrofit().create(APIRequestData.class);
        Call<ResponseModel> tampilTranslate = translateApi.ardKirimTranslate(text.toString(), fromCountry, toCountry);

        tampilTranslate.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                try {
                    hasilTerjemah = response.body().getResult();
                    hasil.setText(hasilTerjemah);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Maaf Server Sedang Sibuk!!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d("gagal", t.getMessage());
                Toast.makeText(MainActivity.this, "gagal" + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void insisialisasi() {
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);

        btnChange = findViewById(R.id.btnChange);

        hasil = findViewById(R.id.tvTerjemahan);
        edtText = findViewById(R.id.edtText);

        tvFrom = findViewById(R.id.tvFromNegaraTerjemahan);
        tvTo = findViewById(R.id.tvNegaraTerjemahan);
        btnVoice = findViewById(R.id.btnVoice);
        btnCopy = findViewById(R.id.btnCopy);
        btnSpeak = findViewById(R.id.btnSpeak);

        toolBar = findViewById(R.id.toolBar);


    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.getDefault());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> hasilSuara = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                );
                edtText.setText(hasilSuara.get(0));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.about) {
            bukaDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @SuppressLint("SetTextI18n")
    private void bukaDialog() {
        try {
//            Mengambil Info Versi Aplikasi
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            versiAplikasi = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_layout, null);

        TextView api = view.findViewById(R.id.link);
        TextView versi_aplikasi = view.findViewById(R.id.tvVersi);

        versi_aplikasi.setText(versiAplikasi);

        api.setText("https://tools.helixs.tech//home/?API");
        api.setAutoLinkMask(RESULT_OK);
        api.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(api, Linkify.ALL);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tentang")
                .setView(view);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
