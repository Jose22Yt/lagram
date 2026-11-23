package org.telegram.ui.lagram.player;

import android.content.Context;

import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AudioPlayerAlert;

/**
 * Entry point for the exteraless-style Material 3 Expressive Music Player in Lagram.
 */
public final class LagramPlayer {

    private LagramPlayer() {
    }

    public static BottomSheet create(Context context, Theme.ResourcesProvider resourcesProvider) {
        MessageObject playing = MediaController.getInstance().getPlayingMessageObject();
        if (playing != null && playing.isMusic()) {
            if (LagramPlayerSheet.instance != null) {
                LagramPlayerSheet.instance.dismissInternal();
            }
            return new LagramPlayerSheet(context, resourcesProvider);
        }
        return new AudioPlayerAlert(context, resourcesProvider);
    }
}
