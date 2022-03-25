package com.bookviewer.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class Login extends AppCompatActivity {
    private LinearLayout spinnerlayout;
    ConnectionClass connectionClass;
    EditText edtemail,edtpass;
    Button btnlogin;
    SharedPreferences shp;
    String email, password;
    String loginid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        spinnerlayout = findViewById(R.id.layoutspinner);
        spinnerlayout.setVisibility(View.GONE);

        connectionClass = new ConnectionClass(); //the class file
        edtemail = findViewById(R.id.edtEmail);
        edtpass = findViewById(R.id.edtpass);
        btnlogin = findViewById(R.id.btnlogin);
        shp = this.getSharedPreferences("UserInfo", MODE_PRIVATE);

        String login = shp.getString("loginid", "none");

        if (Objects.requireNonNull(login).equals("none") || login.trim().equals("")) {
            //Do nothing
        }
        else

        {
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
            finish();
        }
        btnlogin.setOnClickListener(v -> {
            ConnectivityManager cm = (ConnectivityManager) Login.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null) {
                DoLogin();
            }
            else
            {
                Toast.makeText(Login.this, "No internet connection!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void DoLogin()
    {
        Login.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        spinnerlayout.setVisibility(View.VISIBLE);
        email = edtemail.getText().toString().trim();
        password = edtpass.getText().toString();
        Thread thread = new Thread(new Runnable() {
            String z="";
            Boolean isSuccess = false;
            @Override
            public void run() {
                if(email.trim().equals("")|| password.trim().equals(""))
                    z = "Please enter Email and Password";
                else
                {
                    try {
                        Connection con = connectionClass.CONN();
                        if (con != null) {

                                    //  String query = "select * from tblposCustomers where Email='" + email + "' and PasswordHash='" + password + "'";
                                    PreparedStatement stmt = con.prepareStatement("EXEC usp_dologin ?,?");
                                    stmt.setString(1, email);
                                    stmt.setString(2, password);
                                    ResultSet rs = stmt.executeQuery();

                                    if (rs.next()) {

                                        isSuccess = true;
                                        loginid = rs.getString(1);
                                        z = "";
                                    } else {
                                        z = "Invalid Credentials";
                                        isSuccess = false;

                                    }
                                    stmt.close();
                                    rs.close();
                                    con.close();
                                }


                    }
                    catch (Exception ex)
                    {
                        isSuccess = false;
                        z = ex.toString();
                    }
                }
                // GfG Thread Example
                runOnUiThread(() -> {

                    Login.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    spinnerlayout.setVisibility(View.GONE);

                    if (!z.equals("")) {
                        Toast.makeText(Login.this, z, Toast.LENGTH_SHORT).show();
                    }

                    if (isSuccess) {
                        SharedPreferences.Editor edit = shp.edit();
                        edit.putString("loginid", loginid);
                        edit.apply();
                        Intent i = new Intent(Login.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        });
        thread.start();

    }

}