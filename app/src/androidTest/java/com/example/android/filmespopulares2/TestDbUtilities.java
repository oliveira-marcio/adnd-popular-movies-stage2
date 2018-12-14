package com.example.android.filmespopulares2;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.filmespopulares2.data.MovieContract.*;

import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class TestDbUtilities {
    static int VALID_MOVIE_ID = 123;
    static int INVALID_MOVIE_ID = 1234;
    static String TRAILER_NAME_1 = "Nome do Trailer";
    static String TRAILER_NAME_2 = "Outro nome de trailer";

    static final int CONTENT_VALUES_QUANTITY = 5;

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);

            /* Test to see if the column is contained within the cursor */
            String columnNotFoundError = "Coluna '" + columnName + "' não encontrada. " + error;
            assertFalse(columnNotFoundError, index == -1);

            /* Test to see if the expected value equals the actual value (from the Cursor) */
            String expectedValue = entry.getValue().toString();
            String actualValue = valueCursor.getString(index);

            String valuesDontMatchError = "Valor atual '" + actualValue
                    + "' não bate com o valor esperado '" + expectedValue + "'. "
                    + error;

            assertEquals(valuesDontMatchError,
                    expectedValue,
                    actualValue);
        }
    }

    static ContentValues createTestMovieContentValues() {
        ContentValues testMovieValues = new ContentValues();

        testMovieValues.put(MoviesEntry._ID, VALID_MOVIE_ID);
        testMovieValues.put(MoviesEntry.COLUMN_TITLE, "Filme");
        testMovieValues.put(MoviesEntry.COLUMN_SYNOPSIS, "Teste");
        testMovieValues.put(MoviesEntry.COLUMN_AVERAGE_RATING, 1.1);
        testMovieValues.put(MoviesEntry.COLUMN_RATINGS_COUNT, 10);
        testMovieValues.put(MoviesEntry.COLUMN_RELEASE_DATE, "01/01/2017");
        testMovieValues.put(MoviesEntry.COLUMN_POPULAR_ORDER, 1);
        testMovieValues.put(MoviesEntry.COLUMN_TOP_RATED_ORDER, 2);
        testMovieValues.put(MoviesEntry.COLUMN_FAVORITE, 0);

        return testMovieValues;
    }

    static ContentValues[] createBulkInsertTestMovieContentValues() {
        ContentValues[] testMovieValues = new ContentValues[CONTENT_VALUES_QUANTITY];

        for (int i = 0; i < testMovieValues.length; i++) {
            testMovieValues[i] = new ContentValues();
            testMovieValues[i].put(MoviesEntry._ID, i);
            testMovieValues[i].put(MoviesEntry.COLUMN_TITLE, "Filme " + i);
            testMovieValues[i].put(MoviesEntry.COLUMN_SYNOPSIS, "Teste " + i);
            testMovieValues[i].put(MoviesEntry.COLUMN_AVERAGE_RATING, i);
            testMovieValues[i].put(MoviesEntry.COLUMN_RATINGS_COUNT, i);
            testMovieValues[i].put(MoviesEntry.COLUMN_RELEASE_DATE, "01/01/201" + i);
            if(i < CONTENT_VALUES_QUANTITY - 1) {
                testMovieValues[i].put(MoviesEntry.COLUMN_POPULAR_ORDER, i);
            }

            if (i > 0) {
                testMovieValues[i].put(MoviesEntry.COLUMN_TOP_RATED_ORDER, i - 1);
            }
        }

        return testMovieValues;
    }

    static ContentValues[] createBulkInsertTestTrailersContentValues() {
        ContentValues[] testTrailersValues = new ContentValues[CONTENT_VALUES_QUANTITY];

        for (int i = 0; i < testTrailersValues.length; i++) {
            testTrailersValues[i] = new ContentValues();
            testTrailersValues[i].put(TrailersEntry.COLUMN_MOVIE_ID, VALID_MOVIE_ID);
            testTrailersValues[i].put(TrailersEntry.COLUMN_KEY, "abcd" + i);
            testTrailersValues[i].put(TrailersEntry.COLUMN_NAME, "Trailer " + i);
        }

        return testTrailersValues;
    }

    static ContentValues[] createBulkInsertTestReviewsContentValues() {
        ContentValues[] testReviewsValues = new ContentValues[CONTENT_VALUES_QUANTITY];

        for (int i = 0; i < testReviewsValues.length; i++) {
            testReviewsValues[i] = new ContentValues();
            testReviewsValues[i].put(ReviewsEntry.COLUMN_MOVIE_ID, VALID_MOVIE_ID);
            testReviewsValues[i].put(ReviewsEntry.COLUMN_AUTHOR, "Author " + i);
            testReviewsValues[i].put(ReviewsEntry.COLUMN_CONTENT, "content  " + i);
        }

        return testReviewsValues;
    }

    static ContentValues[] getPopularMoviesFromMultipleContentValues(){
        ContentValues[] testMovieValues = createBulkInsertTestMovieContentValues();
        ContentValues[] popularMovieValues = new ContentValues[CONTENT_VALUES_QUANTITY - 1];

        for (int i = 0; i < testMovieValues.length - 1; i++) {
            popularMovieValues[i] = testMovieValues[i];
        }

        return popularMovieValues;
    }

    static ContentValues[] getTopRatedMoviesFromMultipleContentValues(){
        ContentValues[] testMovieValues = createBulkInsertTestMovieContentValues();
        ContentValues[] topRatedMovieValues = new ContentValues[CONTENT_VALUES_QUANTITY - 1];

        for (int i = 0; i < testMovieValues.length - 1; i++) {
            topRatedMovieValues[i] = testMovieValues[i + 1];
        }

        return topRatedMovieValues;
    }


    static ContentValues createTestTrailerContentValues(boolean validMovieId, String trailerName) {
        ContentValues testTrailerValues = new ContentValues();

        testTrailerValues.put(TrailersEntry.COLUMN_MOVIE_ID,
                (validMovieId ? VALID_MOVIE_ID : INVALID_MOVIE_ID));
        testTrailerValues.put(TrailersEntry.COLUMN_KEY, "abcd1");
        testTrailerValues.put(TrailersEntry.COLUMN_NAME, trailerName);

        return testTrailerValues;
    }

    static ContentValues createTestReviewContentValues(boolean validMovieId) {
        ContentValues testReviewValues = new ContentValues();

        testReviewValues.put(ReviewsEntry.COLUMN_MOVIE_ID,
                (validMovieId ? VALID_MOVIE_ID : INVALID_MOVIE_ID));
        testReviewValues.put(ReviewsEntry.COLUMN_AUTHOR, "Nome do Autor");
        testReviewValues.put(ReviewsEntry.COLUMN_CONTENT, "Review do Autor");

        return testReviewValues;
    }

}
