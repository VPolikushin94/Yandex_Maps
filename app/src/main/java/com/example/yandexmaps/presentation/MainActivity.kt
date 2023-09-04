package com.example.yandexmaps.presentation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.yandexmaps.BuildConfig
import com.example.yandexmaps.R
import com.example.yandexmaps.domain.model.Place
import com.example.yandexmaps.util.BitmapConverter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.lang.RuntimeException


class MainActivity : AppCompatActivity()  {

    private lateinit var mapView: MapView
    private lateinit var viewModel: MapViewModel

    private lateinit var bsInfo: LinearLayout
    private lateinit var bsBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var tvScooterName: TextView
    private lateinit var ivCharge: ImageView
    private lateinit var tvChargePercents: TextView
    private lateinit var buttonMinutes: RadioButton
    private lateinit var buttonBook: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapKit: MapKit

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
        MapKitFactory.initialize(this)
        mapKit = MapKitFactory.getInstance()

        viewModel = ViewModelProvider(this)[MapViewModel::class.java]

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setViews()

        bsBehavior = BottomSheetBehavior.from(bsInfo)
        bsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        buttonMinutes.isChecked = true

        mapView.map.addInputListener(inputListener)

        mapView.map.move(
            CameraPosition(Point(55.080650, 38.801548), 16f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
        Log.d("PLACES", viewModel.placeList.toString())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableUserLocation()

        addMarks(viewModel.placeList)

        
    }


    private fun setViews() {
        mapView = findViewById(R.id.map_view)
        bsInfo = findViewById(R.id.bs_marker_info)
        tvScooterName = findViewById(R.id.tv_bs_scooter_name)
        ivCharge = findViewById(R.id.iv_bs_charge)
        tvChargePercents = findViewById(R.id.tv_bs_charge_percents)
        buttonMinutes = findViewById(R.id.bs_button_minutes)
        buttonBook = findViewById(R.id.button_book)
    }

    private fun addMarks(places: List<Place>) {
        places.forEach { place ->
            val chargeIconId = getChargeIconId(place, false)

            val imageProvider =
                ImageProvider.fromBitmap(BitmapConverter.vectorToBitmap(this, chargeIconId))
            val mark = mapView.map.mapObjects.addPlacemark(place.address, imageProvider)
            mark.userData = place
            mark.addTapListener(markTapListener)
        }
    }

    private val inputListener = object : InputListener {
        override fun onMapTap(p0: Map, p1: Point) {
            bsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        override fun onMapLongTap(p0: Map, p1: Point) {

        }
    }

    private val markTapListener = MapObjectTapListener { mark, point ->
        bsBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val place = mark.userData as Place
        tvScooterName.text = place.name
        tvChargePercents.text = getString(R.string.charge_percents, place.charge)

        val chargeIconId = getChargeIconId(place, true)
        ivCharge.setImageResource(chargeIconId)

        mapView.map.move(
            CameraPosition(Point(point.latitude - 0.0005f, point.longitude), 18f, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
        true
    }

    private fun getChargeIconId(place: Place, isBottomSheetIcon: Boolean): Int {
        val chargeLvl = viewModel.getChargeLvl(place.charge)
        return if (isBottomSheetIcon) {
            when (chargeLvl) {
                DISCHARGED -> R.drawable.ic_discharged_bs
                CHARGED_HALF -> R.drawable.ic_charged_half_bs
                CHARGED -> R.drawable.ic_charged_bs
                else -> throw RuntimeException("Unknown charge level")
            }
        } else {
            when (chargeLvl) {
                DISCHARGED -> R.drawable.ic_discharged
                CHARGED_HALF -> R.drawable.ic_charged_half
                CHARGED -> R.drawable.ic_charged
                else -> throw RuntimeException("Unknown charge level")
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun enableUserLocation() {
        if (isPermissionGranted()) {
            val userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
            userLocationLayer.isVisible = true

            moveCameraToCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableUserLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if(location != null) {
                mapView.map.move(
                    CameraPosition(Point(location.latitude, location.longitude), 17f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 2f),
                    null
                )
            }
        }
    }

    override fun onStart() {
        MapKitFactory.getInstance().onStart()
        mapView.onStart();
        super.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    companion object {
        const val DISCHARGED = 0
        const val CHARGED_HALF = 1
        const val CHARGED = 2

        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}