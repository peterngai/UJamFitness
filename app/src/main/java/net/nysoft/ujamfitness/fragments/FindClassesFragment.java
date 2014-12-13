package net.nysoft.ujamfitness.fragments;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.nysoft.ujamfitness.R;
import net.nysoft.ujamfitness.data.UJamClass;
import net.nysoft.ujamfitness.data.UJamClassFetcher;
import net.nysoft.ujamfitness.data.UJamGym;
import net.nysoft.ujamfitness.data.UJamJSONParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;


/**
 * TODO: Write Javadoc for FindClassesFragment.
 *
 * @author pngai
 */
public class FindClassesFragment extends Fragment
        implements OnMarkerClickListener, OnMapClickListener, OnMapLoadedCallback, OnStreetViewPanoramaReadyCallback, View.OnClickListener,
        UJamJSONParser.OnTaskCompleted{

    private static final String TAG = "FindClassesFragment";

    private View _rootView;
    private MapView _mapView;
    private GoogleMap _map;
    private LocationManager _locationManager;
//    StreetViewPanoramaFragment _streetViewPanoramaFragment;
//    StreetViewPanorama _streetViewPanorama;
    private boolean _isInfoPanelPartialOpen = false;
    private UJamClassFetcher _ujamClassFetcher;

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {

        _rootView = inflater.inflate(R.layout.fragment_findclasses, container, false);

        // Gets the MapView from the XML layout and creates it
        _mapView = (MapView) _rootView.findViewById(R.id.map);
        _mapView.onCreate(savedInstanceState);
        _map = _mapView.getMap();
        _map.setOnMapLoadedCallback(this);
        _map.setOnMarkerClickListener(this);
        _map.setOnMapClickListener(this);


        String locationProvider = LocationManager.GPS_PROVIDER;
        _locationManager = (LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);

        // infoPanel
        LinearLayout infoPanelHeader = (LinearLayout)_rootView.findViewById(R.id.PaneHeaderView);
        infoPanelHeader.setOnClickListener(this);
        TextView addressTextView = (TextView)_rootView.findViewById(R.id.infoBox_gymloc);
        addressTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeNavToGym();
            }
        });
        ImageView navIcon = (ImageView)_rootView.findViewById(R.id.infoBox_nav_icon);
        navIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invokeNavToGym();
            }
        });

