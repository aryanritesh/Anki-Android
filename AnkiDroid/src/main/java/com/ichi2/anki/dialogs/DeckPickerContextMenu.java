/****************************************************************************************
 * Copyright (c) 2015 Timothy Rae <perceptualchaos2@gmail.com>                          *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ichi2.anki.AnkiActivity;
import com.ichi2.anki.CardBrowser;
import com.ichi2.anki.CollectionHelper;
import com.ichi2.anki.DeckPicker;
import com.ichi2.anki.NavigationDrawerActivity;
import com.ichi2.anki.R;
import com.ichi2.anki.StudyOptionsFragment;
import com.ichi2.anki.analytics.AnalyticsDialogFragment;
import com.ichi2.anki.dialogs.customstudy.CustomStudyDialog;
import com.ichi2.libanki.Collection;
import com.ichi2.utils.FragmentFactoryUtils;
import com.ichi2.utils.HashUtil;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentFactory;
import timber.log.Timber;

import static com.ichi2.anim.ActivityTransitionAnimation.Direction.START;
import static com.ichi2.anki.NavigationDrawerActivity.REQUEST_BROWSE_CARDS;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class DeckPickerContextMenu extends AnalyticsDialogFragment {
    /**
     * Context Menus
     */
    private static final int CONTEXT_MENU_RENAME_DECK = 0;
    private static final int CONTEXT_MENU_DECK_OPTIONS = 1;
    private static final int CONTEXT_MENU_CUSTOM_STUDY = 2;
    private static final int CONTEXT_MENU_DELETE_DECK = 3;
    private static final int CONTEXT_MENU_EXPORT_DECK = 4;
    private static final int CONTEXT_MENU_UNBURY = 5;
    private static final int CONTEXT_MENU_CUSTOM_STUDY_REBUILD = 6;
    private static final int CONTEXT_MENU_CUSTOM_STUDY_EMPTY = 7;
    private static final int CONTEXT_MENU_CREATE_SUBDECK = 8;
    private static final int CONTEXT_MENU_CREATE_SHORTCUT = 9;
    private static final int CONTEXT_MENU_BROWSE_CARDS = 10;
    @Retention(SOURCE)
    @IntDef( {CONTEXT_MENU_RENAME_DECK,
            CONTEXT_MENU_DECK_OPTIONS,
            CONTEXT_MENU_CUSTOM_STUDY,
            CONTEXT_MENU_DELETE_DECK,
            CONTEXT_MENU_EXPORT_DECK,
            CONTEXT_MENU_UNBURY,
            CONTEXT_MENU_CUSTOM_STUDY_REBUILD,
            CONTEXT_MENU_CUSTOM_STUDY_EMPTY,
            CONTEXT_MENU_CREATE_SUBDECK,
            CONTEXT_MENU_CREATE_SHORTCUT,
            CONTEXT_MENU_BROWSE_CARDS
    })
    public @interface DECK_PICKER_CONTEXT_MENU {}


    public static DeckPickerContextMenu newInstance(long did) {
        DeckPickerContextMenu f = new DeckPickerContextMenu();
        Bundle args = new Bundle();
        args.putLong("did", did);
        f.setArguments(args);
        return f;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long did = getArguments().getLong("did");
        String title = CollectionHelper.getInstance().getCol(getContext()).getDecks().name(did);
        int[] itemIds = getListIds();
        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .cancelable(true)
                .autoDismiss(false)
                .itemsIds(itemIds)
                .items(ContextMenuHelper.getValuesFromKeys(getKeyValueMap(), itemIds))
                .itemsCallback(mContextMenuListener)
                .build();
    }


    private HashMap<Integer, String> getKeyValueMap() {
        Resources res = getResources();
        HashMap<Integer, String> keyValueMap = HashUtil.HashMapInit(9);
        keyValueMap.put(CONTEXT_MENU_RENAME_DECK, res.getString(R.string.rename_deck));
        keyValueMap.put(CONTEXT_MENU_DECK_OPTIONS, res.getString(R.string.menu__deck_options));
        keyValueMap.put(CONTEXT_MENU_CUSTOM_STUDY, res.getString(R.string.custom_study));
        keyValueMap.put(CONTEXT_MENU_DELETE_DECK, res.getString(R.string.contextmenu_deckpicker_delete_deck));
        keyValueMap.put(CONTEXT_MENU_EXPORT_DECK, res.getString(R.string.export_deck));
        keyValueMap.put(CONTEXT_MENU_UNBURY, res.getString(R.string.unbury));
        keyValueMap.put(CONTEXT_MENU_CUSTOM_STUDY_REBUILD, res.getString(R.string.rebuild_cram_label));
        keyValueMap.put(CONTEXT_MENU_CUSTOM_STUDY_EMPTY, res.getString(R.string.empty_cram_label));
        keyValueMap.put(CONTEXT_MENU_CREATE_SUBDECK, res.getString(R.string.create_subdeck));
        keyValueMap.put(CONTEXT_MENU_CREATE_SHORTCUT, res.getString(R.string.create_shortcut));
        keyValueMap.put(CONTEXT_MENU_BROWSE_CARDS, res.getString(R.string.browse_cards));
        return keyValueMap;
    }

    /**
     * Retrieve the list of ids to put in the context menu list
     * @return the ids of which values to show
     */
    private @DECK_PICKER_CONTEXT_MENU
    int[] getListIds() {
        Collection col = CollectionHelper.getInstance().getCol(getContext());
        long did = getArguments().getLong("did");
        boolean dyn = col.getDecks().isDyn(did);
        ArrayList<Integer> itemIds = new ArrayList<>(10); // init with our fixed list size for performance
        itemIds.add(CONTEXT_MENU_BROWSE_CARDS);
        if (dyn) {
            itemIds.add(CONTEXT_MENU_CUSTOM_STUDY_REBUILD);
            itemIds.add(CONTEXT_MENU_CUSTOM_STUDY_EMPTY);
        }
        itemIds.add(CONTEXT_MENU_RENAME_DECK);
        if (!dyn) {
            itemIds.add(CONTEXT_MENU_CREATE_SUBDECK);
        }
        itemIds.add(CONTEXT_MENU_DECK_OPTIONS);
        if (!dyn) {
            itemIds.add(CONTEXT_MENU_CUSTOM_STUDY);
        }
        itemIds.add(CONTEXT_MENU_DELETE_DECK);
        itemIds.add(CONTEXT_MENU_EXPORT_DECK);
        if (col.getSched().haveBuried(did)) {
            itemIds.add(CONTEXT_MENU_UNBURY);
        }
        itemIds.add(CONTEXT_MENU_CREATE_SHORTCUT);

        return ContextMenuHelper.integerListToArray(itemIds);
    }

    // Handle item selection on context menu which is shown when the user long-clicks on a deck
    private final MaterialDialog.ListCallback mContextMenuListener = (materialDialog, view, item, charSequence) -> {
        @DECK_PICKER_CONTEXT_MENU int id = view.getId();
        switch (id) {
            case CONTEXT_MENU_DELETE_DECK:
                Timber.i("Delete deck selected");
                ((DeckPicker) getActivity()).confirmDeckDeletion();
                break;

            case CONTEXT_MENU_DECK_OPTIONS:
                Timber.i("Open deck options selected");
                ((DeckPicker) getActivity()).showContextMenuDeckOptions();
                ((AnkiActivity) getActivity()).dismissAllDialogFragments();
                break;
            case CONTEXT_MENU_CUSTOM_STUDY: {
                Timber.i("Custom study option selected");
                long did = getArguments().getLong("did");

                final AnkiActivity ankiActivity = ((AnkiActivity) requireActivity());
                CustomStudyDialog d = FragmentFactoryUtils.instantiate(ankiActivity, CustomStudyDialog.class);
                d.withArguments(CustomStudyDialog.CONTEXT_MENU_STANDARD, did);
                ankiActivity.showDialogFragment(d);
                break;
            }
            case CONTEXT_MENU_CREATE_SHORTCUT:
                Timber.i("Create icon for a deck");
                ((DeckPicker) getActivity()).createIcon(getContext());
                break;

            case CONTEXT_MENU_RENAME_DECK:
                Timber.i("Rename deck selected");
                ((DeckPicker) getActivity()).renameDeckDialog();
                break;

            case CONTEXT_MENU_EXPORT_DECK:
                Timber.i("Export deck selected");
                ((DeckPicker) getActivity()).showContextMenuExportDialog();
                break;

            case CONTEXT_MENU_UNBURY: {
                Timber.i("Unbury deck selected");
                Collection col = CollectionHelper.getInstance().getCol(getContext());
                col.getSched().unburyCardsForDeck(getArguments().getLong("did"));
                ((StudyOptionsFragment.StudyOptionsListener) getActivity()).onRequireDeckListUpdate();
                ((AnkiActivity) getActivity()).dismissAllDialogFragments();
                break;
            }
            case CONTEXT_MENU_CUSTOM_STUDY_REBUILD: {
                Timber.i("Empty deck selected");
                ((DeckPicker) getActivity()).rebuildFiltered();
                ((AnkiActivity) getActivity()).dismissAllDialogFragments();
                break;
            }
            case CONTEXT_MENU_CUSTOM_STUDY_EMPTY: {
                Timber.i("Empty deck selected");
                ((DeckPicker) getActivity()).emptyFiltered();
                ((AnkiActivity) getActivity()).dismissAllDialogFragments();
                break;
            }
            case CONTEXT_MENU_CREATE_SUBDECK: {
                Timber.i("Create Subdeck selected");
                ((DeckPicker) getActivity()).createSubdeckDialog();
                break;
            }
            case CONTEXT_MENU_BROWSE_CARDS: {
                long did = getArguments().getLong("did");
                ((DeckPicker) getActivity()).getCol().getDecks().select(did);
                Intent intent = new Intent(getActivity(), CardBrowser.class);
                ((DeckPicker) getActivity()).startActivityForResultWithAnimation(intent, REQUEST_BROWSE_CARDS, START);
            }
        }
    };
}
