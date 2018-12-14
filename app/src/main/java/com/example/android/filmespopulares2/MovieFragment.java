package com.example.android.filmespopulares2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.filmespopulares2.data.MovieContract;
import com.example.android.filmespopulares2.sync.MovieSyncTasks;
import com.example.android.filmespopulares2.sync.MovieSyncUtils;

public class MovieFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.MovieAdapterOnClickHandler {
    private static final String CATEGORY_INDEX_PARAM = "index";

    /**
     * De acordo com a categoria selecionada no TabLayout, o Fragmento será carregado com as
     * características correspondentes. Os arrays abaixo representam algumas delas e são acessadas
     * pelo mesmo índice das abas geradas no adapter do ViewPager
     * <p>
     * - mCategoryValues: parâmetros de ordenação dos filmes
     * - mActionValues: parâmetros de sincronização do BD com os dados da Web
     * - mErrorStringsIds: mensagens de erros para serem exibidas em caso de dados nulos no BD.
     */
    private final String[] mCategoryValues = new String[]{
            MovieContract.PATH_POPULAR,
            MovieContract.PATH_TOP_RATED,
            MovieContract.PATH_FAVORITES
    };

    private final String[] mActionValues = new String[]{
            MovieSyncTasks.ACTION_SYNC_POPULAR_MOVIES,
            MovieSyncTasks.ACTION_SYNC_TOP_RATED_MOVIES,
            null
    };

    private final int[] mErrorStringIds = new int[]{
            R.string.no_movies,
            R.string.no_movies,
            R.string.no_favorite_movies
    };

    public static final String[] MAIN_MOVIES_PROJECTION = {
            MovieContract.MoviesEntry._ID,
            MovieContract.MoviesEntry.COLUMN_POSTER
    };

    private int mLoaderId;

    private RecyclerView mMoviesList;
    private MovieAdapter mAdapter;
    private View mRootView;
    private TextView mEmptyStateTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mLoadingIndicator;

    private int mPosition = RecyclerView.NO_POSITION;

    // Classe estática necessária para inicializar um fragmento para uma determinada ordem de
    // filmes e passar o índice dessa categoria via Bundle para ser usado pelo próprio fragmento.
    public static MovieFragment newInstance(int index) {
        MovieFragment f = new MovieFragment();

        Bundle args = new Bundle();
        args.putInt(CATEGORY_INDEX_PARAM, index);
        f.setArguments(args);

        return f;
    }

    // Retorna o índice da ordenação dos filmes correspondente à instância do fragmento criada.
    public int getShownIndex() {
        return getArguments().getInt(CATEGORY_INDEX_PARAM, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLoaderId = getShownIndex();

        mRootView = inflater.inflate(R.layout.fragment_movie, container, false);

        initializeUIElements();

        if (!getLoaderManager().hasRunningLoaders()) {
            showLoading();
        }

        getLoaderManager().initLoader(mLoaderId, null, this);
        MovieSyncUtils.initializeMovies(getActivity(), mActionValues[getShownIndex()]);

        if (!hasInternetConnection()) {
            Toast.makeText(getActivity(), getString(R.string.no_internet_connection),
                    Toast.LENGTH_LONG).show();
        }

        return mRootView;
    }

    public void initializeUIElements() {
        mMoviesList = (RecyclerView) mRootView.findViewById(R.id.rv_movies);
        mEmptyStateTextView = (TextView) mRootView.findViewById(R.id.empty_view);
        mLoadingIndicator = (ProgressBar) mRootView.findViewById(R.id.loading_indicator);

        int pixelWidth = Integer.parseInt(getResources().getString(R.string.thumb_size));
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), calculateBestSpanCount(pixelWidth));

        mMoviesList.setLayoutManager(layoutManager);
        mMoviesList.setHasFixedSize(true);

        mAdapter = new MovieAdapter(getActivity(), this);
        mMoviesList.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);

        // Fragmento de filmes favoritos não precisa de um SwipeRefresh visto que não consulta a Web
        if (getShownIndex() < mCategoryValues.length - 1) {
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    reloadMovies();
                }
            });
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    /**
     * Calcula a quantidade ótima de colunas para o GridLayout de acordo com o tamanho da tela e
     * a largura dos posters solcitados
     */
    private int calculateBestSpanCount(int posterWidth) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float screenWidth = outMetrics.widthPixels;
        return Math.round(screenWidth / posterWidth);
    }

    private void showLoading() {
        mMoviesList.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showMoviesDataView() {
        mEmptyStateTextView.setVisibility(View.INVISIBLE);
        mMoviesList.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    private void showErrorMessage(int errorId) {
        mEmptyStateTextView.setText(errorId);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mMoviesList.setVisibility(View.INVISIBLE);
    }

    public boolean hasInternetConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected()) ? true : false;
    }

    public void reloadMovies() {
        mAdapter.swapCursor(null);
        mSwipeRefreshLayout.setRefreshing(true);
        mEmptyStateTextView.setVisibility(View.GONE);
        if (hasInternetConnection()) {
            getLoaderManager().restartLoader(mLoaderId, null, this);
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_internet_connection),
                    Toast.LENGTH_LONG).show();
        }

        MovieSyncUtils.startImmediateMoviesSync(getActivity(), mActionValues[getShownIndex()]);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortBy = mCategoryValues[getShownIndex()];
        Uri uri = MovieContract.MoviesEntry.buildMovieListUri(sortBy);
        return new CursorLoader(getActivity(),
                uri,
                MAIN_MOVIES_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLoadingIndicator.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mMoviesList.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) {
            showMoviesDataView();
        } else {
            showErrorMessage(mErrorStringIds[getShownIndex()]);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(long movieId, View imageView) {
        Intent movieDetailIntent = new Intent(getActivity(), DetailsActivity.class);
        movieDetailIntent.putExtra(Intent.EXTRA_TEXT, movieId);

        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(getActivity(), imageView,
                        ViewCompat.getTransitionName(imageView));
        startActivity(movieDetailIntent, optionsCompat.toBundle());
    }
}
