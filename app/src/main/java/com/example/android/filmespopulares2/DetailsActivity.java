package com.example.android.filmespopulares2;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.filmespopulares2.data.MovieContract;
import com.example.android.filmespopulares2.databinding.ActivityDetailsBinding;
import com.example.android.filmespopulares2.sync.MovieSyncUtils;
import com.example.android.filmespopulares2.utilities.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TrailerAdapter.TrailerAdapterOnClickHandler,
        ReviewAdapter.ReviewAdapterOnClickHandler {

    /**
     * 3 CursorLoaders necessários para esta Activity:
     * - Dados do filme
     * - Dados dos trailers do filme
     * - Dados dos reviews do filme
     */
    private static final int ID_MOVIE_DETAIL_LOADER = 997;
    private static final int ID_MOVIE_TRAILERS_LOADER = 998;
    private static final int ID_MOVIE_REVIEWS_LOADER = 999;

    private long mMovieId;
    private boolean mIsFavorite;

    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    private int mTrailerPosition = RecyclerView.NO_POSITION;
    private int mReviewPosition = RecyclerView.NO_POSITION;

    private ActivityDetailsBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        // Execução do Shared Element Transition interrompida. Será continuada após a conclusão do
        // ID_MOVIE_DETAIL_LOADER e a conclusão do carregamento da respectiva imagem do filme
        // pelo Picasso.
        supportPostponeEnterTransition();

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(Intent.EXTRA_TEXT))
            throw new NullPointerException("ID do filme não encontrado para DetailsActivity");

        mMovieId = extras.getLong(Intent.EXTRA_TEXT);

        initializeUIElements();

        if (!getSupportLoaderManager().hasRunningLoaders()) {
            showTrailersLoading();
            showReviewsLoading();
        }

        getSupportLoaderManager().initLoader(ID_MOVIE_DETAIL_LOADER, null, this);
        getSupportLoaderManager().initLoader(ID_MOVIE_TRAILERS_LOADER, null, this);
        getSupportLoaderManager().initLoader(ID_MOVIE_REVIEWS_LOADER, null, this);

        MovieSyncUtils.startSyncMovieDetails(this, mMovieId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeUIElements() {
        mDetailBinding.trailersProgressBar.setVisibility(ProgressBar.INVISIBLE);
        mDetailBinding.reviewsProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mDetailBinding.fabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavoriteStatus();
            }
        });

        LinearLayoutManager trailersLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mDetailBinding.rvTrailers.setLayoutManager(trailersLayoutManager);
        mDetailBinding.rvTrailers.setHasFixedSize(true);

        mTrailerAdapter = new TrailerAdapter(this, this);
        mDetailBinding.rvTrailers.setAdapter(mTrailerAdapter);

        LinearLayoutManager reviewsLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mDetailBinding.rvReviews.setLayoutManager(reviewsLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mDetailBinding.rvReviews.getContext(),
                reviewsLayoutManager.getOrientation());
        mDetailBinding.rvReviews.addItemDecoration(dividerItemDecoration);

        mDetailBinding.rvReviews.setHasFixedSize(true);

        mReviewAdapter = new ReviewAdapter(this, this);
        mDetailBinding.rvReviews.setAdapter(mReviewAdapter);

        final int imageWidth = Integer.parseInt(getString(R.string.thumb_size));
        final double IMAGE_RATIO = Double.parseDouble((getString(R.string.image_ratio)));

        mDetailBinding.imagePoster.getLayoutParams().width = imageWidth;
        mDetailBinding.imagePoster.getLayoutParams().height = (int) Math.round(imageWidth * IMAGE_RATIO);

        ViewCompat.setTransitionName(mDetailBinding.imagePoster, Long.toString(mMovieId));
    }

    private void showTrailersLoading() {
        mDetailBinding.tvTrailersLabel.setVisibility(View.GONE);
        mDetailBinding.rvTrailers.setVisibility(View.GONE);
        mDetailBinding.trailersProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void showTrailersDataView() {
        mDetailBinding.tvTrailersLabel.setVisibility(View.VISIBLE);
        mDetailBinding.rvTrailers.setVisibility(View.VISIBLE);
        mDetailBinding.trailersProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void showReviewsLoading() {
        mDetailBinding.tvReviewsLabel.setVisibility(View.GONE);
        mDetailBinding.rvReviews.setVisibility(View.GONE);
        mDetailBinding.reviewsProgressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void showReviewsDataView() {
        mDetailBinding.tvReviewsLabel.setVisibility(View.VISIBLE);
        mDetailBinding.rvReviews.setVisibility(View.VISIBLE);
        mDetailBinding.reviewsProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void hideTrailersSection() {
        mDetailBinding.tvTrailersLabel.setVisibility(View.GONE);
        mDetailBinding.rvTrailers.setVisibility(View.GONE);
        mDetailBinding.trailersProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void hideReviewsSection() {
        mDetailBinding.tvReviewsLabel.setVisibility(View.GONE);
        mDetailBinding.rvReviews.setVisibility(View.GONE);
        mDetailBinding.reviewsProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    public String formatDate(String dateString) {
        SimpleDateFormat firstFormatter, secondFormatter;
        firstFormatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date oldDate = firstFormatter.parse(dateString.replaceFirst(":(?=[0-9]{2}$)", ""));
            secondFormatter = new SimpleDateFormat("dd/LLL/yyyy");
            return secondFormatter.format(oldDate);
        } catch (java.text.ParseException e) {
            return dateString;
        }
    }

    public void setFavoriteFabColor() {
        mDetailBinding.fabFavorite.setBackgroundTintList(
                ColorStateList.valueOf(getResources()
                        .getColor(mIsFavorite ? R.color.colorFavorite : R.color.colorAccent)));
    }

    public void toggleFavoriteStatus() {
        mIsFavorite = !mIsFavorite;

        ContentValues values = new ContentValues();
        values.put(MovieContract.MoviesEntry.COLUMN_FAVORITE,
                mIsFavorite ? MovieContract.IS_FAVORITE : MovieContract.IS_NOT_FAVORITE);

        int updatedRows = getContentResolver().update(
                MovieContract.MoviesEntry.buildSingleMovieUri(mMovieId),
                values,
                null,
                null);

        if (updatedRows > 0)
            Toast.makeText(this,
                    mIsFavorite ? getString(R.string.favorite_added) : getString(R.string.favorite_removed),
                    Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case ID_MOVIE_DETAIL_LOADER:
                return new CursorLoader(this,
                        MovieContract.MoviesEntry.buildSingleMovieUri(mMovieId),
                        null,
                        null,
                        null,
                        null);

            case ID_MOVIE_TRAILERS_LOADER:
                return new CursorLoader(this,
                        MovieContract.TrailersEntry.buildTrailersFromMovieUri(mMovieId),
                        null,
                        null,
                        null,
                        null);

            case ID_MOVIE_REVIEWS_LOADER:
                return new CursorLoader(this,
                        MovieContract.ReviewsEntry.buildReviewsFromMovieUri(mMovieId),
                        null,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader não implementado: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            switch (loader.getId()) {
                case ID_MOVIE_DETAIL_LOADER:
                    finish();
                    break;
                case ID_MOVIE_TRAILERS_LOADER:
                    hideTrailersSection();
                    break;
                case ID_MOVIE_REVIEWS_LOADER:
                    hideReviewsSection();
                    break;
            }
            return;
        }

        switch (loader.getId()) {
            case ID_MOVIE_DETAIL_LOADER:
                int titleColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_TITLE);
                int synopsisColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_SYNOPSIS);
                int releaseDateColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE);
                int avgRatingColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_AVERAGE_RATING);
                int ratingsCountColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_RATINGS_COUNT);
                int posterColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_POSTER);
                int favoriteColumnIndex = data.getColumnIndex(MovieContract.MoviesEntry.COLUMN_FAVORITE);

                mDetailBinding.tvTitle.setText(data.getString(titleColumnIndex));
                mDetailBinding.tvSynopsis.setText(data.getString(synopsisColumnIndex));
                mDetailBinding.tvReleaseDate.setText(formatDate(data.getString(releaseDateColumnIndex)));
                mDetailBinding.tvAverageRating.setText("" + data.getDouble(avgRatingColumnIndex));
                mDetailBinding.tvUserRating.setText("" + data.getInt(ratingsCountColumnIndex));

                // Picasso invocado com alguns callbacks para dar continuidade na execução do
                // Shared Element Transition
                Picasso.with(this)
                        .load(NetworkUtils.buildPosterUrl(data.getString(posterColumnIndex),
                                getString(R.string.thumb_size)).toString())
                        .noFade()
                        .placeholder(R.drawable.placeholder)
                        .into(mDetailBinding.imagePoster, new Callback() {
                            @Override
                            public void onSuccess() {
                                supportStartPostponedEnterTransition();
                            }

                            @Override
                            public void onError() {
                                supportStartPostponedEnterTransition();
                            }
                        });

                mIsFavorite = (data.getInt(favoriteColumnIndex) == MovieContract.IS_FAVORITE);
                setFavoriteFabColor();

                break;

            case ID_MOVIE_TRAILERS_LOADER:
                mTrailerAdapter.swapCursor(data);
                if (mTrailerPosition == RecyclerView.NO_POSITION) {
                    mTrailerPosition = 0;
                }

                mDetailBinding.rvTrailers.smoothScrollToPosition(mTrailerPosition);
                showTrailersDataView();
                break;

            case ID_MOVIE_REVIEWS_LOADER:
                mReviewAdapter.swapCursor(data);
                if (mReviewPosition == RecyclerView.NO_POSITION) {
                    mReviewPosition = 0;
                }

                mDetailBinding.rvReviews.smoothScrollToPosition(mReviewPosition);
                showReviewsDataView();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_MOVIE_TRAILERS_LOADER:
                mTrailerAdapter.swapCursor(null);
                break;
            case ID_MOVIE_REVIEWS_LOADER:
                mReviewAdapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onTrailerClick(String trailerKey) {
        final String youTubeUrl = "https://www.youtube.com/watch?v=";
        Uri uri = Uri.parse(youTubeUrl + trailerKey);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @Override
    public void onReviewClick(String content, String author) {
        ReviewDialogFragment.newInstance(content, author).show(getSupportFragmentManager(), "reviewDialog");
    }
}
