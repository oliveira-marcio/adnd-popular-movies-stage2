package com.example.android.filmespopulares2.utilities;

import android.content.ContentValues;

import com.example.android.filmespopulares2.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Classe utilitária com métodos para criar ContentValues para os respectivos tipos de informações
 * em JSON obtidas da API do TMDB.org, como filmes, trailers e reviews.
 */
public final class TmdbJsonUtils {

    private TmdbJsonUtils() {
    }

    public static ContentValues[] getMoviesContentValuesFromJson(String moviesJsonStr,
                                                                 String sortType)
            throws JSONException {

        String dbColumnOrder;

        switch (sortType) {
            case MovieContract.PATH_POPULAR:
                dbColumnOrder = MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER;
                break;
            case MovieContract.PATH_TOP_RATED:
                dbColumnOrder = MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER;
                break;
            default:
                throw new JSONException("Parâmetro sortType inválido");
        }

        final String TMDB_RESULTS = "results";
        final String TMDB_MESSAGE_CODE = "status_code";

        final String TMDB_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_VOTE_COUNT = "vote_count";
        final String TMDB_RELEASE_DATE = "release_date";

        JSONObject movieJsonObject = new JSONObject(moviesJsonStr);

        if (movieJsonObject.has(TMDB_MESSAGE_CODE)) {
            int errorCode = movieJsonObject.getInt(TMDB_MESSAGE_CODE);

            if (errorCode != HttpURLConnection.HTTP_OK)
                return null;
        }

        JSONArray movieArray = movieJsonObject.getJSONArray(TMDB_RESULTS);

        ContentValues[] movieContentValues = new ContentValues[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject currentMovie = movieArray.getJSONObject(i);

            int id = currentMovie.getInt(TMDB_ID);
            String title = currentMovie.getString(TMDB_TITLE);
            String synopsis = currentMovie.getString(TMDB_OVERVIEW);
            double averageRating = currentMovie.getDouble(TMDB_VOTE_AVERAGE);
            int ratingsCount = currentMovie.getInt(TMDB_VOTE_COUNT);
            String releaseDate = currentMovie.getString(TMDB_RELEASE_DATE);

            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MoviesEntry._ID, id);
            movieValues.put(MovieContract.MoviesEntry.COLUMN_TITLE, title);
            movieValues.put(MovieContract.MoviesEntry.COLUMN_SYNOPSIS, synopsis);
            movieValues.put(MovieContract.MoviesEntry.COLUMN_AVERAGE_RATING, averageRating);
            movieValues.put(MovieContract.MoviesEntry.COLUMN_RATINGS_COUNT, ratingsCount);
            movieValues.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(dbColumnOrder, i + 1);

            if (currentMovie.has(TMDB_POSTER_PATH)) {
                String posterPath = currentMovie.getString(TMDB_POSTER_PATH);
                movieValues.put(MovieContract.MoviesEntry.COLUMN_POSTER, posterPath);
            }

            movieContentValues[i] = movieValues;
        }

        return movieContentValues;
    }

    public static ContentValues[] getTrailersContentValuesFromJson(String trailersJsonStr)
            throws JSONException {

        final String TMDB_MOVIE_ID = "id";
        final String TMDB_RESULTS = "results";
        final String TMDB_MESSAGE_CODE = "status_code";

        final String TMDB_KEY = "key";
        final String TMDB_NAME = "name";
        final String TMDB_SITE = "site";

        final String YOUTUBE_SITE = "YOUTUBE";

        JSONObject trailersJsonObject = new JSONObject(trailersJsonStr);

        if (trailersJsonObject.has(TMDB_MESSAGE_CODE)) {
            int errorCode = trailersJsonObject.getInt(TMDB_MESSAGE_CODE);

            if (errorCode != HttpURLConnection.HTTP_OK)
                return null;
        }

        int movieId = trailersJsonObject.getInt(TMDB_MOVIE_ID);
        JSONArray trailersArray = trailersJsonObject.getJSONArray(TMDB_RESULTS);

        ContentValues[] trailersContentValues = new ContentValues[trailersArray.length()];

        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject currentTrailer = trailersArray.getJSONObject(i);

            String key = currentTrailer.getString(TMDB_KEY);
            String name = currentTrailer.getString(TMDB_NAME);
            String site = currentTrailer.getString(TMDB_SITE);

            if (site.trim().toUpperCase().equals(YOUTUBE_SITE)) {
                ContentValues trailerValues = new ContentValues();
                trailerValues.put(MovieContract.TrailersEntry.COLUMN_MOVIE_ID, movieId);
                trailerValues.put(MovieContract.TrailersEntry.COLUMN_KEY, key);
                trailerValues.put(MovieContract.TrailersEntry.COLUMN_NAME, name);

                trailersContentValues[i] = trailerValues;
            }
        }

        return trailersContentValues;
    }

    public static ContentValues[] getReviewsContentValuesFromJson(String reviewsJsonStr)
            throws JSONException {

        final String TMDB_MOVIE_ID = "id";
        final String TMDB_RESULTS = "results";
        final String TMDB_MESSAGE_CODE = "status_code";

        final String TMDB_AUTHOR = "author";
        final String TMDB_CONTENT = "content";

        JSONObject reviewsJsonObject = new JSONObject(reviewsJsonStr);

        if (reviewsJsonObject.has(TMDB_MESSAGE_CODE)) {
            int errorCode = reviewsJsonObject.getInt(TMDB_MESSAGE_CODE);

            if (errorCode != HttpURLConnection.HTTP_OK)
                return null;
        }

        int movieId = reviewsJsonObject.getInt(TMDB_MOVIE_ID);
        JSONArray reviewsArray = reviewsJsonObject.getJSONArray(TMDB_RESULTS);

        ContentValues[] reviewsContentValues = new ContentValues[reviewsArray.length()];

        for (int i = 0; i < reviewsArray.length(); i++) {
            JSONObject currentReview = reviewsArray.getJSONObject(i);

            String author = currentReview.getString(TMDB_AUTHOR);
            String content = currentReview.getString(TMDB_CONTENT);

            ContentValues reviewValues = new ContentValues();
            reviewValues.put(MovieContract.ReviewsEntry.COLUMN_MOVIE_ID, movieId);
            reviewValues.put(MovieContract.ReviewsEntry.COLUMN_AUTHOR, author);
            reviewValues.put(MovieContract.ReviewsEntry.COLUMN_CONTENT, content);

            reviewsContentValues[i] = reviewValues;
        }

        return reviewsContentValues;
    }
}

