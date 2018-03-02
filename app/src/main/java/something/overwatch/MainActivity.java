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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.os.Handler;

import hotchemi.android.rate.AppRate;
import jxl.Workbook;

//import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String PACKAGE_NAME;
    public static List<String> heroNames = new ArrayList<>(Arrays.asList("Doomfist", "Genji", "Mccree", "Pharah", "Reaper", "Soldier 76", "Sombra", "Tracer", "Bastion", "Hanzo", "Junkrat", "Mei", "Torbjorn", "Widowmaker", "D.va", "Orisa", "Reinhardt",
            "Roadhog", "Winston", "Zarya", "Ana", "Brigitte", "Lucio", "Mercy", "Moira", "Symmetra", "Zenyatta"));
    public static List<String> heroClasses = new ArrayList<>(Arrays.asList("Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Defense", "Defense", "Defense", "Defense", "Defense", "Defense", "Tank", "Tank", "Tank",
            "Tank", "Tank", "Tank", "Support", "Support", "Support", "Support", "Support", "Support", "Support"));
    public static List<String> mapNames = Arrays.asList("Blizzard World", "Dorado", "Eichenwalde", "Hanamura", "Hollywood", "No Ilios Map Yet", "King's Row", "Lijiang Tower", "Nepal", "Numbani", "Route 66", "Temple of Anubis", "Volskaya Industries", "Watchpoint: Gibraltar");
    public static List<String> mapTypes = Arrays.asList("AssaultEscort", "Escort", "AssaultEscort", "Assault", "AssaultEscort", "Control", "AssaultEscort", "Control", "Control", "AssaultEscort", "Escort", "Assault", "Assault", "Escort");
    Toolbar toolbar = null;
    NavigationView navigationView = null;
    private static int currentFragment = 0;
    private ProgressDialog progDialog;
    //private boolean isDialogShowing = false;
    private MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PACKAGE_NAME = getPackageName();

