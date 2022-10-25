package something.overwatch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.perf.FirebasePerformance;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String remoteUrl = "http://s1.retort.ganks.me/";
    private ArrayList<String> heroNames = new ArrayList<>(Arrays.asList("Doomfist", "Genji", "Cassidy", "Pharah", "Reaper", "Soldier 76", "Sombra", "Tracer", "Bastion", "Hanzo", "Junkrat", "Mei", "Torbjorn", "Widowmaker", "D.va", "Orisa", "Reinhardt",
            "Roadhog", "Winston", "Zarya", "Ana", "Brigitte", "Lucio", "Mercy", "Moira", "Symmetra", "Zenyatta"));
    private ArrayList<String> heroClasses = new ArrayList<>(Arrays.asList("Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Damage", "Tank", "Tank", "Tank",
            "Tank", "Tank", "Tank", "Support", "Support", "Support", "Support", "Support", "Support", "Support"));
    public static final ArrayList<String> mapNames = new ArrayList<>(Arrays.asList("Blizzard World", "Busan", "Dorado", "Eichenwalde", "Hollywood", "Ilios", "Junkertown", "King's Row", "Lijiang Tower", "Nepal", "Numbani", "Oasis", "Rialto", "Route 66", "Watchpoint: Gibraltar", "Hanamura", "Horizon Lunar Colony", "Paris", "Temple of Anubis", "Volskaya Industries"));
    public static final ArrayList<String> mapTypes = new ArrayList<>(Arrays.asList("Hybrid", "Control", "Escort", "Hybrid", "Hybrid", "Control", "Escort", "Hybrid", "Control", "Control", "Hybrid", "Control", "Escort", "Escort", "Escort", "Assault", "Assault", "Assault", "Assault", "Assault"));
    private JSONArray heroesJson = null;
