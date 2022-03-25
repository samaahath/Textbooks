package com.bookviewer.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;

public class Bookview extends AppCompatActivity{

    String Bookurl;
    PDFView pdfView;
    int spacingval =0;
    boolean setcheck=true;
    private LinearLayout spinnerlayout;
    SharedPreferences shp;
    ImageButton btnprev;
    ImageButton btnnext;
    EditText edtcurrentpageno;

    Toolbar toolbar;
    String Bookname;
    int pageno;
    String Type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookview);

        Bookurl = (getIntent().getStringExtra("Bookurl"));
        Bookname = (getIntent().getStringExtra("Bookname"));
        Type = (getIntent().getStringExtra("Type"));
        pageno = Integer.parseInt((getIntent().getStringExtra("Pagenum")));
        pdfView = (PDFView) findViewById(R.id.pdfView);
        spinnerlayout = findViewById(R.id.layoutspinner);
        spinnerlayout.setVisibility(View.GONE);
        shp = Bookview.this.getSharedPreferences("UserInfo", MainActivity.MODE_PRIVATE);
        btnprev =(ImageButton) findViewById(R.id.btnprev);
        btnnext =(ImageButton) findViewById(R.id.btnnext);
        edtcurrentpageno =(EditText) findViewById(R.id.edtcurrentpage);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title_bar = findViewById(R.id.title_bar);
        title_bar.setText(Bookname);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        loadbook();
        SwitchCompat sw = findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The toggle is enabled
                setcheck =true;
                spacingval=0;
                loadbook();
            } else {
                // The toggle is disabled
                setcheck=false;
                spacingval=5;
                loadbookvertical();

            }

        });
        edtcurrentpageno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!edtcurrentpageno.getText().toString().trim().equals("")) {
                    if (sw.isChecked()) {
                        if (Type.equals("English")) {
                            pdfView.jumpTo(Integer.parseInt(edtcurrentpageno.getText().toString().trim()) - 1);
                        } else {
                            pdfView.jumpTo((pageno - Integer.parseInt(edtcurrentpageno.getText().toString().trim())));
                        }
                    }
                    else
                    {
                        pdfView.jumpTo(Integer.parseInt(edtcurrentpageno.getText().toString().trim()) - 1);
                    }
                }


            }
        });

        btnprev.setOnClickListener(v -> {
            if(Type.equals("English")) {
                pdfView.jumpTo(pdfView.getCurrentPage() - 1, true);

            }
            else
            {
                pdfView.jumpTo(pdfView.getCurrentPage() + 1, true);
            }
        });
        btnnext.setOnClickListener(v -> {
            if(Type.equals("English")) {
                pdfView.jumpTo(pdfView.getCurrentPage() + 1, true);
            }
            else
            {

                pdfView.jumpTo(pdfView.getCurrentPage() - 1, true);
            }

        });
    }

    public void logout(View v) {
        SharedPreferences.Editor edit = shp.edit();
        edit.putString("loginid", "none");
        edit.apply();
        Intent i = new Intent(Bookview.this, Login.class);
        startActivity(i);
        finishAffinity();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void loadbook() {

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.parentlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
        spinnerlayout.setVisibility(View.VISIBLE);
        Thread thread = new Thread(new Runnable() {
            InputStream inputStream = null;
            @Override
            public void run() {

                try {
                    URL url = new URL(Bookurl);
                    // below is the step where we are
                    // creating our connection.
                    HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == 200) {
                        // response is success.
                        // we are getting input stream from url
                        // and storing it in our variable.
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    }


                } catch (IOException e) {
                    // this is the method
                    // to handle errors.
                    e.printStackTrace();
                }
                // GfG Thread Example
                runOnUiThread(() -> {
                    if(Type.equals("English")) {
                        pdfView.fromStream(inputStream)
                                .defaultPage(0)
                                .autoSpacing(false)
                                .spacing(spacingval)
                                .swipeHorizontal(setcheck)
                                .pageSnap(setcheck)
                                .pageFling(setcheck)
                                .enableDoubletap(true)
                                .pageFitPolicy(FitPolicy.BOTH)
                                .enableSwipe(true)
                                .enableAntialiasing(true)
                                .enableAnnotationRendering(true)
                                .fitEachPage(true)
                                .onPageChange((page, pageCount) -> Toast.makeText(Bookview.this, page + " of " + pageno, Toast.LENGTH_LONG).show())
                                .onLoad(nbPages -> {
                                    RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.parentlayout);
                                    for (int i = 0; i < layout1.getChildCount(); i++) {
                                        View child = layout1.getChildAt(i);
                                        child.setEnabled(true);
                                    }
                                    spinnerlayout.setVisibility(View.GONE);
                                    //Toast.makeText(Bookview.this, Integer.toString(pdfView.getCurrentPage()) + " of " + Integer.toString(nbPages), Toast.LENGTH_LONG).show();

                                })

                                .load();
                    }
                    else
                    {
                        pdfView.fromStream(inputStream)
                                .pages(getPages(pageno))
                                .defaultPage(pageno)
                                .autoSpacing(false)
                                .spacing(spacingval)
                                .swipeHorizontal(setcheck)
                                .pageSnap(setcheck)
                                .pageFling(setcheck)
                                .enableDoubletap(true)
                                .pageFitPolicy(FitPolicy.BOTH)
                                .enableSwipe(true)
                                .enableAntialiasing(true)
                                .enableAnnotationRendering(true)
                                .fitEachPage(true)
                                .onPageChange((page, pageCount) -> Toast.makeText(Bookview.this, (pageCount - page) + " of " + pageno, Toast.LENGTH_LONG).show())
                                .onLoad(nbPages -> {
                                    RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.parentlayout);
                                    for (int i = 0; i < layout1.getChildCount(); i++) {
                                        View child = layout1.getChildAt(i);
                                        child.setEnabled(true);
                                    }   spinnerlayout.setVisibility(View.GONE);
                                    //Toast.makeText(Bookview.this, Integer.toString(nbPages- pdfView.getCurrentPage()) + " of " + Integer.toString(nbPages), Toast.LENGTH_LONG).show();

                                })

                                .load();
                    }

                });
            }
        });
        thread.start();

    }
    private void loadbookvertical() {

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.parentlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }
        spinnerlayout.setVisibility(View.VISIBLE);
        Thread thread = new Thread(new Runnable() {
            InputStream inputStream = null;
            @Override
            public void run() {

                try {
                    URL url = new URL(Bookurl);
                    // below is the step where we are
                    // creating our connection.
                    HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == 200) {
                        // response is success.
                        // we are getting input stream from url
                        // and storing it in our variable.
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    }


                } catch (IOException e) {
                    // this is the method
                    // to handle errors.
                    e.printStackTrace();
                }
                // GfG Thread Example
                runOnUiThread(() -> pdfView.fromStream(inputStream)
                        .defaultPage(0)
                        .autoSpacing(false)
                        .spacing(spacingval)
                        .swipeHorizontal(setcheck)
                        .pageSnap(setcheck)
                        .pageFling(setcheck)
                        .enableDoubletap(true)
                        .pageFitPolicy(FitPolicy.BOTH)
                        .enableSwipe(true)
                        .enableAntialiasing(true)
                        .enableAnnotationRendering(true)
                        .fitEachPage(true)
                        .onPageChange((page, pageCount) -> Toast.makeText(Bookview.this, page + " of " + pageno, Toast.LENGTH_LONG).show())
                        .onLoad(nbPages -> {
                            RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.parentlayout);
                            for (int i = 0; i < layout1.getChildCount(); i++) {
                                View child = layout1.getChildAt(i);
                                child.setEnabled(true);
                            }
                            spinnerlayout.setVisibility(View.GONE);
                            //Toast.makeText(Bookview.this, Integer.toString(pdfView.getCurrentPage()) + " of " + Integer.toString(nbPages), Toast.LENGTH_LONG).show();

                        })

                        .load());
            }
        });
        thread.start();

    }
    private int [] getPages(int pagesNum){
        int [] pages = new int[pagesNum];
        int j = 0;
        for (int i = pages.length-1; i >= 0 ; i--){
            pages[j] = i;
            j++;
        }
        return pages;
    }

}