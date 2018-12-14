package com.example.android.filmespopulares2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

public class MovieDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método necessário para habilitar as constraints de FK no SQLite
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                db.setForeignKeyConstraintsEnabled(true);
            } else {
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MovieContract.MoviesEntry.TABLE_NAME + " (" +
                        MovieContract.MoviesEntry._ID + " INTEGER PRIMARY KEY, " +
                        MovieContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MovieContract.MoviesEntry.COLUMN_POSTER + " TEXT, " +
                        MovieContract.MoviesEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                        MovieContract.MoviesEntry.COLUMN_AVERAGE_RATING + " INTEGER NOT NULL DEFAULT 0, " +
                        MovieContract.MoviesEntry.COLUMN_RATINGS_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
                        MovieContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER + " INTEGER, " +
                        MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER + " INTEGER, " +
                        MovieContract.MoviesEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT " +
                        MovieContract.IS_NOT_FAVORITE + ");";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);

        final String SQL_CREATE_TRAILERS_TABLE =
                "CREATE TABLE " + MovieContract.TrailersEntry.TABLE_NAME + " (" +
                        MovieContract.TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.TrailersEntry.COLUMN_MOVIE_ID + " INTEGER REFERENCES " +
                        MovieContract.MoviesEntry.TABLE_NAME + " ON DELETE CASCADE, " +
                        MovieContract.TrailersEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                        MovieContract.TrailersEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        "UNIQUE (" + MovieContract.TrailersEntry.COLUMN_MOVIE_ID + ", " +
                        MovieContract.TrailersEntry.COLUMN_KEY + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_TRAILERS_TABLE);

        final String SQL_CREATE_TRAILERS_INDEX =
                "CREATE INDEX trailers_index ON " + MovieContract.TrailersEntry.TABLE_NAME + "(" +
                        MovieContract.TrailersEntry.COLUMN_MOVIE_ID + ");";

        db.execSQL(SQL_CREATE_TRAILERS_INDEX);

        final String SQL_CREATE_REVIEWS_TABLE =
                "CREATE TABLE " + MovieContract.ReviewsEntry.TABLE_NAME + " (" +
                        MovieContract.ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.ReviewsEntry.COLUMN_MOVIE_ID + " INTEGER REFERENCES " +
                        MovieContract.MoviesEntry.TABLE_NAME + " ON DELETE CASCADE, " +
                        MovieContract.ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                        MovieContract.ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_REVIEWS_TABLE);

        final String SQL_CREATE_REVIEWS_INDEX =
                "CREATE INDEX reviews_index ON " + MovieContract.ReviewsEntry.TABLE_NAME + "(" +
                        MovieContract.ReviewsEntry.COLUMN_MOVIE_ID + ");";

        db.execSQL(SQL_CREATE_REVIEWS_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MoviesEntry.TABLE_NAME);
        onCreate(db);
    }
}
