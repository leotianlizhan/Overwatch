package something.overwatch;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class PatchNotesFragment extends Fragment {

    public static final String URL_PATCH_NOTES = "https://api.lootbox.eu/patch_notes";
    public static final int INDEX_PATCH_NOTES = 0;
    public static final int TIMEOUT = 2000;
    public static final String COLOR = "#FFFFFF";

    private ProgressBar bar;
    private TextView lblFail;
    private WebView webview;

    /**
     * Memory leak fix. TODO: use view binding in Kotlin
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bar = null;
        lblFail = null;
        webview = null;
    }

    public PatchNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patch_notes, container, false);
        bar = (ProgressBar)v.findViewById(R.id.bar_patch_notes);
        lblFail = (TextView)v.findViewById(R.id.lbl_fail_patch_notes);
        webview = (WebView)v.findViewById(R.id.webview);
        bar.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.text_secondary), PorterDuff.Mode.SRC_IN);
        webview.getSettings().setDefaultFontSize(13);
        webview.getSettings().setJavaScriptEnabled(false);
        webview.setBackgroundColor(Color.TRANSPARENT);
        RequestTask task = new RequestTask(bar, lblFail, webview);
        task.execute();
        return v;
    }

    private static class RequestTask extends AsyncTask<String, Void, String>{
        private final WeakReference<ProgressBar> barRef;
        private final WeakReference<TextView> lblFailRef;
        private final WeakReference<WebView> webviewRef;

        RequestTask(ProgressBar sp, TextView fail,WebView web){
            barRef = new WeakReference<>(sp);
            lblFailRef = new WeakReference<>(fail);
            webviewRef = new WeakReference<>(web);
        }

        @Override
        protected String doInBackground(String... params) {
            //String urlString = "https://api.lootbox.eu/patch_notes";
            //I found this link in LootBox.eu's Gitlab
            //https://gitlab.com/SingularityIO/LootBoxAPI/blob/master/routes/patch_notes.js
//            String urlString = "https://cache-eu.battle.net/system/cms/oauth/api/patchnote/list?program=pro&region=US&locale=enUS&type=RETAIL&page=1&pageSize=5&orderBy=buildNumber&buildNumberMin=0";
            String urlString = "https://playoverwatch.com/en-us/news/patch-notes/live/";
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
                if(result.equals("")) return "";

                StringBuilder htmlString = new StringBuilder();

                if (result.contains("<div class=\"PatchNotes-body\">") && result.contains("<div class=\"PatchNotesTop\">")){
                    String htmlHead = result.substring(result.indexOf("<head"), result.indexOf("</head>"));

                    result = result.substring(result.indexOf("<div class=\"PatchNotes-body\">"), result.indexOf("<div class=\"PatchNotesTop\">"));

                    htmlString.append("<html>")
                            .append(htmlHead)
                            .append("<style type=\"text/css\">")
                            .append("body{color: #b7b9bc !important; background-color: #1a1a1a !important;}")
                            .append("body h1{color: #b7b9bc !important;}")
                            .append("body h2{color: #c98318 !important;}")
                            .append("body h4{color: #c98318 !important;}")
                            .append("body strong{color: #0f97c9 !important;}")
                            .append("</style></head>")
                            .append("<body link=\"orange\">")
                            .append(result)
                            .append("</body></html>");
                }

                return htmlString.toString();
            } catch (Exception e){
                e.printStackTrace();
                result = "";
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            final ProgressBar bar = barRef.get();
            final TextView lblFail = lblFailRef.get();
            final WebView webview = webviewRef.get();
            if(bar == null || lblFail == null || webview == null) return;

            if(!s.equals("")){
                String encodedStr = Base64.encodeToString(s.getBytes(), Base64.NO_PADDING);
                webview.loadData(encodedStr, "text/html", "base64");
                bar.setVisibility(View.GONE);
            }else{
                bar.setVisibility(View.GONE);
                lblFail.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity act = getActivity();
        if(act != null){
            FirebaseAnalytics mFirebase = FirebaseAnalytics.getInstance(act);
            mFirebase.setCurrentScreen(act, this.getClass().getSimpleName(), this.getClass().getSimpleName());
        }
    }
}
