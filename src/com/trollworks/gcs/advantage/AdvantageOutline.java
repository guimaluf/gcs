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

package com.trollworks.gcs.advantage;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.common.ListFile;
import com.trollworks.gcs.menu.edit.Incrementable;
import com.trollworks.gcs.template.Template;
import com.trollworks.gcs.widgets.outline.ListOutline;
import com.trollworks.gcs.widgets.outline.ListRow;
import com.trollworks.gcs.widgets.outline.MultipleRowUndo;
import com.trollworks.gcs.widgets.outline.RowPostProcessor;
import com.trollworks.gcs.widgets.outline.RowUndo;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.collections.FilteredIterator;
import com.trollworks.toolkit.ui.widget.outline.OutlineModel;
import com.trollworks.toolkit.ui.widget.outline.Row;
import com.trollworks.toolkit.utility.Localization;

import java.awt.EventQueue;
import java.awt.dnd.DropTargetDragEvent;
import java.util.ArrayList;
import java.util.List;

/** An outline specifically for Advantages. */
public class AdvantageOutline extends ListOutline implements Incrementable {
    @Localize("Increment Level")
    @Localize(locale = "de", value = "Stufe erhöhen")
    @Localize(locale = "ru", value = "Повысить уровень")
    @Localize(locale = "es", value = "Subir Nivel")
    @Localize(locale = "pt_BR", value = "Aumentar nível")
    private static String INCREMENT;
    @Localize("Decrement Level")
    @Localize(locale = "de", value = "Stufe verringen")
    @Localize(locale = "ru", value = "Понизить уровень")
    @Localize(locale = "es", value = "Bajar Nivel")
    @Localize(locale = "pt_BR", value = "Diminuir nível")
    private static String DECREMENT;

    static {
        Localization.initialize();
    }

    private static OutlineModel extractModel(DataFile dataFile) {
        if (dataFile instanceof GURPSCharacter) {
            return ((GURPSCharacter) dataFile).getAdvantagesModel();
        }
        if (dataFile instanceof Template) {
            return ((Template) dataFile).getAdvantagesModel();
        }
        return ((ListFile) dataFile).getModel();
    }

    /**
     * Create a new Advantages, Disadvantages & Quirks outline.
     *
     * @param dataFile The owning data file.
     */
    public AdvantageOutline(DataFile dataFile) {
        this(dataFile, extractModel(dataFile));
    }

    /**
     * Create a new Advantages, Disadvantages & Quirks outline.
     *
     * @param dataFile The owning data file.
     * @param model    The {@link OutlineModel} to use.
     */
    public AdvantageOutline(DataFile dataFile, OutlineModel model) {
        super(dataFile, model, Advantage.ID_LIST_CHANGED);
        AdvantageColumn.addColumns(this, dataFile);
    }

    @Override
    protected boolean isRowDragAcceptable(DropTargetDragEvent dtde, Row[] rows) {
        return !getModel().isLocked() && rows.length > 0 && rows[0] instanceof Advantage;
    }

    @Override
    public void convertDragRowsToSelf(List<Row> list) {
        OutlineModel       model              = getModel();
        Row[]              rows               = model.getDragRows();
        boolean            forSheetOrTemplate = mDataFile instanceof GURPSCharacter || mDataFile instanceof Template;
        ArrayList<ListRow> process            = new ArrayList<>();

        for (Row element : rows) {
            Advantage advantage = new Advantage(mDataFile, (Advantage) element, true);

            model.collectRowsAndSetOwner(list, advantage, false);
            if (forSheetOrTemplate) {
                addRowsToBeProcessed(process, advantage);
            }
        }

        if (forSheetOrTemplate && !process.isEmpty()) {
            EventQueue.invokeLater(new RowPostProcessor(this, process));
        }
    }

    @Override
    public String getIncrementTitle() {
        return INCREMENT;
    }

    @Override
    public String getDecrementTitle() {
        return DECREMENT;
    }

    @Override
    public boolean canIncrement() {
        return (mDataFile instanceof GURPSCharacter || mDataFile instanceof Template) && selectionHasLeveledRows(false);
    }

    @Override
    public boolean canDecrement() {
        return (mDataFile instanceof GURPSCharacter || mDataFile instanceof Template) && selectionHasLeveledRows(true);
    }

    private boolean selectionHasLeveledRows(boolean requireLevelAboveZero) {
        for (Advantage advantage : new FilteredIterator<>(getModel().getSelectionAsList(), Advantage.class)) {
            if (!advantage.canHaveChildren() && advantage.isLeveled() && (!requireLevelAboveZero || advantage.hasLevel())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public void increment() {
        ArrayList<RowUndo> undos = new ArrayList<RowUndo>();
        for (Advantage advantage : new FilteredIterator<Advantage>(getModel().getSelectionAsList(), Advantage.class)) {
            if (!advantage.canHaveChildren() && advantage.isLeveled()) {
                RowUndo undo = new RowUndo(advantage);

                advantage.adjustLevel(1);
                if (undo.finish()) {
                    undos.add(undo);
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void decrement() {
        ArrayList<RowUndo> undos = new ArrayList<RowUndo>();
        for (Advantage advantage : new FilteredIterator<Advantage>(getModel().getSelectionAsList(), Advantage.class)) {
            if (!advantage.canHaveChildren() && advantage.isLeveled()) {
                if (advantage.isLeveled()) {
                    RowUndo undo = new RowUndo(advantage);

                    advantage.adjustLevel(-1);
                    if (undo.finish()) {
                        undos.add(undo);
                    }
                }
            }
        }
        if (!undos.isEmpty()) {
            repaintSelection();
            new MultipleRowUndo(undos);
        }
    }
}
