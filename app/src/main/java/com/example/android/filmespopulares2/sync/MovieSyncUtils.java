package com.example.android.filmespopulares2.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class MovieSyncUtils {

    private static boolean sPopularInitialized;
    private static boolean sTopRatedInitialized;

    synchronized public static void initializeMovies(@NonNull final Context context, final String action) {

        if (sPopularInitialized && sTopRatedInitialized) return;

        switch (action) {
            case MovieSyncTasks.ACTION_SYNC_POPULAR_MOVIES:
                sPopularInitialized = true;
                break;
            case MovieSyncTasks.ACTION_SYNC_TOP_RATED_MOVIES:
                sTopRatedInitialized = true;
                break;
            default:
                return;
        }

        startImmediateMoviesSync(context, action);
    }

    public static void startImmediateMoviesSync(@NonNull final Context context, String action) {
        Intent intentToSyncImmediately = new Intent(context, MovieSyncIntentService.class);
        intentToSyncImmediately.setAction(action);
        context.startService(intentToSyncImmediately);
    }

    public static void startSyncMovieDetails(@NonNull final Context context, long movieId) {
        Intent intentDetails = new Intent(context, MovieSyncIntentService.class);
        intentDetails.setAction(MovieSyncTasks.ACTION_SYNC_MOVIE_DETAILS);
        intentDetails.putExtra(Intent.EXTRA_TEXT, movieId);
        context.startService(intentDetails);
    }
}
