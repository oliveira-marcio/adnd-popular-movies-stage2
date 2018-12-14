package com.example.android.filmespopulares2.utilities;

import android.net.Uri;

import com.example.android.filmespopulares2.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Classe utilitária com métodos para estabelecer a conexão com a rede e construir as URL's
 * necessárias.
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    final static String TMDB_BASE_URL =
            "http://api.themoviedb.org/3/movie/";

    final static String TMDB_BASE_POSTER_URL =
            "http://image.tmdb.org/t/p/";

    // Paramêtros da TMDB API para indicar a ordenação dos filmes
    public final static String SORT_BY_POPULAR = "popular";
    public final static String SORT_BY_TOP_RATED = "top_rated";

    // Paramêtros da TMDB API para obter dados extras de um determinado filme
    public final static String TRAILERS_PARAM = "videos";
    public final static String REVIEWS_PARAM = "reviews";

    // Paramêtros da TMDB API para configurações dos resultados
    final static String APPID_PARAM = "api_key";
    final static String LANGUAGE_PARAM = "language";
    final static String LANGUAGE_VALUE = "pt-BR";

    /**
     * Constrói a URL para consultar os filmes do TMDB
     */
    public static URL buildMoviesUrl(String sortBy) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(sortBy)
                .appendQueryParameter(APPID_PARAM, BuildConfig.TMDB_API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, LANGUAGE_VALUE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Constrói a URL para consultar os extras de um filme do TMDB
     */
    public static URL buildMovieExtrasUrl(long movieId, String type) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(Long.toString(movieId))
                .appendPath(type)
                .appendQueryParameter(APPID_PARAM, BuildConfig.TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Contrói a URL para acessar o poster de um filme especificando o tamanho desejado
     */
    public static URL buildPosterUrl(String filename, String thumbSize) {
        Uri builtUri = Uri.parse(TMDB_BASE_POSTER_URL).buildUpon()
                .appendPath("w" + thumbSize)
                .appendEncodedPath(filename)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Retorna a string JSON com o resultado da consulta ao TMDB.
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}