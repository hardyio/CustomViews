package com.yio.customview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.yio.customview.model.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<ViewModel> modelList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        modelList = new ArrayList<>();
        modelList.add(new ViewModel("仿全名K歌", R.layout.view_pager1));
        modelList.add(new ViewModel("仿中兴", R.layout.view_pager2));

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        TabViewPagerAdapter adapter = new TabViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    class TabViewPagerAdapter extends FragmentStatePagerAdapter {

        public TabViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return modelList.size();
        }

        @Override
        public Fragment getItem(int position) {
            PagerFragment pagerFragment = new PagerFragment();
            Bundle args = new Bundle();
            args.putInt("resource", modelList.get(position).getResource());
            pagerFragment.setArguments(args);
            return pagerFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return modelList.get(position).getTitle();
        }
    }
}
