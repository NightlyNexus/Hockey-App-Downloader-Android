package com.nightlynexus.hockey.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.nightlynexus.hockey.R;
import com.nightlynexus.hockey.fragment.AbsTitleFragment;
import com.nightlynexus.hockey.fragment.AppsListFragment;
import com.nightlynexus.hockey.fragment.LoginFragment;
import com.nightlynexus.hockey.model.App;

import java.util.ArrayList;

public class LoginActivity extends ActionBarActivity {

    private static final int CONTAINER_RES_ID = R.id.container;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (savedInstanceState == null) {
            goToLogin();
        } else {
            setTitle();
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        setTitle();
                    }
                }
        );
    }

    private void setTitle() {
        final int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        final CharSequence title = backStackEntryCount == 0 ? getText(R.string.title_app_name)
                : findCurrentFragment().getTitle();
        setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(
                backStackEntryCount > 1);
    }

    private AbsTitleFragment findCurrentFragment() {
        return (AbsTitleFragment) getSupportFragmentManager()
                .findFragmentById(CONTAINER_RES_ID);
    }

    private void goToLogin() {
        final Fragment fragmentLogin = new LoginFragment();
        final Bundle bundleLogin = new Bundle();
        bundleLogin.putCharSequence(AbsTitleFragment.ARG_TITLE,
                getText(R.string.title_login));
        fragmentLogin.setArguments(bundleLogin);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(CONTAINER_RES_ID, fragmentLogin)
                .commit();
    }

    private void goToAppsList(ArrayList<App> appsList) {
        final Fragment fragmentAppsList = new AppsListFragment();
        final Bundle bundleAppsList = new Bundle();
        bundleAppsList.putString(LoginFragment.ARG_TITLE, getString(R.string.title_apps_list));
        bundleAppsList.putParcelableArrayList(AppsListFragment.ARG_APPS, appsList);
        fragmentAppsList.setArguments(bundleAppsList);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .add(CONTAINER_RES_ID, fragmentAppsList)
                .commit();
    }

    public void completeLogin(ArrayList<App> appsList) {
        doCompleteLogin(appsList);
    }

    private void doCompleteLogin(ArrayList<App> appsList) {
        getSupportFragmentManager().popBackStack();
        goToAppsList(appsList);
    }

    public void completeLogout() {
        doCompleteLogout();
    }

    private void doCompleteLogout() {
        getSupportFragmentManager().popBackStack();
        goToLogin();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                doOnBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        doOnBackPressed();
    }

    private void doOnBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }
}
