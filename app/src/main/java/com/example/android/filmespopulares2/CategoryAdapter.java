package com.example.android.filmespopulares2;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CategoryAdapter extends FragmentPagerAdapter {

    // Array com os títulos de todas as abas para o TabLayout de ordenação dos filmes.
    private String[] mCategoryLabels;
    private Context mContext;

    public CategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mCategoryLabels = mContext.getResources().getStringArray(R.array.category_labels);
    }

    @Override
    public Fragment getItem(int position) {
        return MovieFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mCategoryLabels.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mCategoryLabels[position];
    }
}
