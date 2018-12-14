package com.example.android.filmespopulares2.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.filmespopulares2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_TOP_RATED = "top_rated";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    public static final int IS_FAVORITE = 1;
    public static final int IS_NOT_FAVORITE = 0;

    public static final class MoviesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_AVERAGE_RATING = "average_rating";
        public static final String COLUMN_RATINGS_COUNT = "ratings_count";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POPULAR_ORDER = "popular_order";
        public static final String COLUMN_TOP_RATED_ORDER = "top_rated_order";
        public static final String COLUMN_FAVORITE = "favorite";

        public static Uri buildMovieListUri(String order) {
            switch (order) {
                case PATH_POPULAR:
                    break;
                case PATH_TOP_RATED:
                    break;
                case PATH_FAVORITES:
                    break;
                default:
                    order = null;
            }

            if (order == null || order.isEmpty()) return null;

            return MoviesEntry.CONTENT_URI.buildUpon()
                    .appendPath(order)
                    .build();
        }

        public static Uri buildSingleMovieUri(long movieId) {
            return MoviesEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId))
                    .build();
        }
    }

    public static final class TrailersEntry implements BaseColumns {

        public static final String TABLE_NAME = "trailers";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";

        public static Uri buildTrailersFromMovieUri(long movieId) {
            return MoviesEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId))
                    .appendPath(PATH_TRAILERS)
                    .build();
        }
    }

    public static final class ReviewsEntry implements BaseColumns {

        public static final String TABLE_NAME = "reviews";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";

        public static Uri buildReviewsFromMovieUri(long movieId) {
            return MoviesEntry.CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(movieId))
                    .appendPath(PATH_REVIEWS)
                    .build();
        }
    }
}