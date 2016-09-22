package something.overwatch;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.PopupMenuCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

    private String region = "US";
    private MenuItem menuItem = null;
    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_player, container, false);
        final FloatingSearchView searchView = (FloatingSearchView) v.findViewById(R.id.searchBar);
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                Intent intent = new Intent(getContext(), InfoPlayerActivity.class);
                intent.putExtra("query", currentQuery);
                intent.putExtra("region", region);
                getContext().startActivity(intent);
            }
        });

        final CharSequence[] regions = {"US","EU","KR","CN","XBL","PSN"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Region");
        builder.setItems(regions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                searchView.setSearchHint("Search " + regions[which] + " player...");
                region=regions[which].toString();
            }
        });
        final AlertDialog selectRegions = builder.create();

        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                selectRegions.show();
            }
        });

        return v;
    }
}
