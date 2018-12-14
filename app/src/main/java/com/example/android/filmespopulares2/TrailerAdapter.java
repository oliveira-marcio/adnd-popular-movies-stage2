package com.example.android.filmespopulares2;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.filmespopulares2.data.MovieContract;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    final private TrailerAdapterOnClickHandler mOnClickListener;

    private Cursor mCursor;
    private Context mContext;

    public interface TrailerAdapterOnClickHandler {
        void onTrailerClick(String trailerKey);
    }

    public TrailerAdapter(@NonNull Context context, TrailerAdapterOnClickHandler listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailer_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int nameColumnIndex = mCursor.getColumnIndex(MovieContract.TrailersEntry.COLUMN_NAME);

        String nameTrailer = mCursor.getString(nameColumnIndex);

        holder.nameTextView.setText(nameTrailer);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView nameTextView;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int keyColumnIndex = mCursor.getColumnIndex(MovieContract.TrailersEntry.COLUMN_KEY);
            String trailerKey = mCursor.getString(keyColumnIndex);
            mOnClickListener.onTrailerClick(trailerKey);
        }
    }
}
