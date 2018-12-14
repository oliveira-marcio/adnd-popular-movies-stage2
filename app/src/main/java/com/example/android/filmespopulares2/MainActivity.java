package com.example.android.filmespopulares2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CategoryAdapter adapter = new CategoryAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.page_margin));
        viewPager.setPageMarginDrawable(R.color.colorPrimary);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        // Execução do Shared Element Transition interrompida. Será continuada após a conclusão do
        // Loader no MovieFragment e a conclusão do carregamento da respectiva imagem do filme
        // pelo Picasso no MovieAdapter.
        supportPostponeEnterTransition();
    }
}
