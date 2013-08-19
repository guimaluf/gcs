/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is GURPS Character Sheet.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2002,
 * 2005-2008 the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.gcs.character;

import com.trollworks.gcs.app.GCSFonts;
import com.trollworks.ttk.border.BoxedDropShadowBorder;
import com.trollworks.ttk.text.TextDrawing;
import com.trollworks.ttk.text.TextUtility;
import com.trollworks.ttk.utility.GraphicsUtilities;
import com.trollworks.ttk.utility.LocalizedMessages;
import com.trollworks.ttk.widgets.ActionPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;

import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/** The notes panel. */
public class NotesPanel extends ActionPanel {
	private static String		MSG_NOTES;
	private static String		MSG_NOTES_TOOLTIP;
	private static String		MSG_NOTES_CONTINUED;
	/** The default action command generated by this panel. */
	public static final String	CMD_EDIT_NOTES	= "EditNotes";	//$NON-NLS-1$
	private static final String	NEWLINE			= "\n";		//$NON-NLS-1$
	private String				mNotes;

	static {
		LocalizedMessages.initialize(NotesPanel.class);
	}

	/**
	 * Creates a new {@link NotesPanel}.
	 * 
	 * @param notes The notes to display.
	 * @param continued Whether to use the "continued" title or not.
	 */
	public NotesPanel(String notes, boolean continued) {
		super();
		setBorder(new CompoundBorder(new BoxedDropShadowBorder(UIManager.getFont(GCSFonts.KEY_LABEL), continued ? MSG_NOTES_CONTINUED : MSG_NOTES), new EmptyBorder(0, 2, 0, 2)));
		setAlignmentY(-1f);
		setEnabled(true);
		setOpaque(true);
		setBackground(Color.white);
		setActionCommand(CMD_EDIT_NOTES);
		setToolTipText(MSG_NOTES_TOOLTIP);
		mNotes = TextUtility.standardizeLineEndings(notes);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					notifyActionListeners();
				}
			}
		});
	}

	/** @param width The width to wrap at. */
	public void setWrapWidth(int width) {
		mNotes = TextDrawing.wrapToPixelWidth(UIManager.getFont(GCSFonts.KEY_NOTES), null, mNotes, width);
	}

	/**
	 * @param height The maximum height allowed.
	 * @return The remaining text, or <code>null</code> if there isn't any.
	 */
	public String setMaxHeight(int height) {
		StringBuilder buffer = new StringBuilder();
		Insets insets = getInsets();
		int lineHeight = TextDrawing.getPreferredSize(UIManager.getFont(GCSFonts.KEY_NOTES), null, "Mg").height; //$NON-NLS-1$
		StringTokenizer tokenizer = new StringTokenizer(mNotes, NEWLINE, true);
		boolean wasReturn = false;

		height -= insets.top + insets.bottom;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			if (NEWLINE.equals(token)) {
				if (wasReturn) {
					height -= lineHeight;
				}
				wasReturn = true;
			} else {
				height -= lineHeight;
				wasReturn = false;
			}
			buffer.append(token);
			if (height < lineHeight) {
				boolean hasMore = tokenizer.hasMoreTokens();

				if (hasMore && NEWLINE.equals(tokenizer.nextToken())) {
					buffer.append('\n');
					hasMore = tokenizer.hasMoreTokens();
				}
				if (hasMore) {
					String notes = mNotes.substring(buffer.length());

					mNotes = buffer.toString();
					return notes;
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public Dimension getMinimumSize() {
		Insets insets = getInsets();
		int height = TextDrawing.getPreferredSize(UIManager.getFont(GCSFonts.KEY_NOTES), null, "Mg").height; //$NON-NLS-1$
		return new Dimension(insets.left + insets.right, height + insets.top + insets.bottom);
	}

	@Override
	public Dimension getPreferredSize() {
		Insets insets = getInsets();
		Dimension size = TextDrawing.getPreferredSize(UIManager.getFont(GCSFonts.KEY_NOTES), null, mNotes);
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		Dimension minSize = getMinimumSize();
		if (minSize.width > size.width) {
			size.width = minSize.width;
		}
		if (minSize.height > size.height) {
			size.height = minSize.height;
		}
		return size;
	}

	/** @param notes The notes to display. */
	public void setNotes(String notes) {
		mNotes = notes;
		revalidate();
	}

	@Override
	protected void paintComponent(Graphics gc) {
		super.paintComponent(GraphicsUtilities.prepare(gc));
		gc.setFont(UIManager.getFont(GCSFonts.KEY_NOTES));
		Rectangle bounds = getBounds();
		Insets insets = getInsets();
		bounds.x = insets.left;
		bounds.y = insets.top;
		bounds.width -= insets.left + insets.right;
		bounds.height -= insets.top + insets.bottom;
		TextDrawing.draw(gc, bounds, mNotes, SwingConstants.TOP, SwingConstants.LEFT);
	}
}
