package com.bookviewer.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ArrayList<classlistitems> itemArrayList;
    private MyAppAdapter myAppAdapter; //Array Adapter
    private ListView listView; // Listview
    private ConnectionClass connectionClass; //Connection Class Variable
    private ProgressBar spinner;
    private RelativeLayout parentlayout;
    private RelativeLayout emptylayout;
    Toolbar toolbar;
    SharedPreferences shp;
    String currentVersion="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemArrayList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.shippinglistview); //Listview Declaration
        connectionClass = new ConnectionClass();

        spinner = (ProgressBar) findViewById(R.id.progressBar1);

        parentlayout = (RelativeLayout) findViewById(R.id.parentlayout);
        emptylayout = (RelativeLayout) findViewById(R.id.emptylayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title_bar = findViewById(R.id.title_bar);
        title_bar.setText(R.string.textbooks);
        checkinternet();

        try {

             currentVersion = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            Log.e("Current Version","::"+currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

         GetVersionCode();
    }

    private void GetVersionCode()
    {
        Thread thread = new Thread(new Runnable() {
            String newVersion = null;
            @Override
            public void run() {

                try {
                    Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName()  + "&hl=en")
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get();
                    if (document != null) {
                        Elements element = document.getElementsContainingOwnText("Current Version");
                        for (Element ele : element) {
                            if (ele.siblingElements() != null) {
                                Elements sibElemets = ele.siblingElements();
                                for (Element sibElemet : sibElemets) {
                                    newVersion = sibElemet.text();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // GfG Thread Example
                runOnUiThread(() -> {
                    if (newVersion != null && !newVersion.isEmpty()) {

                        if (Float.parseFloat(currentVersion) < Float.parseFloat(newVersion)) {
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Update");
                            alertDialog.setIcon(R.mipmap.ic_launcher);
                            alertDialog.setMessage("New Update is available");

                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Update", (dialog, which) -> {
                                try {
                                   new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                                } catch (ActivityNotFoundException anfe) {
                                   new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                                }
                            });

                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());

                            alertDialog.show();

                            alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(MainActivity.this.getColor(R.color.colorAccent));

                            alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(MainActivity.this.getColor(R.color.colorAccent));

                        }

                    }

                    Log.d("update", "Current version " + currentVersion + "playstore version " + newVersion);

                });
            }
        });
        thread.start();
    }

    private void checkinternet() {
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            // connected to the internet
            // Toast.makeText(MainActivity.this, "Unable to connect to Mobile data!", Toast.LENGTH_LONG).show();
            shp = MainActivity.this.getSharedPreferences("UserInfo", MainActivity.MODE_PRIVATE);


                emptylayout.setVisibility(View.GONE);
                listView.setAdapter(null);
                // Calling Async Task
                loadgrades();
                parentlayout.setVisibility(View.VISIBLE);


//            if (!cm.getNetworkCapabilities(cm.getActiveNetwork()).hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || !cm.getNetworkCapabilities(cm.getActiveNetwork()).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                 Toast.makeText(getContext(), "No internet connection, click to try again!", Toast.LENGTH_LONG).show();
//            }
        } else {
            emptylayout.setVisibility(View.VISIBLE);
            parentlayout.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);

        }
    }
    public void logout(View v) {
        SharedPreferences.Editor edit = shp.edit();
        edit.putString("loginid", "none");
        edit.apply();
        Intent i = new Intent(MainActivity.this, Login.class);
        startActivity(i);
            finishAffinity();

    }
    private void loadgrades() {
        spinner.setVisibility(View.VISIBLE);
        itemArrayList.clear();
        Thread thread = new Thread(() -> {
            try {
                Connection con = connectionClass.CONN(); //Connection Object
                if (con != null) {

                    // Change below query according to your own database.
                    String query = "select GradeId,Name from tblgrades ORDER BY tblgrades.GradeId ASC";
                    Statement stmt = con.createStatement();
                    ResultSet rs = ((Statement) stmt).executeQuery(query);
                    if (rs != null) // if resultset not null, I add items to itemArraylist using class created
                    {
                        while (rs.next()) {
                            try {
                                itemArrayList.add(new classlistitems(rs.getString("GradeId"), rs.getString("Name")));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        stmt.close();
                        rs.close();
                        con.close();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            // GfG Thread Example
            runOnUiThread(() -> {
                try {

                    spinner.setVisibility(View.GONE);
                    myAppAdapter = new MyAppAdapter(itemArrayList, MainActivity.this);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setAdapter(myAppAdapter);
                    if (itemArrayList.size() != 0) {
                        //getActivity().setTitle(String.format("Cart (" + itemArrayList.size()+ ")", toString() ));
                        parentlayout.setVisibility(View.VISIBLE);

                    } else {
                        //getActivity().setTitle(String.format("Cart (" + itemArrayList.size()+ ")", toString() ));
                        parentlayout.setVisibility(View.GONE);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //   Toast.makeText(PackageShipping.this, ignored.toString(), Toast.LENGTH_LONG).show();
                }


            });
        });
        thread.start();

    }

    public class MyAppAdapter extends BaseAdapter        //has a class viewholder which holds
    {

        class ViewHolder {
            TextView txtclassname;
        }

        final List<classlistitems> parkingList;

        public final Context context;
        final ArrayList<classlistitems> arraylist;

        private MyAppAdapter(List<classlistitems> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
            arraylist = new ArrayList<>();
            arraylist.addAll(parkingList);
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) // inflating the layout and initializing widgets
        {

            View rowView = convertView;
            ViewHolder viewHolder;


            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.classlistview, parent, false);
                viewHolder = new ViewHolder();

                viewHolder.txtclassname = (TextView) rowView.findViewById(R.id.txtclassname);
                rowView.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) convertView.getTag();

            }

            viewHolder.txtclassname.setText( parkingList.get(position).getClassname());



            rowView.setOnClickListener(v -> {
                Intent i = new Intent(MainActivity.this, Booksbygrade.class);
               i.putExtra("GradeId", parkingList.get(position).getClassId());
                i.putExtra("Gradename", parkingList.get(position).getClassname());

                startActivity(i);

            });
            return rowView;


        }

    }
}