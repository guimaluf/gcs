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

package com.trollworks.gcs.template;

import com.trollworks.gcs.advantage.Advantage;
import com.trollworks.gcs.advantage.AdvantageOutline;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.equipment.EquipmentOutline;
import com.trollworks.gcs.notes.Note;
import com.trollworks.gcs.notes.NoteOutline;
import com.trollworks.gcs.preferences.SheetPreferences;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.skill.SkillOutline;
import com.trollworks.gcs.spell.Spell;
import com.trollworks.gcs.spell.SpellOutline;
import com.trollworks.gcs.widgets.outline.ListRow;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.border.EmptyBorder;
import com.trollworks.toolkit.ui.layout.ColumnLayout;
import com.trollworks.toolkit.ui.scale.Scale;
import com.trollworks.toolkit.ui.scale.ScaleRoot;
import com.trollworks.toolkit.ui.widget.outline.Outline;
import com.trollworks.toolkit.ui.widget.outline.OutlineHeader;
import com.trollworks.toolkit.ui.widget.outline.OutlineSyncer;
import com.trollworks.toolkit.ui.widget.outline.Row;
import com.trollworks.toolkit.ui.widget.outline.RowSelection;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.notification.BatchNotifierTarget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** The template sheet. */
public class TemplateSheet extends JPanel implements Scrollable, BatchNotifierTarget, DropTargetListener, ActionListener, ScaleRoot {
    @Localize("Advantages, Disadvantages & Quirks")
    @Localize(locale = "de", value = "Vorteile, Nachteile & Marotten")
    @Localize(locale = "ru", value = "Преимущества, недостатки и причуды")
    @Localize(locale = "es", value = "Ventajas, Desventajas y Singularidades")
    private static String ADVANTAGES;
    @Localize("Skills")
    @Localize(locale = "de", value = "Fertigkeiten")
    @Localize(locale = "ru", value = "Умения")
    @Localize(locale = "es", value = "Habilidades")
    private static String SKILLS;
    @Localize("Spells")
    @Localize(locale = "de", value = "Zauber")
    @Localize(locale = "ru", value = "Заклинания")
    @Localize(locale = "es", value = "Sortilegios")
    private static String SPELLS;
    @Localize("Equipment")
    @Localize(locale = "de", value = "Ausrüstung")
    @Localize(locale = "ru", value = "Снаряжение")
    @Localize(locale = "es", value = "Equipo")
    private static String EQUIPMENT;
    @Localize("Notes")
    @Localize(locale = "de", value = "Notizen")
    @Localize(locale = "ru", value = "Заметка")
    @Localize(locale = "es", value = "Notas")
    private static String NOTES;

    static {
        Localization.initialize();
    }

    private static final EmptyBorder NORMAL_BORDER = new EmptyBorder(5);
    private Scale                    mScale;
    private Template                 mTemplate;
    private boolean                  mBatchMode;
    private AdvantageOutline         mAdvantageOutline;
    private SkillOutline             mSkillOutline;
    private SpellOutline             mSpellOutline;
    private EquipmentOutline         mEquipmentOutline;
    private NoteOutline              mNoteOutline;
    /** Used to determine whether an edit cell is pending. */
    protected boolean                mStartEditingPending;
    /** Used to determine whether a resize action is pending. */
    protected boolean                mSizePending;

    /**
     * Creates a new {@link TemplateSheet}.
     *
     * @param template The template to display the data for.
     */
    public TemplateSheet(Template template) {
        super(new ColumnLayout(1, 0, 5));
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(NORMAL_BORDER);

        mScale            = SheetPreferences.getInitialUIScale().getScale();
        mTemplate         = template;

        // Make sure our primary outlines exist
        mAdvantageOutline = new AdvantageOutline(mTemplate);
        mSkillOutline     = new SkillOutline(mTemplate);
        mSpellOutline     = new SpellOutline(mTemplate);
        mEquipmentOutline = new EquipmentOutline(mTemplate);
        mNoteOutline      = new NoteOutline(mTemplate);
        add(new TemplateOutlinePanel(mAdvantageOutline, ADVANTAGES));
        add(new TemplateOutlinePanel(mSkillOutline, SKILLS));
        add(new TemplateOutlinePanel(mSpellOutline, SPELLS));
        add(new TemplateOutlinePanel(mEquipmentOutline, EQUIPMENT));
        add(new TemplateOutlinePanel(mNoteOutline, NOTES));
        mAdvantageOutline.addActionListener(this);
        mSkillOutline.addActionListener(this);
        mSpellOutline.addActionListener(this);
        mEquipmentOutline.addActionListener(this);
        mNoteOutline.addActionListener(this);

        // Ensure everything is laid out and register for notification
        revalidate();
        mTemplate.addTarget(this, Template.TEMPLATE_PREFIX, GURPSCharacter.CHARACTER_PREFIX);

        setDropTarget(new DropTarget(this, this));

        runAdjustSize();
    }

