package com.example.android.filmespopulares2;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.filmespopulares2.data.MovieContract;
import com.example.android.filmespopulares2.data.MovieDbHelper;
import com.example.android.filmespopulares2.data.MovieProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestMovieProvider {
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() {
        deleteAllRecordsFromMoviesTable();
    }

    @Test
    public void testUriMatcher() {
        UriMatcher matcher = MovieProvider.buildUriMatcher();

        Uri uri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_POPULAR);
        String uriError = "Erro no parsing da Uri de filmes populares.";
        assertEquals(uriError, MovieProvider.CODE_POPULAR_MOVIES, matcher.match(uri));

        uri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_TOP_RATED);
        uriError = "Erro no parsing da Uri de filmes top-rated.";
        assertEquals(uriError, MovieProvider.CODE_TOP_RATED_MOVIES, matcher.match(uri));

        uri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_FAVORITES);
        uriError = "Erro no parsing da Uri de filmes favoritos.";
        assertEquals(uriError, MovieProvider.CODE_FAVORITES_MOVIES, matcher.match(uri));

        uri = MovieContract.MoviesEntry.buildSingleMovieUri(0);
        uriError = "Erro no parsing da Uri de um único filme.";
        assertEquals(uriError, MovieProvider.CODE_SINGLE_MOVIE, matcher.match(uri));

        uri = MovieContract.TrailersEntry.buildTrailersFromMovieUri(0);
        uriError = "Erro no parsing da Uri de trailers.";
        assertEquals(uriError, MovieProvider.CODE_TRAILERS, matcher.match(uri));

        uri = MovieContract.ReviewsEntry.buildReviewsFromMovieUri(0);
        uriError = "Erro no parsing da Uri de um único filme.";
        assertEquals(uriError, MovieProvider.CODE_REVIEWS, matcher.match(uri));
    }

    /**
     * Este teste valida ambos os métodos de bulkInsert e Query do provider para as URI's de filmes
     *
     * 1) Insere vários filmes populares e confere os dados
     * 2) Insere vários filmes top-rated (atualizando registros de filmes já existentes) e confere
     * apenas os dados de top-rated
     * 3) Confere os dados completos do banco para avaliar se o merge (upsert) foi bem sucedido
     */
    @Test
    public void testMoviesBulkInsert() {
        Uri popularUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_POPULAR);
        ContentValues[] popularValues = TestDbUtilities.getPopularMoviesFromMultipleContentValues();

        int rowsInserted = mContext.getContentResolver().bulkInsert(popularUri, popularValues);

        String bulkinsertFailed = "Houveram falhas para inserir os filmes populares no database";
        assertEquals(bulkinsertFailed, popularValues.length, rowsInserted);

        Cursor cursor = mContext.getContentResolver().query(popularUri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de filmes";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de filmes populares atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    popularValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();

        Uri topRatedUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_TOP_RATED);
        ContentValues[] topRatedValues = TestDbUtilities.getTopRatedMoviesFromMultipleContentValues();

        rowsInserted = mContext.getContentResolver().bulkInsert(topRatedUri, topRatedValues);

        bulkinsertFailed = "Houveram falhas para inserir os filmes top-rated no database";
        assertEquals(bulkinsertFailed, topRatedValues.length, rowsInserted);

        cursor = mContext.getContentResolver().query(topRatedUri, null, null, null, null);

        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de filmes top-rated atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    topRatedValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();

        ContentValues[] allMovies = TestDbUtilities.createBulkInsertTestMovieContentValues();
        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getReadableDatabase();

        cursor = database.query(MovieContract.MoviesEntry.TABLE_NAME, null, null, null, null, null, null);

        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de filmes totais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    allMovies[i++]);
        } while (cursor.moveToNext());

        cursor.close();
        database.close();
    }

    /**
     * Testa os seguintes métodos do provider:
     * - bulkInsert de vários dados de trailers para um determinado filme
     * - Query dos dados de trailers para um determinado filme
     */
    @Test
    public void testTrailersBulkInsert() {
        long id = insertSingleMovie();

        Uri trailersUri = MovieContract.TrailersEntry.buildTrailersFromMovieUri(id);
        ContentValues[] trailersValues = TestDbUtilities.createBulkInsertTestTrailersContentValues();

        int rowsInserted = mContext.getContentResolver().bulkInsert(trailersUri, trailersValues);

        String bulkinsertFailed = "Houveram falhas para inserir os trailers no database";
        assertEquals(bulkinsertFailed, trailersValues.length, rowsInserted);

        Cursor cursor = mContext.getContentResolver().query(trailersUri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de trailers";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de trailers atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    trailersValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa os seguintes métodos do provider:
     * - bulkInsert de vários dados de reviews para um determinado filme
     * - Query dos dados de reviews para um determinado filme
     */
    @Test
    public void testReviewsBulkInsert() {
        long id = insertSingleMovie();

        Uri reviewsUri = MovieContract.ReviewsEntry.buildReviewsFromMovieUri(id);
        ContentValues[] reviewsValues = TestDbUtilities.createBulkInsertTestReviewsContentValues();

        int rowsInserted = mContext.getContentResolver().bulkInsert(reviewsUri, reviewsValues);

        String bulkinsertFailed = "Houveram falhas para inserir os reviews no database";
        assertEquals(bulkinsertFailed, reviewsValues.length, rowsInserted);

        Cursor cursor = mContext.getContentResolver().query(reviewsUri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de reviews";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de reviews atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    reviewsValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a atualização do status de favorito de um único filme e testa o método query com a URI
     * de único filme.
     */
    @Test
    public void testUpdateFavoriteStatus(){
        testMoviesBulkInsert();

        ContentValues[] testMoviesContentValues = TestDbUtilities.createBulkInsertTestMovieContentValues();

        final int MOVIE_ID = 0;

        testMoviesContentValues[MOVIE_ID].put(MovieContract.MoviesEntry.COLUMN_FAVORITE, 1);

        Uri movieUri = MovieContract.MoviesEntry.buildSingleMovieUri(MOVIE_ID);

        int rowsUpdated = mContext.getContentResolver().update(movieUri,
                testMoviesContentValues[MOVIE_ID],
                null, null);

        int rowsExpectedToBeUpdated = 1;
        String updateFailed = "Não foi possível atualizar o status de favorito";

        assertEquals(updateFailed, rowsExpectedToBeUpdated, rowsUpdated);

        Cursor cursor = mContext.getContentResolver().query(movieUri, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta do filme";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores do filme atual não batem com o esperado.";

        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testMoviesContentValues[MOVIE_ID]);

        assertFalse("Erro: Mais de uma linha retornada da consulta de filmes",
                cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa se o método query do provider retorna apenas os filmes marcados como favoritos.
     */
    @Test
    public void testQueryFavoriteMovies(){
        testMoviesBulkInsert();

        ContentValues[] testFavoritesContentValues = TestDbUtilities.createBulkInsertTestMovieContentValues();

        final int MOVIES_TO_BE_FAVORITED = 2;

        for (int i = 0; i < MOVIES_TO_BE_FAVORITED; i++) {
            Uri movieUri = MovieContract.MoviesEntry.buildSingleMovieUri(i);

            testFavoritesContentValues[i].put(MovieContract.MoviesEntry.COLUMN_FAVORITE, 1);
            int rowsUpdated = mContext.getContentResolver().update(movieUri,
                    testFavoritesContentValues[i],
                    null, null);

            int rowsExpectedToBeUpdated = 1;
            String updateFailed = "Não foi possível atualizar o status de favorito do filme " + i;

            assertEquals(updateFailed, rowsExpectedToBeUpdated, rowsUpdated);
        }

        Uri favoritesUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_FAVORITES);

        Cursor cursor = mContext.getContentResolver().query(favoritesUri, null, null, null, null);

        String InvalidResultsCount = "A quantidade de filmes favoritos está incorreta";
        assertEquals(InvalidResultsCount, MOVIES_TO_BE_FAVORITED, cursor.getCount());

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de filmes favoritos";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores do filme favoritado atual (" + i + ") não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    testFavoritesContentValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a deleção de trailers de um determinado filme. Ao final, verifica se o restante do
     * database de trailers continua como estava.
     */
    @Test
    public void testDeleteTrailersFromMovie(){
        testMoviesBulkInsert();
        testTrailersBulkInsert();

        final int testMovieId = 0;
        insertSingleTrailerForMovie(testMovieId);

        Uri trailersUri = MovieContract.TrailersEntry.buildTrailersFromMovieUri(testMovieId);

        int deletedRows = mContext.getContentResolver().delete(trailersUri, null, null);

        String deleteFailed = "Houveram falhas para deletar os trailers no database";
        assertEquals(deleteFailed, 1, deletedRows);

        ContentValues [] testTrailersValues = TestDbUtilities.createBulkInsertTestTrailersContentValues();

        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        Cursor cursor = database.query(MovieContract.TrailersEntry.TABLE_NAME, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de trailers";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de trailers atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    testTrailersValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
        database.close();
    }

    /**
     * Testa a deleção de reviews de um determinado filme. Ao final, verifica se o restante do
     * database de trailers continua como estava.
     */
    @Test
    public void testDeleteReviewsFromMovie(){
        testMoviesBulkInsert();
        testReviewsBulkInsert();

        final int testMovieId = 0;
        insertSingleReviewForMovie(testMovieId);

        Uri reviewsUri = MovieContract.ReviewsEntry.buildReviewsFromMovieUri(testMovieId);

        int deletedRows = mContext.getContentResolver().delete(reviewsUri, null, null);

        String deleteFailed = "Houveram falhas para deletar os reviews no database";
        assertEquals(deleteFailed, 1, deletedRows);

        ContentValues [] testReviewsValues = TestDbUtilities.createBulkInsertTestReviewsContentValues();

        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        Cursor cursor = database.query(MovieContract.ReviewsEntry.TABLE_NAME, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de reviews";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        int i = 0;
        do {
            String expectedResultDidntMatchActual =
                    "Valores de reviews atuais não batem com o esperado.";
            TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                    cursor,
                    testReviewsValues[i++]);
        } while (cursor.moveToNext());

        cursor.close();
        database.close();
    }

    /**
     * Serão feitos 2 testes neste teste:
     * 1) Deleta apenas os filmes populares que não estão marcados como top-rated e favoritos
     * (esperado: 1)
     * 2) Seta o único filme deletável do teste acima como favorito e tenta novamente a deleção,
     * onde o esperado é que nada seja deletado
     */
    @Test
    public void testDeletePopularMovies(){
        testMoviesBulkInsert();
        Uri popularUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_POPULAR);
        int deletedRows = mContext.getContentResolver().delete(popularUri, null, null);

        String deleteFailed = "Houveram falhas para deletar os filmes populares do database";
        assertEquals(deleteFailed, 1, deletedRows);

        deleteAllRecordsFromMoviesTable();
        testUpdateFavoriteStatus();

        deletedRows = mContext.getContentResolver().delete(popularUri, null, null);

        assertEquals(deleteFailed, 0, deletedRows);
    }

    /**
     * Serão feitos 2 testes neste teste:
     * 1) Deleta apenas os filmes top-rated que não estão marcados como populares e favoritos
     * (esperado: 1)
     * 2) Seta o único filme deletável do teste acima como favorito e tenta novamente a deleção,
     * onde o esperado é que nada seja deletado
     */
    @Test
    public void testDeleteTopRatedMovies(){
        testUpdateFavoriteStatus();

        Uri topRatedUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_TOP_RATED);
        int deletedRows = mContext.getContentResolver().delete(topRatedUri, null, null);

        String deleteFailed = "Houveram falhas para deletar os filmes top-rated do database";
        assertEquals(deleteFailed, 1, deletedRows);

        deleteAllRecordsFromMoviesTable();
        testMoviesBulkInsert();

        final int MOVIE_ID = TestDbUtilities.CONTENT_VALUES_QUANTITY - 1;

        ContentValues testValue = new ContentValues();
        testValue.put(MovieContract.MoviesEntry.COLUMN_FAVORITE, 1);

        Uri movieUri = MovieContract.MoviesEntry.buildSingleMovieUri(MOVIE_ID);

        int rowsUpdated = mContext.getContentResolver().update(movieUri,
                testValue,
                null, null);

        deletedRows = mContext.getContentResolver().delete(topRatedUri, null, null);

        assertEquals(deleteFailed, 0, deletedRows);
    }

    /**
     * Testa se o método update do Provider seta para null os dados de ranking de filmes populares.
     */
    @Test
    public void testUpdatePopularMovies(){
        testMoviesBulkInsert();

        Uri popularUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_POPULAR);
        int rowsUpdated = mContext.getContentResolver().update(popularUri, null, null, null);

        String updateFailed = "Houveram falhas para atualizar os filmes populares do database";
        assertEquals(updateFailed, TestDbUtilities.CONTENT_VALUES_QUANTITY, rowsUpdated);

        Cursor cursor = mContext.getContentResolver().query(popularUri, null, null, null, null);

        assertFalse(updateFailed, cursor.moveToFirst());

        cursor.close();

        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        cursor = database.query(MovieContract.MoviesEntry.TABLE_NAME, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de filmes";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        assertEquals(updateFailed, TestDbUtilities.CONTENT_VALUES_QUANTITY, cursor.getCount());

        cursor.close();
        database.close();
    }

    /**
     * Testa se o método update do Provider seta para null os dados de ranking de filmes top-rated.
     */
    @Test
    public void testUpdateTopRatedMovies(){
        testMoviesBulkInsert();

        Uri topRatedUri = MovieContract.MoviesEntry.buildMovieListUri(MovieContract.PATH_TOP_RATED);
        int rowsUpdated = mContext.getContentResolver().update(topRatedUri, null, null, null);

        String updateFailed = "Houveram falhas para atualizar os filmes top-rated do database";
        assertEquals(updateFailed, TestDbUtilities.CONTENT_VALUES_QUANTITY, rowsUpdated);

        Cursor cursor = mContext.getContentResolver().query(topRatedUri, null, null, null, null);

        assertFalse(updateFailed, cursor.moveToFirst());

        cursor.close();

        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        cursor = database.query(MovieContract.MoviesEntry.TABLE_NAME, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta de filmes";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        assertEquals(updateFailed, TestDbUtilities.CONTENT_VALUES_QUANTITY, cursor.getCount());

        cursor.close();
        database.close();
    }

    private long insertSingleMovie() {
        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues insertValues = TestDbUtilities.createTestMovieContentValues();

        long id = database.insert(MovieContract.MoviesEntry.TABLE_NAME, null, insertValues);

        database.close();

        return id;
    }

    private long insertSingleTrailerForMovie(long movieId) {
        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues insertValues = new ContentValues();
        insertValues.put(MovieContract.TrailersEntry.COLUMN_MOVIE_ID, movieId);
        insertValues.put(MovieContract.TrailersEntry.COLUMN_KEY, "abcd1234");
        insertValues.put(MovieContract.TrailersEntry.COLUMN_NAME, "Trailer Qualquer");

        long id = database.insert(MovieContract.TrailersEntry.TABLE_NAME, null, insertValues);

        database.close();

        return id;
    }

    private long insertSingleReviewForMovie(long movieId) {
        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        ContentValues insertValues = new ContentValues();
        insertValues.put(MovieContract.ReviewsEntry.COLUMN_MOVIE_ID, movieId);
        insertValues.put(MovieContract.ReviewsEntry.COLUMN_AUTHOR, "Nome do Autor");
        insertValues.put(MovieContract.ReviewsEntry.COLUMN_CONTENT, "Review do Autor");

        long id = database.insert(MovieContract.ReviewsEntry.TABLE_NAME, null, insertValues);

        database.close();

        return id;
    }

    private void deleteAllRecordsFromMoviesTable() {
        MovieDbHelper helper = new MovieDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        database.delete(MovieContract.MoviesEntry.TABLE_NAME, null, null);

        database.close();
    }
}