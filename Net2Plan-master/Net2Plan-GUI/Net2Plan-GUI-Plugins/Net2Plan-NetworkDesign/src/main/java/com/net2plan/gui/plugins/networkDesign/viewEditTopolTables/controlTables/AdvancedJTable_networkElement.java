/*******************************************************************************
 * Copyright (c) 2017 Pablo Pavon Marino and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 2-clause BSD License 
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/BSD-2-Clause
 *
 * Contributors:
 *     Pablo Pavon Marino and others - initial API and implementation
 *******************************************************************************/


package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackLink;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNet;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetwork;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetworkElement;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNode;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackSubnet;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackUser;
import com.net2plan.gui.plugins.networkDesign.interfaces.ITableRowFilter;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;
import com.net2plan.gui.utils.JScrollPopupMenu;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.internal.ErrorHandling;
import java.util.stream.Collectors;


/**
 */
@SuppressWarnings("unchecked")
public abstract class AdvancedJTable_networkElement<T extends OpenStackNetworkElement> extends AdvancedJTable_abstractElement<T>
{
    protected final AJTableType ajtType;

    public AdvancedJTable_networkElement(GUINetworkDesign networkViewer, AJTableType ajtType , boolean hasAggregationRow)
    {
        super(networkViewer, ajtType.getTabName() , hasAggregationRow);
        this.ajtType = ajtType;
        updateView();
    }