//        //Set the fragment initially
//        if(currentFragment==R.id.nav_players) {
//            PlayerFragment fragment = new PlayerFragment();
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("Players");
//            currentFragment=R.id.nav_players;
//        }else {
//            MainFragment fragment = new MainFragment();
//            mainFragment = fragment;
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("Heroes");
//            currentFragment = R.id.nav_heroes;
//        }
        MainFragment fragment = new MainFragment();
        mainFragment = fragment;
        android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        setTitle("Heroes");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //prepare dialog for checking updates
        progDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progDialog.setTitle("Please wait...");
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        progDialog.setCancelable(false);
        //checks if file exists and valid, then check if there's an update
        checkForUpdate();
        //Toast.makeText(this, "onCreate called", Toast.LENGTH_LONG).show();

        // rate app
        AppRate.with(this)
                .setInstallDays(1)
                .setLaunchTimes(3)
                .setRemindInterval(1)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        //setup fragment backstack listner
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (f != null){
                    updateTitleAndDrawer (f);
                }

            }
        });
    }

    //check if file exists function
    public boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    //check file integrity of data.xls and heroes.xls
    public boolean fileIntegrity(){
        try {
            Workbook.getWorkbook(openFileInput("data.xls"));
            Workbook.getWorkbook(openFileInput("heroes.xls"));
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String heroesList = sharedPref.getString("heroesList", "-1");
            if(!heroesList.equals("-1")){
                JSONArray heroesArray = new JSONArray(heroesList);
            }
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private void checkForUpdate(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //check if file exists
        if (!fileExistance("data.xls") || !fileExistance("heroes.xls") || !sharedPref.contains("heroesList")) {
            //fetching data for the first time. Must stop user from all action.
            progDialog.setMessage("Downloading data for the first time");
        } else {
            if(fileIntegrity()){
                progDialog.setMessage("Checking for updates");
            } else {
                progDialog.setMessage("Invalid data detected, redownloading hero data");
            }
        }
        progDialog.show();
        //isDialogShowing = true;
        DownloadDataTask task = new DownloadDataTask();
        task.execute();
    }

    private void updateHeroesList(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String heroesList = sharedPref.getString("heroesList", "-1");
        if(heroesList.equals("-1") || mainFragment==null) return;
        try{
            JSONArray heroesArray = new JSONArray(heroesList);
            heroNames.clear();
            heroClasses.clear();
            for (int i = 0; i < heroesArray.length(); i++) {
                JSONObject hero = heroesArray.getJSONObject(i);
                heroNames.add(hero.getString("name"));
                heroClasses.add(hero.getString("class"));
            }
            if(mainFragment!=null) mainFragment.updateAdapter();
        }catch (JSONException e){
            e.printStackTrace();
            return;
        }
    }

    public void startHeroInfo(){
        Intent heroInfoIntent = new Intent(this, HeroInfoActivity.class);
        startActivity(heroInfoIntent);
    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

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

//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.nav_heroes) {
//            MainFragment fragment = new MainFragment();
//            mainFragment = fragment;
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("Heroes");
//            currentFragment = R.id.nav_heroes;
//        } else if (id == R.id.nav_maps) {
//            MapsFragment fragment = new MapsFragment();
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("Maps");
//            currentFragment = R.id.nav_maps;
//        } else if (id == R.id.nav_patchnotes) {
//            PatchNotesFragment fragment = new PatchNotesFragment();
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("Patch Notes");
//            currentFragment = R.id.nav_patchnotes;
//        } else if (id == R.id.nav_players) {
//            PlayerFragment fragment = new PlayerFragment();
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("Players");
//            currentFragment = R.id.nav_players;
//        } else if (id == R.id.nav_about) {
//            AboutFragment fragment = new AboutFragment();
//            android.support.v4.app.FragmentTransaction fragmentTransaction =
//                    getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentTransaction.commit();
//            setTitle("About");
//            currentFragment = R.id.nav_about;
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt("fragment", currentFragment);
//        super.onSaveInstanceState(outState);
//    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        private AlertDialog exitDialog;
        private String versionRemote;
        private String versionLocal;
        private JSONArray heroesList;
        private JSONObject getJson(String url){
            try {
                URL urlVersion = new URL(url);
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
                return new JSONObject(sb.toString());
            } catch (Exception e){
                return null;
            }
        }
        private String getRemoteVersion(){
            JSONObject json = getJson("http://158.69.60.95/version.json");
            if(json!=null){
                try {
                    return json.getString("version");
                } catch (Exception e){
                    return "noInternet";
                }
            } else {
                return "noInternet";
            }
        }
        private JSONArray getHeroesList(){
            JSONObject json = getJson("http://158.69.60.95/data.json");
            if(json!=null){
                try {
                    return json.getJSONArray("list");
                } catch (Exception e){
                    return null;
                }
            } else {
                return null;
            }
        }
        private void saveNewVersionCode(String v){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("version", v);
            editor.apply();
        }
        private void saveHeroesList(JSONArray jArray){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("heroesList", jArray.toString());
            editor.apply();
        }
        @Override
        protected String doInBackground(String... params) {
            versionRemote = getRemoteVersion();
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
            versionLocal = sharedPref.getString("version", "-1");
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
                //get hero list update
                heroesList = getHeroesList();
                if(heroesList==null) throw new JSONException("Getting heroes list failed");
            } catch(Exception e){
                e.printStackTrace();
                return "error";
            }
            return "updated";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //check file integrity
            if(s.equals("needInternetError")) {
                // show alert dialog because it needs to download data for the first time
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                alert.setTitle("No Internet Connection");
                alert.setMessage("You must be connected to internet to download data for the first time.");
                alert.setCancelable(false);
                alert.setPositiveButton("RETRY", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkForUpdate();
                    }
                });
                alert.setNegativeButton("EXIT", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
                alert.show();
            } else if(!fileIntegrity()){
                // show alert dialog because it needs to re-download due to invalid file
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                alert.setTitle("Download Failed");
                alert.setMessage("Downloading hero data failed. Make sure you have a stable internet connection.");
                alert.setCancelable(false);
                alert.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkForUpdate();
                    }
                });
                alert.setNegativeButton("Exit", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
                alert.show();
            } else if (s.equals("updated")){
                Toast.makeText(getApplicationContext(), "All data up to date", Toast.LENGTH_SHORT).show();
                //store new version code
                saveNewVersionCode(versionRemote);
                saveHeroesList(heroesList);
            } else if(s.equals("error")) {
                Toast.makeText(getApplicationContext(), "Update failed. Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
            updateHeroesList();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable(){
//                @Override
//                public void run() {
//                    progDialog.dismiss();
//                    //isDialogShowing = false;
//                }
//            }, 100);
            progDialog.dismiss();
//            isDialogShowing = false;
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
    //*************** FRAGMENT CODE ***************************/
    private void updateTitleAndDrawer (Fragment fragment){
        String fragClassName = fragment.getClass().getName();

        if (fragClassName.equals(MainFragment.class.getName())){
            setTitle("Heroes");
            navigationView.setCheckedItem(R.id.nav_heroes);
        }
        else if (fragClassName.equals(MapsFragment.class.getName())){
            setTitle("Maps");
            navigationView.setCheckedItem(R.id.nav_maps);
        }
        else if (fragClassName.equals(PatchNotesFragment.class.getName())){
            setTitle("Patch Notes");
            navigationView.setCheckedItem(R.id.nav_patchnotes);
        }
        else if (fragClassName.equals(PlayerFragment.class.getName())){
            setTitle("Players");
            navigationView.setCheckedItem(R.id.nav_players);
        }
        else if (fragClassName.equals(AboutFragment.class.getName())){
            setTitle("About");
            navigationView.setCheckedItem(R.id.nav_about);
        }
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
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        navigate(id);

        // Close nav drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void navigate(int id){
        Fragment fragment = null;
        if (id == R.id.nav_heroes) {
            fragment = new MainFragment();
        } else if (id == R.id.nav_maps) {
            fragment = new MapsFragment();
        } else if (id == R.id.nav_patchnotes) {
            fragment = new PatchNotesFragment();
        } else if (id == R.id.nav_players) {
            fragment = new PlayerFragment();
        } else if (id == R.id.nav_about) {
            fragment = new AboutFragment();
        }

        if(fragment != null){
            replaceFragment(fragment);
        }
    }

    private void replaceFragment (Fragment fragment){
        String backStateName =  fragment.getClass().getName();
        String fragmentTag = backStateName;

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, fragment, fragmentTag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }
}
