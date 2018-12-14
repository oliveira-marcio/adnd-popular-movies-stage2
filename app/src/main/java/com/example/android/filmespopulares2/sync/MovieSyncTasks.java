package com.example.android.filmespopulares2.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.example.android.filmespopulares2.data.MovieContract;
import com.example.android.filmespopulares2.utilities.NetworkUtils;
import com.example.android.filmespopulares2.utilities.TmdbJsonUtils;

import java.net.URL;

public class MovieSyncTasks {

    public static final String ACTION_SYNC_POPULAR_MOVIES = "sync-popular-movies";
    public static final String ACTION_SYNC_TOP_RATED_MOVIES = "sync-top-rated-movies";
    public static final String ACTION_SYNC_MOVIE_DETAILS = "sync-movie-details";
    public static final String ACTION_SET_FAVORITE = "set-favorite";

    synchronized public static void executeTask(Context context, String action, long movieId) {
        switch (action) {
            case ACTION_SYNC_POPULAR_MOVIES:
                syncMovies(context, MovieContract.PATH_POPULAR);
                break;
            case ACTION_SYNC_TOP_RATED_MOVIES:
                syncMovies(context, MovieContract.PATH_TOP_RATED);
                break;
            case ACTION_SYNC_MOVIE_DETAILS:
                syncMovieDetails(context, movieId);
        }
    }

    /**
     * Estratégia utilizada para sincronia do banco de dados de filmes com os dados do TMDB
     * (Ex: filmes populares):
     * 1) Deletar todos os filmes que não são favoritos e não são "top-rated"
     * 2) Remover os dados de ordenação de filmes populares
     * 3) Fazer a inserção dos filmes populares (ou atualizar os filmes que já existem na base)
     * <p>
     * A idéia dessa estratégia é que um mesmo filme pode constar como popular ou top-rated ou estar
     * marcado como favorito, então os dados existentes não devem ser removidos, apenas atualizados.
     */
    synchronized private static void syncMovies(Context context, String sortOrder) {
        if (sortOrder == null) return;

        String tMdbOrder;

        try {
            switch (sortOrder) {
                case MovieContract.PATH_POPULAR:
                    tMdbOrder = NetworkUtils.SORT_BY_POPULAR;
                    break;
                case MovieContract.PATH_TOP_RATED:
                    tMdbOrder = NetworkUtils.SORT_BY_TOP_RATED;
                    break;
                default:
                    return;
            }

            URL urlMovies = NetworkUtils.buildMoviesUrl(tMdbOrder);

            String tMdbSearchResults = NetworkUtils.getResponseFromHttpUrl(urlMovies);

            ContentValues[] moviesValues = TmdbJsonUtils
                    .getMoviesContentValuesFromJson(tMdbSearchResults, sortOrder);

            if (moviesValues != null && moviesValues.length != 0) {
                Uri moviesUri = MovieContract.MoviesEntry.buildMovieListUri(sortOrder);
                ContentResolver moviesContentResolver = context.getContentResolver();

                moviesContentResolver.delete(moviesUri, null, null);
                moviesContentResolver.update(moviesUri, null, null, null);
                moviesContentResolver.bulkInsert(moviesUri, moviesValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A sincronia dos extras de um filme (trailers e reviews) é feita sempre que um filme é
     * acessado na UI. Basicamente os dados para o filme em questão são deletados e
     * re-inseridos.
     */
    synchronized private static void syncMovieDetails(Context context, long movieId) {
        if (movieId == -1) return;

        syncDetails(context, movieId, NetworkUtils.TRAILERS_PARAM);
        syncDetails(context, movieId, NetworkUtils.REVIEWS_PARAM);
    }

    private static void syncDetails(Context context, long movieId, String pathDetail) {
        try {
            URL urlDetails = NetworkUtils.buildMovieExtrasUrl(movieId, pathDetail);
            String tMdbSearchResults = NetworkUtils.getResponseFromHttpUrl(urlDetails);

            ContentValues[] detailValues;
            Uri detailsUri;

            if (NetworkUtils.TRAILERS_PARAM.equals(pathDetail)) {
                detailValues = TmdbJsonUtils
                        .getTrailersContentValuesFromJson(tMdbSearchResults);
                detailsUri = MovieContract.TrailersEntry.buildTrailersFromMovieUri(movieId);
            } else {
                detailValues = TmdbJsonUtils
                        .getReviewsContentValuesFromJson(tMdbSearchResults);
                detailsUri = MovieContract.ReviewsEntry.buildReviewsFromMovieUri(movieId);
            }

            if (detailValues != null && detailValues.length != 0) {
                ContentResolver detailsContentResolver = context.getContentResolver();

                detailsContentResolver.delete(detailsUri, null, null);
                detailsContentResolver.bulkInsert(detailsUri, detailValues);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
