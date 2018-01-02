package com.udeshcoffee.android.ui.main

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewCompat
import android.support.v4.widget.DrawerLayout
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import com.udeshcoffee.android.R
import com.udeshcoffee.android.addFragmentToActivity
import com.udeshcoffee.android.doTransaction
import com.udeshcoffee.android.getService
import com.udeshcoffee.android.ui.MiniPlayerActivity
import com.udeshcoffee.android.ui.main.equalizer.EqualizerFragment
import com.udeshcoffee.android.ui.main.equalizer.EqualizerPresenter
import com.udeshcoffee.android.ui.main.favorites.FavoritesFragment
import com.udeshcoffee.android.ui.main.favorites.FavoritesPresenter
import com.udeshcoffee.android.ui.main.library.LibraryFragment
import com.udeshcoffee.android.ui.main.playlist.PlaylistFragment
import com.udeshcoffee.android.ui.main.search.SearchFragment
import com.udeshcoffee.android.ui.main.search.SearchPresenter
import com.udeshcoffee.android.ui.settings.SettingsActivity
import com.udeshcoffee.android.utils.Injection
import com.udeshcoffee.android.utils.PreferenceUtil
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class MainActivity : MiniPlayerActivity(), NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener{

    override val TAG = "MainActivity"

    object Fragments {
        val FAVORITES = "favorites"
        val HOME = "home"
        val LIBRARY = "library"
        val PLAYLISTS = "playlists"
        val EQUALIZER = "equalizer"
        val SEARCH = "search"
        val ARTIST_DETAIL = "artist_detail"
        val ALBUM_DETAIL = "album_detail"
        val GENRE_DETAIL = "genre_detail"
        val PLAYLIST_DETAIL = "playlist_detail"
        val EDITOR = "editor"
    }

    object DetailFragments {
        val ARGUMENT_ID = "ARGUMENT_ID"
        val ARGUMENT_NAME = "ARGUMENT_NAME"
        val ARGUMENT_ARTIST_NAME = "ARGUMENT_ARTIST_NAME"
        val ARGUMENT_TRANSITION_NAME = "ARGUMENT_TRANSITION_NAME"
        val PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f
        val PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f
        val ALPHA_ANIMATIONS_DURATION = 200
    }

    private var loadListOnConnection = false
    private var pendingUri: Uri? = null
    private var mainContentItem : Int? = null

    // Pref
    //TODO Home - Change this to PreferenceUtil.APP_START_HOME
    private var prefAppStart: Int = PreferenceUtil.APP_START_LIB

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        drawer.setNavigationItemSelectedListener(this)
        prefAppStart = sharedPreferences.getString(PreferenceUtil.PREF_APP_START, "${PreferenceUtil.APP_START_LIB}").toInt()

        // Mini Player Fragment
        when (prefAppStart) {
        //TODO Home - Uncomment
//            PreferenceUtil.APP_START_HOME -> {
//                val homeFragment = supportFragmentManager.findFragmentByTag(Fragments.HOME)
//                        as HomeFragment? ?: HomeFragment().also {
//                    addFragmentToActivity(it, R.id.main_container, Fragments.HOME)
//                }
//                HomePresenter(homeFragment, Injection.provideMediaRepository(applicationContext))
//                drawer.setCheckedItem(R.id.navigation_home)
//                mainContentItem = R.id.navigation_home
//            }
            PreferenceUtil.APP_START_LIB -> {
                supportFragmentManager.findFragmentByTag(Fragments.LIBRARY)
                        as LibraryFragment? ?: LibraryFragment().also {
                    addFragmentToActivity(it, R.id.main_container, Fragments.LIBRARY)
                }
                drawer.setCheckedItem(R.id.navigation_library)
                mainContentItem = R.id.navigation_library
            }
        }

        intent?.let {
            handleIntent(it)
            this.intent = Intent()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            handleIntent(it)
            this.intent = Intent()
        }

    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    override fun onPause() {
        super.onPause()
        supportFragmentManager.removeOnBackStackChangedListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_search -> {
                val detailFragment = supportFragmentManager.findFragmentByTag(MainActivity.Fragments.SEARCH)
                        as SearchFragment? ?: SearchFragment()
                SearchPresenter(detailFragment, Injection.provideMediaRepository(applicationContext))
                doTransaction(R.id.main_container, detailFragment, MainActivity.Fragments.SEARCH)
//                mainContentItem = R.id.navigation_playlist
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleIntent(intent: Intent) {
        Single.fromCallable {
            when (intent.action) {
                Intent.ACTION_VIEW -> {
                    val service = getService()
                    if (service == null)
                        pendingUri = intent.data
                    else
                        service.initFile(intent.data)
                }
                Intent.ACTION_MAIN -> {
                    val service = getService()
                    if (service == null)
                        loadListOnConnection = true
                    else
                        service.loadLastListAndInit()
                }
            }
        }
                .delaySubscription(350, TimeUnit.MILLISECONDS)
                .subscribe()

    }

    fun openDrawerLayout() {
        drawerLayout.openDrawer(Gravity.START)
    }

    fun closeDrawerLayout() {
        drawerLayout.closeDrawer(Gravity.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == mainContentItem)
            return true
        when (item.itemId) {
            R.id.navigation_now_play -> {
                closeDrawerLayout()
                openNowPlay()
            }
            //TODO Home - Uncomment
//            R.id.navigation_home -> {
//
//                if (prefAppStart == PreferenceUtil.APP_START_HOME) {
//                    supportFragmentManager.popBackStack()
//                    mainContentItem = R.id.navigation_home
//                    closeDrawerLayout()
//                } else {
//
//                    supportFragmentManager.popBackStack()
//                    val homeFragment = supportFragmentManager.findFragmentByTag(Fragments.HOME)
//                            as HomeFragment? ?: HomeFragment()
//                    HomePresenter(homeFragment, Injection.provideMediaRepository(applicationContext))
//                    doTransaction(R.id.main_container, homeFragment, MainActivity.Fragments.HOME)
//                    mainContentItem = R.id.navigation_home
//                    closeDrawerLayout()
//                }
//            }
            R.id.navigation_library -> {
                if (prefAppStart == PreferenceUtil.APP_START_LIB) {
                    supportFragmentManager.popBackStack()
                    mainContentItem = R.id.navigation_library
                    closeDrawerLayout()
                } else {
                    supportFragmentManager.popBackStack()
                    val libFragment = supportFragmentManager.findFragmentByTag(Fragments.LIBRARY)
                            as LibraryFragment? ?: LibraryFragment()
                    doTransaction(R.id.main_container, libFragment, MainActivity.Fragments.LIBRARY)
                    mainContentItem = R.id.navigation_library
                    closeDrawerLayout()
                }
            }
            R.id.navigation_playlist -> {
                supportFragmentManager.popBackStack()
                val detailFragment = supportFragmentManager.findFragmentByTag(MainActivity.Fragments.PLAYLISTS)
                        as PlaylistFragment? ?: PlaylistFragment()
                doTransaction(R.id.main_container, detailFragment, MainActivity.Fragments.PLAYLISTS)
                mainContentItem = R.id.navigation_playlist
                closeDrawerLayout()
            }
            R.id.navigation_equalizer -> {
                supportFragmentManager.popBackStack()
                val detailFragment = supportFragmentManager.findFragmentByTag(MainActivity.Fragments.EQUALIZER)
                        as EqualizerFragment? ?: EqualizerFragment()
                EqualizerPresenter(detailFragment)
                doTransaction(R.id.main_container, detailFragment, MainActivity.Fragments.EQUALIZER)
                mainContentItem = R.id.navigation_equalizer
                closeDrawerLayout()
            }
            R.id.navigation_favorites -> {
                supportFragmentManager.popBackStack()
                val detailFragment = supportFragmentManager.findFragmentByTag(MainActivity.Fragments.FAVORITES)
                        as FavoritesFragment? ?: FavoritesFragment()
                FavoritesPresenter(detailFragment, Injection.provideMediaRepository(this.applicationContext),
                        Injection.provideDataRepository(this.applicationContext))
                doTransaction(R.id.main_container, detailFragment, MainActivity.Fragments.FAVORITES)
                mainContentItem = R.id.navigation_favorites
                closeDrawerLayout()
            }
            R.id.navigation_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
            }
        }
        return true
    }

    override fun onBackStackChanged() {
        when (supportFragmentManager.backStackEntryCount) {
            0 -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                when (prefAppStart) {
                    //TODO Home - Uncomment
//                    PreferenceUtil.APP_START_HOME -> {
//                        drawer.setCheckedItem(R.id.navigation_home)
//                        mainContentItem = R . id . navigation_home
//                    }
                    PreferenceUtil.APP_START_LIB -> {
                        drawer.setCheckedItem(R.id.navigation_library)
                        mainContentItem = R . id . navigation_library
                    }
                }
            }
            else -> {
                when (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name){
                    Fragments.HOME -> { drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) }
                    Fragments.LIBRARY -> { drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) }
                    Fragments.PLAYLISTS -> { drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) }
                    Fragments.EQUALIZER -> { drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) }
                    Fragments.FAVORITES -> { drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) }
                    else -> { drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED) }
                }
            }
        }
        ViewCompat.requestApplyInsets(findViewById(R.id.main_container))
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ${supportFragmentManager.backStackEntryCount}")
        when {
            drawerLayout.isDrawerOpen(drawer) -> closeDrawerLayout()
            canChangeMainContent() -> when (supportFragmentManager.backStackEntryCount) {
                0 -> {
                    super.onBackPressed()
                }
                else -> {
                    supportFragmentManager.popBackStack()
                }
            }
            else -> super.onBackPressed()
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        super.onServiceConnected(p0, p1)
        pendingUri?.let {
            getService()?.initFile(it)
            pendingUri = null
        }
        if (loadListOnConnection) {
            getService()?.loadLastListAndInit()
            loadListOnConnection = false
        }
    }

    fun onHomeLibraryClick() {
        if (prefAppStart == PreferenceUtil.APP_START_LIB) {
            supportFragmentManager.popBackStack()
            mainContentItem = R.id.navigation_library
            drawer.setCheckedItem(R.id.navigation_library)
            closeDrawerLayout()
        } else {
            supportFragmentManager.popBackStack()
            val libFragment = supportFragmentManager.findFragmentByTag(Fragments.LIBRARY)
                    as LibraryFragment? ?: LibraryFragment()
            doTransaction(R.id.main_container, libFragment, MainActivity.Fragments.LIBRARY)
            mainContentItem = R.id.navigation_library
            drawer.setCheckedItem(R.id.navigation_library)
            closeDrawerLayout()
        }
    }
}