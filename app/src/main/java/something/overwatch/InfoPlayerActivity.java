package something.overwatch;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class InfoPlayerActivity extends AppCompatActivity {

    private ProgressDialog progDailog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_player);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_player);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String query = getIntent().getStringExtra("query");
        String region = getIntent().getStringExtra("region");

        progDailog = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDailog.setCancelable(false);

        String currentUrl;
        if(region.equals("XBL")||region.equals("PSN")){
            currentUrl = "https://playoverwatch.com/en-us/career/" + region.toLowerCase() + "/" + query.replace("#", "-").replace(" ", "%20");
        }else{
            currentUrl = "https://playoverwatch.com/en-us/career/pc/" + region.toLowerCase() + "/" + query.replace("#", "-").replace(" ", "");
        }

        WebView webView = (WebView)findViewById(R.id.webview_player);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                progDailog.show();
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByClassName('navbars')[0].style.display = 'none'; " +
                        "document.getElementById('footer').style.display = 'none'; " +
                        "document.getElementsByClassName('bootstrap-footer')[0].style.display = 'none'; " +
                        "document.getElementById('profile-platforms').style.display = 'none'; " +
                        "})()");
                view.setVisibility(View.VISIBLE);
                progDailog.dismiss();
            }
        });

        webView.loadUrl(currentUrl);

    }
}
