package com.mysticraccoon.runningappyt.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.mysticraccoon.runningappyt.NavGraphDirections
import com.mysticraccoon.runningappyt.R
import com.mysticraccoon.runningappyt.core.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.mysticraccoon.runningappyt.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        navController = findNavController(R.id.nav_host_fragment_container)

        navController?.let { controller ->
            binding.bottomNavigationView.setupWithNavController(controller)
            binding.bottomNavigationView.setOnNavigationItemReselectedListener {
                
            }
            navigateToTrackingFragmentIfNeeded(intent, controller)
            appBarConfiguration =
                AppBarConfiguration.Builder(R.id.setupFragment)
                    .build()
            setupActionBarWithNavController(controller, appBarConfiguration)
        }


        navController?.addOnDestinationChangedListener{_, destination, _ ->
            when(destination.id){
                R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else ->  binding.bottomNavigationView.visibility = View.GONE
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController?.let { controller ->
            navigateToTrackingFragmentIfNeeded(intent, controller)
        }

    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?, navController: NavController){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navController.navigate(NavGraphDirections.actionGlobalTrackingFragment())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_container).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }



}