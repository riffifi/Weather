package io.levs.weather;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class WeatherApplication extends Application {
    private static final String PREFS_NAME = "WeatherAppPreferences";

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
    
    private class ActivityLifecycleCallbacks implements android.app.Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(android.app.Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(android.app.Activity activity) {

        }

        @Override
        public void onActivityResumed(android.app.Activity activity) {

        }

        @Override
        public void onActivityPaused(android.app.Activity activity) {

        }

        @Override
        public void onActivityStopped(android.app.Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(android.app.Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(android.app.Activity activity) {

        }
    }
}
