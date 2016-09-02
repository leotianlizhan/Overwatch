package something.overwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String PACKAGE_NAME;
    public static List<String> heroNames = Arrays.asList("Genji", "Mccree", "Pharah", "Reaper", "Soldier 76", "Tracer", "Bastion", "Hanzo", "Junkrat", "Mei", "Torbjorn", "Widowmaker", "D.va", "Reinhardt",
            "Roadhog", "Winston", "Zarya", "Ana", "Lucio", "Mercy", "Symmetra", "Zenyatta");
    public static List<String> heroClasses = Arrays.asList("Offense", "Offense", "Offense", "Offense", "Offense", "Offense", "Defense", "Defense", "Defense", "Defense", "Defense", "Defense", "Tank", "Tank",
            "Tank", "Tank", "Tank", "Support", "Support", "Support", "Support", "Support");
    public static List<String> mapNames = Arrays.asList("Dorado", "Hanamura", "Hollywood", "No Ilios Map Yet", "King's Row", "Lijiang Tower", "Nepal", "Numbani", "Route 66", "Temple of Anubis", "Volskaya Industries", "Watchpoint: Gibraltar");
    Toolbar toolbar = null;
    NavigationView navigationView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PACKAGE_NAME = getPackageName();

        //Set the fragment initially
        MainFragment fragment = new MainFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        setTitle("Heroes");

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

//        AppRate.with(this).setInstallDays(3).monitor();
//        AppRate.showRateDialogIfMeetsConditions(this);
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
        if (id == R.id.action_settings) {
            return true;
        }

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
        } else if (id == R.id.nav_maps) {
            MapsFragment fragment = new MapsFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Maps");
        } else if (id == R.id.nav_patchnotes) {
            PatchNotesFragment fragment = new PatchNotesFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Patch Notes");
        } else if (id == R.id.nav_players) {
            TemporaryFragment fragment = new TemporaryFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Players");
        } else if (id == R.id.nav_about) {
            AboutFragment fragment = new AboutFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("About");
        } else if (id == R.id.nav_send) {
            TemporaryFragment fragment = new TemporaryFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle("Overwatch");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
