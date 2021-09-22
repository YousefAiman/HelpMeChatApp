package app.alhamad.helpmechat;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import javax.annotation.Nullable;

public class tabAdapter extends FragmentStatePagerAdapter {
    List<Fragment> FragmentList;
    List<String> FragmentTitleList;
    Context context;

    public tabAdapter(@NonNull FragmentManager fm, int behavior, Context context, List<Fragment> FragmentList, List<String> FragmentTitleList) {
        super(fm, behavior);
        this.context = context;
        this.FragmentList = FragmentList;
        this.FragmentTitleList = FragmentTitleList;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentList.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return FragmentTitleList.get(position);
    }


    @Override
    public int getCount() {
        return FragmentList.size();
    }
}
