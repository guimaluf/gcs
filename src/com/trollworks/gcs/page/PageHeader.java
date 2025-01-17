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

package com.trollworks.gcs.page;

import com.trollworks.gcs.app.GCSFonts;
import com.trollworks.toolkit.ui.widget.Label;
import com.trollworks.toolkit.utility.text.Text;

import java.awt.Color;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

/** A header within the page. */
public class PageHeader extends Label {
    /**
     * Creates a new {@link PageHeader}.
     *
     * @param title   The title to use.
     * @param tooltip The tooltip to use.
     */
    public PageHeader(String title, String tooltip) {
        super(title, SwingConstants.CENTER);
        setFont(UIManager.getFont(GCSFonts.KEY_LABEL));
        setForeground(Color.white);
        setToolTipText(Text.wrapPlainTextForToolTip(tooltip));
    }
}
