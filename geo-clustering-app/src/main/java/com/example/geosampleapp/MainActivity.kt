package com.example.geosampleapp

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.maplibre.view.AmplifyMapView
import com.amplifyframework.geo.maplibre.view.MapLibreView
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.VectorSource

class MainActivity : AppCompatActivity() {

    private val amplifyMapView by lazy {
        findViewById<AmplifyMapView>(R.id.mapView)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // signUp()
        // confirmSignUp()
        // signIn()
        setupMap()
    }
    
    private fun signUp() {
        val options = AuthSignUpOptions.builder()
            .userAttribute(AuthUserAttributeKey.email(), "eatoeric@amazon.com")
            .build()
        Amplify.Auth.signUp("user1", "Password123", options,
            { Log.i("Testing", "Sign up succeeded: $it") },
            { Log.e ("Testing", "Sign up failed", it) }
        )
    }
    
    private fun confirmSignUp() {
        Amplify.Auth.confirmSignUp(
            "user1", "425841",
            { result ->
                if (result.isSignUpComplete) {
                    Log.i("Testing", "Confirm signUp succeeded")
                } else {
                    Log.i("Testing","Confirm sign up not complete")
                }
            },
            { Log.e("Testing", "Failed to confirm sign up", it) }
        )
    }
    
    private fun signIn() {
        Amplify.Auth.signIn("user1", "Password123",
            { _ ->
                Log.i("Testing", "Signed in")
                /*this.runOnUiThread {
                    setupMap()
                }*/
            },
            { Log.e("Testing", "Failed to sign in", it) }
        )
    }
    
    private fun setupMap() {
        val mapLibreView = amplifyMapView.mapView

        mapLibreView.getMapAsync { map ->
            val seattle = LatLng(47.6160281982247, -122.32642111977668)
            map.cameraPosition = CameraPosition.Builder()
                .target(seattle)
                .zoom(13.0)
                .build()
        }

        mapLibreView.getStyle { map, style ->
            val spaceNeedle = LatLng(47.6205063, -122.3514661)
            mapLibreView.symbolManager.create(
                SymbolOptions()
                    .withIconImage("place")
                    .withLatLng(spaceNeedle)
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(spaceNeedle, 16.0))
        }

        /*amplifyMapView.onPlaceSelect { place, symbol ->
            // place is an instance of AmazonLocationPlace
            // symbol is an instance of Symbol from MapLibre
            Log.i("Testing", "The selected place is ${place.label}")
            Log.i("Testing", "It is located at ${place.coordinates}")
        }*/

        /*val searchQuery = "Amazon Go"
        Amplify.Geo.searchByText(searchQuery,
            {
                for (place in it.places) {
                    Log.i("Testing", place.toString())
                }
            },
            { Log.e("Testing", "Failed to search for $searchQuery", it) }
        )*/

        Log.i("Testing", "About to get map style sources.")
        mapLibreView.getStyle { _, mapStyle ->
            val geoJsonSources = mapStyle.sources.filterIsInstance<GeoJsonSource>()
            if (geoJsonSources.isEmpty()) {
                Log.i("Testing", "No GeoJson sources found.")
            } else {
                Log.i("Testing", "Number of GeoJson sources: ${geoJsonSources.size}")
                val geoJsonSourceId = geoJsonSources[0].id
                Log.i("Testing", "GeoJson source ID: $geoJsonSourceId")

                val clusterCircleLayer = CircleLayer("cluster-circles", geoJsonSourceId)
                clusterCircleLayer.setProperties(circleColor(ContextCompat.getColor(this, com.mapbox.mapboxsdk.R.color.mapbox_blue)),
                    circleRadius(18f))
                clusterCircleLayer.setFilter(Expression.has("point_count"))

                val clusterNumberLayer = SymbolLayer("cluster-numbers", geoJsonSourceId)
                clusterNumberLayer.setProperties(
                    textField(Expression.toString(Expression.get("point_count"))),
                    textSize(12f),
                    textFont(arrayOf("Arial Bold")),
                    textColor(Color.WHITE),
                    textIgnorePlacement(true),
                    textAllowOverlap(true)
                )
                mapStyle.addLayer(clusterCircleLayer)
                mapStyle.addLayer(clusterNumberLayer)
            }
        }
    }
}