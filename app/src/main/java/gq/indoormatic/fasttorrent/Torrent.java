package gq.indoormatic.fasttorrent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Created by Ciborg on 30/12/2016.
 */

public class Torrent {

    private String name;
    private String qualityFormat;
    private String url;
    private String coverUrl;
    private String description;

    public Torrent(String name, String qualityFormat, String coverUrl, String infoUrl) {
        this.name = name;
        this.qualityFormat = qualityFormat;
        this.coverUrl = coverUrl;
        url = infoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualityFormat() {
        return qualityFormat;
    }

    public void setQualityFormat(String qualityFormat) {
        this.qualityFormat = qualityFormat;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

class GetTorrentsJob extends AsyncTask<Object, Void, ArrayList<Torrent>> {
    public static Activity mActivity;
    private RecyclerView rvTorrents;

    public static Activity getActivityContext() {
        return mActivity;
    }

    @Override
    protected ArrayList<Torrent> doInBackground(Object[] params) {
        mActivity = (Activity) params[2];
        ArrayList<Torrent> list = new ArrayList<Torrent>();
        try {
            for (int j = 1; j < 5; j++) {
                rvTorrents = (RecyclerView) params[1];
                Document doc = Jsoup.connect(params[0].toString() + j).get();
                Elements movies = doc.select("div[align='justify']");
                for (Element movie : movies) {
                    Elements links = movie.select("a");
                    Elements quality = movie.select("b");
                    for (int i = 0; i < 3; i++) {
                        if (quality.get(i).text().equals("(MicroHD-1080p)")) {
                            Document image = Jsoup.parseBodyFragment(links.get(i).html());
                            Elements imageHtml = image.getElementsByTag("img");
                            list.add(new Torrent(links.get(i + 3).text(),
                                    "(MicroHD-1080p)",
                                    "http://www.mejortorrent.com" + imageHtml.attr("src"),
                                    "http://www.mejortorrent.com" + links.get(i).attr("href")));
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.d("JSOUP", "Couldn't GET the URL");
            return null;
        }
        return list;
    }

    @Override
    protected void onPostExecute(ArrayList<Torrent> list) {
        // Create adapter passing in the sample user data
        TorrentsAdapter adapter = new TorrentsAdapter(MainActivity.getAppContext(), list);
        // Attach the adapter to the recyclerview to populate items
        rvTorrents.setAdapter(adapter);
        // Set layout manager to position the items
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvTorrents.setLayoutManager(gridLayoutManager);
        rvTorrents.setItemAnimator(new SlideInUpAnimator());


    }

}


class GetInfoTorrents extends AsyncTask<Object, Void, Torrent> {
    private RecyclerView rvTorrents;
    private Context mContext;

    @Override
    protected Torrent doInBackground(Object[] params) {
        Torrent torrent = (Torrent) params[0];
        mContext = (Context) params[1];
        try {
            Document doc = Jsoup.connect(torrent.getUrl()).get();
            if (torrent.getDescription() == null) {
                Elements description = doc.select("div[align='justify']");
                torrent.setDescription(description.get(0).text());
            }
            Elements downloadSearcher = doc.select("a:contains(Descargar)");
            String url = "http://www.mejortorrent.com/" + downloadSearcher.get(0).attr("href");
            doc = Jsoup.connect(url).get();
            downloadSearcher = doc.select("a:contains(aquí)");
            url = "http://www.mejortorrent.com" + downloadSearcher.get(0).attr("href");
            torrent.setUrl(url);
            Log.d("Torrent url", url);

        } catch (Exception e) {
            Log.d("Torrent info", "Couldn't get the torrent page: " + e);
        }
        return torrent;
    }

    @Override
    protected void onPostExecute(final Torrent torrent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GetTorrentsJob.getActivityContext(), R.style.MyAlertDialogStyle);

        builder.setTitle(torrent.getName());
        builder.setMessage(torrent.getDescription());

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                new DownloadFileFromURL().execute(torrent.getUrl(), mContext);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();


    }

}

/**
 * Background Async Task to download file
 */
class DownloadFileFromURL extends AsyncTask<Object, String, String> {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * Before starting background thread Show Progress Bar Dialog
     *
     * @Override protected void onPreExecute() {
     * super.onPreExecute();
     * showDialog(progress_bar_type);
     * }
     */
    private Context mContext;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission

        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 10);

        }
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(Object[] params) {
        int count;
        mContext = (Context) params[1];
        try {
            URL url = new URL((String) params[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(Environment
                    .getExternalStorageDirectory().toString()
                    + "/Movie.torrent");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        String localUri = Environment
                .getExternalStorageDirectory().toString()
                + "/Movie.torrent";
        if (localUri.substring(0, 7).matches("file://")) {
            localUri = localUri.substring(7);
        }
        return localUri;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        // pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);
        // Do nothing but close the dialog
        File file = new File(file_url);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null)
            type = "*/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setDataAndType(data, type);

        MainActivity.getAppContext().startActivity(intent);

    }

}