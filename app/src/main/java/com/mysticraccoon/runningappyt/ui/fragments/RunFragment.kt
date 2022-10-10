package com.mysticraccoon.runningappyt.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mysticraccoon.runningappyt.R
import com.mysticraccoon.runningappyt.core.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.mysticraccoon.runningappyt.core.utils.TrackingUtility
import com.mysticraccoon.runningappyt.databinding.FragmentRunBinding
import com.mysticraccoon.runningappyt.domain.SortType
import com.mysticraccoon.runningappyt.ui.adapters.RunAdapter
import com.mysticraccoon.runningappyt.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(){

    private var _binding: FragmentRunBinding? = null
    val binding: FragmentRunBinding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            checkLocationPermissions()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        when(viewModel.sortType) {
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        //pro tip: you can remove two when-blocks and use power of enums:
        //1) filterSpinner.setSelection(viewModel.sortType.ordinal)
        //2) viewModel.sortRuns(SortTypes.values()[position])
        //Also you have to be sure that enum and string-array have the same order
        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }

        binding.fab.setOnClickListener {
            checkLocationPermissions()
        }

        viewModel.runs.observe(viewLifecycleOwner){ runs ->
            runAdapter.submitList(runs)
        }
    }

    private fun setupRecyclerView(){
        binding.rvRuns.apply {
            runAdapter = RunAdapter()
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun checkLocationPermissions() {
        val backgroundLocationAllowed = if(TrackingUtility.runningAndroidQOrLater()){
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        }else true

        val foregroundLocationAllowed = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(foregroundLocationAllowed && backgroundLocationAllowed){
            //happy path
            //allow the behavior of the app
            //Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
            findNavController().navigate(RunFragmentDirections.actionRunFragmentToTrackingFragment())
        }else{
            //Android 11 enforces incremental location permission requests for apps that target API level 30
            //Any permission requests that include both foreground location and background location permissions will be ignored and result in an error message
            if(!foregroundLocationAllowed){
                //if it is the foreground one that is not allowed we must first request it
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                    displayFineLocationPermissionRationale()
                }else{
                    //should not display rationale then just request permission
                    requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }else{
                //if background one is not allowed we must request it
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                    displayBackgroundLocationPermissionRationale()

                }else{
                    //should not display rationale then just request permission
                    requestPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }

        }

    }

    private fun displayFineLocationPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Fine location permission")
            .setMessage("We need to access you location for the app o work")
            .setPositiveButton(getString(R.string.text_continue)) { _, _ ->
                requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                //todo permission denied show settings rationale
              //  AppSettingsDialog.Builder(this).build().show()
            }
            .show()
    }

    private fun displayBackgroundLocationPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Background location permission")
            .setMessage("We need to access your background location for the app o work")
            .setPositiveButton(getString(R.string.text_continue)) { _, _ ->
                requestPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                //todo permission denied show settings rationale
              //  AppSettingsDialog.Builder(this).build().show()
            }
            .show()
    }

}