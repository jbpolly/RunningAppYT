package com.mysticraccoon.runningappyt.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mysticraccoon.runningappyt.R
import com.mysticraccoon.runningappyt.core.services.Polyline
import com.mysticraccoon.runningappyt.core.services.TrackingService
import com.mysticraccoon.runningappyt.core.utils.Constants.ACTION_PAUSE_SERVICE
import com.mysticraccoon.runningappyt.core.utils.Constants.ACTION_START_RESUME_SERVICE
import com.mysticraccoon.runningappyt.core.utils.Constants.ACTION_STOP_SERVICE
import com.mysticraccoon.runningappyt.core.utils.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.mysticraccoon.runningappyt.core.utils.Constants.MAP_ZOOM
import com.mysticraccoon.runningappyt.core.utils.Constants.POLYLINE_COLOR
import com.mysticraccoon.runningappyt.core.utils.Constants.POLYLINE_WIDTH
import com.mysticraccoon.runningappyt.core.utils.TrackingUtility
import com.mysticraccoon.runningappyt.databinding.FragmentTrackingBinding
import com.mysticraccoon.runningappyt.domain.Run
import com.mysticraccoon.runningappyt.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.currentCoroutineContext
import java.lang.Math.round
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class TrackingFragment : Fragment(), MenuProvider {

    private var _binding: FragmentTrackingBinding? = null
    val binding: FragmentTrackingBinding
        get() = _binding!!

    @Inject
    @Named("Fragment")
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu

        if (curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return false
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener(::stopRun)
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        //navigate back to run fragment
        findNavController().navigate(TrackingFragmentDirections.actionTrackingFragmentToRunFragment())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                setMyLocation()
            }
        }
        if(savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as? CancelTrackingDialog
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) { tracking ->
            updateTracking(tracking)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) { points ->
            pathPoints = points
            //new location from our service
            addLatestPolyline()
            //update camera to user
            moveCameraToUser()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocation() {
        map?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location?.let {
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.latitude,
                                it.longitude
                            ), MAP_ZOOM
                        )
                    )
                }
            }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            //we are paused
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking) {
            binding.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            //if we have at least one coordinate
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), MAP_ZOOM)
            )
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions =
                PolylineOptions().color(POLYLINE_COLOR).width(POLYLINE_WIDTH).addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            //if there is at least two points in the last polyline
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions =
                PolylineOptions().color(POLYLINE_COLOR).width(POLYLINE_WIDTH).add(preLastLatLng)
                    .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            //average speed in km/h
            //so get the distance in meters and divide by 1000 to convert to km
            //then divide by the time in millis converted to hours
            //because we want one decimal place for display we use the orund function and multiply by 10 and then divide by 10 again
            //this calculat the average speed, multiply by 10, round it (so there is no decimal place) and then we divide by then to grab only one decimal place
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                //if we choose a view from the fragment it will crash when the fragment is destroyed
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

}