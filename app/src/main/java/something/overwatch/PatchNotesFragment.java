package something.overwatch;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class PatchNotesFragment extends Fragment {

    public static final String URL_PATCH_NOTES = "https://api.lootbox.eu/patch_notes";
    public static final int INDEX_PATCH_NOTES = 0;
    public static final int TIMEOUT = 2000;

    private TextView patchNotes;

    public PatchNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patch_notes, container, false);

        patchNotes = (TextView)v.findViewById(R.id.lbl_patch_notes);
        //patchNotes.setText(readJsonFromUrl(URL_PATCH_NOTES));
        return v;
    }

//    public String readJsonFromUrl(String urlString){
//        URL url;
//
//        BufferedReader br;
//        StringBuilder sb = new StringBuilder();
//
//        try {
//            URLConnection c = new URL(urlString).openConnection();
//            br = new BufferedReader(new InputStreamReader(is));
//            HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
//            c.connect();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()));
//            int cp;
//            while((cp = rd.read())!=-1){
//                sb.append((char)cp);
//            }
//            return sb.toString();
//
//
//        } catch (MalformedURLException m){
//            Toast.makeText(getContext(), "MalformedURLException", Toast.LENGTH_LONG).show();
//        } catch (IOException e){
//            Toast.makeText(getContext(), "IOException in first try", Toast.LENGTH_LONG).show();
//        } finally {
//            try{
//                if(is!=null) is.close();
//            } catch (IOException ioe){
//                Toast.makeText(getContext(), "IOException", Toast.LENGTH_LONG).show();
//            }
//        }
//        return "";
//    }

}
