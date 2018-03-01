package something.overwatch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hotchemi.android.rate.AppRate;

//import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String PACKAGE_NAME;
    public static List<String> heroNames = Arrays.asList("Doomfist", "Genji", "Mccree", "Pharah", "Reaper", "Soldier 76", "Sombra", "Tracer", "Bastion", "Hanzo", "Junkrat", "Mei", "Torbjorn", "Widowmaker", "D.va", "Orisa", "Reinhardt",
            "Roadhog", "Winston", "Zarya", "Ana", "Brigitte", "Lucio", "Mercy", "Moira", "Symmetra", "Zenyatta");
    public static List<String> heroClasses = Arrays.asList("Offense","Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Defense", "Defense", "Defense", "Defense", "Defense", "Defense", "Tank", "Tank", "Tank",
            "Tank", "Tank", "Tank", "Support", "Support", "Support", "Support", "Support", "Support", "Support");
    public static List<String> mapNames = Arrays.asList("Blizzard World", "Dorado", "Eichenwalde", "Hanamura", "Hollywood", "No Ilios Map Yet", "King's Row", "Lijiang Tower", "Nepal", "Numbani", "Route 66", "Temple of Anubis", "Volskaya Industries", "Watchpoint: Gibraltar");
    public static List<String> mapTypes = Arrays.asList("AssaultEscort", "Escort", "AssaultEscort", "Assault", "AssaultEscort", "Control", "AssaultEscort", "Control", "Control", "AssaultEscort", "Escort", "Assault", "Assault", "Escort");
    Toolbar toolbar = null;
    NavigationView navigationView = null;
    private static int currentFragment = 0;
    private ProgressDialog progDialog;
    private boolean isDialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PACKAGE_NAME = getPackageName();

        //Set the fragment initially
        if(currentFragment==R.id.nav_players) {
            PlayerFragment fragment = new PlayerFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Players");
            currentFragment=R.id.nav_players;
        }else {
            MainFragment fragment = new MainFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Heroes");
            currentFragment = R.id.nav_heroes;
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //check if file exists
        if (!fileExistance("data.xls") || !fileExistance("heroes.xls")) {
            //fetching data for the first time. Must stop user from all action.
            progDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
            progDialog.setTitle("Please wait...");
            progDialog.setMessage("Downloading data for the first time");
            progDialog.setCancelable(false);
            progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
            progDialog.show();
            isDialogShowing = true;
        }
        DownloadDataTask task = new DownloadDataTask();
        task.execute();

        AppRate.with(this)
                .setInstallDays(0)
                .setLaunchTimes(2)
                .setRemindInterval(1)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);
    }

    //check if file exists function
    public boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    public void startHeroInfo(){
        Intent heroInfoIntent = new Intent(this, HeroInfoActivity.class);
        startActivity(heroInfoIntent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_heroes) {
            MainFragment fragment = new MainFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Heroes");
            currentFragment = R.id.nav_heroes;
        } else if (id == R.id.nav_maps) {
            MapsFragment fragment = new MapsFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Maps");
            currentFragment = R.id.nav_maps;
        } else if (id == R.id.nav_patchnotes) {
            PatchNotesFragment fragment = new PatchNotesFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Patch Notes");
            currentFragment = R.id.nav_patchnotes;
        } else if (id == R.id.nav_players) {
            PlayerFragment fragment = new PlayerFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Players");
            currentFragment = R.id.nav_players;
        } else if (id == R.id.nav_about) {
            AboutFragment fragment = new AboutFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("About");
            currentFragment = R.id.nav_about;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("fragment", currentFragment);
        super.onSaveInstanceState(outState);
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        private AlertDialog exitDialog;
        private String getRemoteVersion(){
            String versionRemote;
            try {
                URL urlVersion = new URL("http://158.69.60.95/version.json");
                InputStream is = urlVersion.openConnection().getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                    sb.append('\r');
                }
                rd.close();
                is.close();
                JSONObject json = new JSONObject(sb.toString());
                versionRemote = json.getString("version");
            } catch (Exception e){
                return "noInternet";
            }
            return versionRemote;
        }
        private void saveNewVersionCode(String v){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("version", v);
            editor.apply();
        }
        @Override
        protected String doInBackground(String... params) {
            String versionRemote = getRemoteVersion();
            //check if getting the remote version is successful
            if(versionRemote.equals("noInternet")) {
                //if this is the first time, must not let user click anything
                if(!fileExistance("data.xls") || !fileExistance("heroes.xls"))
                    return "needInternetError";
                //otherwise, there are local files, do not do anything
                return "doNothing";
            }
            //check if local data is up to date
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String versionLocal = sharedPref.getString("version", "-1");
            if (versionLocal.equals(versionRemote)) {
                //if local data is up to date or there is no internet connection, do not update
                return "doNothing";
            }
            try{
                String fileNameHp = "data.xls";
                String fileNameData = "heroes.xls";
                URL urlHp = new URL("http://158.69.60.95/data.xls");
                URL urlData = new URL("http://158.69.60.95/heroes.xls");
                InputStream input = urlHp.openConnection().getInputStream();
                FileOutputStream output = openFileOutput(fileNameHp, Context.MODE_PRIVATE);
                int read;
                byte[] data = new byte[1024];
                while ((read = input.read(data)) != -1)
                    output.write(data, 0, read);
                output.close();
                input.close();
                input = urlData.openConnection().getInputStream();
                output = openFileOutput(fileNameData, Context.MODE_PRIVATE);
                while ((read = input.read(data)) != -1)
                    output.write(data, 0, read);
                output.close();
                input.close();
                //store new version code
                saveNewVersionCode(versionRemote);
            } catch(Exception e){
                e.printStackTrace();
                return "error";
            }
            return "updated";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isDialogShowing){
                progDialog.dismiss();
                isDialogShowing = false;
            }
            if (s.equals("updated")){
                Toast.makeText(getApplicationContext(), "All data up to date", Toast.LENGTH_SHORT).show();
            } else if(s.equals("error")) {
                Toast.makeText(getApplicationContext(), "Update failed. Please check your internet connection.", Toast.LENGTH_SHORT).show();
            } else if(s.equals("needInternetError")) {
                // show alert dialog because it needs to download data for the first time
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                alert.setTitle("No Internet Connection");
                alert.setMessage("You must be connected to internet to download data for the first time.");
                alert.setCancelable(false);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
                alert.show();
            }
        }
    }

    /*
    // GET the modified time of data on Google Drive
    private class RequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlString = "https://www.googleapis.com/drive/v3/files/19Dw0CMdQc8CuMmQudC2upkG9LHIAl_8WAeXRk-bbj2s?key=AIzaSyC99M-7t3fE3HLDO-mXGB0gIVf0nxXU7OA&fields=modifiedTime";
            HttpURLConnection c = null;
            String result = "";

            try {
                c = (HttpURLConnection)(new URL(urlString).openConnection());
                c.setRequestMethod("GET");

                InputStream is = c.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = rd.readLine())!=null){
                    sb.append(line);
                    sb.append('\r');
                }
                rd.close();
                result = sb.toString();
            } catch (Exception e){
                e.printStackTrace();
                result = "";
            } finally {
                //if(c!=null) c.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!s.equals("")){
                try {
                    JSONObject json = new JSONObject(s);
                    String modifiedTime = json.getString("modifiedTime");
                    Toast.makeText(getApplicationContext(), modifiedTime, Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed to check for update", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "Failed to check for update", Toast.LENGTH_SHORT).show();
            }
        }
    }
    */
}
