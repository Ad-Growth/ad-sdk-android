package com.adgrowth.internal.helpers;

import com.adgrowth.internal.interfaces.FullScreenListener;

import java.util.ArrayList;
import java.util.List;

/**
 * this class handle full screen ads to pause another ads and
 * avoid to show two or more full screen ads at same time
 */
public class FullScreenEventManager {
    private static Integer adCurrentlyShown = null;
    private static List<FullScreenListener> fullScreenListeners = new ArrayList<>();

    public static void registerFullScreenListener(FullScreenListener listener) {
        fullScreenListeners.add(listener);
    }

    public static void unregisterFullScreenListener(FullScreenListener listener) {
        fullScreenListeners.remove(listener);
    }

    public static void notifyFullScreenShown(int instanceHash) {
        adCurrentlyShown = instanceHash;
        for (FullScreenListener listener : fullScreenListeners) {
            listener.onFullScreenShown(instanceHash);
        }
    }

    public static void notifyFullScreenDismissed() {
        adCurrentlyShown = null;
        for (FullScreenListener listener : fullScreenListeners) {
            listener.onFullScreenDismissed();
        }
    }

    public static boolean getShowPermission() {
        return adCurrentlyShown == null;
    }
}
