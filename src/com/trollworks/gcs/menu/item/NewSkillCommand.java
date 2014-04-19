/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.menu.item;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;


import com.trollworks.gcs.character.SheetWindow;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.library.LibraryWindow;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.skill.Technique;
import com.trollworks.gcs.template.TemplateWindow;
import com.trollworks.gcs.widgets.outline.ListOutline;
import com.trollworks.toolkit.ui.menu.Command;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;

/** Provides the "New Skill" command. */
public class NewSkillCommand extends Command {
	@Localize("New Skill")
	private static String SKILL;
	@Localize("New Skill Container")
	private static String SKILL_CONTAINER;
	@Localize("New Technique")
	private static String TECHNIQUE;

	static {
		Localization.initialize();
	}

	/** The action command this command will issue. */
	public static final String			CMD_NEW_SKILL			= "NewSkill";																											//$NON-NLS-1$
	/** The action command this command will issue. */
	public static final String			CMD_NEW_SKILL_CONTAINER	= "NewSkillContainer";																									//$NON-NLS-1$
	/** The action command this command will issue. */
	public static final String			CMD_NEW_TECHNIQUE		= "NewTechnique";																										//$NON-NLS-1$

	/** The "New Skill" command. */
	public static final NewSkillCommand	INSTANCE				= new NewSkillCommand(false, false, SKILL, CMD_NEW_SKILL, KeyEvent.VK_K, COMMAND_MODIFIER);
	/** The "New Skill Container" command. */
	public static final NewSkillCommand	CONTAINER_INSTANCE		= new NewSkillCommand(true, false, SKILL_CONTAINER, CMD_NEW_SKILL_CONTAINER, KeyEvent.VK_K, SHIFTED_COMMAND_MODIFIER);
	/** The "New Technique" command. */
	public static final NewSkillCommand	TECHNIQUE_INSTANCE		= new NewSkillCommand(false, true, TECHNIQUE, CMD_NEW_TECHNIQUE, KeyEvent.VK_T, COMMAND_MODIFIER);
	private boolean						mContainer;
	private boolean						mTechnique;

	private NewSkillCommand(boolean container, boolean isTechnique, String title, String cmd, int keyCode, int modifiers) {
		super(title, cmd, keyCode, modifiers);
		mContainer = container;
		mTechnique = isTechnique;
	}

	@Override
	public void adjustForMenu(JMenuItem item) {
		Window window = getActiveWindow();
		if (window instanceof LibraryWindow) {
			setEnabled(!((LibraryWindow) window).getOutline().getModel().isLocked());
		} else {
			setEnabled(window instanceof SheetWindow || window instanceof TemplateWindow);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		ListOutline outline;
		DataFile dataFile;

		Window window = getActiveWindow();
		if (window instanceof LibraryWindow) {
			LibraryWindow libraryWindow = (LibraryWindow) window;
			libraryWindow.switchToSkills();
			dataFile = libraryWindow.getLibraryFile();
			outline = libraryWindow.getOutline();
		} else if (window instanceof SheetWindow) {
			SheetWindow sheetWindow = (SheetWindow) window;
			outline = sheetWindow.getSheet().getSkillOutline();
			dataFile = sheetWindow.getCharacter();
		} else if (window instanceof TemplateWindow) {
			TemplateWindow templateWindow = (TemplateWindow) window;
			outline = templateWindow.getSheet().getSkillOutline();
			dataFile = templateWindow.getTemplate();
		} else {
			return;
		}

		Skill skill = mTechnique ? new Technique(dataFile) : new Skill(dataFile, mContainer);
		outline.addRow(skill, getTitle(), false);
		outline.getModel().select(skill, false);
		outline.scrollSelectionIntoView();
		outline.openDetailEditor(true);
	}
}