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
package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.table.TableModel;

import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetwork;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetworkElement;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNode;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackSubnet;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackUser;
import com.net2plan.gui.plugins.utils.FilteredTablePanel;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_users;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_networkElement;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_networks;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_nodes;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_subnets;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.utils.Pair;

@SuppressWarnings("unchecked")
public class ViewEditTopologyTablesPane extends JPanel
{
    public enum AJTableType
    {
        NODES("AlL NODES"),
        LINKS("AlL LINKS"),
        USERS("AlL USER"),
        NETWORKS("ALL NETWORKS"),
        SUBNETS("ALL SUBNETS");

        private final String tabName;

        private AJTableType(String tabName)
        {
            this.tabName = tabName;
        }

        public String getTabName()
        {
            return tabName;
        }


        public static AJTableType getTypeOfElement(OpenStackNetworkElement e)
        {
            if (e instanceof OpenStackNode)
                return AJTableType.NODES;
            if (e instanceof OpenStackUser)
                return AJTableType.LINKS;
            if (e instanceof OpenStackUser)
                return AJTableType.USERS;
            if (e instanceof OpenStackNetwork)
                return AJTableType.NETWORKS;
            if (e instanceof OpenStackSubnet)
                return AJTableType.SUBNETS;
            return null;
        }
    }

    private final GUINetworkDesign callback;
    private final Map<AJTableType, Pair<AdvancedJTable_networkElement, FilteredTablePanel>> ajTables = new EnumMap<>(AJTableType.class);
    private final JTabbedPane viewEditHighLevelTabbedPane;

    private final JMenuBar menuBar;

    /**
     * Main Panel with CENTER: high level tabbed pane (Network, layer, nodes, ...), SOUTH: Export as Excel mention
     *
     * @param callback
     * @param layout
     */
    public ViewEditTopologyTablesPane(GUINetworkDesign callback, LayoutManager layout)
    {
        super(layout);

        this.callback = callback;

        this.viewEditHighLevelTabbedPane = new JTabbedPane();

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(viewEditHighLevelTabbedPane);

        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.3);
        splitPane.setEnabled(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.4);

        this.add(splitPane, BorderLayout.CENTER);

        for (AJTableType ajType : AJTableType.values())
            ajTables.put(ajType, createPanelComponentInfo(ajType));


        /* The rest of high level tabs */
        for (AJTableType type : Arrays.asList(AJTableType.NODES,AJTableType.LINKS,AJTableType.USERS, AJTableType.NETWORKS,
                AJTableType.SUBNETS))
            viewEditHighLevelTabbedPane.addTab(type.getTabName(), ajTables.get(type).getSecond());


        menuBar = new JMenuBar();

        this.add(menuBar, BorderLayout.SOUTH);
    }

    public SortedSet<? extends OpenStackNetworkElement> getSelectedElements (AJTableType ajType)
    {
        return ajTables.get(ajType).getFirst().getSelectedElements();
    }

    private Pair<AdvancedJTable_networkElement, FilteredTablePanel> createPanelComponentInfo(AJTableType type)
    {
        AdvancedJTable_networkElement table = null;
        switch (type)
        {
            case NODES:
                table = new AdvancedJTable_nodes(callback);
                break;
            case USERS:
                table = new AdvancedJTable_users(callback);
                break;
            case NETWORKS:
                table = new AdvancedJTable_networks(callback);
                break;
            case SUBNETS:
                table = new AdvancedJTable_subnets(callback);
                break;
            default:
                assert false;
        }


        return Pair.of(table, new FilteredTablePanel(callback, table.getTableScrollPane()));
    }


    public void resetPickedState()
    {
        ajTables.values().stream().filter(q -> q.getFirst() != null).forEach(q -> q.getFirst().clearSelection());

    }

    public void restoreView()
    {
        this.updateView();
    }

    public void updateView()
    {
        /* Load current network state */
        final NetPlan currentState = callback.getDesign();
        if (ErrorHandling.isDebugEnabled()) currentState.checkCachesConsistency();

        ajTables.values().stream().map(t -> t.getFirst()).forEach(t -> t.updateView());

        // Update filter header
        for (AJTableType type : AJTableType.values())
            ajTables.get(type).getSecond().updateHeader();



        if (ErrorHandling.isDebugEnabled()) currentState.checkCachesConsistency();
    }

    /**
     * Shows the tab corresponding associated to a network element.
     *
     * @param type   Network element type
     */
    public void selectItemTab(AJTableType type)
    {
        switch (type)
        {
            case NODES:
            case LINKS:
            case USERS:
            case NETWORKS:
            case SUBNETS:
                viewEditHighLevelTabbedPane.setSelectedComponent(ajTables.get(type).getSecond());
                break;
            default:
                System.out.println(type);
                assert false;
        }
    }

    public void selectItems(List<OpenStackNetworkElement> elements , AJTableType ajTableType)
    {
        if (elements == null) return;
        if (elements.isEmpty()) return;

        final AJTableType tableType = ajTableType != null? ajTableType : AJTableType.getTypeOfElement(elements.get(0));
        if (tableType == null) return;

        final AdvancedJTable_networkElement table = ajTables.get(tableType).getFirst();
        if (table == null) return;

        table.clearSelection();

        final TableModel model = table.getModel();
        assert model != null;

        final Set<String> idstoSelect = elements.stream().map(e -> e.getId()).collect(Collectors.toSet());
        final int numRows = table.getRowCount();

        selectItemTab(tableType);

        if (elements.size() == 1)
        {
            // Single-selection
            OpenStackNetworkElement element = elements.get(0);


        } else
        {
           /* // Multiple-selection
            treePanel.restoreView();*/
        }

        for (int row = 0; row < numRows; row++)
        {
            try
            {
                final Object obj = model.getValueAt(row, 0);
                if (obj == null) continue;

                final long elementID = (long) obj;
                if (idstoSelect.contains(elementID))
                {
                    final int viewRow = table.convertRowIndexToView(row);

                    table.addRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));

                    if (table.getSelectedRowCount() == elements.size()) return;
                }
            } catch (ClassCastException e)
            {
                ErrorHandling.log("Tried to use aggregation row at: " + row);
            }
        }
    }

    public void selectItemsAllTables(List<OpenStackNetworkElement> elements)
    {
        if (elements == null) return;
        if (elements.isEmpty()) return;

        for (AJTableType tableType : AJTableType.values())
        {
            final AdvancedJTable_networkElement table = ajTables.get(tableType).getFirst();
            if (table == null) continue;

            table.clearSelection();

            final TableModel model = table.getModel();
            assert model != null;

            final Set<String> idstoSelect = elements.stream().map(e -> e.getId()).collect(Collectors.toSet());
            final int numRows = table.getRowCount();

            for (int row = 0; row < numRows; row++)
            {
                try
                {
                    final Object obj = model.getValueAt(row, 0);
                    if (obj == null) continue;

                    final String elementID = (String) obj;
                    if (idstoSelect.contains(elementID))
                    {
                        final int viewRow = table.convertRowIndexToView(row);

                        table.addRowSelectionInterval(viewRow, viewRow);

                        if (table.getSelectedRowCount() == elements.size()) break;
                    }
                } catch (ClassCastException e)
                {
                    ErrorHandling.log("Tried to use aggregation row at: " + row);
                }
            }
        }
    }
}
