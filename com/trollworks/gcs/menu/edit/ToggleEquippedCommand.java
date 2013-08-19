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

package com.trollworks.gcs.menu.edit;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.equipment.EquipmentOutline;
import com.trollworks.gcs.menu.Command;
import com.trollworks.gcs.utility.collections.FilteredIterator;
import com.trollworks.gcs.utility.io.LocalizedMessages;
import com.trollworks.gcs.widgets.outline.MultipleRowUndo;
import com.trollworks.gcs.widgets.outline.OutlineProxy;
import com.trollworks.gcs.widgets.outline.RowUndo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JMenuItem;

/** Provides the "Toggle Equipped State" command. */
public class ToggleEquippedCommand extends Command {
	private static String						MSG_TOGGLE_EQUIPPED;

	static {
		LocalizedMessages.initialize(ToggleEquippedCommand.class);
	}

	/** The singleton {@link ToggleEquippedCommand}. */
	public static final ToggleEquippedCommand	INSTANCE	= new ToggleEquippedCommand();

	private ToggleEquippedCommand() {
		super(MSG_TOGGLE_EQUIPPED, KeyEvent.VK_QUOTE);
	}

	@Override public void adjustForMenu(JMenuItem item) {
		Component focus = getFocusOwner();
		if (focus instanceof OutlineProxy) {
			focus = ((OutlineProxy) focus).getRealOutline();
		}
		if (focus instanceof EquipmentOutline) {
			EquipmentOutline outline = (EquipmentOutline) focus;
			setEnabled(outline.getDataFile() instanceof GURPSCharacter && outline.isCarried() && outline.getModel().hasSelection());
		} else {
			setEnabled(false);
		}
	}

	@Override public void actionPerformed(ActionEvent event) {
		Component focus = getFocusOwner();
		if (focus instanceof OutlineProxy) {
			focus = ((OutlineProxy) focus).getRealOutline();
		}
		EquipmentOutline outline = (EquipmentOutline) focus;
		ArrayList<RowUndo> undos = new ArrayList<RowUndo>();
		for (Equipment equipment : new FilteredIterator<Equipment>(outline.getModel().getSelectionAsList(), Equipment.class)) {
			RowUndo undo = new RowUndo(equipment);
			equipment.setEquipped(!equipment.isEquipped());
			if (undo.finish()) {
				undos.add(undo);
			}
		}
		if (!undos.isEmpty()) {
			outline.repaintSelection();
			new MultipleRowUndo(undos);
		}
	}
}
