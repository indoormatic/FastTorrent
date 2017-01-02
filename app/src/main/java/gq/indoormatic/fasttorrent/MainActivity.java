package gq.indoormatic.fasttorrent;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "gq.indoormatic.fasttorrent.MESSAGE";
    private static Context context;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);
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

        // Initialize torrents
        new GetTorrentsJob().execute("http://www.mejortorrent.com/secciones.php?sec=descargas&ap=peliculas_hd&p=", rvTorrents, MainActivity.this);
        DownloadFileFromURL.verifyStoragePermissions(this);


    }


}
