package gq.indoormatic.fasttorrent;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Ciborg on 04/01/2017.
 */

public class SearchAdapter extends ArrayAdapter<Torrent> {

    public SearchAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public SearchAdapter(Context context, int resource, List<Torrent> items) {
        super(context, resource, items);
    }

 /*   @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.itemlistrow, null);
        }

        Item p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.id);
            TextView tt2 = (TextView) v.findViewById(R.id.categoryId);
            TextView tt3 = (TextView) v.findViewById(R.id.description);

            if (tt1 != null) {
                tt1.setText(p.getId());
            }

            if (tt2 != null) {
                tt2.setText(p.getCategory().getId());
            }

            if (tt3 != null) {
                tt3.setText(p.getDescription());
            }
        }

        return v;
    }*/

}
