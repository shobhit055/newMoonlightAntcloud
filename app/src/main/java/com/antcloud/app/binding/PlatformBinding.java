package com.antcloud.app.binding;

import android.content.Context;

import com.antcloud.app.binding.crypto.AndroidCryptoProvider;
import com.antcloud.app.nvstream.http.LimelightCryptoProvider;

public class PlatformBinding {
    public static LimelightCryptoProvider getCryptoProvider(Context c) {
        return new AndroidCryptoProvider(c);
    }
}