//    private Bundle heroesBundle = new Bundle();
    private Bundle mapsBundle = new Bundle();
    private Toolbar toolbar = null;
    private NavigationView navigationView = null;
    public SharedPreferences sharedPref;
    private ProgressDialog progDialog;
    private AlertDialog.Builder alertError;
    public boolean oldDataIntegrity = false;

    public ArrayList<String> getHeroNames() { return heroNames; }
    public ArrayList<String> getHeroClasses() { return heroClasses; }
    public JSONArray getHeroesJson() { return heroesJson; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAnalytics.getInstance(this);

//        heroesBundle.putString("packageName", getPackageName());
        mapsBundle.putString("packageName", getPackageName());

        // AlertDialog for when Internet Unavailable or Download Error
        alertError = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
        alertError.setCancelable(false);
        alertError.setPositiveButton("RETRY", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkForUpdate();
            }
        });
        alertError.setNegativeButton("EXIT", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        //prepare dialog for checking updates
        progDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progDialog.setTitle("Please wait...");
        progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
        progDialog.setCancelable(false);
        progDialog.setMessage("Processing data...");
        progDialog.show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                View v = getCurrentFocus();
                if(v != null) inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        checkForUpdate();
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
            FirebaseCrashlytics.getInstance().log("MainActivity dataIntegrity() == false");
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
        String versionLocal = sharedPref.getString("version", "-1");
        DownloadDataTask task = new DownloadDataTask(this, versionLocal, oldDataIntegrity);
        task.execute();
    }

    private void finishUpdate(){
        String heroesList = sharedPref.getString("heroesList", "-1");
        if(heroesList.equals("-1")){
            FirebaseCrashlytics.getInstance().log("MainActivity finishUpdate() sharedPref empty");
            return;
        }
        try{
            heroesJson = new JSONArray(heroesList);
            heroNames.clear();
            heroClasses.clear();
            for (int i = 0; i < heroesJson.length(); i++) {
                JSONObject hero = heroesJson.getJSONObject(i);
                heroNames.add(hero.getString("name"));
                heroClasses.add(hero.getString("class"));
            }
//            heroesBundle.putString("heroesJson", heroesJson.toString());
//            heroesBundle.putStringArrayList("heroNames", heroNames);
//            heroesBundle.putStringArrayList("heroClasses", heroClasses);
        }catch (JSONException e){
            //TODO: data is corrupt, do something
            FirebaseCrashlytics.getInstance().log("MainActivity finishUpdate() failed");
            e.printStackTrace();
            return;
        }
        // Init fragment
        MainFragment fragment = new MainFragment();
//        fragment.setArguments(heroesBundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment);
        // This is OK fix for now, but if error in resuming activity in future, revisit this
        ft.commitAllowingStateLoss();
        setTitle("Heroes");
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

    private static class DownloadDataTask extends AsyncTask<String, Void, String> {
        private final WeakReference<MainActivity> ref;
        private final boolean oldDataIntegrity;
        private JSONObject versionRemoteJson;
        private String versionRemote;
        private String versionLocal;
        private JSONArray heroesList;
        DownloadDataTask(MainActivity activity, String v, boolean good){
            ref = new WeakReference<>(activity);
            versionLocal = v;
            oldDataIntegrity = good;
        }

        private JSONObject getJson(String url) throws JSONException, IOException{
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
        }
        private JSONObject getRemoteVersion() throws JSONException, IOException{
            return getJson(MainActivity.remoteUrl + "version.json");
        }
        private JSONArray getHeroesList() throws JSONException, IOException{
            JSONObject json = getJson(MainActivity.remoteUrl + "data_min.json");
            return json.getJSONArray("list");
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                versionRemoteJson = getRemoteVersion();
                versionRemote = versionRemoteJson.getString("version");
            } catch (Exception e){
                Log.e("****VERSION", e.toString());
                versionRemote = "noInternet";
            }
            //check if getting the remote version is successful
            if(versionRemote.equals("noInternet")) {
                return versionRemote;
            }
            //check if local data is up to date
            if (versionLocal.equals(versionRemote) && oldDataIntegrity)
                return "doNothing";

            try {
                //get new data
                heroesList = getHeroesList();
                //check new data for error
                if(heroesList==null || !heroesList.getJSONObject(10).has("hpTotal") || !heroesList.getJSONObject(26).has("abilities")){
                    heroesList = null;
                    return "error";
                }
            } catch (Exception e){
                Log.e("***DoInBackground", e.toString());
                heroesList = null;
                return "error";
            }
            return "updated";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            final MainActivity act = ref.get();
            if(act == null || act.isDestroyed() || act.isFinishing()) {
                return;
            }
            act.progDialog.dismiss();
            if(s.equals("noInternet") && !oldDataIntegrity) {
                // show alert dialog because it MUST download data for the first time
                AlertDialog.Builder alert = new AlertDialog.Builder(act, R.style.MyAlertDialogStyle);
                alert.setTitle("No Internet Connection");
                alert.setMessage("You must be connected to internet to download data for the first time.");
                //alert.setMessage(versionRemote);
                alert.setCancelable(false);
                alert.setPositiveButton("RETRY", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        act.checkForUpdate();
                    }
                });
                alert.setNegativeButton("EXIT", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        act.finish();
                    }
                });
                alert.show();
                FirebaseCrashlytics.getInstance().log("MainActivity AsyncTask noInternet");
            } else if (s.equals("updated")){
                Toast.makeText(act, "All data up to date", Toast.LENGTH_SHORT).show();
                //store new version code
                act.saveNewVersionCode(versionRemote);
                act.saveHeroesList(heroesList);
                act.finishUpdate();
                FirebaseCrashlytics.getInstance().log("MainActivity AsyncTask updated");
            } else if(s.equals("error") && !oldDataIntegrity) {
                // show alert dialog because it needs to re-download due to invalid file
                AlertDialog.Builder alert = new AlertDialog.Builder(act, R.style.MyAlertDialogStyle);
                alert.setTitle("Download Failed");
                alert.setMessage("Downloading hero data failed. Make sure you have a stable internet connection and try again.");
                alert.setCancelable(false);
                alert.setPositiveButton("Retry", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        act.checkForUpdate();
                    }
                });
                alert.setNegativeButton("Exit", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        act.finish();
                    }
                });
                alert.show();
                FirebaseCrashlytics.getInstance().log("MainActivity AsyncTask error");
            } else {
                FirebaseCrashlytics.getInstance().log("MainActivity AsyncTask doNothing");
                act.finishUpdate();
            }
//            FirebasePerformance.getInstance().reportFullyDrawn();
//            final Handler handler = new Handler();
//            handler.postDelayed(new Runnable(){
//                @Override
//                public void run() {
//                    progDialog.dismiss();
//                }
//            }, 4000);
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
        } else if (!getOnBackPressedDispatcher().hasEnabledCallbacks() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            // Fix Android Q's platform's memory leak
            finishAfterTransition();
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
//            fragment.setArguments(heroesBundle);
        } else if (id == R.id.nav_maps) {
            fragment = new MapsFragment();
            fragment.setArguments(mapsBundle);
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
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null){ //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, fragment, fragmentTag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    @Override
    protected void onDestroy() {
        // Fix dismiss-dialog-in-AsyncTask-after-activity-finished crash
        progDialog.dismiss();
        super.onDestroy();
    }
}
