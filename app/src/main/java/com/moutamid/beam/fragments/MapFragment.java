package com.moutamid.beam.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moutamid.beam.utilis.Stash;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.moutamid.beam.R;
import com.moutamid.beam.databinding.FragmentMapBinding;
import com.moutamid.beam.models.UserModel;
import com.moutamid.beam.utilis.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {
    GoogleMap mMap;
    FragmentMapBinding binding;
    LatLng dropLocation;

    public MapFragment() {
        // Required empty public constructor
    }

    public MapFragment(LatLng dropLocation) {
        this.dropLocation = dropLocation;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(getLayoutInflater(), container, false);



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private final OnMapReadyCallback callback = googleMap -> {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        LatLng currentLatLng = new LatLng(userModel.location.lat, userModel.location.log);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f));
        getPath(currentLatLng, dropLocation);
    };


    private void getPath(LatLng currentLatLng, LatLng dropLocation) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + currentLatLng.latitude + "," + currentLatLng.longitude +
                "&destination=" + dropLocation.latitude + "," + dropLocation.longitude +
                "&key=AIzaSyAuIxeEpQQgN84bBitDRksZTcLHtIKSAeY";

        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.connect();

                // Read the response
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String response = buffer.toString();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedPolyline = overviewPolyline.getString("points");

                    List<LatLng> points = decodePolyline(encodedPolyline);

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(points)
                                    .width(5)
                                    .color(Color.BLUE);
                            currentPolyline = mMap.addPolyline(polylineOptions);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private Polyline currentPolyline;

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

}