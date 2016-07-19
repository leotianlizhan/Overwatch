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
import java.net.URL;
import java.nio.charset.Charset;


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
        try {
            JSONObject json = readJsonFromUrl(URL_PATCH_NOTES);
            patchNotes.setText(json.getString("detail"));
        }catch (Exception e){
            Toast.makeText(getContext(), "Failed to obtain patch notes", Toast.LENGTH_LONG).show();
        }

        return v;
    }

    public JSONObject readJsonFromUrl(String urlString) throws IOException, JSONException {
        URL url = new URL(urlString);
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) url.openConnection();
            c.connect();
            BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            //get the latest patch notes
            JSONArray jArray = json.getJSONArray("patchNotes");
            c.disconnect();
            return jArray.getJSONObject(INDEX_PATCH_NOTES);
        } finally {

        }
    }

    private String readAll(Reader rd) throws IOException{
        StringBuilder sb = new StringBuilder();
        int cp;
        while((cp = rd.read())!=-1){
            sb.append((char)cp);
        }
        return sb.toString();
    }

}
