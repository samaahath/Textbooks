package com.bookviewer.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Booksbygrade extends AppCompatActivity {
    private ArrayList<booklistitems> itemArrayList;  //List items Array
    private MyAppAdapter myAppAdapter; //Array Adapter
    private boolean success = false; // boolean
    private ConnectionClass connectionClass; //Connection Class Variable
    TextView txtfooter;
    RelativeLayout footer;
    TextView txtunvailable;
    private int firstVisibleItem, visibleItemCount,totalItemCount;
    private RelativeLayout parentlayout;
    private  RelativeLayout emptylayout;
    GridView gridView;
    String GradeId;
    private LinearLayout spinnerlayout;
    Toolbar toolbar;
    String Gradename;
    SharedPreferences shp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booksbygrade);
        connectionClass = new ConnectionClass(); // Connection Class Initialization
        itemArrayList = new ArrayList<>(); // Arraylist Initialization
        GradeId = (getIntent().getStringExtra("GradeId"));
        Gradename = (getIntent().getStringExtra("Gradename"));
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView title_bar = findViewById(R.id.title_bar);
        title_bar.setText(Gradename);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        footer = findViewById(R.id.footer);
        txtfooter = findViewById(R.id.textView3);
        txtunvailable = findViewById(R.id.txtunavailable);
        footer.setVisibility(View.GONE);
        txtunvailable.setVisibility(View.GONE);

        parentlayout = findViewById(R.id.parentlayout);
        emptylayout = findViewById(R.id.emptylayout);
        RelativeLayout outerparentlayout = findViewById(R.id.outerparentlayout);
        spinnerlayout = findViewById(R.id.layoutspinner);
        spinnerlayout.setVisibility(View.GONE);
        gridView = findViewById(R.id.booklist);

        checkinternet();


        outerparentlayout.setOnClickListener(v -> checkinternet());
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount && scrollState == SCROLL_STATE_IDLE) {
                    ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(Booksbygrade.this.getSystemService(Context.CONNECTIVITY_SERVICE));
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                    if (activeNetwork != null) {
                        loadmorebooks();

                    }
                    else {
                        //get next 10-20 items(your choice)items

                        emptylayout.setVisibility(View.VISIBLE);
                        parentlayout.setVisibility(View.GONE);
                        spinnerlayout.setVisibility(View.GONE);
                    }

                    //get next 10-20 items(your choice)items

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItemm, int visibleItemCountt, int totalItemCountt) {
                firstVisibleItem = firstVisibleItemm;
                visibleItemCount = visibleItemCountt;
                totalItemCount = totalItemCountt;


            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void checkinternet()

    {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(Booksbygrade.this.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            // connected to the internet
            // Toast.makeText(MainActivity.this, "Unable to connect to Mobile data!", Toast.LENGTH_LONG).show();
            shp = Booksbygrade.this.getSharedPreferences("UserInfo", MainActivity.MODE_PRIVATE);
            emptylayout.setVisibility(View.GONE);

           loadbooks();
            parentlayout.setVisibility(View.VISIBLE);

//            if (!cm.getNetworkCapabilities(cm.getActiveNetwork()).hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || !cm.getNetworkCapabilities(cm.getActiveNetwork()).hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                 Toast.makeText(getContext(), "No internet connection, click to try again!", Toast.LENGTH_LONG).show();
//            }
        }
        else
        {
            emptylayout.setVisibility(View.VISIBLE);
            parentlayout.setVisibility(View.GONE);

        }
    }
    public void logout(View v) {
        SharedPreferences.Editor edit = shp.edit();
        edit.putString("loginid", "none");
        edit.apply();
        Intent i = new Intent(Booksbygrade.this, Login.class);
        startActivity(i);
        finishAffinity();

    }
    public void loadbooks()
    {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.parentlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }  spinnerlayout.setVisibility(View.VISIBLE);
        itemArrayList.clear();
        Thread thread = new Thread(new Runnable() {
            String msg = "";
            int value;
            @Override
            public void run() {
                try {
                    Connection conn = connectionClass.CONN(); //Connection Object
                    if (conn == null) {
                        success = false;
                    } else {
                        value = 0;
                        // Change below query according to your own database.
                        String query = "select BookName,CoverimgUrl,BookUrl,Type,Pagenumber from tblbooks where GradeId ='"+GradeId +"'ORDER BY tblbooks.BookId DESC OFFSET "+ value +"  ROWS FETCH NEXT 6 ROWS ONLY";
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(query);
                        if (rs != null) // if resultset not null, I add items to itemArraylist using class created
                        {
                            while (rs.next()) {
                                try {
                                    itemArrayList.add(new booklistitems("", rs.getString("BookName"),rs.getString("CoverimgUrl"),rs.getString("BookUrl"),rs.getString("Type"),rs.getString("Pagenumber"),"",""));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                            success = true;
                            stmt.close();
                            rs.close();
                            conn.close();
                        } else {

                            success = false;
                            msg="";
                        }
                    }
                } catch (Exception e) {
                    msg=e.toString();

                    success = false;
                }
                // GfG Thread Example
                runOnUiThread(() -> {
                    if(itemArrayList.size()==0)
                    {
                        // txtfooter.setText("no items in this category!");
                        txtunvailable.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        txtunvailable.setVisibility(View.GONE);
                    }

                    if (!msg.equals("")) {
                        Toast.makeText(Booksbygrade.this, msg + "", Toast.LENGTH_LONG).show();
                    }

                        try {
                            myAppAdapter = new MyAppAdapter(itemArrayList, Booksbygrade.this);
                            gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                            gridView.setAdapter(myAppAdapter);
                            footer.setVisibility(View.GONE);
                        } catch (Exception ex) {
                            Toast.makeText(Booksbygrade.this, ex.toString(), Toast.LENGTH_LONG).show();
                        }

                    RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.parentlayout);
                    for (int i = 0; i < layout1.getChildCount(); i++) {
                        View child = layout1.getChildAt(i);
                        child.setEnabled(true);
                    }
                    spinnerlayout.setVisibility(View.GONE);
                });
            }
        });
        thread.start();

    }

    private  void loadmorebooks()
    {
        footer.setVisibility(View.VISIBLE);
        Thread thread = new Thread(new Runnable() {
            int value;
            @Override
            public void run() {
                try {
                    Connection conn = connectionClass.CONN(); //Connection Object
                    if (conn != null) {
                        value = itemArrayList.size();
                        // Change below query according to your own database.
                        String query = "select BookName,CoverimgUrl,BookUrl,Type,Pagenumber from tblbooks where GradeId ='"+GradeId +"'ORDER BY tblbooks.BookId DESC OFFSET "+ value +"  ROWS FETCH NEXT 6 ROWS ONLY";
                        Statement stmt = conn.createStatement();
                        ResultSet rs = ((Statement) stmt).executeQuery(query);
                        if (rs != null) // if resultset not null, I add items to itemArraylist using class created
                        {

                            while (rs.next()) {
                                try {

                                    itemArrayList.add(new booklistitems("", rs.getString("BookName"),rs.getString("CoverimgUrl"),rs.getString("BookUrl"),rs.getString("Type"),rs.getString("Pagenumber"),"",""));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                            stmt.close();
                            rs.close();
                            conn.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // GfG Thread Example
                runOnUiThread(() -> {
                    if(itemArrayList.size()==0)
                    {
                          txtunvailable.setVisibility(View.GONE);

                            txtunvailable.setVisibility(View.VISIBLE);

                    }
                    else if(itemArrayList.size()==value)
                    {
                        txtfooter.setText(R.string.nomoreitems);
                        myAppAdapter = new MyAppAdapter(itemArrayList, Booksbygrade.this);
                        gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        gridView.setAdapter(myAppAdapter);
                        gridView.setSelection(myAppAdapter.getCount());
                        footer.setVisibility(View.GONE);
                    }
                    else
                    {
                        txtunvailable.setVisibility(View.GONE);
                        try {
                            myAppAdapter = new MyAppAdapter(itemArrayList, Booksbygrade.this);
                            gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                            gridView.setAdapter(myAppAdapter);
                            gridView.setSelection(myAppAdapter.getCount() - 6);
                            footer.setVisibility(View.GONE);

                        } catch (Exception ignored) {

                        }
                    }
                });
            }
        });
        thread.start();

    }
    static class ViewHolder {
        final TextView textName;
        final ImageView imageView;


        public ViewHolder(View view) {
            textName = view.findViewById(R.id.itemName);
            imageView = view.findViewById(R.id.itemThumbnail);
        }
    }


    public class MyAppAdapter extends BaseAdapter        //has a class viewholder which holds
    {


        final List<booklistitems> parkingList;

        public final Context context;
        final ArrayList<booklistitems> arraylist;

        private MyAppAdapter(List<booklistitems> apps, Context context) {
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
                rowView = inflater.inflate(R.layout.bookslistview, parent, false);
                viewHolder = new ViewHolder(rowView);



                rowView.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) convertView.getTag();

            }
            // here setting up names and images
            viewHolder.textName.setText(String.format("%s", parkingList.get(position).getBookName()));
             // Picasso.get().load(parkingList.get(position).getImg()).placeholder(R.drawable.noimage).into(viewHolder.imageView);
           //Glide.with(Booksbygrade.this).load(parkingList.get(position).getCoverimgUrl()).centerCrop().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).placeholder(R.drawable.noimage).into(viewHolder.imageView);
          // Glide.with(Booksbygrade.this).load(parkingList.get(position).getCoverimgUrl()).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(viewHolder.imageView);
            Glide.with(Booksbygrade.this).load(parkingList.get(position).getCoverimgUrl()).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).placeholder(R.drawable.noimage).into(viewHolder.imageView);

            rowView.setOnClickListener(v -> {
             Intent i = new Intent(Booksbygrade.this, Bookview.class);
                i.putExtra("Bookurl", parkingList.get(position).getBookUrl());
                i.putExtra("Bookname", parkingList.get(position).getBookName());
                i.putExtra("Type", parkingList.get(position).getType());
                i.putExtra("Pagenum", parkingList.get(position).getPagenumber());

                startActivity(i);


            });
            return rowView;


        }


    }


}