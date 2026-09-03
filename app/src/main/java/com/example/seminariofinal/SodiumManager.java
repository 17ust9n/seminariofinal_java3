package com.example.seminariofinal;

import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.SodiumAndroid;

public class SodiumManager {
    private static LazySodiumAndroid instance;

    public static synchronized LazySodiumAndroid getInstance() {
        if (instance == null) {
            instance = new LazySodiumAndroid(new SodiumAndroid());
        }
        return instance;
    }
}