    @Override
    public Scale getScale() {
        return mScale;
    }

    @Override
    public void setScale(Scale scale) {
        if (mScale.getScale() != scale.getScale()) {
            mScale = scale;
            revalidate();
            repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (Outline.CMD_POTENTIAL_CONTENT_SIZE_CHANGE.equals(command)) {
            adjustSize();
        }
    }

    private void adjustSize() {
        if (!mSizePending) {
            mSizePending = true;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runAdjustSize();
                }
            });
        }
    }

    void runAdjustSize() {
        Scale scale = Scale.get(this);
        mSizePending = false;
        Dimension size = getLayout().preferredLayoutSize(TemplateSheet.this);
        size.width = scale.scale(8 * 72);
        int min = scale.scale(4 * 72);
        if (size.height < min) {
            size.height = min;
        }
        UIUtilities.setOnlySize(TemplateSheet.this, size);
        setSize(size);
    }

    /**
     * Prepares the specified outline for embedding in the sheet.
     *
     * @param outline The outline to prepare.
     */
    public static void prepOutline(Outline outline) {
        OutlineHeader header = outline.getHeaderPanel();
        outline.setDynamicRowHeight(true);
        outline.setAllowColumnDrag(false);
        outline.setAllowColumnResize(false);
        outline.setAllowColumnContextMenu(false);
        header.setIgnoreResizeOK(true);
        header.setBackground(Color.black);
        header.setTopDividerColor(Color.black);
    }

    /** @return The outline containing the Advantages, Disadvantages & Quirks. */
    public AdvantageOutline getAdvantageOutline() {
        return mAdvantageOutline;
    }

    /** @return The outline containing the skills. */
    public SkillOutline getSkillOutline() {
        return mSkillOutline;
    }

    /** @return The outline containing the spells. */
    public SpellOutline getSpellOutline() {
        return mSpellOutline;
    }

    /** @return The outline containing the equipment. */
    public EquipmentOutline getEquipmentOutline() {
        return mEquipmentOutline;
    }

    /** @return The outline containing the notes equipment. */
    public NoteOutline getNoteOutline() {
        return mNoteOutline;
    }

    @Override
    public void enterBatchMode() {
        mBatchMode = true;
    }

    @Override
    public void leaveBatchMode() {
        mBatchMode = false;
        validate();
    }

    @Override
    public void handleNotification(Object producer, String type, Object data) {
        if (type.startsWith(Advantage.PREFIX)) {
            OutlineSyncer.add(mAdvantageOutline);
        } else if (type.startsWith(Skill.PREFIX)) {
            OutlineSyncer.add(mSkillOutline);
        } else if (type.startsWith(Spell.PREFIX)) {
            OutlineSyncer.add(mSpellOutline);
        } else if (type.startsWith(Equipment.PREFIX)) {
            OutlineSyncer.add(mEquipmentOutline);
        } else if (type.startsWith(Note.PREFIX)) {
            OutlineSyncer.add(mNoteOutline);
        }
        if (!mBatchMode) {
            validate();
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    private boolean        mDragWasAcceptable;
    private ArrayList<Row> mDragRows;

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        mDragWasAcceptable = false;

        try {
            if (dtde.isDataFlavorSupported(RowSelection.DATA_FLAVOR)) {
                Row[] rows = (Row[]) dtde.getTransferable().getTransferData(RowSelection.DATA_FLAVOR);

                if (rows != null && rows.length > 0) {
                    mDragRows = new ArrayList<>(rows.length);

                    for (Row element : rows) {
                        if (element instanceof ListRow) {
                            mDragRows.add(element);
                        }
                    }
                    if (!mDragRows.isEmpty()) {
                        mDragWasAcceptable = true;
                        dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                    }
                }
            }
        } catch (Exception exception) {
            Log.error(exception);
        }

        if (!mDragWasAcceptable) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (mDragWasAcceptable) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (mDragWasAcceptable) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(dtde.getDropAction());
        UIUtilities.getAncestorOfType(this, TemplateDockable.class).addRows(mDragRows);
        mDragRows = null;
        dtde.dropComplete(true);
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        mDragRows = null;
    }

    @Override
    public int getNotificationPriority() {
        return 0;
    }
}
