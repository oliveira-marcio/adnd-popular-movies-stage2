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

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    final private ReviewAdapterOnClickHandler mOnClickListener;

    private Cursor mCursor;
    private Context mContext;

    public interface ReviewAdapterOnClickHandler {
        void onReviewClick(String content, String author);
    }

    public ReviewAdapter(@NonNull Context context, ReviewAdapterOnClickHandler listener) {
        mContext = context;
        mOnClickListener = listener;
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int contentColumnIndex = mCursor.getColumnIndex(MovieContract.ReviewsEntry.COLUMN_CONTENT);
        int authorColumnIndex = mCursor.getColumnIndex(MovieContract.ReviewsEntry.COLUMN_AUTHOR);

        String content = mCursor.getString(contentColumnIndex);
        String author = "(" + mCursor.getString(authorColumnIndex) + ")";

        holder.contentTextView.setText(content);
        holder.authorTextView.setText(author);
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView contentTextView;
        public TextView authorTextView;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            contentTextView = (TextView) itemView.findViewById(R.id.tv_content);
            authorTextView = (TextView) itemView.findViewById(R.id.tv_author);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            int contentColumnIndex = mCursor.getColumnIndex(MovieContract.ReviewsEntry.COLUMN_CONTENT);
            int authorColumnIndex = mCursor.getColumnIndex(MovieContract.ReviewsEntry.COLUMN_AUTHOR);

            String content = mCursor.getString(contentColumnIndex);
            String author = mCursor.getString(authorColumnIndex);

            mOnClickListener.onReviewClick(content, author);
        }
    }
}