    @Override
    protected void addExtendedKeyboardActions()
    {
        final InputMap inputMap = this.getInputMap();
        final ActionMap actionMap = this.getActionMap();
        final AbstractAction pickElementAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                final Set<T> selectedElements = AdvancedJTable_networkElement.this.getSelectedElements();
                if (selectedElements.isEmpty()) return;
                SwingUtilities.invokeLater(() -> pickSelection(selectedElements));
            }
        };

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.VK_UNDEFINED), "pickElements");
        actionMap.put("pickElements", pickElementAction);
    }


    @Override
    protected final List<T> getAllAbstractElementsInTable()
    {
        final ITableRowFilter rf = callback.getVisualizationState().getTableRowFilter();
        return rf == null ? (List<T>) ITableRowFilter.getAllElements(callback.getOpenStackNet(), ajtType) : (List<T>) rf.getVisibleElements(callback.getOpenStackNet(), ajtType);
    }

    public final AJTableType getAjType () { return ajtType; }

    protected void pickSelection(Set<T> selection)
    {
        assert selection != null;

        final NetPlan np = callback.getDesign();

        final List<NetworkElement> elementsToPick = new ArrayList<>();

        if (ajtType == AJTableType.NODES)
        {
            selection.stream().map(s->np.getNodeFromId(((OpenStackNode) s).getInternalId())).collect(Collectors.toList());
            //selection.forEach(n->elementsToPick.add(np.getNodeFromId(((OpenStackNode) n).getInternalId())));
        }

        if (!elementsToPick.isEmpty())
        {
            callback.getVisualizationState().pickElement(elementsToPick);
            callback.updateVisualizationAfterPick();
        }
    }

    @Override
    protected void showPopup(MouseEvent me)
    {
        // Debug mode removes the scroll bar so that tests do not have conflicts with it.
        final JScrollPopupMenu popup = new JScrollPopupMenu(20);
        final Optional<AjtRcMenu> viewMenu = getViewMenuAndSubmenus();
        final List<T> visibleElementsInTable = this.getAllAbstractElementsInTable();
        final List<AjtRcMenu> allPopupMenusButView = new ArrayList<> ();
        if (viewMenu.isPresent()) allPopupMenusButView.add(viewMenu.get());
        allPopupMenusButView.add(new AjtRcMenu("Pick selection", e-> SwingUtilities.invokeLater(() -> pickSelection(this.getSelectedElements())), (a,b)->b>0, null));
        allPopupMenusButView.add(AjtRcMenu.createMenuSeparator());
        allPopupMenusButView.addAll(getNonBasicRightClickMenusInfo());
        for (AjtRcMenu popupInfo : allPopupMenusButView)
        {
            if (popupInfo.isSeparator())  { popup.addSeparator(); continue; }
            final JComponent menu = processMenu (popupInfo , visibleElementsInTable);
            popup.add(menu);
        }
        popup.show(me.getComponent(), me.getX(), me.getY());
    }

    /**
     * Under testing, do not use just yet
     **/
    @Override
    public T getElementAtModelRowIndex(int rowModelIndex)
    {
        try
        {
            return (T) callback.getOpenStackNet().getOpenStackNetworkElementByOpenStackId((String) this.getModel().getValueAt(rowModelIndex, 0));
        } catch (Exception ex)
        {
            return null;
        }
    }

    protected abstract List<AjtRcMenu> getNonBasicRightClickMenusInfo ();

    protected abstract List<AjtColumnInfo<T>> getNonBasicUserDefinedColumnsVisibleOrNot ();

    @Override
    protected void reactToMouseClickInTable (int numClicks , int rowModelIndexOfClickOrMinus1IfOut , int columnModelIndexOfClickOrMinus1IfOut)
    {
        final SortedSet<T> selectedElements = this.getSelectedElements();
        if (numClicks == 1)
        {
            if (selectedElements.isEmpty()) callback.resetPickedStateAndUpdateView();
            if (rowModelIndexOfClickOrMinus1IfOut == -1) return;
            final Object value = getModel().getValueAt(rowModelIndexOfClickOrMinus1IfOut, columnModelIndexOfClickOrMinus1IfOut);
            if (value instanceof OpenStackNetworkElement)
            {
                //pickSelection(Sets.newHashSet((OpenStackNetworkElement) value));
            }
            else if (value instanceof Collection)
            {
                if (((Collection) value).isEmpty()) return;

                final Object firstElement = ((Collection) value).iterator().next();
                                /*
				if (firstElement instanceof OpenStackNetworkElement || firstElement instanceof Bidi)
				{
					final List<OpenStackNetworkElement> es = new ArrayList<>();

					if (firstElement instanceof Bidi)
					{
						for (Bidi bidi : (Collection<? extends Bidi>) value)
						{
							es.add((OpenStackNetworkElement) bidi.getLowUnidi());
							es.add((OpenStackNetworkElement) bidi.getHighUnidi());
						}

					} else es.addAll((Collection) value);

					callback.getVisualizationState().pickElement(es, null);
					callback.updateVisualizationAfterPick();
				}*/

            }
            else if (selectedElements.isEmpty()) callback.resetPickedStateAndUpdateView();
        } else if (numClicks >= 2)
        {
            SwingUtilities.invokeLater(() -> pickSelection(selectedElements));
        }

    }

    @Override
    protected void reactToMouseMoveInTable (int rowModelIndexOfClickOrMinus1IfOut , int columnModelIndexOfClickOrMinus1IfOut)
    {
        if (rowModelIndexOfClickOrMinus1IfOut == -1) return;
        final Object value = getModel().getValueAt(rowModelIndexOfClickOrMinus1IfOut,
                columnModelIndexOfClickOrMinus1IfOut);
        if (value instanceof OpenStackNetworkElement)
        {
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return;
        } else if (value instanceof Collection && !((Collection) value).isEmpty())
        {
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return;
        }
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    private final Set<T> invertSelection(Set<T> selectedElements)
    {
        // Check all elements belong to the same NetPlan
        final OpenStackNet os = this.callback.getOpenStackNet();
        for (OpenStackNetworkElement networkElement : selectedElements)
        {
            if (networkElement.getOpenStackNet()!= os) return null;
            if (AJTableType.getTypeOfElement(networkElement) != ajtType) return null;
        }
        final List<? extends OpenStackNetworkElement> allElements = ITableRowFilter.getAllElements(os , ajtType);
        Set<T> invertedElements = new HashSet<> ((List<T>) (List<?>) allElements);
        invertedElements.removeAll(selectedElements);
        return invertedElements;
    }

    protected Double dialogGetDouble (String message , String title )
    {
        final String str = JOptionPane.showInputDialog(null, message , title , JOptionPane.QUESTION_MESSAGE);
        if (str == null) return null;
        return Double.parseDouble(str);
    }
    protected Integer dialogGetInteger (String message , String title )
    {
        final String str = JOptionPane.showInputDialog(null, message , title , JOptionPane.QUESTION_MESSAGE);
        if (str == null) return null;
        return Integer.parseInt(str);
    }
    protected String dialogGetString (String message , String title )
    {
        final String val = JOptionPane.showInputDialog(null, message , title , JOptionPane.QUESTION_MESSAGE);
        return val;
    }

    @Override
    protected final List<AjtColumnInfo<T>> getAllColumnsVisibleOrNot ()
    {
        final List<AjtColumnInfo<T>> completeListIncludingCommonColumns = new ArrayList<> ();
        completeListIncludingCommonColumns.add((AjtColumnInfo<T>) (AjtColumnInfo<?>) AjtColumnInfo.createIdColumn(this));
        completeListIncludingCommonColumns.add((AjtColumnInfo<T>) (AjtColumnInfo<?>) AjtColumnInfo.createIndexColumn(this));
        completeListIncludingCommonColumns.addAll(getNonBasicUserDefinedColumnsVisibleOrNot());
        completeListIncludingCommonColumns.add((AjtColumnInfo<T>) (AjtColumnInfo<?>) AjtColumnInfo.createTagsColumn(this));
        completeListIncludingCommonColumns.add((AjtColumnInfo<T>) (AjtColumnInfo<?>) AjtColumnInfo.createAttributesColumn(this));
        return completeListIncludingCommonColumns;
    }



}
