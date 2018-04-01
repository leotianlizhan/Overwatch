package something.overwatch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.os.Handler;

import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String PACKAGE_NAME;
    public static List<String> heroNames = new ArrayList<>(Arrays.asList("Doomfist", "Genji", "Mccree", "Pharah", "Reaper", "Soldier 76", "Sombra", "Tracer", "Bastion", "Hanzo", "Junkrat", "Mei", "Torbjorn", "Widowmaker", "D.va", "Orisa", "Reinhardt",
            "Roadhog", "Winston", "Zarya", "Ana", "Brigitte", "Lucio", "Mercy", "Moira", "Symmetra", "Zenyatta"));
    public static List<String> heroClasses = new ArrayList<>(Arrays.asList("Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Defense", "Defense", "Defense", "Defense", "Defense", "Defense", "Tank", "Tank", "Tank",
            "Tank", "Tank", "Tank", "Support", "Support", "Support", "Support", "Support", "Support", "Support"));
    public static List<String> mapNames = Arrays.asList("Blizzard World", "Dorado", "Eichenwalde", "Hanamura", "Hollywood", "Horizon Lunar Colony", "Ilios", "Junkertown", "King's Row", "Lijiang Tower", "Nepal", "Numbani", "Oasis", "Route 66", "Temple of Anubis", "Volskaya Industries", "Watchpoint: Gibraltar");
    public static List<String> mapTypes = Arrays.asList("AssaultEscort", "Escort", "AssaultEscort", "Assault", "AssaultEscort", "Assault", "Control", "Escort", "AssaultEscort", "Control", "Control", "AssaultEscort", "Control", "Escort", "Assault", "Assault", "Escort");
    Toolbar toolbar = null;
    NavigationView navigationView = null;
    private SharedPreferences sharedPref;
    private ProgressDialog progDialog;
    private MainFragment mainFragment;
    public boolean oldDataIntegrity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        progDialog.setMessage("Processing data...");
        progDialog.show(); // must show dialog before dataIntegrity() to prevent users from clicking
        //checks if heroesList exists and valid, then check if there's an update
        checkForUpdate();

        // rate app
        AppRate.with(this)
                .setInstallDays(2)
                .setLaunchTimes(4)
                .setRemindInterval(2)
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

    //check data integrity of SharedPreferences.heroesList (data_min.json)
    //MUST BE CALLED BEFORE DownloadTask!
    public boolean dataIntegrity(){
        try {
            JSONArray data = new JSONArray(sharedPref.getString("heroesList", "-1"));
            if(!data.getJSONObject(10).has("hpTotal") || !data.getJSONObject(26).has("abilities"))
                return false;
        }catch (JSONException e) {
            Log.e("**DATA_INTEGRITY", e.toString());
            return false;
        }
        oldDataIntegrity = true;
        return true;
    }

    private void checkForUpdate(){
        if (sharedPref.contains("heroesList") && dataIntegrity()) {
            progDialog.setMessage("Checking for updates");
        } else if (sharedPref.contains("heroesList")){
            progDialog.setMessage("Invalid data detected, re-downloading hero data");
        } else {
            progDialog.setMessage("Downloading data for the first time");
        }
        DownloadDataTask task = new DownloadDataTask();
        task.execute();
    }

    private void updateHeroesList(){
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
        private JSONObject versionRemoteJson;
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
        private JSONObject getRemoteVersion(){
            return getJson("http://158.69.60.95/version.json");
        }
        private JSONArray getHeroesList(){
            JSONObject json = getJson("http://158.69.60.95/data_min.json");
            if(json==null)
                return null;
            try {
                return json.getJSONArray("list");
            } catch (Exception e){
                return null;
            }
        }
        private void saveNewVersionCode(String v){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("version", v);
            editor.apply();
        }
        private void saveHeroesList(JSONArray jArray){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("heroesList", jArray.toString());
            editor.apply();
        }
        @Override
        protected String doInBackground(String... params) {
            versionRemoteJson = getRemoteVersion();
            if(versionRemoteJson!=null){
                try{
                    versionRemote = versionRemoteJson.getString("version");
                } catch (Exception e){
                    Log.e("****VERSION", e.toString());
                    versionRemote = "noInternet";
                }
            } else versionRemote = "noInternet";
            //check if getting the remote version is successful
            if(versionRemote.equals("noInternet")) {
                return versionRemote;
            }
            //check if local data is up to date
            versionLocal = sharedPref.getString("version", "-1");
            if (versionLocal.equals(versionRemote) && oldDataIntegrity)
                return "doNothing";
            //get new data
            heroesList = getHeroesList();
            //check new data for error
            try {
                if(heroesList==null || !heroesList.getJSONObject(10).has("hpTotal") || !heroesList.getJSONObject(26).has("abilities")){
                    return "error";
                }
            } catch (JSONException e){
                Log.e("***DoInBackground", e.toString());
                return "error";
            }
            return "updated";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("noInternet") && !oldDataIntegrity) {
                // show alert dialog because it MUST download data for the first time
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                alert.setTitle("No Internet Connection");
                alert.setMessage("You must be connected to internet to download data for the first time.");
                //alert.setMessage(versionRemote);
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
            } else if (s.equals("updated")){
                Toast.makeText(getApplicationContext(), "All data up to date", Toast.LENGTH_SHORT).show();
                //store new version code
                saveNewVersionCode(versionRemote);
                saveHeroesList(heroesList);
            } else if(s.equals("error") && !oldDataIntegrity) {
                // show alert dialog because it needs to re-download due to invalid file
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                alert.setTitle("Download Failed");
                alert.setMessage("Downloading hero data failed. Make sure you have a stable internet connection and try again.");
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
            }
            updateHeroesList();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable(){
//                @Override
//                public void run() {
//                    progDialog.dismiss();
//                }
//            }, 4000);
            progDialog.dismiss();
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
