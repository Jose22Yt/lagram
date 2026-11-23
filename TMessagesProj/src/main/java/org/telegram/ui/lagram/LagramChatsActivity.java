package org.telegram.ui.lagram;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

/**
 * Clean Chat Interface category screen segmented into 3 rounded cards:
 * 1. Chat Top Bar Styles (FlexyGram-inspired: Default, Signature, Fusion, Portrait Matte, Floating)
 * 2. iOS 3-Pill Bottom Input Bar (attach circle + text pill + send/mic circle)
 * 3. Message Tap Focus Animation & 3 Menu Styles (List, Tiles, Capsule)
 */
public class LagramChatsActivity extends UniversalFragment {

    private static final int ID_PREVIEW_HEADER = 100;
    private static final int ID_HEADER_STYLE = 101;

    private static final int ID_PREVIEW_BOTTOM_BAR = 200;
    private static final int ID_IOS27_BAR = 201;

    private static final int ID_PREVIEW_MSG_MENU = 300;
    private static final int ID_FOCUS_ENABLED = 301;
    private static final int ID_FOCUS_BLUR = 302;
    private static final int ID_FOCUS_STYLE = 303;

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.LagramCategoryChats);
    }

    @Override
    public View createView(Context context) {
        LagramConfig.ensureLoaded();
        View view = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        int bg = Theme.getColor(Theme.key_windowBackgroundGray);
        view.setBackgroundColor(bg);
        listView.setBackgroundColor(bg);
        return view;
    }

    private void refreshList() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        LagramConfig.ensureLoaded();

        // Card 1: Chat Top Bar Styles (FlexyGram-inspired)
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramChatHeaderStyle)));
        items.add(LagramRealPreviews.RealChatHeaderPreviewCell.Factory.of(ID_PREVIEW_HEADER));
        items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                ID_HEADER_STYLE,
                null,
                new String[]{
                        LocaleController.getString(R.string.LagramChatHeaderDefault),
                        LocaleController.getString(R.string.LagramChatHeaderSignature),
                        LocaleController.getString(R.string.LagramChatHeaderFusion),
                        LocaleController.getString(R.string.LagramChatHeaderPortrait),
                        LocaleController.getString(R.string.LagramChatHeaderFloating)
                },
                LagramConfig.chatHeaderStyle,
                idx -> {
                    LagramConfig.setChatHeaderStyle(idx);
                    refreshList();
                }));
        items.add(UItem.asShadow(-1, null));

        // Card 2: iOS 3-Pill Bottom Input Bar
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramIos27BottomBar)));
        items.add(LagramRealPreviews.RealInteractiveChatBoxPreviewCell.Factory.of(ID_PREVIEW_BOTTOM_BAR));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_IOS27_BAR, 0,
                LocaleController.getString(R.string.LagramIos27BottomBar),
                null,
                LagramConfig.ios27BottomBar));
        items.add(UItem.asShadow(-2, null));

        // Card 3: Message Tap Focus Animation + 3 Menu Styles
        items.add(UItem.asHeader(LocaleController.getString(R.string.LagramMessageFocusHeader)));
        items.add(LagramRealPreviews.RealMessageMenuPreviewCell.Factory.of(ID_PREVIEW_MSG_MENU));
        items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                ID_FOCUS_ENABLED, 0,
                LocaleController.getString(R.string.LagramMessageFocus),
                null,
                LagramConfig.messageFocusEnabled));
        if (LagramConfig.messageFocusEnabled) {
            items.add(OpexgramStyleWidgets.OpexSwitchCell.Factory.of(
                    ID_FOCUS_BLUR, 0,
                    LocaleController.getString(R.string.LagramMessageFocusBlur),
                    null,
                    LagramConfig.messageFocusBlur));
            items.add(OpexgramStyleWidgets.OpexCheckPillsCell.Factory.single(
                    ID_FOCUS_STYLE,
                    LocaleController.getString(R.string.LagramMessageFocusStyle),
                    new String[]{
                            LocaleController.getString(R.string.LagramFocusStyleList),
                            LocaleController.getString(R.string.LagramFocusStyleTiles),
                            LocaleController.getString(R.string.LagramFocusStyleCapsule)
                    },
                    LagramConfig.messageFocusStyle,
                    idx -> {
                        LagramConfig.setMessageFocusStyle(idx);
                        refreshList();
                    }));
        }
        items.add(UItem.asShadow(-3, LocaleController.getString(R.string.LagramMessageFocusInfo)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_IOS27_BAR:
                LagramConfig.toggleIos27BottomBar();
                refreshList();
                break;
            case ID_FOCUS_ENABLED:
                LagramConfig.toggleMessageFocusEnabled();
                refreshList();
                break;
            case ID_FOCUS_BLUR:
                LagramConfig.toggleMessageFocusBlur();
                refreshList();
                break;
        }
    }
}
