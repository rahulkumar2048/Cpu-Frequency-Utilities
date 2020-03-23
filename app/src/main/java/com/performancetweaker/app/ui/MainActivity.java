package com.performancetweaker.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.asksven.android.common.utils.SystemAppInstaller;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.performancetweaker.app.BuildConfig;
import com.performancetweaker.app.R;
import com.performancetweaker.app.ui.fragments.BuildPropEditorFragment;
import com.performancetweaker.app.ui.fragments.CpuFrequencyFragment;
import com.performancetweaker.app.ui.fragments.CpuHotplugFragment;
import com.performancetweaker.app.ui.fragments.GovernorTuningFragment;
import com.performancetweaker.app.ui.fragments.GpuControlFragment;
import com.performancetweaker.app.ui.fragments.IOControlFragment;
import com.performancetweaker.app.ui.fragments.SettingsFragment;
import com.performancetweaker.app.ui.fragments.TimeInStatesFragment;
import com.performancetweaker.app.ui.fragments.VirtualMemoryFragment;
import com.performancetweaker.app.ui.fragments.WakeLocksFragment;
import com.performancetweaker.app.utils.AdUtils;
import com.performancetweaker.app.utils.CPUHotplugUtils;
import com.performancetweaker.app.utils.GpuUtils;
import com.splunk.mint.Mint;
import com.stericson.RootTools.RootTools;

import static com.performancetweaker.app.PerfTweakerApplication.getAppContext;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private InterstitialAd mInterstitialAd;

    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;
    private NavigationView navigationView;
    private TextView appCompatibilityMessage;
    private ProgressBar progressBar;
    private GpuUtils gpuUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_layout_navbar);

        navigationView = findViewById(R.id.navigation);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        appCompatibilityMessage = findViewById(R.id.app_compatibility_status);
        progressBar = findViewById(R.id.loading_main);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        //disable the navigation bar initially
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, toolbar,
                R.string.settings, R.string.settings) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        navigationView.setNavigationItemSelectedListener(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        new Task().execute();
    }

    public void populateGui() {

        //enable navigation drawer
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        actionBar.setTitle("CPU");

        gpuUtils = GpuUtils.getInstance();

        //TODO add settings based on whether they are supported or not
        if (gpuUtils.isGpuFrequencyScalingSupported()) {
            MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_gpu);
            menuItem.setVisible(true);
        }
        if (CPUHotplugUtils.hasCpuHotplug()) {
            MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_cpu_hotplug);
            menuItem.setVisible(true);
        }

        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_cpu));
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Fragment fragment = null;

                switch (menuItem.getItemId()) {
                    case R.id.nav_cpu:
                        fragment = new CpuFrequencyFragment();
                        actionBar.setTitle(getString(R.string.cpu_frequency));
                        break;
                    case R.id.nav_tis:
                        fragment = new TimeInStatesFragment();
                        actionBar.setTitle(R.string.time_in_state);
                        break;
                    case R.id.nav_iocontrol:
                        fragment = new IOControlFragment();
                        actionBar.setTitle(getString(R.string.io));
                        break;
                    case R.id.nav_wakelocks:
                        fragment = new WakeLocksFragment();
                        actionBar.setTitle(getString(R.string.wakelocks));
                        break;
                    case R.id.nav_settings:
                        fragment = new SettingsFragment();
                        actionBar.setTitle(getString(R.string.settings));
                        break;
                    case R.id.nav_gpu:
                        fragment = new GpuControlFragment();
                        actionBar.setTitle(getString(R.string.gpu_frequency));
                        break;
                    case R.id.build_prop:
                        fragment = new BuildPropEditorFragment();
                        actionBar.setTitle(R.string.build_prop);
                        break;
                    case R.id.vm:
                        fragment = new VirtualMemoryFragment();
                        actionBar.setTitle(getString(R.string.vm));
                        break;
                    case R.id.nav_cpu_hotplug:
                        fragment = new CpuHotplugFragment();
                        actionBar.setTitle(getString(R.string.cpu_hotplug));
                        break;
                }
                if (fragment != null) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.animator.enter_anim, R.animator.exit_animation);
                    fragmentTransaction.replace(R.id.main_content, fragment).commitAllowingStateLoss();
                }
            }
        }, 400);

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().findFragmentByTag(GovernorTuningFragment.TAG) != null
                && getFragmentManager().findFragmentByTag(GovernorTuningFragment.TAG).isVisible()) {
            //To go back to cpu frequency fragment by pressing back button
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.main_content, new CpuFrequencyFragment())
//                    .commit();
        } else {
            //  Toast.makeText(getBaseContext(), "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

    private class Task extends AsyncTask<Void, Void, Void> {
        private boolean hasRoot, hasBusyBox;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            hasRoot = RootTools.isAccessGiven();
            hasBusyBox = RootTools.isBusyboxAvailable() || RootTools.findBinary("toybox");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (hasRoot && hasBusyBox) {
                populateGui();
            } else {

                progressBar.setVisibility(View.GONE);

                appCompatibilityMessage.setVisibility(View.VISIBLE);
                appCompatibilityMessage
                        .setText(!hasRoot ? "No root access found" : "No Busybox found");

                if (hasRoot) {
                    //TODO redirect to playstore for installing busybox
                    try {
                        startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri
                                        .parse("market://details?id=stericson.busybox")));
                    } catch (ActivityNotFoundException ignored) {
                    }
                }
            }
        }
    }
}
