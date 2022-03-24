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
            .userAttribute(AuthUserAttributeKey.email(), "email-address")
            .build()
        Amplify.Auth.signUp("username", "Password", options,
            { Log.i("Testing", "Sign up succeeded: $it") },
            { Log.e ("Testing", "Sign up failed", it) }
        )
    }
    
    private fun confirmSignUp() {
        Amplify.Auth.confirmSignUp(
            "username", "code received via email",
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
        Amplify.Auth.signIn("username", "Password",
            { _ ->
                Log.i("Testing", "Signed in")
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
    }
}