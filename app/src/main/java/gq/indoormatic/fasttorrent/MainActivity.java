package gq.indoormatic.fasttorrent;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity {
    public final static String QUERY = "gq.indoormatic.fasttorrent.QUERY";
    public final static String RESULTS = "gq.indoormatic.fasttorrent.RESULTS";
    public static LinearLayoutManager mLinearLayoutManager;
    private static Context context;
    private RecyclerView mRecyclerView;

    public static LinearLayoutManager getmLinearLayoutManager() {
        return mLinearLayoutManager;
    }

    public static void setmLinearLayoutManager(LinearLayoutManager mLinearLayoutManager) {
        MainActivity.mLinearLayoutManager = mLinearLayoutManager;
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Intent searchIntent = new Intent(getApplicationContext(), SearchResultsActivity.class);
            searchIntent.putExtra(QUERY, query);
            MainActivity.this.startActivity(searchIntent);
            finish();
        } else {
            Log.d("INFO", "Creada página");
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .defaultDisplayImageOptions(defaultOptions)
                    .build();
            ImageLoader.getInstance().init(config);

            // Lookup the recyclerview in activity layout
            RecyclerView rvTorrents = (RecyclerView) findViewById(R.id.rvTorrents);

            RecyclerView.ItemDecoration itemDecoration = new
                    DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            rvTorrents.addItemDecoration(itemDecoration);
            // Retain an instance so that you can call `resetState()` for fresh searches

            // Initialize torrents
            if (!RESULTS.equals(intent.getAction())) {
                new GetTorrentsJob().execute("http://www.mejortorrent.com/secciones.php?sec=descargas&ap=peliculas_hd&p=", rvTorrents, MainActivity.this);
                DownloadFileFromURL.verifyStoragePermissions(this);
            }

        }

    }

    public void setScroller(RecyclerView rvTorrents) {
        Log.d("Scroller", "Setting up scroller");
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d("Scroller", "Loading more data?");
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTorrents.addOnScrollListener(scrollListener);
    }

    private void loadNextDataFromApi(int page) {
        Log.d("Scroll", "Cogiendo página:" + page);
        RecyclerView rvTorrents = (RecyclerView) findViewById(R.id.rvTorrents);
        new AddTorrentsJob().execute("http://www.mejortorrent.com/secciones.php?sec=descargas&ap=peliculas_hd&p=", rvTorrents, this, page);
    }


}
