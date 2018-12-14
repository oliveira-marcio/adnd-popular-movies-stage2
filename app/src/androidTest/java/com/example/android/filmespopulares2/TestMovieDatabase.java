package com.example.android.filmespopulares2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.filmespopulares2.data.MovieContract;
import com.example.android.filmespopulares2.data.MovieDbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TestMovieDatabase {
    private final Context context = InstrumentationRegistry.getTargetContext();
    private SQLiteDatabase database;
    private MovieDbHelper dbHelper;

    private final String MOVIES_TABLE = MovieContract.MoviesEntry.TABLE_NAME;
    private final String TRAILERS_TABLE = MovieContract.TrailersEntry.TABLE_NAME;
    private final String REVIEWS_TABLE = MovieContract.ReviewsEntry.TABLE_NAME;

    @Before
    public void before() {
        try {
            dbHelper = new MovieDbHelper(context);
            context.deleteDatabase(MovieDbHelper.DATABASE_NAME);

            Method getWritableDatabase = SQLiteOpenHelper.class.getDeclaredMethod("getWritableDatabase");
            database = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        if (database != null)
            database.close();
    }

    /**
     * Testa a criação do DB e checa se todas as tabelas esperadas estão presentes bem como as
     * suas respectivas colunas.
     */
    @Test
    public void testCreateDb() {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MOVIES_TABLE);
        tableNameHashSet.add(TRAILERS_TABLE);
        tableNameHashSet.add(REVIEWS_TABLE);

        String error = "Database não pôde ser aberto";
        assertEquals(error,
                true,
                database.isOpen());

        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null);

        String errorInCreatingDatabase =
                "Erro: Database não foi criado corretamente.";
        assertTrue(errorInCreatingDatabase,
                tableNameCursor.moveToFirst());
        do {
            tableNameHashSet.remove(tableNameCursor.getString(0));
        } while (tableNameCursor.moveToNext());

        assertTrue("Erro: Database criado sem as tabelas esperadas.",
                tableNameHashSet.isEmpty());

        tableNameCursor.close();

        final ArrayList<String> tableNames = new ArrayList<>();
        tableNames.add(MOVIES_TABLE);
        tableNames.add(TRAILERS_TABLE);
        tableNames.add(REVIEWS_TABLE);

        final List<HashSet<String>> tableColumnsHashSetArray = new ArrayList<HashSet<String>>();

        for (int i = 0; i < tableNames.size(); i++) {
            tableColumnsHashSetArray.add(new HashSet<String>());
        }

        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry._ID);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_AVERAGE_RATING);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_POPULAR_ORDER);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_POSTER);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_RATINGS_COUNT);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_SYNOPSIS);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_TOP_RATED_ORDER);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_TITLE);
        tableColumnsHashSetArray.get(0).add(MovieContract.MoviesEntry.COLUMN_FAVORITE);

        tableColumnsHashSetArray.get(1).add(MovieContract.TrailersEntry._ID);
        tableColumnsHashSetArray.get(1).add(MovieContract.TrailersEntry.COLUMN_KEY);
        tableColumnsHashSetArray.get(1).add(MovieContract.TrailersEntry.COLUMN_MOVIE_ID);
        tableColumnsHashSetArray.get(1).add(MovieContract.TrailersEntry.COLUMN_NAME);

        tableColumnsHashSetArray.get(2).add(MovieContract.ReviewsEntry._ID);
        tableColumnsHashSetArray.get(2).add(MovieContract.ReviewsEntry.COLUMN_AUTHOR);
        tableColumnsHashSetArray.get(2).add(MovieContract.ReviewsEntry.COLUMN_CONTENT);
        tableColumnsHashSetArray.get(2).add(MovieContract.ReviewsEntry.COLUMN_MOVIE_ID);

        for (int i = 0; i < tableNames.size(); i++) {
            tableNameCursor = database.query(tableNames.get(i), null, null, null, null, null, null);
            String[] tableColumnNames = tableNameCursor.getColumnNames();
            tableNameCursor.close();

            for (String tableColumnName : tableColumnNames) {
                tableColumnsHashSetArray.get(i).remove(tableColumnName);
            }

            assertTrue("Erro: Tabela '" + tableNames.get(i) + "' criada sem as colunas esperadas.",
                    tableColumnsHashSetArray.get(i).isEmpty());
        }
    }

    /**
     * Testa a inclusão de um único registro na tabela de filmes e checa se os dados foram
     * armazenados conforme esperado
     */
    @Test
    public void testInsertSingleRecordIntoMovieTable() {
        ContentValues testValues = TestDbUtilities.createTestMovieContentValues();

        long rowId = database.insert(MOVIES_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        Cursor cursor = database.query(MOVIES_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de filmes";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores de filmes atuais não batem com o esperado.";
        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testValues);

        assertFalse("Erro: Mais de uma linha retornada da consulta de filmes",
                cursor.moveToNext());

        cursor.close();
    }

    /**
     * Insere um registro na tabela de filmes e checa se o ON CONFLICT da PK irá atualizar
     * os dados do registro atual ao tentar inserir outro filme com o mesmo ID.
     */
//    @Test
//    public void testConflictOnMoviesTable(){
//        testInsertSingleRecordIntoMovieTable();
//
//        // Força a criação de um registro com ID válido para a FK
//        ContentValues originalValue = TestDbUtilities.createTestMovieContentValues();
////        ContentValues testValues = TestDbUtilities.createAnotherTestMovieContentValues();
//        ContentValues testValues = new ContentValues();
//        testValues.put(MovieContract.MoviesEntry._ID, originalValue.getAsInteger(MovieContract.MoviesEntry._ID));
//        testValues.put(MovieContract.MoviesEntry.COLUMN_TITLE, originalValue.getAsString(MovieContract.MoviesEntry.COLUMN_TITLE));
//        testValues.put(MovieContract.MoviesEntry.COLUMN_SYNOPSIS, originalValue.getAsString(MovieContract.MoviesEntry.COLUMN_SYNOPSIS));
//        testValues.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, originalValue.getAsString(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE));
//        testValues.put(MovieContract.MoviesEntry.COLUMN_FAVORITE, 1);
//        originalValue.put(MovieContract.MoviesEntry.COLUMN_FAVORITE, 1);
//
//
//        long rowId = database.insert(MOVIES_TABLE, null, testValues);
//
//        int valueOfIdIfInsertFails = -1;
//        String insertFailed = "Não foi possível inserir no database";
//        assertTrue(insertFailed,
//                valueOfIdIfInsertFails != rowId);
//
//        Cursor cursor = database.query(MOVIES_TABLE, null, null, null, null, null, null);
//
//        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de filmes";
//        assertTrue(emptyQueryError,
//                cursor.moveToFirst());
//
//        String expectedResultDidntMatchActual =
//                "Valores do filme atual não batem com o esperado.";
//        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
//                cursor,
//                originalValue);
//
//        assertFalse("Erro: Mais de uma linha retornada da consulta de filmes",
//                cursor.moveToNext());
//
//        cursor.close();
//    }

    /**
     * Testa a inclusão de um único registro na tabela de trailers e checa se os dados foram
     * armazenados conforme esperado.
     * <p>
     * OBS: Foi necessário incluir antes um registro na tabela-pai (filmes).
     */
    @Test
    public void testInsertSingleRecordIntoTrailersTable() {
        testInsertSingleRecordIntoMovieTable();

        // Força a criação de um registro com ID válido para a FK
        ContentValues testValues = TestDbUtilities.createTestTrailerContentValues(true,
                TestDbUtilities.TRAILER_NAME_1);

        long rowId = database.insert(TRAILERS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        Cursor cursor = database.query(TRAILERS_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de trailers";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores de trailers atuais não batem com o esperado.";
        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testValues);

        assertFalse("Erro: Mais de uma linha retornada da consulta de trailers",
                cursor.moveToNext());

        cursor.close();
    }

    /**
     * Insere um registro na tabela de trailers e checa se a UNIQUE da tabela irá substituir
     * no registro atual o nome do trailer para o mesmo ID de filme já inserido.
     */
    @Test
    public void testUniqueOnTrailersTable() {
        testInsertSingleRecordIntoTrailersTable();

        // Força a criação de um registro com ID válido para a FK
        ContentValues testValues = TestDbUtilities.createTestTrailerContentValues(true,
                TestDbUtilities.TRAILER_NAME_2);

        long rowId = database.insert(TRAILERS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        Cursor cursor = database.query(TRAILERS_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de trailers";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores de trailers atuais não batem com o esperado.";
        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testValues);

        assertFalse("Erro: Mais de uma linha retornada da consulta de trailers",
                cursor.moveToNext());

        cursor.close();
    }

    @Test
    public void testBulkInsertTrailers() {
        testInsertSingleRecordIntoTrailersTable();

        ContentValues[] trailersValues = TestDbUtilities.createBulkInsertTestTrailersContentValues();

        int rowsInserted = 0;
        database.beginTransaction();
        for (ContentValues value : trailersValues) {
            long _id = database.insert(TRAILERS_TABLE, null, value);
            if (_id != -1) {
                rowsInserted++;
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();


        int expectedInserts = trailersValues.length;
        String insertFailed = "Não foi possível inserir no database";

        assertEquals(insertFailed,
                expectedInserts, rowsInserted);

        Cursor cursor = database.query(TRAILERS_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de trailers";

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
     * Testa a constraint de FK da tabela de trailers.
     */
    @Test
    public void testTrailersTableFkConstraint() {
        testInsertSingleRecordIntoMovieTable();

        // Força a criação de um registro com ID inválido para a FK
        ContentValues testValues = TestDbUtilities.createTestTrailerContentValues(false,
                TestDbUtilities.TRAILER_NAME_1);

        long rowId = database.insert(TRAILERS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Erro; Registro inserido indevidamente. A FK constraint falhou.";
        assertEquals(insertFailed,
                valueOfIdIfInsertFails,
                rowId);
    }

    /**
     * Testa a deleção em cascata da tabela de filmes para a tabela de trailers.
     */
    @Test
    public void testDeleteCascadeOnTrailersTable() {
        testInsertSingleRecordIntoMovieTable();

        ContentValues testValues = TestDbUtilities.createTestTrailerContentValues(true,
                TestDbUtilities.TRAILER_NAME_1);
        long rowId = database.insert(TRAILERS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        int rowsDeleted = database.delete(MOVIES_TABLE, null, null);
        int rowsExpectedToBeDeleted = 1;
        String deleteFailed = "Erro: Problemas na exclusão de dados da tabela de filmes.";
        assertEquals(deleteFailed,
                rowsExpectedToBeDeleted,
                rowsDeleted);

        Cursor cursor = database.query(TRAILERS_TABLE, null, null, null, null, null, null);
        String deleteCascadeError = "Erro: A deleção em cascata falhou para a tabela de trailers.";
        assertFalse(deleteCascadeError, cursor.moveToFirst());
        cursor.close();
    }

    /**
     * Testa a inclusão de um único registro na tabela de reviews e checa se os dados foram
     * armazenados conforme esperado.
     * <p>
     * OBS: Foi necessário incluir antes um registro na tabela-pai (filmes).
     */
    @Test
    public void testInsertSingleRecordIntoReviewsTable() {
        testInsertSingleRecordIntoMovieTable();

        // Força a criação de um registro com ID válido para a FK
        ContentValues testValues = TestDbUtilities.createTestReviewContentValues(true);

        long rowId = database.insert(REVIEWS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        Cursor cursor = database.query(REVIEWS_TABLE, null, null, null, null, null, null);

        String emptyQueryError = "Erro: Nenhuma linha retornada da consulta na tabela de reviews";
        assertTrue(emptyQueryError,
                cursor.moveToFirst());

        String expectedResultDidntMatchActual =
                "Valores de reviews atuais não batem com o esperado.";
        TestDbUtilities.validateCurrentRecord(expectedResultDidntMatchActual,
                cursor,
                testValues);

        assertFalse("Erro: Mais de uma linha retornada da consulta de reviews",
                cursor.moveToNext());

        cursor.close();
    }

    /**
     * Testa a constraint de FK da tabela de reviews.
     */
    @Test
    public void testReviewsTableFkConstraint() {
        testInsertSingleRecordIntoMovieTable();

        // Força a criação de um registro com ID inválido para a FK
        ContentValues testValues = TestDbUtilities.createTestReviewContentValues(false);

        long rowId = database.insert(REVIEWS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Erro; Registro inserido indevidamente. A FK constraint falhou.";
        assertEquals(insertFailed,
                valueOfIdIfInsertFails,
                rowId);
    }

    /**
     * Testa a deleção em cascata da tabela de filmes para a tabela de reviews.
     */
    @Test
    public void testDeleteCascadeOnReviewsTable() {
        testInsertSingleRecordIntoMovieTable();

        ContentValues testValues = TestDbUtilities.createTestReviewContentValues(true);
        long rowId = database.insert(REVIEWS_TABLE, null, testValues);

        int valueOfIdIfInsertFails = -1;
        String insertFailed = "Não foi possível inserir no database";
        assertTrue(insertFailed,
                valueOfIdIfInsertFails != rowId);

        int rowsDeleted = database.delete(MOVIES_TABLE, null, null);
        int rowsExpectedToBeDeleted = 1;
        String deleteFailed = "Erro: Problemas na exclusão de dados da tabela de filmes.";
        assertEquals(deleteFailed,
                rowsExpectedToBeDeleted,
                rowsDeleted);

        Cursor cursor = database.query(REVIEWS_TABLE, null, null, null, null, null, null);
        String deleteCascadeError = "Erro: A deleção em cascata falhou para a tabela de reviews.";
        assertFalse(deleteCascadeError, cursor.moveToFirst());
        cursor.close();
    }

}
