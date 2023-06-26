package com.pixeldev.pdfbook.activity


import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.AdManagerAdViewOptions
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.material.navigation.NavigationView
import com.pixeldev.pdfbook.R
import com.pixeldev.pdfbook.activity.ActivitySettings
import com.pixeldev.pdfbook.databinding.ActivityMainBinding
import com.pixeldev.pdfbook.fragments.RecentFragment
import com.pixeldev.pdfbook.helper.SharedPref
import java.util.Locale


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var sharedPref: SharedPref? = null
    var mAdView: AdView? = null
    private var binding: ActivityMainBinding? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val sharedPref2 = SharedPref(this)
        sharedPref = sharedPref2
        if (sharedPref2.loadNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        MobileAds.initialize(this) { initializationStatus: InitializationStatus? -> }
        mAdView = findViewById<View>(R.id.adView) as AdView
        mAdView!!.loadAd(AdRequest.Builder().build())


        //Bottom Navigation
        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_recent,
            R.id.navigation_category,
            R.id.navigation_featured,
            R.id.navigation_fav
        ).build()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment?
        val navController = navHostFragment!!.navController
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding!!.appBarMain2.mainContent.bottomNav, navController)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.drawer_about) {
            showDialogAbout()
            //drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        } else if (item.itemId == R.id.drawer_exit_app) {
            finish()
            //drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        } else if (item.itemId == R.id.drawer_rate) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
            //drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        } else if (item.itemId == R.id.drawer_recent) {
            val selectedFragment: Fragment = RecentFragment.newInstance()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment_activity_main, selectedFragment)
            transaction.commit()
            //   navView.getMenu().getItem(1).setChecked(true);
            // drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        } else if (item.itemId == R.id.drawer_request_submit) {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(
                Intent.EXTRA_EMAIL, arrayOf(resources.getString(R.string.email))
            )
            emailIntent.type = "text/plain"
            var best: ResolveInfo? = null
            for (info in packageManager.queryIntentActivities(emailIntent, 0)) {
                if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.lowercase(
                        Locale.getDefault()
                    ).contains("gmail")
                ) {
                    best = info
                }
            }
            if (best != null) {
                emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name)
            }
            startActivity(emailIntent)
            // drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        } else if (item.itemId == R.id.drawer_setting) {
            startActivity(Intent(this, ActivitySettings::class.java))
            // drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        } else if (item.itemId == R.id.drawer_share) {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            intent.putExtra(
                Intent.EXTRA_TEXT,
                """${getString(R.string.share_text)}https://play.google.com/store/apps/details?id=$packageName
     """.trimIndent()
            )
            intent.type = "text/plain"
            startActivity(intent)
            //drawerLayout.closeDrawer((int) GravityCompat.START);
            return true
        }
        return true
    }

    private fun showDialogAbout() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(1)
        dialog.setContentView(R.layout.dialog_about)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = -2
        lp.height = -2
        (dialog.findViewById<View>(R.id.tv_version) as TextView).text =
            resources.getString(R.string.app_version)
        dialog.findViewById<View>(R.id.bt_getcode).setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }
        dialog.findViewById<View>(R.id.bt_close).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.bt_more_apps).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.more_apps))
                )
            )
        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun openDrawer() {
        (findViewById<View>(R.id.drawer_layout) as DrawerLayout).openDrawer(Gravity.LEFT)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_top_setting) {
            startActivity(Intent(this, ActivitySettings::class.java))
            return true
        } else if (item.itemId == R.id.action_searchBtn) {
            startActivity(Intent(this, BookSearch::class.java))
            return true
        } else if (item.itemId == R.id.menu_top_rate) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")
                    )
                )
                return true
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())
                    )
                )
                return true
            }
        } else if (item.itemId == R.id.menu_top_moreapps) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.more_apps))
                )
            )
            return true
        } else if (item.itemId == R.id.menu_top_share) {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra("android.intent.extra.SUBJECT", getString(R.string.app_name))
            intent.putExtra(
                Intent.EXTRA_TEXT, """
     ${getString(R.string.share_text)}
     https://play.google.com/store/apps/details?id=${packageName}
     """.trimIndent()
            )
            intent.type = "text/plain"
            startActivity(intent)
            return true
        } else if (item.itemId != R.id.menu_top_about) {
            return true
        } else {
            showDialogAbout()
            return true
        }
    }
}