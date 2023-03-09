package com.othman.locationReminder.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.othman.locationReminder.R
import com.othman.locationReminder.base.BaseFragment
import com.othman.locationReminder.base.NavigationCommand
import com.othman.locationReminder.databinding.FragmentSelectLocationBinding
import com.othman.locationReminder.locationreminders.savereminder.SaveReminderViewModel
import com.othman.locationReminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val REQUEST_LOCATION_PERMISSION = 1

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    var selectedLatLng: LatLng? = null
    lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TO DO: add the map setup implementation
//        TO DO: zoom to the user location after taking his permission
//        TO DO: add style to the map
//        TO DO: put a marker to location that the user selected


//        TO DO: call this function after the user confirms on the selected location
        binding.save.setOnClickListener {
            if (selectedLatLng != null)
                onLocationSelected()
        }
        val mapFragment: SupportMapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.latitude.value = selectedLatLng?.latitude
        _viewModel.longitude.value = selectedLatLng?.longitude
        _viewModel.reminderSelectedLocationStr.value = locationTitle
        _viewModel.navigationCommand.value =
            NavigationCommand.BackTo(R.id.saveReminderFragment)
        //        TO DO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TO DO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private var poiMarker: Marker? = null
    private var locationTitle: String? = null
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableMyLocation()
        setMapStyle(mMap)
        setPoiClick(mMap)
        setMapLongClick(mMap)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else
                Toast.makeText(
                    context,
                    getString(R.string.permission_denied_explanation),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun enableMyLocation() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                activity?.let {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf<String>(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
                return
            }
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(30.267680, 31.296564), 12f))
        mMap.isMyLocationEnabled = true
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            poiMarker?.remove()
            poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            locationTitle = poi.name
            selectedLatLng = poi.latLng
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            poiMarker?.remove()
            poiMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )

            locationTitle = context?.getString(R.string.dropped_pin)
            selectedLatLng = latLng
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            Log.e("setMapStyle", "Can't find style. Error: ", e)
        }
    }
}
