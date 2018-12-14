package com.example.android.filmespopulares2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MovieProvider extends ContentProvider {
    public static final int CODE_POPULAR_MOVIES = 101;
    public static final int CODE_TOP_RATED_MOVIES = 102;
    public static final int CODE_FAVORITES_MOVIES = 103;
    public static final int CODE_SINGLE_MOVIE = 200;
    public static final int CODE_TRAILERS = 201;
    public static final int CODE_REVIEWS = 202;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/" + MovieContract.PATH_POPULAR,
                CODE_POPULAR_MOVIES);

        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/" + MovieContract.PATH_TOP_RATED,
                CODE_TOP_RATED_MOVIES);

        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/" + MovieContract.PATH_FAVORITES,
                CODE_FAVORITES_MOVIES);

        matcher.addURI(authority,
                MovieContract.PATH_MOVIES + "/#", CODE_SINGLE_MOVIE);

        matcher.addURI(authority,
                MovieContract.PATH_MOVIES + "/#/" + MovieContract.PATH_TRAILERS,
                CODE_TRAILERS);

        matcher.addURI(authority,
                MovieContract.PATH_MOVIES + "/#/" + MovieContract.PATH_REVIEWS,
                CODE_REVIEWS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    private String checkTableConstraints(String tableName, ContentValues values) {
        String error = null;

        switch (tableName) {
            case MovieContract.MoviesEntry.TABLE_NAME:
                Integer id = values.getAsInteger(MovieContract.MoviesEntry._ID);
                if (id == null) {
                    error = "É necessário fornecer o ID de um filme (conforme TMDB)";
                }

                String title = values.getAsString(MovieContract.MoviesEntry.COLUMN_TITLE);
                if (title == null) {
                    error = "É necessário fornecer um título";
                }

                String synopsys = values.getAsString(MovieContract.MoviesEntry.COLUMN_SYNOPSIS);
                if (synopsys == null) {
                    error = "É necessário fornecer uma synopse";
                }

                String release_date = values.getAsString(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE);
                if (release_date == null) {
                    error = "É necessário fornecer uma data de lançamento";
                }

                Integer favorite = values.getAsInteger(MovieContract.MoviesEntry.COLUMN_FAVORITE);
                if (favorite != null && (favorite < MovieContract.IS_NOT_FAVORITE
                        || favorite > MovieContract.IS_FAVORITE)) {
                    error = "Status de favorito inválido.";
                }

                break;

            case MovieContract.TrailersEntry.TABLE_NAME:
                String key = values.getAsString(MovieContract.TrailersEntry.COLUMN_KEY);
                if (key == null) {
                    error = "É necessário fornecer a key do trailer";
                }

                String name = values.getAsString(MovieContract.TrailersEntry.COLUMN_NAME);
                if (name == null) {
                    error = "É necessário fornecer o nome do trailer";
                }

                break;

            case MovieContract.ReviewsEntry.TABLE_NAME:
                String author = values.getAsString(MovieContract.ReviewsEntry.COLUMN_AUTHOR);
                if (author == null) {
                    error = "É necessário fornecer o autor do review";
                }

                String content = values.getAsString(MovieContract.ReviewsEntry.COLUMN_CONTENT);
                if (content == null) {
                    error = "É necessário fornecer o conteúdo do review";
                }

                break;
        }

        return error;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, ContentValues[] values) {
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case CODE_POPULAR_MOVIES:
                // Insert de filmes populares não devem alterar a ordenação de top-rated's e status
                // de favoritos
                for (ContentValues value : values) {
                    if (value.containsKey(MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER)) {
                        value.remove(MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER);
                    }

                    if (value.containsKey(MovieContract.MoviesEntry.COLUMN_FAVORITE)) {
                        value.remove(MovieContract.MoviesEntry.COLUMN_FAVORITE);
                    }
                }
                return bulkUpsertMovies(uri, values);

            case CODE_TOP_RATED_MOVIES:
                // Insert de filmes top-rated não devem alterar a ordenação de populares e status
                // de favoritos
                for (ContentValues value : values) {
                    if (value.containsKey(MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER)) {
                        value.remove(MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER);
                    }

                    if (value.containsKey(MovieContract.MoviesEntry.COLUMN_FAVORITE)) {
                        value.remove(MovieContract.MoviesEntry.COLUMN_FAVORITE);
                    }
                }
                return bulkUpsertMovies(uri, values);

            case CODE_TRAILERS:
                tableName = MovieContract.TrailersEntry.TABLE_NAME;
                return bulkInsertMovieExtras(uri, tableName, values);

            case CODE_REVIEWS:
                tableName = MovieContract.ReviewsEntry.TABLE_NAME;
                return bulkInsertMovieExtras(uri, tableName, values);
            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);
        }
    }

    /**
     * Implementação de um "bulkUpsertMovies" no lugar do bulkInsert.
     * <p>
     * Basicamente serão feitos inserts de novos registros e update dos que já existirem.
     * <p>
     * Como não existe uma implementação já pronta de Upsert (MERGE INTO) no SQLite, foi necessário
     * implementar este método.
     * <p>
     * Maiores informações: https://en.wikipedia.org/wiki/Merge_(SQL)
     */
    private int bulkUpsertMovies(Uri uri, ContentValues[] values) {
        final String tableName = MovieContract.MoviesEntry.TABLE_NAME;
        final String idMovieColumnName = MovieContract.MoviesEntry._ID;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.beginTransaction();
        int rowsInsertedOrUpdated = 0;
        try {
            for (ContentValues value : values) {
                String errorConstraints = checkTableConstraints(tableName, value);
                if (errorConstraints != null) {
                    throw new IllegalArgumentException(errorConstraints);
                }

                if (value.size() == 0) {
                    continue;
                }

                int rowsUpdated = db.update(
                        tableName,
                        value,
                        idMovieColumnName + "=" + value.getAsInteger(idMovieColumnName),
                        null);

                if (rowsUpdated == 0) {
                    long _id = db.insert(tableName, null, value);
                    if (_id != -1) {
                        rowsInsertedOrUpdated++;
                    }
                } else {
                    rowsInsertedOrUpdated++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowsInsertedOrUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsInsertedOrUpdated;
    }

    private int bulkInsertMovieExtras(Uri uri, String tableName, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                String errorConstraints = checkTableConstraints(tableName, value);
                if (errorConstraints != null) {
                    throw new IllegalArgumentException(errorConstraints);
                }

                if (value.size() == 0) {
                    continue;
                }

                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsInserted;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        String tableName;

        switch (sUriMatcher.match(uri)) {
            case CODE_POPULAR_MOVIES:
                tableName = MovieContract.MoviesEntry.TABLE_NAME;
                selection = MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER + " IS NOT NULL";
                sortOrder = MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER + " ASC";
                break;

            case CODE_TOP_RATED_MOVIES:
                tableName = MovieContract.MoviesEntry.TABLE_NAME;
                selection = MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER + " IS NOT NULL";
                sortOrder = MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER + " ASC";
                break;

            case CODE_FAVORITES_MOVIES:
                tableName = MovieContract.MoviesEntry.TABLE_NAME;
                selection = MovieContract.MoviesEntry.COLUMN_FAVORITE + "=" + MovieContract.IS_FAVORITE;
                sortOrder = MovieContract.MoviesEntry.COLUMN_TITLE + " ASC";
                break;

            case CODE_SINGLE_MOVIE:
                tableName = MovieContract.MoviesEntry.TABLE_NAME;
                selection = MovieContract.MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                break;

            case CODE_TRAILERS:
                tableName = MovieContract.TrailersEntry.TABLE_NAME;
                selection = MovieContract.TrailersEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                break;

            case CODE_REVIEWS:
                tableName = MovieContract.ReviewsEntry.TABLE_NAME;
                selection = MovieContract.ReviewsEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                break;

            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);

        }

        cursor = mOpenHelper.getReadableDatabase().query(
                tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;
        String tableName;

        switch (sUriMatcher.match(uri)) {

            case CODE_POPULAR_MOVIES:
                // Deleção será feita apenas em filmes que não são favoritos e não top-rated
                tableName = MovieContract.MoviesEntry.TABLE_NAME;
                selection = MovieContract.MoviesEntry.COLUMN_FAVORITE + "="
                        + MovieContract.IS_NOT_FAVORITE + " AND " +
                        MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER + " IS NULL";
                break;

            case CODE_TOP_RATED_MOVIES:
                // Deleção será feita apenas em filmes que não são favoritos e não populares
                tableName = MovieContract.MoviesEntry.TABLE_NAME;
                selection = MovieContract.MoviesEntry.COLUMN_FAVORITE + "="
                        + MovieContract.IS_NOT_FAVORITE + " AND " +
                        MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER + " IS NULL";
                break;

            case CODE_TRAILERS:
                tableName = MovieContract.TrailersEntry.TABLE_NAME;
                selection = MovieContract.TrailersEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                break;

            case CODE_REVIEWS:
                tableName = MovieContract.ReviewsEntry.TABLE_NAME;
                selection = MovieContract.ReviewsEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{uri.getPathSegments().get(1)};
                break;

            default:
                throw new UnsupportedOperationException("URI desconhecida ou inválida para deleção: " + uri);
        }

        numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                tableName,
                selection,
                selectionArgs);

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ContentValues selectedValues = new ContentValues();

        switch (sUriMatcher.match(uri)) {

            case CODE_SINGLE_MOVIE:
                // Update de um filme específico deverá ser apenas do status de favorito
                selection = MovieContract.MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                String favoriteColumnName = MovieContract.MoviesEntry.COLUMN_FAVORITE;
                if (values.containsKey(favoriteColumnName)) {
                    Integer favorite = values.getAsInteger(favoriteColumnName);
                    if (favorite != null && (favorite < MovieContract.IS_NOT_FAVORITE
                            || favorite > MovieContract.IS_FAVORITE)) {
                        throw new IllegalArgumentException("Status de favorito inválido.");
                    }

                    selectedValues.put(favoriteColumnName, favorite);
                }

                break;

            case CODE_POPULAR_MOVIES:
                // Update de filmes populares deverão apenas setar a coluna correspondente como null
                String popularColumnName = MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER;
                selectedValues.putNull(popularColumnName);
                break;

            case CODE_TOP_RATED_MOVIES:
                // Update de filmes top-rated deverão apenas setar a coluna correspondente como null
                String topRatedColumnName = MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER;
                selectedValues.putNull(topRatedColumnName);
                break;

            default:
                throw new UnsupportedOperationException("URI desconhecida: " + uri);
        }

        int rowsUpdated = mOpenHelper.getWritableDatabase().update(
                MovieContract.MoviesEntry.TABLE_NAME,
                selectedValues,
                selection,
                selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);

            if (sUriMatcher.match(uri) == CODE_SINGLE_MOVIE) {
                Uri uriAllFavorites = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_FAVORITES);
                getContext().getContentResolver().notifyChange(uriAllFavorites, null);
            }
        }

        return rowsUpdated;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException(
                "Não há implementação de insert neste aplicativo. Use bulkUpsertMovies no lugar.");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Não há implementação de getType neste aplicativo.");
    }
}
