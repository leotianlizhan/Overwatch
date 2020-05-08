package something.overwatch;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.NavUtils;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class HeroInfoActivity extends AppCompatActivity {

    private TabLayout tabLayout = null;
    private ViewPager viewPager = null;
    private JSONObject heroJson = null;
    boolean isMobileData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hero_info);
        //some toolbar stuff
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_hero);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get position
        try {
            heroJson = new JSONObject(getIntent().getStringExtra("json"));
        } catch (JSONException e) {
            Crashlytics.log(3, "HeroInfoActivity", "new JSONObject(getIntent()) failed");
        } finally {
            if (heroJson == null) return;
        }

        LoadHeroTask mTask = new LoadHeroTask(this, heroJson, getPackageName());
        mTask.execute();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OverviewFragment(), "OVERVIEW");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class LoadHeroTask extends AsyncTask<Void, Integer, Boolean>{
        private JSONObject hero;
        private String hero_name;
        private String hero_class;
        private String hp_total;
        private String hp_normal;
        private String hp_armor;
        private String hp_shield;
        private Ability[] abilities;
        private Uri uri;
        ConnectivityManager cm;
        private boolean isMobileData;
        private final String PACKAGE_NAME;

        private final WeakReference<HeroInfoActivity> activityReference;
        int pos;

        LoadHeroTask(HeroInfoActivity context, JSONObject hero, String pName){
            activityReference = new WeakReference<>(context);
            this.hero = hero;
            cm = (ConnectivityManager)activityReference.get().getSystemService(HeroInfoActivity.CONNECTIVITY_SERVICE);
            PACKAGE_NAME = pName;
        }

        @Override
        protected void onPreExecute() {
            HeroInfoActivity activity = activityReference.get();
            if(activity == null || activity.isFinishing()) return;
        }

        private void getAbilityInfo(JSONObject hero) throws JSONException
        {
            JSONArray abilitiesJson = hero.getJSONArray("abilities");
            int len = abilitiesJson.length();
            abilities = new Ability[len];
            for(int i=0; i<len; i++){
                JSONObject ability = abilitiesJson.getJSONObject(i);
                if(ability.has("iconUrl") && !isMobileData)
                    abilities[i] = new Ability(activityReference.get(), ability.getString("name"), ability.getString("key"), ability.getString("description"), ability.getString("iconUrl"));
                else
                    abilities[i] = new Ability(activityReference.get(), ability.getString("name"), ability.getString("key"), ability.getString("description"));
                JSONArray statsArray = ability.getJSONArray("stats");
                for(int j=0; j<statsArray.length(); j++){
                    JSONObject stat = statsArray.getJSONObject(j);
                    abilities[i].addStat(new AbilityStat(activityReference.get(), stat.getString("name"), stat.getString("value")));
                }
                //TODO: do progress updates
                //publishProgress(i);
            }
            return;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //determine network state
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isMobileData = activeNetwork == null || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

            try{
                hero_name = hero.getString("name");
                hero_class = hero.getString("class");
                hp_total = hero.getString("hpTotal");
                hp_normal = hero.getString("hpNormal");
                hp_armor = hero.getString("hpArmor");
                hp_shield = hero.getString("hpShield");
                getAbilityInfo(hero);
            } catch (JSONException e){
                Crashlytics.log(3, "HeroInfoActivity", "AsyncTask doInBackground accessing JSON props failed");
                e.printStackTrace();
                return false;
            }

            String hero_pic_id = hero_name.toLowerCase().replace(".", "").replace(" ", "");
            HeroInfoActivity activity = activityReference.get();
            if(activity == null || activity.isFinishing()) return false;
            int resId = activity.getResources().getIdentifier("pic_" + hero_pic_id, "drawable", PACKAGE_NAME);
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(resId))
                    .build();

            return abilities != null;
        }

//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            int index = values[0];
//            HeroInfoActivity activity = activityReference.get();
//            if(activity == null || activity.isFinishing()) return;
//
//            LinearLayout abilitySection = (LinearLayout)activity.findViewById(R.id.ability_section);
//            abilitySection.addView(abilities[index]);
//
//            activity = null;
//        }

        @Override
        protected void onPostExecute(Boolean succ) {
            HeroInfoActivity activity = activityReference.get();
            if(activity == null || activity.isFinishing()) return;
            if(!succ){
                TextView lblHeroName = (TextView)activity.findViewById(R.id.lbl_hero_name);
                lblHeroName.setText("ERROR");
                return;
            }

            //****************hero name, class, and icon****************/
            TextView lblHeroName = (TextView)activity.findViewById(R.id.lbl_hero_name);
            TextView lblHeroClass = (TextView)activity.findViewById(R.id.lbl_hero_role);
            SimpleDraweeView pic = (SimpleDraweeView)activity.findViewById(R.id.pic_hero_info);
            lblHeroName.setText(hero_name);
            lblHeroClass.setText(hero_class);
            pic.setImageURI(uri);

            //****************set hero hp****************/
            TextView hpTotal = (TextView) activity.findViewById(R.id.lbl_hp_total_value);
            TextView hpNormal = (TextView) activity.findViewById(R.id.lbl_hp_normal_value);
            TextView hpArmor = (TextView) activity.findViewById(R.id.lbl_hp_armor_value);
            TextView hpShield = (TextView) activity.findViewById(R.id.lbl_hp_shield_value);
            hpTotal.setText(hp_total);
            hpNormal.setText(hp_normal);
            hpArmor.setText(hp_armor);
            hpShield.setText(hp_shield);

            //****************abilities stats****************/
            LinearLayout abilitySection = (LinearLayout)activity.findViewById(R.id.ability_section);
            abilitySection.setOrientation(LinearLayout.VERTICAL);
            for(Ability a : abilities) abilitySection.addView(a);

            activity = null;
        }
    }
}
