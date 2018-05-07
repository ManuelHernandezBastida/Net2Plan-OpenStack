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

package com.net2plan.gui.plugins;

  import com.net2plan.gui.GUINet2Plan;
import com.net2plan.gui.plugins.networkDesign.GUIWindow;
import com.net2plan.gui.plugins.networkDesign.NetworkDesignWindow;
import com.net2plan.gui.plugins.networkDesign.focusPane.FocusPane;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNet;
import com.net2plan.gui.plugins.networkDesign.interfaces.ITopologyCanvas;
import com.net2plan.gui.plugins.networkDesign.offlineExecPane.OfflineExecutionPanel;
import com.net2plan.gui.plugins.networkDesign.topologyPane.TopologyPanel;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.CanvasFunction;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.GUILink;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.GUINode;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.JUNGCanvas;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;
import com.net2plan.gui.plugins.networkDesign.viewReportsPane.ViewReportPane;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.UndoRedoManager;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.gui.plugins.networkDesign.whatIfAnalysisPane.WhatIfAnalysisPane;
import com.net2plan.gui.utils.ProportionalResizeJSplitPaneListener;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.internal.Constants.NetworkElementType;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.plugins.IGUIModule;
import com.net2plan.internal.plugins.PluginSystem;
import com.net2plan.internal.sim.SimCore.SimState;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

    /**
     * Targeted to evaluate the network designs generated by built-in or user-defined
     * static planning algorithms, deciding on aspects such as the network topology,
     * the traffic routing, link capacities, protection routes and so on. Algorithms
     * based on constrained optimization formulations (i.e. ILPs) can be fast-prototyped
     * using the open-source Java Optimization Modeler library, to interface
     * to a number of external solvers such as GPLK, CPLEX or IPOPT.
     *
     * @author Pablo
     */
    public class GUINetworkDesign extends IGUIModule
    {
        private final static String TITLE = "Net2plan and OpenStack";
        private final static int MAXSIZEUNDOLISTCHANGES = 0; // deactivate, not robust yet
        private final static int MAXSIZEUNDOLISTPICK = 10;

        private TopologyPanel topologyPanel;

        private FocusPane focusPanel;

        private ViewEditTopologyTablesPane viewEditTopTables;
        private ViewReportPane reportPane;
        private OfflineExecutionPanel executionPane;
        private WhatIfAnalysisPane whatIfAnalysisPane;

        private VisualizationState vs;
        private UndoRedoManager undoRedoManager;

        private NetPlan currentNetPlan;
        private OpenStackNet currentOpenStackNet;
        private WindowController windowController;
        private GUIWindow tableControlWindow;

        /**
         * Default constructor.
         *
         * @since 0.2.0
         */
        public GUINetworkDesign()
        {
            this(TITLE);
        }


        /**
         * Constructor that allows set a title for the tool in the top section of the panel.
         *
         * @param title Title of the tool (null or empty means no title)
         * @since 0.2.0
         */
        public GUINetworkDesign(String title)
        {
            super(title);
        }

        @Override
        public void start()
        {
            // Default start
            super.start();

            // Additional commands
            this.tableControlWindow.setLocationRelativeTo(this);
            this.tableControlWindow.showWindow(false);
        }

        @Override
        public void stop()
        {
            tableControlWindow.setVisible(false);
            windowController.hideAllWindows();
        }

        @Override
        public void configure(JPanel contentPane)
        {
            // Configuring PluginSystem for this plugin...
            try
            {
                // Add canvas plugin
                PluginSystem.addExternalPlugin(ITopologyCanvas.class);

                /* Add default canvas systems */
                PluginSystem.addPlugin(ITopologyCanvas.class, JUNGCanvas.class);
            } catch (RuntimeException ignored)
            {
                // NOTE: ITopologyCanvas has already been added. Meaning that JUNGCanvas has already been too.
            }

            this.currentNetPlan = new NetPlan();
            this.currentOpenStackNet = new OpenStackNet();
            BidiMap<NetworkLayer, Integer> mapLayer2VisualizationOrder = new DualHashBidiMap<>();
            Map<NetworkLayer, Boolean> layerVisibilityMap = new HashMap<>();
            for (NetworkLayer layer : currentNetPlan.getNetworkLayers())
            {
                mapLayer2VisualizationOrder.put(layer, mapLayer2VisualizationOrder.size());
                layerVisibilityMap.put(layer, true);
            }
            this.vs = new VisualizationState(currentNetPlan, mapLayer2VisualizationOrder, layerVisibilityMap, MAXSIZEUNDOLISTPICK);

            topologyPanel = new TopologyPanel(this, JUNGCanvas.class);

            JPanel leftPane = new JPanel(new BorderLayout());
            JPanel logSection = configureLeftBottomPanel();
            if (logSection == null)
            {
                leftPane.add(topologyPanel, BorderLayout.CENTER);
            } else
            {
                JSplitPane splitPaneTopology = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                splitPaneTopology.setTopComponent(topologyPanel);
                splitPaneTopology.setBottomComponent(logSection);
                splitPaneTopology.addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
                splitPaneTopology.setBorder(new LineBorder(contentPane.getBackground()));
                splitPaneTopology.setOneTouchExpandable(true);
                splitPaneTopology.setDividerSize(7);
                leftPane.add(splitPaneTopology, BorderLayout.CENTER);
            }
            contentPane.add(leftPane, "grow");

            viewEditTopTables = new ViewEditTopologyTablesPane(GUINetworkDesign.this, new BorderLayout());

            reportPane = new ViewReportPane(GUINetworkDesign.this, JSplitPane.VERTICAL_SPLIT);

            setDesign(currentNetPlan);
            Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> res = VisualizationState.generateCanvasDefaultVisualizationLayerInfo(getDesign());
            vs.setCanvasLayerVisibilityAndOrder(getDesign(), res.getFirst(), res.getSecond());

            /* Initialize the undo/redo manager, and set its initial design */
            this.undoRedoManager = new UndoRedoManager(this, MAXSIZEUNDOLISTCHANGES);
            this.undoRedoManager.addNetPlanChange();

            executionPane = new OfflineExecutionPanel(this);
            whatIfAnalysisPane = new WhatIfAnalysisPane(this);

            final JTabbedPane tabPane = new JTabbedPane();
            tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.network), viewEditTopTables);
            tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.offline), executionPane);
            tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.whatif), whatIfAnalysisPane);
            tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.report), reportPane);

            // Installing customized mouse listener
            MouseListener[] ml = tabPane.getListeners(MouseListener.class);

            for (MouseListener mouseListener : ml)
            {
                tabPane.removeMouseListener(mouseListener);
            }

            // Left click works as usual, right click brings up a pop-up menu.
            tabPane.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    JTabbedPane tabPane = (JTabbedPane) e.getSource();

                    int tabIndex = tabPane.getUI().tabForCoordinate(tabPane, e.getX(), e.getY());

                    if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex))
                    {
                        if (tabIndex == tabPane.getSelectedIndex())
                        {
                            if (tabPane.isRequestFocusEnabled())
                            {
                                tabPane.requestFocus();

                                tabPane.repaint(tabPane.getUI().getTabBounds(tabPane, tabIndex));
                            }
                        } else
                        {
                            tabPane.setSelectedIndex(tabIndex);
                        }

                        if (!tabPane.isEnabled() || SwingUtilities.isRightMouseButton(e))
                        {
                            final JPopupMenu popupMenu = new JPopupMenu();

                            final JMenuItem popWindow = new JMenuItem("Pop window out");
                            popWindow.addActionListener(e1 ->
                            {
                                final int selectedIndex = tabPane.getSelectedIndex();
                                final String tabName = tabPane.getTitleAt(selectedIndex);

                                // Pops up the selected tab.
                                final NetworkDesignWindow networkDesignWindow = NetworkDesignWindow.parseString(tabName);

                                if (networkDesignWindow != null)
                                {
                                    switch (networkDesignWindow)
                                    {
                                        case offline:
                                            windowController.showOfflineWindow(true);
                                            break;
                                        case online:
                                            windowController.showOnlineWindow(true);
                                            break;
                                        case whatif:
                                            windowController.showWhatifWindow(true);
                                            break;
                                        case report:
                                            windowController.showReportWindow(true);
                                            break;
                                        default:
                                            return;
                                    }
                                }

                                tabPane.setSelectedIndex(0);
                            });

                            // Disabling the pop up button for the network state tab.
                            if (NetworkDesignWindow.parseString(tabPane.getTitleAt(tabPane.getSelectedIndex())) == NetworkDesignWindow.network)
                            {
                                popWindow.setEnabled(false);
                            }

                            popupMenu.add(popWindow);

                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });

            // Building windows
            this.tableControlWindow = new GUIWindow(tabPane)
            {
                @Override
                public String getTitle()
                {
                    return "Net2Plan - Design tables and control window";
                }
            };

            // Building tab controller
            this.windowController = new WindowController(executionPane, null, whatIfAnalysisPane, reportPane);

            addKeyCombinationActions();
            updateVisualizationAfterNewTopology();
        }

        public void connectToOpenStack(String user, String password)
        {
            this.currentOpenStackNet = OpenStackNet.buildOpenStackNetFromServer(password, user, password, user);
        }

        public OpenStackNet getOpenStackNet() { return this.currentOpenStackNet; }
        private JPanel configureLeftBottomPanel()
        {
            this.focusPanel = new FocusPane(this);
            final JPanel focusPanelContainer = new JPanel(new BorderLayout());
            final JToolBar navigationToolbar = new JToolBar(JToolBar.VERTICAL);
            navigationToolbar.setRollover(true);
            navigationToolbar.setFloatable(false);
            navigationToolbar.setOpaque(false);

            final JButton btn_pickNavigationUndo, btn_pickNavigationRedo;

            btn_pickNavigationUndo = new JButton("");
            btn_pickNavigationUndo.setIcon(new ImageIcon(TopologyPanel.class.getResource("/resources/gui/undoPick.png")));
            btn_pickNavigationUndo.setToolTipText("Navigate back to the previous element picked");
            btn_pickNavigationRedo = new JButton("");
            btn_pickNavigationRedo.setIcon(new ImageIcon(TopologyPanel.class.getResource("/resources/gui/redoPick.png")));
            btn_pickNavigationRedo.setToolTipText("Navigate forward to the next element picked");

            final ActionListener action = e ->
            {
                Object backOrForward;
                do
                {
                    backOrForward = (e.getSource() == btn_pickNavigationUndo) ? GUINetworkDesign.this.getVisualizationState().getPickNavigationBackElement() : GUINetworkDesign.this.getVisualizationState().getPickNavigationForwardElement();
                    if (backOrForward == null) break;
                    final NetworkElement ne = backOrForward instanceof NetworkElement ? (NetworkElement) backOrForward : null; // For network elements
                    final Pair<Demand, Link> fr = backOrForward instanceof Pair ? (Pair) backOrForward : null; // For forwarding rules
                    if (ne != null)
                    {
                        if (ne.getNetPlan() != GUINetworkDesign.this.getDesign()) continue;
                        if (ne.getNetPlan() == null) continue;
                        break;
                    } else if (fr != null)
                    {
                        if (fr.getFirst().getNetPlan() != GUINetworkDesign.this.getDesign()) continue;
                        if (fr.getFirst().getNetPlan() == null) continue;
                        if (fr.getSecond().getNetPlan() != GUINetworkDesign.this.getDesign()) continue;
                        if (fr.getSecond().getNetPlan() == null) continue;
                        break;
                    } else break; // null,null => reset picked state
                } while (true);
                if (backOrForward != null)
                {
                    final NetworkElement ne = backOrForward instanceof NetworkElement ? (NetworkElement) backOrForward : null; // For network elements
                    final Pair<Demand, Link> fr = backOrForward instanceof Pair ? (Pair) backOrForward : null; // For forwarding rules

                    if (ne != null)
                        GUINetworkDesign.this.getVisualizationState().pickElement(ne);
                    else if (fr != null)
                        GUINetworkDesign.this.getVisualizationState().pickForwardingRule(fr);
                    else GUINetworkDesign.this.getVisualizationState().resetPickedState();

                    GUINetworkDesign.this.updateVisualizationAfterPick();
                }
            };

            btn_pickNavigationUndo.addActionListener(action);
            btn_pickNavigationRedo.addActionListener(action);

            btn_pickNavigationRedo.setFocusable(false);
            btn_pickNavigationUndo.setFocusable(false);

            navigationToolbar.add(btn_pickNavigationUndo);
            navigationToolbar.add(btn_pickNavigationRedo);

            final JScrollPane scPane = new JScrollPane(focusPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scPane.getVerticalScrollBar().setUnitIncrement(20);
            scPane.getHorizontalScrollBar().setUnitIncrement(20);
            scPane.setBorder(BorderFactory.createEmptyBorder());

            // Control the scroll
            scPane.getHorizontalScrollBar().addAdjustmentListener(e ->
            {
                // Repaints the panel each time the horizontal scroll bar is moves, in order to avoid ghosting.
                focusPanelContainer.revalidate();
                focusPanelContainer.repaint();
            });

            focusPanelContainer.add(navigationToolbar, BorderLayout.WEST);
            focusPanelContainer.add(scPane, BorderLayout.CENTER);

            JPanel pane = new JPanel(new MigLayout("fill, insets 0 0 0 0"));
            pane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.BLACK), "Focus panel"));

            pane.add(focusPanelContainer, "grow");
            return pane;
        }

        @Override
        public String getDescription()
        {
            return getName();
        }

        @Override
        public KeyStroke getKeyStroke()
        {
            return KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
        }

        @Override
        public String getMenu()
        {

            return "Tools|" + TITLE;
        }

        @Override
        public String getName()
        {
            return TITLE + " (GUI)";
        }

        @Override
        public List<Triple<String, String, String>> getParameters()
        {
            return null;
        }

        @Override
        public int getPriority()
        {
            return Integer.MAX_VALUE;
        }


        public NetPlan getDesign()
        {
            return currentNetPlan;
        }

        public NetPlan getInitialDesign()
        {
            return null;
        }

        public WhatIfAnalysisPane getWhatIfAnalysisPane()
        {
            return whatIfAnalysisPane;
        }

        public void addNetPlanChange()
        {
            undoRedoManager.addNetPlanChange();
        }

        public void requestUndoAction()
        {

            final Triple<NetPlan, Map<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> back = undoRedoManager.getNavigationBackElement();
            if (back == null) return;
            this.currentNetPlan = back.getFirst();
            this.vs.setCanvasLayerVisibilityAndOrder(this.currentNetPlan, back.getSecond(), back.getThird());
            updateVisualizationAfterNewTopology();
        }

        public void requestRedoAction()
        {

            final Triple<NetPlan, Map<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> forward = undoRedoManager.getNavigationForwardElement();
            if (forward == null) return;
            this.currentNetPlan = forward.getFirst();
            this.vs.setCanvasLayerVisibilityAndOrder(this.currentNetPlan, forward.getSecond(), forward.getThird());
            updateVisualizationAfterNewTopology();
        }

        public void setDesign(NetPlan netPlan)
        {
            if (ErrorHandling.isDebugEnabled()) netPlan.checkCachesConsistency();
            this.currentNetPlan = netPlan;
        }


        public VisualizationState getVisualizationState()
        {
            return vs;
        }

        public void showTableControlWindow()
        {
            tableControlWindow.showWindow(true);
        }

        private void resetButton()
        {
            try
            {
                final int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset? This will remove all unsaved data", "Reset", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) return;


                setDesign(new NetPlan());
                //algorithmSelector.reset();
                executionPane.reset();

//            reportSelector.reset();
//            reportContainer.removeAll();
            } catch (Throwable ex)
            {
                ErrorHandling.addErrorOrException(ex, GUINetworkDesign.class);
                ErrorHandling.showErrorDialog("Unable to reset");
            }
            Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> res = VisualizationState.generateCanvasDefaultVisualizationLayerInfo(getDesign());
            vs.setCanvasLayerVisibilityAndOrder(getDesign(), res.getFirst(), res.getSecond());
            updateVisualizationAfterNewTopology();
            undoRedoManager.addNetPlanChange();
        }

        public void resetPickedStateAndUpdateView()
        {
            vs.resetPickedState();
            topologyPanel.getCanvas().cleanSelection();
            viewEditTopTables.resetPickedState();
        }

        /**
         * Shows the tab corresponding associated to a network element.
         *
         * @param type   Network element type
         * @since 0.3.0
         */
        @SuppressWarnings("unchecked")
        private void selectNetPlanViewItem(AJTableType type)
        {

            viewEditTopTables.selectItemTab(type);
        }

        /**
         * Indicates whether or not the initial {@code NetPlan} object is stored to be
         * compared with the current one (i.e. after some simulation steps).
         *
         * @return {@code true} if the initial {@code NetPlan} object is stored. Otherwise, {@code false}.
         * @since 0.3.0
         */


        private void addKeyCombinationActions()
        {
            addKeyCombinationAction("Resets the tool", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    resetButton();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));

            addKeyCombinationAction("Outputs current design to console", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    System.out.println(getDesign().toString());
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK));

            /* FROM THE OFFLINE ALGORITHM EXECUTION */

            addKeyCombinationAction("Execute algorithm", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    executionPane.doClickInExecutionButton();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));

            /* FROM REPORT */
            addKeyCombinationAction("Close selected report", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int tab = reportPane.getReportContainer().getSelectedIndex();
                    if (tab == -1) return;
                    reportPane.getReportContainer().remove(tab);
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));

            addKeyCombinationAction("Close all reports", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    reportPane.getReportContainer().removeAll();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));


            /* Online simulation */
            addKeyCombinationAction("Run simulation", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {


                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));

            // Windows
            addKeyCombinationAction("Show control window", new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    tableControlWindow.showWindow(true);
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));

            GUINet2Plan.addGlobalActions(this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), this.getActionMap());

            viewEditTopTables.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
            viewEditTopTables.setActionMap(this.getActionMap());

            reportPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
            reportPane.setActionMap(this.getActionMap());

            executionPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
            executionPane.setActionMap(this.getActionMap());

            whatIfAnalysisPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
            whatIfAnalysisPane.setActionMap(this.getActionMap());
        }

        public void putTransientColorInElementTopologyCanvas(Collection<? extends NetworkElement> linksAndNodes, Color color)
        {
            for (NetworkElement e : linksAndNodes)
            {
                if (e instanceof Link)
                {
                    final GUILink gl = vs.getCanvasAssociatedGUILink((Link) e);
                    if (gl != null)
                    {
                        gl.setEdgeDrawPaint(color);
                    }
                } else if (e instanceof Node)
                {
                    for (GUINode gn : vs.getCanvasVerticallyStackedGUINodes((Node) e))
                    {
                        gn.setBorderPaint(color);
                        gn.setFillPaint(color);
                    }
                } else throw new RuntimeException();
            }

            resetPickedStateAndUpdateView();
        }

        public void updateVisualizationAfterPick()
        {
            final NetworkElementType type = vs.getPickedElementType();
            topologyPanel.getCanvas().refresh(); // needed with or w.o. pick, since maybe you unpick with an undo
            topologyPanel.updateTopToolbar();
            focusPanel.updateView();
        }

        public void updateVisualizationAfterNewTopology()
        {
            vs.updateTableRowFilter(null, null);
            topologyPanel.updateMultilayerPanel();
            topologyPanel.getCanvas().rebuildGraph();
            topologyPanel.getCanvas().zoomAll();
            viewEditTopTables.updateView();
            focusPanel.updateView();
        }

        public void updateVisualizationAfterCanvasState()
        {
            topologyPanel.updateTopToolbar();
        }

        public void updateVisualizationJustCanvasLinkNodeVisibilityOrColor()
        {
            topologyPanel.getCanvas().refresh();
        }

        public void updateVisualizationAfterChanges(Set<NetworkElementType> modificationsMade)
        {
            if (modificationsMade == null)
            {
                throw new RuntimeException("Unable to update non-existent network elements");
            }

            if (modificationsMade.contains(NetworkElementType.LAYER))
            {
                topologyPanel.updateMultilayerPanel();
                topologyPanel.getCanvas().rebuildGraph();
                viewEditTopTables.updateView();
                focusPanel.updateView();
            } else if ((modificationsMade.contains(NetworkElementType.LINK) || modificationsMade.contains(NetworkElementType.NODE) || modificationsMade.contains(NetworkElementType.LAYER)))
            {
                topologyPanel.getCanvas().rebuildGraph();
                viewEditTopTables.updateView();
                focusPanel.updateView();
            } else
            {
                viewEditTopTables.updateView();
                focusPanel.updateView();
            }
        }

        public void runCanvasOperation(CanvasFunction operation)
        {
            switch (operation)
            {
                case ZOOM_ALL:
                    topologyPanel.getCanvas().zoomAll();
                    break;
                case ZOOM_IN:
                    topologyPanel.getCanvas().zoomIn();
                    break;
                case ZOOM_OUT:
                    topologyPanel.getCanvas().zoomOut();
                    break;
            }
        }

        public void updateVisualizationJustTables()
        {
            viewEditTopTables.updateView();
        }


        private class WindowController
        {
            private GUIWindow reportWindow;
            private GUIWindow offlineWindow;
            private GUIWindow onlineWindow;
            private GUIWindow whatifWindow;

            private final JComponent offlineWindowComponent, onlineWindowComponent;
            private final JComponent whatitWindowComponent, reportWindowComponent;

            WindowController(final JComponent offlineWindowComponent, final JComponent onlineWindowComponent, final JComponent whatifWindowComponent, final JComponent reportWindowComponent)
            {
                this.offlineWindowComponent = offlineWindowComponent;
                this.onlineWindowComponent = onlineWindowComponent;
                this.whatitWindowComponent = whatifWindowComponent;
                this.reportWindowComponent = reportWindowComponent;
            }

            private void buildOfflineWindow(final JComponent component)
            {
                final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.offline);

                offlineWindow = new GUIWindow(component)
                {
                    @Override
                    public String getTitle()
                    {
                        return "Net2Plan - " + tabName;
                    }
                };

                offlineWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
            }

            void showOfflineWindow(final boolean gainFocus)
            {
                buildOfflineWindow(offlineWindowComponent);

                if (offlineWindow != null)
                {
                    offlineWindow.showWindow(gainFocus);
                    offlineWindow.setLocationRelativeTo(tableControlWindow);
                }
            }

            private void buildOnlineWindow(final JComponent component)
            {
                final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.online);

                onlineWindow = new GUIWindow(component)
                {
                    @Override
                    public String getTitle()
                    {
                        return "Net2Plan - " + tabName;
                    }
                };

                onlineWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
            }

            void showOnlineWindow(final boolean gainFocus)
            {
                buildOnlineWindow(onlineWindowComponent);

                if (onlineWindow != null)
                {
                    onlineWindow.showWindow(gainFocus);
                    onlineWindow.setLocationRelativeTo(tableControlWindow);
                }
            }

            private void buildWhatifWindow(final JComponent component)
            {
                final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.whatif);

                whatifWindow = new GUIWindow(component)
                {
                    @Override
                    public String getTitle()
                    {
                        return "Net2Plan - " + tabName;
                    }
                };

                whatifWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
            }

            void showWhatifWindow(final boolean gainFocus)
            {
                buildWhatifWindow(whatitWindowComponent);
                if (whatifWindow != null)
                {
                    whatifWindow.showWindow(gainFocus);
                    whatifWindow.setLocationRelativeTo(tableControlWindow);
                }
            }

            private void buildReportWindow(final JComponent component)
            {
                final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.report);

                reportWindow = new GUIWindow(component)
                {
                    @Override
                    public String getTitle()
                    {
                        return "Net2Plan - " + tabName;
                    }
                };

                reportWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
            }

            void showReportWindow(final boolean gainFocus)
            {
                buildReportWindow(reportWindowComponent);
                if (reportWindow != null)
                {
                    reportWindow.showWindow(gainFocus);
                    reportWindow.setLocationRelativeTo(tableControlWindow);
                }
            }

            void hideAllWindows()
            {
                if (offlineWindow != null)
                    offlineWindow.dispatchEvent(new WindowEvent(offlineWindow, WindowEvent.WINDOW_CLOSING));
                if (onlineWindow != null)
                    onlineWindow.dispatchEvent(new WindowEvent(onlineWindow, WindowEvent.WINDOW_CLOSING));
                if (whatifWindow != null)
                    whatifWindow.dispatchEvent(new WindowEvent(whatifWindow, WindowEvent.WINDOW_CLOSING));
                if (reportWindow != null)
                    reportWindow.dispatchEvent(new WindowEvent(reportWindow, WindowEvent.WINDOW_CLOSING));
            }

            private class CloseWindowAdapter extends WindowAdapter
            {
                private final String tabName;
                private final JComponent component;

                private final NetworkDesignWindow[] tabCorrectOrder =
                        {NetworkDesignWindow.network, NetworkDesignWindow.offline, NetworkDesignWindow.online, NetworkDesignWindow.whatif, NetworkDesignWindow.report};

                CloseWindowAdapter(final String tabName, final JComponent component)
                {
                    this.tabName = tabName;
                    this.component = component;
                }

                @Override
                public void windowClosing(WindowEvent e)
                {
                    addTabToControlWindow(tabName, component);
                }

                private void addTabToControlWindow(final String newTabName, final JComponent newTabComponent)
                {
                    final JTabbedPane tabPane = (JTabbedPane) tableControlWindow.getInnerComponent();

                    final Map<String, Component> toSortTabs = new HashMap<>();
                    toSortTabs.put(newTabName, newTabComponent);

                    for (int i = 0; i < tabPane.getTabCount(); i = 0)
                    {
                        toSortTabs.put(tabPane.getTitleAt(i), tabPane.getComponentAt(i));
                        tabPane.remove(i);
                    }

                    for (int i = 0; i < tabCorrectOrder.length; i++)
                    {
                        final String tabName = NetworkDesignWindow.getWindowName(tabCorrectOrder[i]);

                        if (toSortTabs.containsKey(tabName))
                        {
                            final Component tabComponent = toSortTabs.get(tabName);

                            tabPane.addTab(tabName, tabComponent);
                        }
                    }
                }
            }
        }
    }