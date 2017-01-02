package gq.indoormatic.fasttorrent;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ciborg on 02/01/2017.
 */

public class SearchResultsActivity extends ListActivity {
    private static Context context;
    private static List<Torrent> torrents;

    public static List<Torrent> getTorrents() {
        return torrents;
    }

    public static void addTorrent(Torrent torrent) {
        torrents.add(torrent);
    }

    public static void initializeTorrents() {
        torrents = new LinkedList<Torrent>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        handleIntent(getIntent());
        torrents = new LinkedList<Torrent>();
        super.onCreate(savedInstanceState);
        //SearchResultsActivity.context = this.getApplicationContext();
        setContentView(R.layout.search);
        // Lookup the recyclerview in activity layout
        //RecyclerView rvTorrents = (RecyclerView) findViewById(R.id.rvTorrents);
        // Initialize torrents

    }

    @Override
    public void onListItemClick(ListView l,
                                View v, int position, long id) {
        Torrent torrent = torrents.get(position);
        Log.d("Clicked url", torrent.getUrl());
        new GetInfoTorrents().execute(torrent, context, SearchResultsActivity.this);

    }

    private void handleIntent(Intent intent) {
        String query = intent.getStringExtra(MainActivity.QUERY);
        new SearchTorrentsJob().execute(this, query);
    }

    private void doSearch(String queryStr) {
        // get a Cursor, prepare the ListAdapter
        // and set it
    }

}

class SearchTorrentsJob extends AsyncTask<Object, Void, ArrayList<Torrent>> {
    public static ListActivity mActivity;
    private RecyclerView rvTorrents;


    @Override
    protected ArrayList<Torrent> doInBackground(Object[] params) {
        mActivity = (ListActivity) params[0];
        String query = (String) params[1];
        ArrayList<Torrent> torrents = new ArrayList<Torrent>();
        SearchResultsActivity.initializeTorrents();
        try {
            Log.d("SRA", "Query: " + query);
            String queryURL = "http://www.mejortorrent.com/secciones.php?sec=buscador&valor=" + URLEncoder.encode(query, "UTF-8");
            Log.d("SRA", "Search query URL: " + queryURL);
            Document doc = Jsoup.connect(queryURL).get();
            Log.d("SRA", "Got the document.");
            Elements movies = doc.select("tr[height='22']");
            for (Element movie : movies) {
                Elements quality = movie.select("span[style='color:gray;']");
                if (quality.size() != 0 && quality.get(0).text().contains("MicroHD")) {
                    Elements link = movie.select("a");
                    if (link.size() != 0) {
                        Torrent torrent = new Torrent(link.get(0).text(), "MicroHD-1080p", "http://www.mejortorrent.com" + link.attr("href"));
                        torrents.add(torrent);
                        SearchResultsActivity.addTorrent(torrent);
                    }
                }
            }

        } catch (Exception e) {
            Log.d("SRA-JSOUP", "Couldn't GET the URL: " + e.getMessage());
            return null;
        }
        return torrents;
    }

    @Override
    protected void onPostExecute(ArrayList<Torrent> list) {
        ListView listView = mActivity.getListView();
        List<String> torrents = new LinkedList<String>();
        if (list != null) {
            for (Torrent torrent : list) {
                torrents.add(torrent.getName());
            }
        }
        mActivity.setListAdapter(new ArrayAdapter<String>(mActivity, R.layout.item_torrent, R.id.torrent_name, torrents));


    }

}