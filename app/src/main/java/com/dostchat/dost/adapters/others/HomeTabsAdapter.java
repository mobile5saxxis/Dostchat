package com.dostchat.dost.adapters.others;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.util.SparseArray;

import com.dostchat.dost.fragments.home.CallsFragment;
import com.dostchat.dost.fragments.home.CameraFragment;
import com.dostchat.dost.fragments.home.ContactsFragment;
import com.dostchat.dost.fragments.home.ConversationsFragment;

/**
 * Created by Abderrahim El imame on 27/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class HomeTabsAdapter extends FragmentPagerAdapter {

    private SparseArray<Fragment> fragments;

    public HomeTabsAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>();
    }


    @Override
    public Fragment getItem(int position) {

        if (fragments.get(position) == null) {
            Fragment fragment;

            switch (position) {
                default:
                case 0:
                    fragment = new CameraFragment();
                    break;
                case 1:
                    fragment = new CallsFragment();
                    break;
                case 2:
                    fragment = new ConversationsFragment();
                    break;
                case 3:
                    fragment = new ContactsFragment();
                    break;
            }

            fragments.put(position, fragment);
        }

        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Calls";
            case 1:
                return "Discussion";
            case 2:
            default:
                return "Contacts";
        }
    }

    public Fragment getCameraFragment() {
        return fragments.get(0);
    }
}