package dev.hassanabid.pizzafinder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.ui.core.*
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.res.imageResource
import androidx.ui.tooling.preview.Preview
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dev.hassanabid.android.architecture.service.PizzaFinderResponse
import dev.hassanabid.android.architecture.viewmodel.PizzaViewModel
import dev.hassanabid.android.architecture.viewmodel.ViewModelFactory
import dev.hassanabid.pizzafinder.utils.Constants
import dev.hassanabid.pizzafinder.utils.FetchAddressIntentService
import dev.hassanabid.pizzafinder.utils.imageResources
import dev.hassanabid.pizzafinder.utils.lightThemeColors

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private val ADDRESS_REQUESTED_KEY = "address-request-pending"
    private val LOCATION_ADDRESS_KEY = "location-address"

    lateinit var viewModel: PizzaViewModel
    private var restaurants: List<PizzaFinderResponse>? = null

    private var newPlace = "KLCC"

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var addressRequested = false
    private var addressOutput: String? = ""
    private lateinit var resultReceiver: AddressResultReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (applicationContext as MainApplication).pizzaFinderRepository
        viewModel = ViewModelProvider(this, ViewModelFactory(repository)).get(PizzaViewModel::class.java)

        resultReceiver = AddressResultReceiver(Handler())

        // Set defaults, then update using values stored in the Bundle.
        addressRequested = true
        updateValuesFromBundle(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }


    fun fetchRestList() {

        var lat = "3.156954"
        var lng = "101.7101143"

        if(lastLocation?.latitude != null && lastLocation?.longitude != null) {

            lat = "${lastLocation?.latitude}"
            lng = "${lastLocation?.longitude}"
        }
        Log.d(TAG, "location ${lastLocation?.latitude} ${lastLocation?.longitude}")
        viewModel.pizzaPlacesList("$lat", "$lng").observe(this, Observer {

            it.onSuccess {
                restaurants = it
                Log.d(TAG, "rest list : ${it.toString()}")
                setAppView()
                if(it.isEmpty()) {
                    showSnackbar(R.string.no_pizza)
                }
            }

        })
    }

    private fun setAppView() {

        setContent {
            MaterialTheme (colors = lightThemeColors) {
                FlexColumn {
                    inflexible {
                        AppTopBar()
                    }

                    flexible(flex = 1f) {
                        VerticalScroller {
                            Column {
                                Padding(top = 16.dp, left = 16.dp, right = 16.dp, bottom = 0.dp) {
                                    Text(
                                        text = "Pizza restaurants nearby $newPlace",
                                        style = (+themeTextStyle { subtitle1 }).withOpacity(0.87f)
                                    )
                                }
                                PizzaList(restaurants)
                            }
                        }
                    }
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getAddress()
        }
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        ADDRESS_REQUESTED_KEY.let {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(it)) {
                addressRequested = savedInstanceState.getBoolean(it)
            }
        }

        LOCATION_ADDRESS_KEY.let {
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(it)) {
                addressOutput = savedInstanceState.getString(it)
                displayAddressOutput()
            }
        }


    }

    fun displayAddressOutput(){

    }

    @SuppressLint("MissingPermission")
    private fun getAddress() {

        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->

            if(location != null) lastLocation = location

            fetchRestList()

            if (location == null) {
                Log.w(TAG, "onSuccess:null")
                return@addOnSuccessListener
            }


            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Log.e(TAG, "no geocoder service available")
                return@addOnSuccessListener
            }

            // If the user pressed the fetch address button before we had the location,
            // this will be set to true indicating that we should kick off the intent
            // service after fetching the location.
            if (addressRequested) startIntentService()
        }?.addOnFailureListener(this) { e -> Log.w(TAG, "getLastLocation:onFailure", e) }
    }

    private fun startIntentService() {

        val intent = Intent(this, FetchAddressIntentService::class.java).apply {
            putExtra(Constants.RECEIVER, resultReceiver)
            putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation)
        }
        Log.d(TAG, "latitude : ${lastLocation?.latitude} longitude: ${lastLocation?.longitude}")
        startService(intent)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState ?: return

        with(savedInstanceState) {
            // Save whether the address has been requested.
            putBoolean(ADDRESS_REQUESTED_KEY, addressRequested)

            // Save the address string.
            putString(LOCATION_ADDRESS_KEY, addressOutput)
        }

        super.onSaveInstanceState(savedInstanceState)
    }

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

            // Display the address string
            // or an error message sent from the intent service.
            val addressOutput = resultData?.getString(Constants.RESULT_DATA_KEY) ?: ""
            Log.d(TAG, "success - address : $addressOutput")
            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "success - address was found")
                newPlace = addressOutput
            }

            addressRequested = false

        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")

        if (requestCode != REQUEST_PERMISSIONS_REQUEST_CODE) return

        when {
            grantResults.isEmpty() ->
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> // Permission granted.
                getAddress()
            else -> {
                // Permission denied.
                showSnackbar(R.string.permission_denied_explanation)
            }

        }
    }

    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId),
            Snackbar.LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }


}

@Composable
fun PizzaList(restaurants: List<PizzaFinderResponse>?) {
    VerticalScroller() {
        Column {
            HeightSpacer(height = 16.dp)
            restaurants?.forEachIndexed {index, rest ->
                var address = "Not Available"
                rest.address?.let { address = it  }
                var resourceId = imageResources[0]
                if(index < imageResources.size)
                    resourceId = imageResources[index]
                PizzaListItem(rest.name, address, resourceId)
            }
            ListDivider()

        }
    }
}


@Composable
fun PizzaListItem(name: String, address: String, imageResourceId : Int){
    val image = +imageResource(imageResourceId)
    Padding(left = 16.dp, right = 16.dp) {
        FlexRow(crossAxisAlignment = CrossAxisAlignment.Center) {
            inflexible { 
                Container(width = 80.dp, height = 80.dp) {
                    Clip(RoundedCornerShape(4.dp)) {
                        DrawImage(image)
                    }
                }
            }
            expanded(1f) {

                Column(crossAxisSize = LayoutSize.Expand) {
                    Text(
                            text = name,
                            modifier = Spacing(16.dp),
                            style = +themeTextStyle { h6 })
                    Text(
                            text = address,
                            modifier = Spacing(16.dp, (-8.dp), 16.dp, 16.dp),
                            maxLines = 2,
                            style = (+themeTextStyle { body2 }).withOpacity(0.87f))
                }
            }

        }
    }
}

@Composable
private fun ListDivider() {
    Opacity(0.08f) {
        Divider(Spacing(top = 8.dp, bottom = 8.dp, left = 72.dp))
    }
}

@Composable
private fun AppTopBar() {
    FlexColumn {
        inflexible {
            TopAppBar(
                title = { Text(text = "PizzaFinder") }
               /* navigationIcon = {
                    VectorIm(R.drawable.ic_jetnews_logo) {
                        openDrawer()
                    }
                }*/
            )
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme ( colors = lightThemeColors) {
        AppTopBar()
        Padding(top = 64.dp, left = 16.dp, right = 16.dp, bottom = 8.dp) {
            Text(
                text = "Pizza restaurants nearby you",
                style = (+themeTextStyle { subtitle1 }).withOpacity(0.87f)
            )
        }
        PizzaList(emptyList())
    }


}


