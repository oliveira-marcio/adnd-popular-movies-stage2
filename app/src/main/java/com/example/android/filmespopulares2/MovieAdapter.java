package com.example.android.filmespopulares2;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.android.filmespopulares2.data.MovieContract;
import com.example.android.filmespopulares2.utilities.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    final private MovieAdapterOnClickHandler mOnClickListener;

    private Cursor mCursor;
    private final Context mContext;
    private int lastPosition = -1;


    public interface MovieAdapterOnClickHandler {
        void onClick(long movieId, View v);
    }

    public MovieAdapter(@NonNull Context context, MovieAdapterOnClickHandler listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        view.setFocusable(true);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int idColumnIndex = mCursor.getColumnIndex(MovieContract.MoviesEntry._ID);
        int posterColumnIndex = mCursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_POSTER);

        String posterPath = mCursor.getString(posterColumnIndex);
        String transitionName = mCursor.getString(idColumnIndex);

        String posterUrl = NetworkUtils.buildPosterUrl(
                posterPath,
                mContext.getString(R.string.thumb_size)).toString();

        ViewCompat.setTransitionName(holder.moviePoster, transitionName);

        final Activity activity = (Activity) mContext;

        // Picasso invocado com alguns callbacks para dar continuidade na execução do
        // Shared Element Transition
        Picasso.with(mContext)
                .load(posterUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.moviePoster, new Callback() {
                    @Override
                    public void onSuccess() {
                        ActivityCompat.startPostponedEnterTransition(activity);
                    }

                    @Override
                    public void onError() {
                        ActivityCompat.startPostponedEnterTransition(activity);
                    }
                });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView moviePoster;

        MovieViewHolder(View itemView) {
            super(itemView);
            final int imageWidth = Integer.parseInt(mContext.getString(R.string.thumb_size));
            final double IMAGE_RATIO = Double.parseDouble((mContext.getString(R.string.image_ratio)));

            moviePoster = (ImageView) itemView.findViewById(R.id.image_poster);
            moviePoster.getLayoutParams().width = imageWidth;
            moviePoster.getLayoutParams().height = (int) Math.round(imageWidth * IMAGE_RATIO);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int idColumnIndex = mCursor.getColumnIndex(MovieContract.MoviesEntry._ID);
            long movieId = mCursor.getLong(idColumnIndex);
            mOnClickListener.onClick(movieId, moviePoster);
        }
    }
}
