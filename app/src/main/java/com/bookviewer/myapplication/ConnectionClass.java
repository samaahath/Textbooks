package com.bookviewer.myapplication;
import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
public class ConnectionClass {
  // String ip = "SQL5102.site4now.net";
   String ip = "3.135.23.234";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    String db = "db_a773c6_bookviewer";
    //String un = "db_a773c6_bookviewer_admin";
    String un = "sa";
   // String password = "jughead96";
    String password = "Jauf1996!";
    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL;
        try {

            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + un + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (Exception se) {
            Log.e("ERRO", se.getMessage());
        }
        return conn;
    }
}
