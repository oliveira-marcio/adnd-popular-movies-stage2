package com.example.android.filmespopulares2.sync;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class MovieSyncIntentService extends IntentService {

    public MovieSyncIntentService() {
        super("MovieSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Boolean goAhead = false;
        String action = intent.getAction();
        long movieId = -1;
        if (action != null) {
            switch (action) {
                case MovieSyncTasks.ACTION_SYNC_POPULAR_MOVIES:
                case MovieSyncTasks.ACTION_SYNC_TOP_RATED_MOVIES:
                    goAhead = true;
                    break;
                case MovieSyncTasks.ACTION_SYNC_MOVIE_DETAILS:
                case MovieSyncTasks.ACTION_SET_FAVORITE:
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.containsKey(Intent.EXTRA_TEXT)) {
                        movieId = extras.getLong(Intent.EXTRA_TEXT);
                        goAhead = true;
                    }
                    break;

            }

            if (goAhead) {
                MovieSyncTasks.executeTask(this, action, movieId);
            }
        }
    }
}
