package gq.indoormatic.fasttorrent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by indoormatic on 30/12/2016.
 */

public class TorrentsAdapter extends RecyclerView.Adapter<TorrentsAdapter.ViewHolder> {
    // Store a member variable for the torrents
    private List<Torrent> mTorrents;
    // Store the context for easy access
    private Context mContext;

    // Pass in the torrent array into the constructor
    public TorrentsAdapter(Context context, List<Torrent> torrents) {
        mTorrents = torrents;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public TorrentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View torrentView = inflater.inflate(R.layout.item_torrent, parent, false);

        // Return a new holder instance
        return new ViewHolder(torrentView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(TorrentsAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Torrent torrent = mTorrents.get(position);
        viewHolder.itemView.setTag(R.id.movie_url, torrent);
        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(torrent.getName());
        ImageLoader imageLoader = ImageLoader.getInstance();
        ImageView imageView = viewHolder.movieImageView;
        imageLoader.displayImage(torrent.getCoverUrl(), imageView);


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Torrent torrent = (Torrent) v.getTag(R.id.movie_url);
                Log.d("Clicked url", torrent.getUrl());
                new GetInfoTorrents().execute(torrent, mContext);
            }
        });


    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        try {
            return mTorrents.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public ImageView movieImageView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.torrent_name);
            movieImageView = (ImageView) itemView.findViewById(R.id.movie_image);
        }
    }
}
