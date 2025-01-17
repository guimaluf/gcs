/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.preferences;

import com.trollworks.gcs.pdfview.PdfRef;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.border.EmptyBorder;
import com.trollworks.toolkit.ui.border.LineBorder;
import com.trollworks.toolkit.ui.layout.ColumnLayout;
import com.trollworks.toolkit.ui.preferences.PreferencePanel;
import com.trollworks.toolkit.ui.preferences.PreferencesWindow;
import com.trollworks.toolkit.ui.widget.BandedPanel;
import com.trollworks.toolkit.ui.widget.EditorField;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.text.IntegerFormatter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.text.DefaultFormatterFactory;

/** The page reference lookup preferences panel. */
public class ReferenceLookupPreferences extends PreferencePanel {
    @Localize("Page References")
    private static String TITLE;
    @Localize("Remove")
    private static String REMOVE;
    @Localize("If your PDF is opening up to the wrong page when opening page references, enter an offset here to compensate.")
    private static String OFFSET_FIELD_TOOLTIP;

    static {
        Localization.initialize();
    }

    private BandedPanel mPanel;

    /**
     * Creates a new {@link ReferenceLookupPreferences}.
     *
     * @param owner The owning {@link PreferencesWindow}.
     */
    public ReferenceLookupPreferences(PreferencesWindow owner) {
        super(TITLE, owner);
        setLayout(new BorderLayout());
        mPanel = new BandedPanel(TITLE);
        mPanel.setLayout(new ColumnLayout(4, 5, 0));
        mPanel.setBorder(new EmptyBorder(2, 5, 2, 5));
        mPanel.setOpaque(true);
        mPanel.setBackground(Color.WHITE);
        for (PdfRef ref : PdfRef.getKnown(false)) {
            JButton button = new JButton(REMOVE);
            UIUtilities.setOnlySize(button, button.getPreferredSize());
            button.addActionListener(event -> {
                ref.remove();
                Component[] children = mPanel.getComponents();
                for (int i = 0; i < children.length; i++) {
                    if (children[i] == button) {
                        for (int j = i + 4; --j >= i;) {
                            mPanel.remove(j);
                        }
                        mPanel.setSize(mPanel.getPreferredSize());
                        break;
                    }
                }
            });
            mPanel.add(button);
            JLabel idLabel = new JLabel(ref.getId(), SwingConstants.CENTER);
            idLabel.setBorder(new CompoundBorder(new LineBorder(), new EmptyBorder(1, 4, 1, 4)));
            idLabel.setOpaque(true);
            idLabel.setBackground(Color.YELLOW);
            mPanel.add(idLabel);
            EditorField field = new EditorField(new DefaultFormatterFactory(new IntegerFormatter(-9999, 9999, true)), event -> {
                ref.setPageToIndexOffset(((Integer) event.getNewValue()).intValue());
            }, SwingConstants.RIGHT, Integer.valueOf(ref.getPageToIndexOffset()), Integer.valueOf(-9999), OFFSET_FIELD_TOOLTIP);
            mPanel.add(field);
            mPanel.add(new JLabel(ref.getFile().getAbsolutePath()));
        }
        mPanel.setSize(mPanel.getPreferredSize());
        JScrollPane scroller      = new JScrollPane(mPanel);
        Dimension   preferredSize = scroller.getPreferredSize();
        if (preferredSize.height > 200) {
            preferredSize.height = 200;
        }
        scroller.setPreferredSize(preferredSize);
        add(scroller);
    }

    @Override
    public boolean isSetToDefaults() {
        return PdfRef.isSetToDefaults();
    }

    @Override
    public void reset() {
        PdfRef.reset();
        mPanel.removeAll();
        mPanel.setSize(mPanel.getPreferredSize());
    }
}
