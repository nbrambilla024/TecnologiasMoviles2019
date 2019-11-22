package com.login.mobi.loginapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class WeatherFragment extends Fragment {

    private CurrentWeatherService currentWeatherService;

    private View weatherContainer;
    private ProgressBar weatherProgressBar;
    private TextView temperature, location, weatherCondition;
    private ImageView weatherConditionIcon;
    private EditText locationField;
    private FloatingActionButton fab;

    private boolean fetchingWeather = false;
    private int textCount = 0;
    private String currentLocation = "Cordoba";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        currentWeatherService = new CurrentWeatherService(getActivity());

        weatherContainer = getActivity().findViewById(R.id.weather_container);
        weatherProgressBar = getActivity().findViewById(R.id.weather_progress_bar);
        temperature = getActivity().findViewById(R.id.temperature);
        location = getActivity().findViewById(R.id.location);
        weatherCondition = getActivity().findViewById(R.id.weather_condition);
        weatherConditionIcon = getActivity().findViewById(R.id.weather_condition_icon);
        locationField = getActivity().findViewById(R.id.location_field);
        fab = getActivity().findViewById(R.id.fab);

        locationField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                count = s.toString().trim().length();
                fab.setImageResource(count == 0 ? R.drawable.ic_refresh : R.drawable.ic_search);
                textCount = count;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textCount == 0) {
                    refreshWeather();
                } else {
                    searchForWeather(locationField.getText().toString());
                    locationField.setText("");
                }
            }
        });

        // Start a search for the default location
        searchForWeather(currentLocation);
    }

    public void onDestroy() {
        super.onDestroy();
        currentWeatherService.cancel();
    }

    private void refreshWeather() {
        if (fetchingWeather) {
            return;
        }
        searchForWeather(currentLocation);
    }

    private void searchForWeather(@NonNull final String location) {
        toggleProgress(true);
        fetchingWeather = true;
        currentWeatherService.getCurrentWeather(location, currentWeatherCallback);
    }

    private void toggleProgress(final boolean showProgress) {
        weatherContainer.setVisibility(showProgress ? View.GONE : View.VISIBLE);
        weatherProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }

    private final CurrentWeatherService.CurrentWeatherCallback currentWeatherCallback = new CurrentWeatherService.CurrentWeatherCallback() {

        @Override
        public void onCurrentWeather(@NonNull CurrentWeather currentWeather) {
            currentLocation = currentWeather.location;
            temperature.setText(String.valueOf(currentWeather.getTempCelsius()));
            location.setText(currentWeather.location);
            weatherCondition.setText(currentWeather.weatherCondition);
            weatherConditionIcon.setImageResource(CurrentWeatherUtils.getWeatherIconResId
                    (currentWeather.conditionId));
            toggleProgress(false);
            fetchingWeather = false;
        }

        @Override
        public void onError(@Nullable Exception exception) {
            toggleProgress(false);
            fetchingWeather = false;
            Toast.makeText(getActivity(), "There was an error fetching weather, " +
                    "try again.", Toast.LENGTH_SHORT).show();
        }
    };
}
