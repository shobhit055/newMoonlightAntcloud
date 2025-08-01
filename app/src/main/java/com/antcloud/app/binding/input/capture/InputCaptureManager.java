package com.antcloud.app.binding.input.capture;

import android.app.Activity;

import com.antcloud.app.BuildConfig;
import com.antcloud.app.ui.LimeLog;
import com.antcloud.app.R;
import com.antcloud.app.binding.input.evdev.EvdevCaptureProviderShim;
import com.antcloud.app.binding.input.evdev.EvdevListener;

public class InputCaptureManager {
    public static InputCaptureProvider getInputCaptureProvider(Activity activity, EvdevListener rootListener) {
        if (AndroidNativePointerCaptureProvider.isCaptureProviderSupported()) {
           // LimeLog.info("Using Android O+ native mouse capture");
            return new AndroidNativePointerCaptureProvider(activity, activity.findViewById(R.id.surfaceView));
        }
        // LineageOS implemented broken NVIDIA capture extensions, so avoid using them on root builds.
        else if (!BuildConfig.ROOT_BUILD && ShieldCaptureProvider.isCaptureProviderSupported()) {
            //LimeLog.info("Using NVIDIA mouse capture extension");
            return new ShieldCaptureProvider(activity);
        }
        else if (EvdevCaptureProviderShim.isCaptureProviderSupported()) {
          //  LimeLog.info("Using Evdev mouse capture");
            return EvdevCaptureProviderShim.createEvdevCaptureProvider(activity, rootListener);
        }
        else if (AndroidPointerIconCaptureProvider.isCaptureProviderSupported()) {
            // Android N's native capture can't capture over system UI elements
            // so we want to only use it if there's no other option.
         //   LimeLog.info("Using Android N+ pointer hiding");
            return new AndroidPointerIconCaptureProvider(activity, activity.findViewById(R.id.surfaceView));
        }
        else {
          //  LimeLog.info("Mouse capture not available");
            return new NullCaptureProvider();
        }
    }
}