//        _streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetview);
//        _streetViewPanoramaFragment.onCreate(savedInstanceState);
//        _streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        return _rootView;
    }

    private void invokeNavToGym() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + _ujamClassFetcher.getSelectedGym().getAddress()));
        startActivity(intent);
    }

    private boolean ensureLocServices() {
        if(!_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Ask the user to enable GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Location Manager");
            builder.setMessage("We want to use your location, but GPS is disabled.\n"
                    +"Would you like to change these settings now?");
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Launch settings, allowing user to make a change
                            Intent i =
                                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    });
            builder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //No location service, no Activity
                            getActivity().finish();
                        }
                    });
            builder.create().show();
            return false;
        }
        return true;
    }

    private void setupMap() {

        if (ensureLocServices()) {

            // Gets to GoogleMap from the MapView and does initialization stuff
            _map.getUiSettings().setMyLocationButtonEnabled(false);
//            _map.getUiSettings().setCompassEnabled(true);
//            _map.getUiSettings().setZoomControlsEnabled(true);
            _map.setMyLocationEnabled(true);

            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            MapsInitializer.initialize(this.getActivity());

            Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(11)                   // Sets the zoom
                            //                .bearing(0)                // Sets the orientation of the camera to east
                            //                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

//        _map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            _map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onMapLoaded() {

        _ujamClassFetcher = new UJamClassFetcher(this);
        _ujamClassFetcher.invokeGymFetch();
    }

    @Override
    public void onTaskCompleted() {
        /*
         * After the list of gyms is loaded from JSON then place markers on the map
         */
        LinkedHashMap<Integer, UJamGym> ujamClasses = _ujamClassFetcher.get_gymList();

        UJamGym ujamGym;
        Integer ujamClassKey;
        MarkerOptions marker;
        for (Iterator<Integer> iter = ujamClasses.keySet().iterator(); iter.hasNext(); ) {
            ujamClassKey = iter.next();
            ujamGym = ujamClasses.get(ujamClassKey);
            marker = new MarkerOptions().position(new LatLng(ujamGym.getLatitude(), ujamGym.getLongitude()))
                    .title(ujamClassKey.toString());
            _map.addMarker(marker);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _mapView.onPause();
//        _locationManager.removeUpdates(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        _mapView.onResume();
        ////        _streetViewPanoramaFragment.onResume();

        setupMap();

//        //Register for updates
//        int minTime = 5000;
//        float minDistance = 0;
//        _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                minTime, minDistance, listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _mapView.onDestroy();
//        _streetViewPanoramaFragment.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _mapView.onLowMemory();
//        _streetViewPanoramaFragment.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        Log.d(TAG, "Marker clicked=" + marker.getId() + ":" + marker.getTitle() + ":" + marker.getSnippet());

//        if (_streetViewPanorama != null) {
//            _streetViewPanorama.setPosition(marker.getPosition());
//        }

        // look up location based on marker
        // set up string vars
        String instructor = "", day = "", time = "", gymName = "", gymLoc = "", gymTel = "";

        _ujamClassFetcher.set_selectedGym(new Integer(marker.getTitle()));
        UJamGym gym = _ujamClassFetcher.get_gymList().get(new Integer(marker.getTitle()));
        UJamClass uJamClass;

        if (_ujamClassFetcher.selectedGymHasMultiClasses()) {
            // iterate and gather instructor list, etc.., to present
            ArrayList<UJamClass> list = gym.getUjamClasses();
            StringBuffer buf = new StringBuffer();
            for (Iterator<UJamClass> iter = list.iterator(); iter.hasNext();) {
                uJamClass = iter.next();
                buf.append(uJamClass.getInstructor() + ", ");
            }
            instructor = buf.toString();
            instructor = instructor.substring(0, instructor.length() - 2);
            day = "More";
            time = "...";
        } else {
            uJamClass = gym.getUjamClasses().get(0);
            instructor = uJamClass.getInstructor();
            day = uJamClass.getDay();
            time = uJamClass.getTime();
        }
        gymName = gym.getName();
        gymLoc = gym.getAddress();
        gymTel = gym.getTelephone();

        TextView instructorTextView = (TextView) _rootView.findViewById(R.id.infoBox_instructor);
        TextView dayTextView = (TextView) _rootView.findViewById(R.id.infoBox_day);
        TextView timeTextView = (TextView) _rootView.findViewById(R.id.infoBox_time);
        TextView gymNameTextView = (TextView) _rootView.findViewById(R.id.infoBox_gymname);
        TextView gymLocTextView = (TextView) _rootView.findViewById(R.id.infoBox_gymloc);
        TextView gymTelTextView = (TextView) _rootView.findViewById(R.id.infoBox_gymphone);

        instructorTextView.setText(instructor, TextView.BufferType.NORMAL);
        dayTextView.setText(day);
        timeTextView.setText(time);
        gymNameTextView.setText(gymName);
        gymLocTextView.setText(gymLoc);
        gymTelTextView.setText(gymTel);

        // multi-class scrollview
        LinearLayout multiInstructorPanel = (LinearLayout) _rootView.findViewById(R.id.infoBox_multi_class_layout);
        if (_ujamClassFetcher.selectedGymHasMultiClasses()) {
            multiInstructorPanel.setVisibility(View.VISIBLE);
            fillInMultiClassPanel();
        } else {
            multiInstructorPanel.setVisibility(View.GONE);
        }

        // animate the opening of the info window
        LinearLayout infoBoxLayout = (LinearLayout)_rootView.findViewById(R.id.infobox_layout);
//        LinearLayout headerView = (LinearLayout) _rootView.findViewById(R.id.PaneHeaderView);
//        int height = infoBoxLayout.getHeight() - headerView.getHeight() -
//                (multiInstructorPanel.getVisibility() == View.VISIBLE ? multiInstructorPanel.getHeight() : 0);

        // hardcode offset -- TODO need to calculate instead
        float height = 226f + (multiInstructorPanel.getVisibility() == View.VISIBLE ? multiInstructorPanel.getHeight() : 0);
        ObjectAnimator anim = ObjectAnimator.ofFloat(infoBoxLayout, "translationY", height); // height - 30f);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setDuration(500l);
        anim.start();

        _isInfoPanelPartialOpen = true;

//        _streetViewPanoramaFragment.(new LatLng(37.77396, -121.9702431));

        return true;
    }

    private void fillInMultiClassPanel() {
        TextView multiInstructor0 = (TextView)_rootView.findViewById(R.id.infoBox_multi_instructor0);
        TextView multiDay0 = (TextView)_rootView.findViewById(R.id.infoBox_multi_day0);
        TextView multiTime0 = (TextView)_rootView.findViewById(R.id.infoBox_multi_time0);
        TextView multiInstructor1 = (TextView)_rootView.findViewById(R.id.infoBox_multi_instructor1);
        TextView multiDay1 = (TextView)_rootView.findViewById(R.id.infoBox_multi_day1);
        TextView multiTime1 = (TextView)_rootView.findViewById(R.id.infoBox_multi_time1);
        TextView multiInstructor2 = (TextView)_rootView.findViewById(R.id.infoBox_multi_instructor2);
        TextView multiDay2 = (TextView)_rootView.findViewById(R.id.infoBox_multi_day2);
        TextView multiTime2 = (TextView)_rootView.findViewById(R.id.infoBox_multi_time2);

        if (_ujamClassFetcher.getSelectedGym().getUjamClasses().size() < 3) {
            multiInstructor2.setVisibility(View.GONE);
            multiDay2.setVisibility(View.GONE);
            multiTime2.setVisibility(View.GONE);
        }

        // set up the mult instructors and day time views
        UJamClass subClass;
        int i = 0;
        for (Iterator<UJamClass> iter = _ujamClassFetcher.getSelectedGym().getUjamClasses().iterator(); iter.hasNext();) {
            subClass = iter.next();
            switch (i++) {
                case 0:
                    multiInstructor0.setText(subClass.getInstructor());
                    multiDay0.setText(subClass.getDay());
                    multiTime0.setText(subClass.getTime());
                    break;
                case 1:
                    multiInstructor1.setText(subClass.getInstructor());
                    multiDay1.setText(subClass.getDay());
                    multiTime1.setText(subClass.getTime());
                    break;
                case 2:
                    multiInstructor2.setText(subClass.getInstructor());
                    multiDay2.setText(subClass.getDay());
                    multiTime2.setText(subClass.getTime());
                    break;
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // user clicked outside on the map so lower the info panel, if open

        LinearLayout infoBoxLayout = (LinearLayout)_rootView.findViewById(R.id.infobox_layout);
        LinearLayout multiInstructorPanel = (LinearLayout) _rootView.findViewById(R.id.infoBox_multi_class_layout);

        // animate the opening of the info window
//        int height = infoBoxLayout.getHeight() - (multiInstructorPanel.getVisibility() == View.VISIBLE ? multiInstructorPanel.getHeight() : 0);
        // TODO need to calculate offset
        float height = 0f;
        if (!_ujamClassFetcher.selectedGymHasMultiClasses()) {
            height = 460f;
        } else {
            height = 917f;
        }

//        460f + (multiInstructorPanel.getVisibility() != View.VISIBLE ? multiInstructorPanel.getHeight() : 0);
        ObjectAnimator anim = ObjectAnimator.ofFloat(infoBoxLayout, "translationY", height);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setDuration(500l);
        anim.start();

        _isInfoPanelPartialOpen = false;
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
//        _streetViewPanorama = streetViewPanorama;
    }

    private void toggleMoreLess() {
        TextView dayTextView = (TextView) _rootView.findViewById(R.id.infoBox_day);
        if (_ujamClassFetcher.selectedGymHasMultiClasses() && !_isInfoPanelPartialOpen) {
            dayTextView.setText("Less");
        } else if (_ujamClassFetcher.selectedGymHasMultiClasses()) {
            dayTextView.setText("More");
        }
    }

    /*
    ** When clicking on the header infoPanel
     */
    @Override
    public void onClick(View v) {

        LinearLayout infoBoxLayout = (LinearLayout)_rootView.findViewById(R.id.infobox_layout);
        LinearLayout headerView = (LinearLayout) _rootView.findViewById(R.id.PaneHeaderView);

        int height = 0;
        if (!_isInfoPanelPartialOpen) {
            height = infoBoxLayout.getHeight() - headerView.getHeight() - 30;
            _isInfoPanelPartialOpen = true;
        } else {
            height = 0;
            _isInfoPanelPartialOpen = false;
        }
        toggleMoreLess();

        // animate the opening of the info window
        ObjectAnimator anim = ObjectAnimator.ofFloat(infoBoxLayout, "translationY", height);
        anim.setInterpolator(new OvershootInterpolator());
        anim.setDuration(500l);
        anim.start();
    }

}