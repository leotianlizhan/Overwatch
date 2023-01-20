package something.overwatch;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoPlayerActivity extends AppCompatActivity {

    private ProgressDialog progDialog;
    private ArrayList<String> favorites = null;
    private String query = "";
    private String region = "";
    private Boolean isFavorited = false;
    private int y = 0;
    private WebView webView;
    private MenuItem favButton;
    private static final Pattern p = Pattern.compile("^https://overwatch\\.blizzard\\.com/(?:.+/)?career/(.+)/$");
    private Matcher m = p.matcher("");
    String currentUrl = "https://overwatch.blizzard.com/search/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_player);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_player);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            TypedValue tv = new TypedValue();
            if(getTheme().resolveAttribute(android.R.attr.actionBarSize,tv,true))
                y = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        handleIntent(getIntent());

        //get stuff from intent
        query = getIntent().getStringExtra(SearchManager.QUERY).trim();
        region = getIntent().getStringExtra("region");
        favorites = getIntent().getStringArrayListExtra("favoriteslist");
        isFavorited = favorites != null && favorites.contains(query);
        //progDialog = new ProgressDialog(this);
        //progDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //progDialog.show();
        //progDailog.setCancelable(false);

        // PSN allows alphanumeric, hyphen, and underscore
        // Xbox allows alphanumeric and white space
        // Battlenet allows alphanumeric, foreign words, but NO symbols like hyphen
        // Blizzard's search is bugged for "-" currently. revert this change after they fix it.
        if (isFavorited) {
            currentUrl = "https://overwatch.blizzard.com/career/" + query;
        } else if (!query.isEmpty()) {
            currentUrl = "https://overwatch.blizzard.com/search?q=" + query;
        }

        webView = (WebView)findViewById(R.id.webview_player);
        webView.setBackgroundColor(Color.TRANSPARENT);
        // required to make blizzard's website work
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.INVISIBLE)
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            private boolean isRedirected = false;

            private void removeFooter(WebView view) {
                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByTagName('blz-nav')[0].style.display = 'none'; " +
                        "document.getElementsByTagName('blz-social-section')[0].style.display = 'none'; " +
                        //"document.getElementsByClassName('bootstrap-footer')[0].style.display = 'none'; " +
                        "document.getElementById('Page-footer').style.display = 'none'; " +
                        "})()");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                //check if this is loading the redirected url
                m.reset(url);
                if (m.matches()){
                    //provide some feedback to make sure the user know it's not frozen
                    Toast t = Toast.makeText(getApplicationContext(), "Player found. Redirecting...", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER,0,0);
                    t.show();

                    isFavorited = favorites != null && favorites.contains(m.group(1));
                    favButton.setIcon(getFavoriteIcon(isFavorited));
                    favButton.setVisible(true);
                } else {
                    favButton.setVisible(false);
                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                //deletes top blizzard bar
                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByTagName('blz-nav')[0].style.display = 'none'; " +
                        "})()");
                view.requestFocus();
                super.onPageCommitVisible(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                m.reset(url);
//                if (!m.matches()) {
//                    view.loadUrl("javascript:(function() { " +
//                            "document.getElementsByClassName('search-input')[0].value = '" + query + "';" +
//                            "document.getElementsByClassName('search-button')[0].click();" +
//                            "})()");
//                }
                // deletes bottom blizzard bar
                removeFooter(view);
                m.reset(url);
                if (!m.matches()) {
                    if (query.contains(" ")) {
                        Toast t = Toast.makeText(getApplicationContext(), "For names containing space, please enter manually here", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }
                    view.requestFocus();
                    view.loadUrl("javascript:document.getElementsByName('q')[0].focus();");
                }
                // add this line if u want to hide platform buttons
                //"document.getElementById('profile-platforms').style.display = 'none'; " +
                //view.setVisibility(View.VISIBLE);
                //if(progDialog.isShowing()) progDialog.dismiss();
            }
        });
        // webView.loadUrl(currentUrl);
    }

    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player_favorite, menu);
        favButton = menu.findItem(R.id.action_favorite);
        // must be put after this because the webView will use favButton
        webView.loadUrl(currentUrl);
        favButton.setIcon(getFavoriteIcon(isFavorited));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_favorite) {
            item.setIcon(getFavoriteIcon(favorite(query, region)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (webView.copyBackForwardList().getCurrentIndex() > 0) {
            webView.goBack();
        } else {
            // Your exit alert code, or alternatively line below to finish
            super.onBackPressed(); // finishes activity
        }
    }

    Toast favoriteToast;
    private void favoriteToast(String text) {
        if(favoriteToast != null) {
            favoriteToast.cancel();
        }
        favoriteToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        favoriteToast.setGravity(Gravity.TOP|Gravity.END,0,y);
        favoriteToast.show();
    }

    public void saveInfo(String v){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        StringBuilder result = new StringBuilder();
        if (favorites == null)
            favorites = new ArrayList<>();
        favorites.add(v);
        for(int i=0; i<favorites.size(); i++){
            result.append(",").append(favorites.get(i));
        }
        result = new StringBuilder(result.substring(1));
        editor.putString("favorites", result.toString());
        editor.commit();
        favoriteToast("Favorited");
    }
    public void removeInfo(String v){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        StringBuilder result = new StringBuilder();
        favorites.remove(v);
        for(int i=0; i<favorites.size(); i++){
            result.append(",").append(favorites.get(i));
        }
        if(!result.toString().equals(""))
            result = new StringBuilder(result.substring(1));
        editor.putString("favorites", result.toString());
        editor.commit();
        favoriteToast("Unfavorited");
    }

    // Favorites/Unfavorite current player the current player
    private boolean favorite(String query, String region){
        m.reset(webView.getUrl());
        if (!m.matches()) {
            favoriteToast("Favorite failed");
            return isFavorited;
        }
        String v = m.group(1);
        if(isFavorited)
            removeInfo(v);
        else
            saveInfo(v);
        isFavorited = !isFavorited;
        return isFavorited;
    }

    private Drawable getFavoriteIcon(boolean isFavorited) {
        return isFavorited ? getResources().getDrawable(R.drawable.ic_star_on_86dp) : getResources().getDrawable(R.drawable.ic_star_off_86dp);
    }
}
