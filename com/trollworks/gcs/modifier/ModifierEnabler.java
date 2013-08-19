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
 * 2005-2007 the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.gcs.modifier;

import com.trollworks.gcs.advantage.Advantage;
import com.trollworks.gcs.utility.io.LocalizedMessages;
import com.trollworks.gcs.utility.text.TextUtility;
import com.trollworks.gcs.widgets.WindowUtils;
import com.trollworks.gcs.widgets.layout.ColumnLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Asks the user to enable/disable modifiers. */
public class ModifierEnabler extends JPanel {
	private static String	MSG_MODIFIER_TITLE;
	private static String	MSG_MODIFIER_ONE_REMAINING;
	private static String	MSG_MODIFIER_REMAINING;
	private static String	MSG_CANCEL_REST;
	private static String	MSG_CANCEL;
	private static String	MSG_APPLY;
	private Advantage		mAdvantage;
	private JCheckBox[]		mEnabled;
	private Modifier[]		mModifiers;

	static {
		LocalizedMessages.initialize(ModifierEnabler.class);
	}

	/**
	 * Brings up a modal dialog that allows {@link Modifier}s to be enabled or disabled for the
	 * specified {@Link CMAdvantage}s.
	 * 
	 * @param comp The component to open the dialog over.
	 * @param advantages The {@Link CMAdvantage}s to process.
	 * @return Whether anything was modified.
	 */
	static public boolean process(Component comp, ArrayList<Advantage> advantages) {
		ArrayList<Advantage> list = new ArrayList<Advantage>();
		boolean modified = false;
		int count;

		for (Advantage advantage : advantages) {
			if (!advantage.getModifiers().isEmpty()) {
				list.add(advantage);
			}
		}

		count = list.size();
		for (int i = 0; i < count; i++) {
			Advantage advantage = list.get(i);
			boolean hasMore = i != count - 1;
			ModifierEnabler panel = new ModifierEnabler(advantage, count - i - 1);
			switch (WindowUtils.showOptionDialog(comp, panel, MSG_MODIFIER_TITLE, true, hasMore ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(advantage.getImage(true)), hasMore ? new String[] { MSG_APPLY, MSG_CANCEL, MSG_CANCEL_REST } : new String[] { MSG_APPLY, MSG_CANCEL }, MSG_APPLY)) {
				case JOptionPane.YES_OPTION:
					panel.applyChanges();
					modified = true;
					break;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
				case JOptionPane.CLOSED_OPTION:
					return modified;
			}
		}
		return modified;
	}

	private ModifierEnabler(Advantage advantage, int remaining) {
		super(new BorderLayout());
		mAdvantage = advantage;
		add(createTop(advantage, remaining), BorderLayout.NORTH);
		add(createCenter(), BorderLayout.CENTER);
	}

	private Container createTop(Advantage advantage, int remaining) {
		JPanel top = new JPanel(new ColumnLayout());
		JLabel label = new JLabel(TextUtility.truncateIfNecessary(advantage.toString(), 80, SwingConstants.RIGHT), SwingConstants.LEFT);

		top.setBorder(new EmptyBorder(0, 0, 15, 0));
		if (remaining > 0) {
			String msg;

			if (remaining == 1) {
				msg = MSG_MODIFIER_ONE_REMAINING;
			} else {
				msg = MessageFormat.format(MSG_MODIFIER_REMAINING, new Integer(remaining));
			}
			top.add(new JLabel(msg, SwingConstants.CENTER));
		}
		label.setBorder(new CompoundBorder(LineBorder.createBlackLineBorder(), new EmptyBorder(0, 2, 0, 2)));
		label.setOpaque(true);
		top.add(new JPanel());
		top.add(label);
		return top;
	}

	private Container createCenter() {
		JPanel wrapper = new JPanel(new ColumnLayout());

		mModifiers = mAdvantage.getModifiers().toArray(new Modifier[0]);
		Arrays.sort(mModifiers);

		mEnabled = new JCheckBox[mModifiers.length];
		for (int i = 0; i < mModifiers.length; i++) {
			mEnabled[i] = new JCheckBox(mModifiers[i].getFullDescription(), mModifiers[i].isEnabled());
			wrapper.add(mEnabled[i]);
		}
		return wrapper;
	}

	private void applyChanges() {
		for (int i = 0; i < mModifiers.length; i++) {
			mModifiers[i].setEnabled(mEnabled[i].isSelected());
		}
	}
}
