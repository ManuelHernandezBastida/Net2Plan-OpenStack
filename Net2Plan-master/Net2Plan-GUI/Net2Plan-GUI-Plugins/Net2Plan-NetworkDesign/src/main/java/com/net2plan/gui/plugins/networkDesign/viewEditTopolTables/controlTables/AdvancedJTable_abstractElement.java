package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.ActionMap;
import javax.swing.DefaultRowSorter;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.CellRenderers;
import com.net2plan.gui.plugins.networkDesign.LastRowAggregatedValue;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNet;
import com.net2plan.gui.plugins.networkDesign.TableColumnComparator;
import com.net2plan.gui.utils.AdvancedJTable;
import com.net2plan.gui.utils.ClassAwareTableModel;
import com.net2plan.gui.utils.ColumnHeaderToolTips;
import com.net2plan.gui.utils.FixedColumnDecorator;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.utils.Pair;


/**
 */
@SuppressWarnings("unchecked")
public abstract class AdvancedJTable_abstractElement<T> extends AdvancedJTable
{
    public static enum AGTYPE
    {
        NOAGGREGATION((v,a)->0.0) , MAXDOUBLE((v,a)->Math.max((Double)v, (Double)a)) , SUMDOUBLE ((v,a)->(Double)v+(Double)a) ,
        MAXINT((v,a)->Math.max((Integer)v, (Double)a)) ,
        SUMINT ((v,a)->(Integer)v+(Double)a) ,
        SUMCOLLECTIONCOUNT((v,a)->((Collection<?>)v).size()+(Double)a),
        COUNTTRUE((v,a)->((Boolean) v)? ((Double)a) + 1 : (Double)a);
        private BiFunction<Object,Object,Number> aggregationFunction;
        private AGTYPE (BiFunction<Object,Object,Number> aggregationFunction) { this.aggregationFunction = aggregationFunction; }
        public final boolean isToAggregate () { return this != NOAGGREGATION; }
        public final Number agg (Object value , Number previousAggregate) { return aggregationFunction.apply(value, previousAggregate); }
    };

    //    protected final NetworkElementType networkElementType;
    protected final GUINetworkDesign callback;
    private final JScrollPane tableScrollPane;
    private FixedColumnDecorator decorator;
    private final Map<AjtColumnInfo,Pair<Integer,Integer>> widthAndGlobalViewIndexIfPreviouslyVisible = new HashMap<> ();
    private String control_currentViewTypeForColumns;
    private final boolean hasAggregationRow;
    private final String tableTitle;

    /**
     * Constructor that allows to set the table model.
     *
     * @param networkViewer Network callback
     * @since 0.2.0
     */
    public AdvancedJTable_abstractElement(GUINetworkDesign networkViewer, String tableTitle , boolean hasAggregationRow)
    {
        super();
        this.hasAggregationRow = hasAggregationRow;
        this.callback = networkViewer;
        this.control_currentViewTypeForColumns = null;
        this.tableScrollPane = new JScrollPane(this);
        this.tableTitle = tableTitle;
        this.addMouseListener(new AjTableMouseAdapter());
        this.addMouseMotionListener(new AjTableMouseMoveAdapter());
    }

    protected abstract void reactToMouseClickInTable (int numClicks , int rowModelIndexOfClickOrMinus1IfOut , int columnModelIndexOfClickOrMinus1IfOut);

    protected abstract void reactToMouseMoveInTable (int rowModelIndexOfClickOrMinus1IfOut , int columnModelIndexOfClickOrMinus1IfOut);


    protected abstract void addExtendedKeyboardActions();

    public JScrollPane getTableScrollPane()
    {
        return tableScrollPane;
    }

    private int getViewIndexInUnifiedView (AjtColumnInfo<T> col)
    {
        final Pair<TableColumn,Boolean> tcInfo = getTableColumnObject(col);
        if (tcInfo.getFirst() == null) return -1;
        final JTable jtable = tcInfo.getSecond()? this.decorator.getFixedTable() : this.decorator.getMainTable();
        final int viewIndexWithinJtable = jtable.convertColumnIndexToView(tcInfo.getFirst().getModelIndex());
        final int viewIndexInUnified = tcInfo.getSecond()? viewIndexWithinJtable : this.decorator.getFrozenColumns() + viewIndexWithinJtable;
        return viewIndexInUnified;
    }

    public final void updateView()
    {
        this.setEnabled(false);

        /* Save current width of columns */
        for (AjtColumnInfo<T> col : this.getColumnsInfo(false))
        {
            final Pair<TableColumn,Boolean> tcInfo = getTableColumnObject(col);
            if (tcInfo.getFirst() == null) continue;
            final int widthPixels = tcInfo.getFirst().getWidth();
            final int indexInViewGlobal = getViewIndexInUnifiedView(col);
            this.widthAndGlobalViewIndexIfPreviouslyVisible.put(col , Pair.of(widthPixels,indexInViewGlobal));
        }

        final List<AjtColumnInfo<T>> visibleTableColumnsInTableModelOrder = this.getColumnsInfo(true);
        final List<AjtColumnInfo<T>> visibleTableColumnsToBeInUnifiedViewOrder = visibleTableColumnsInTableModelOrder.stream().
                sorted((c1,c2)->
                {
                  final int indexView1 = widthAndGlobalViewIndexIfPreviouslyVisible.getOrDefault(c1 , Pair.of(-1, Integer.MAX_VALUE)).getSecond();
                    final int indexView2 = widthAndGlobalViewIndexIfPreviouslyVisible.getOrDefault(c2 , Pair.of(-1, Integer.MAX_VALUE)).getSecond();
                    final int comp1 = Integer.compare(indexView1, indexView2);
                    if (comp1 != 0) return comp1;
                    return Integer.compare(c1.getColumnIndexOriginallyUserDefined(), c2.getColumnIndexOriginallyUserDefined());
                }).
                collect(Collectors.toCollection(ArrayList::new));
        final Object[][] dataVector = computeDataVector (visibleTableColumnsInTableModelOrder);
        this.setModel(createTableModel(visibleTableColumnsInTableModelOrder));//this.getModel();
        final String[] tableHeaders = this.getTableHeaders(visibleTableColumnsInTableModelOrder);
        if (dataVector.length == 0)
            this.getModel().setDataVector(new Object[1][tableHeaders.length], tableHeaders);
        else
            this.getModel().setDataVector(dataVector, tableHeaders);

        /* Create initial column model: all in one table.
         * The fixed/scroll decorator will change this column model */
        final TableModel m = getModel();
        if (m != null)
        {
            // Remove any current columns
            final TableColumnModel cm = getColumnModel();
            while (cm.getColumnCount() > 0) cm.removeColumn(cm.getColumn(0));

            /* Restore the columns width and view order. */
            for (int indexView = 0 ; indexView < visibleTableColumnsToBeInUnifiedViewOrder.size() ; indexView ++)
            {
                final AjtColumnInfo<T> col = visibleTableColumnsToBeInUnifiedViewOrder.get(indexView);
                final int indexModel = col.getColumnIndexInTableModelWhenVissible();
                assert indexModel >= 0;
                final TableColumn newColumn = new TableColumn(indexModel);
                addColumn(newColumn);
                if (this.widthAndGlobalViewIndexIfPreviouslyVisible.containsKey(col))
                    newColumn.setPreferredWidth(widthAndGlobalViewIndexIfPreviouslyVisible.get(col).getFirst());
            }
        }

        /* Create the two column models, one for fixed and scrollable subtables */
        this.decorator = new FixedColumnDecorator(tableScrollPane, 2);

        /* Get user key strokes, put them in the two tables */
        this.addExtendedKeyboardActions();
        final InputMap keyStrokesSavedInMainTable = this.getInputMap();
        final ActionMap actionMapSavedInMainTable = this.getActionMap();
        for (KeyStroke keyStroke : keyStrokesSavedInMainTable.allKeys())
        {
            if (keyStroke == KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK)) continue;
            final Object o = keyStrokesSavedInMainTable.get(keyStroke);
            this.decorator.getFixedTable().getInputMap().put(keyStroke, keyStrokesSavedInMainTable.get(keyStroke));
            this.decorator.getFixedTable().getActionMap().put(o, actionMapSavedInMainTable.get(o));
        }

        this.setCellRenders(visibleTableColumnsInTableModelOrder);
        this.setRowSelectionAllowed(true);
        this.getTableHeader().setReorderingAllowed(true);
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setColumnRowSorting();
        this.getRowSorter().setSortKeys(this.getRowSorter().getSortKeys());
        this.setTips(visibleTableColumnsInTableModelOrder);
        this.getModel().fireTableDataChanged();


        /* Enable the table before returning */
        this.setEnabled(true);
    }

    public final SortedSet<T> getSelectedElements()
    {
        final int[] rowViewIndexes = this.getSelectedRows();
        final OpenStackNet os = callback.getOpenStackNet();

        final SortedSet<T> elements = new TreeSet<> ();

        if (rowViewIndexes.length != 0)
        {
            final int maxValidRowIndex = this.getModel().getRowCount() - 1 - (hasAggregationRow ? 1 : 0);
            final List<Integer> validRows = new ArrayList<>();
            for (int a : rowViewIndexes) if ((a >= 0) && (a <= maxValidRowIndex)) validRows.add(a);

            for (int rowViewIndex : validRows)
            {
                final int viewRowIndex = this.convertRowIndexToModel(rowViewIndex);
                final String id = (String) getModel().getValueAt(viewRowIndex, 0);
                elements.add((T) os.getOpenStackNetworkElementByOpenStackId(id));
            }
        }

        return elements;
    }

    protected abstract List<T> getAllAbstractElementsInTable();

    @Override
    public DefaultTableModel getModel()
    {
        return ((DefaultTableModel) super.getModel());
    }

    private void setCellRenders(List<AjtColumnInfo<T>> tableColumns)
    {
        Function<Object,Color> allRowMandatorySpetialBgColorIfNotSelected = null;
        AjtColumnInfo<T> baseColumnForRowMandatorySpetialBgColorIfNotSelected = null;
        final JTable fixedTable = decorator.getFixedTable();
        final JTable mainTable = decorator.getMainTable();
        for (AjtColumnInfo<T> col : tableColumns)
        {
            final TableCellRenderer render;
            if (col.getValueShownIfNotAggregation().equals(Boolean.class))
                render = new AjtCellRenderers.CheckBoxRenderer (col);
            else
                render = new AjtCellRenderers.JLabelRenderer(callback , col);
            if (allRowMandatorySpetialBgColorIfNotSelected == null && col.getGetAllRowMandatorySpetialBgColorIfNotSelected() != null)
            {
                allRowMandatorySpetialBgColorIfNotSelected = col.getGetAllRowMandatorySpetialBgColorIfNotSelected();
                baseColumnForRowMandatorySpetialBgColorIfNotSelected = col;
            }
            final TableColumn tableColumn = getTableColumnObject(col).getFirst();
            if (tableColumn == null)
                assert false;
            tableColumn.setCellRenderer(render);
        }
        if (allRowMandatorySpetialBgColorIfNotSelected != null)
            for (AjtColumnInfo<T> col : tableColumns)
                col.setGetAllRowMandatorySpetialBgColorIfNotSelected(allRowMandatorySpetialBgColorIfNotSelected, baseColumnForRowMandatorySpetialBgColorIfNotSelected);
        assert fixedTable != null;

        fixedTable.getTableHeader().setDefaultRenderer(new CellRenderers.FixedTableHeaderRenderer());

    }

    private void setColumnRowSorting()
    {
        this.setAutoCreateRowSorter(true);

        final DefaultRowSorter rowSorter = ((DefaultRowSorter) this.getRowSorter());
        for (int col = 0; col < this.getModel().getColumnCount(); col++)
            rowSorter.setComparator(col, new TableColumnComparator(rowSorter));
        if (this.decorator.getFixedTable() != null)
            this.decorator.getFixedTable().setRowSorter(this.getRowSorter());
    }

    private TableColumnModel getFixedLeftTableColumnModel ()
    {
        return this.decorator.getFixedTable().getColumnModel();
    }
    private TableColumnModel getScrollableRightTableColumnModel ()
    {
        return this.decorator.getMainTable().getColumnModel();
    }


    private void setTips(List<AjtColumnInfo<T>> tableColumnsInModel)
    {
        /* configure the tips */
        String[] columnTips = this.getTableTips(tableColumnsInModel);
        String[] columnHeader = this.getTableHeaders(tableColumnsInModel);
        if (columnTips.length != columnHeader.length)
            throw new IllegalArgumentException("Number of tips does not match the number of columns");
        final ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
        for (AjtColumnInfo<T> col : tableColumnsInModel)
        {
            final TableColumn tableCol = getTableColumnObject(col).getFirst();
            tips.setToolTip(tableCol, columnTips[col.getColumnIndexInTableModelWhenVissible()]);
        }
        getTableHeader().addMouseMotionListener(tips);
    }

    protected abstract void showPopup (MouseEvent me);

    public abstract T getElementAtModelRowIndex(int rowModelIndex);


    protected abstract List<AjtColumnInfo<T>> getAllColumnsVisibleOrNot ();

    private List<AjtColumnInfo<T>> getColumnsInfo (boolean onlyVisible)
    {
        final List<AjtColumnInfo<T>> completeListIncludingCommonColumns = new ArrayList<> ();
        completeListIncludingCommonColumns.addAll(getAllColumnsVisibleOrNot());
        final List<AjtColumnInfo<T>> res = new ArrayList<> ();
        int contVisible = 0;
        int contVisibleOrNot = 0;
        for (AjtColumnInfo col : completeListIncludingCommonColumns)
        {
            col.setColumnIndexOriginallyUserDefined(contVisibleOrNot ++);
            final boolean couldBeVisible = col.isVisibleInView(this.control_currentViewTypeForColumns);
            if (onlyVisible && !couldBeVisible)
            {
                col.setColumnIndexInTableModelWhenVissible(-1);
                continue;
            }
            else if (onlyVisible && couldBeVisible)
            {
                col.setColumnIndexInTableModelWhenVissible(contVisible ++);
            }
            else if (!onlyVisible)
            {
                // do not touch the visibility index
            }
            res.add(col);
        }
        return res;
    }

    private class AjTableMouseMoveAdapter extends MouseAdapter
    {
        @Override
        public void mouseMoved(MouseEvent e)
        {
            try
            {
                final AdvancedJTable_abstractElement<T> parent = AdvancedJTable_abstractElement.this;
                final boolean clickFixedTable = parent.decorator.getFixedTable() == e.getComponent();
                final boolean clickOutside = rowAtPoint(e.getPoint()) == -1;
                final int rowModelIndex = clickOutside || clickFixedTable? -1 : parent.convertRowIndexToModel(rowAtPoint(e.getPoint()));
                final int columnModelIndex = clickOutside || clickFixedTable? -1 : parent.convertColumnIndexToModel(columnAtPoint(e.getPoint()));
                reactToMouseMoveInTable (rowModelIndex , columnModelIndex);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                ErrorHandling.showErrorDialog("The GUI has suffered a problem.\nPlease see the console for more information.", "Error");
            }
        }
    }

    private class AjTableMouseAdapter extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            try
            {
                final AdvancedJTable_abstractElement<T> parent = AdvancedJTable_abstractElement.this;
//                final SortedSet<T> selection = parent.getSelectedElements();
                if (SwingUtilities.isRightMouseButton(e))
                {
                    parent.showPopup(e);
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    final boolean clickFixedTable = parent.decorator.getFixedTable() == e.getComponent();
                    final boolean clickOutside = rowAtPoint(e.getPoint()) == -1;
                    final int rowModelIndex = clickOutside || clickFixedTable? -1 : parent.convertRowIndexToModel(rowAtPoint(e.getPoint()));
                    final int columnModelIndex = clickOutside || clickFixedTable? -1 : parent.convertColumnIndexToModel(columnAtPoint(e.getPoint()));
                    reactToMouseClickInTable (e.getClickCount(), rowModelIndex , columnModelIndex);
//
//
//                    if (e.getClickCount() == 1)
//                    {
//                    	if (selection.isEmpty()) callback.resetPickState();
//
//                    	if(rowAtPoint(e.getPoint()) == -1) return;
//                        final int rowViewIndex = parent.convertRowIndexToModel(rowAtPoint(e.getPoint()));
//
//                        if (parent.decorator.getFixedTable() == e.getComponent()) return;
//
//                        final int columnViewIndex = parent.convertColumnIndexToModel(columnAtPoint(e.getPoint()));
//
//                        final Object value = getModel().getValueAt(rowViewIndex, columnViewIndex);
//                        if (value instanceof MtnNetworkElement)
//                        {
//                            callback.getVisualizationState().pickElement((MtnNetworkElement) value);
//                            callback.updateVisualizationAfterPick();
//                        }
//                        else if (value instanceof List)
//                        {
//                        	if (((List) value).isEmpty()) return;
//
//                        	callback.getVisualizationState().pickElement((List<? extends MtnNetworkElement>) value, null);
//                            callback.updateVisualizationAfterPick();
//                        }
//                        else if (selection.isEmpty()) callback.resetPickState();
//                    } else if (e.getClickCount() >= 2)
//                    {
//                        SwingUtilities.invokeLater(() -> pickSelection(selection));
//                    }
//
                    return;
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                ErrorHandling.showErrorDialog("The GUI has suffered a problem.\nPlease see the console for more information.", "Error");
            }
        }
    }

//    private final Set<T> invertSelection(Set<T> selectedElements)
//    {
//        // Check all elements belong to the same NetPlan
//        final Mtn netPlan = this.callback.getDesign();
//        for (MtnNetworkElement networkElement : selectedElements)
//        {
//            if (networkElement.getMtn() != netPlan) return null;
//            if (AJTableType.getTypeOfElement(networkElement) != ajtType) return null;
//        }
//        final List<MtnNetworkElement> allElements = ITableRowFilterTableType.getAllElements(netPlan , ajtType);
//        Set<T> invertedElements = new HashSet<> ((List<T>) (List<?>) allElements);
//        invertedElements.removeAll(selectedElements);
//        return invertedElements;
//    }

    private final DefaultTableModel createTableModel(List<AjtColumnInfo<T>> tableColumns)
    {
        final int C = tableColumns.size();
        final DefaultTableModel tableModel = new ClassAwareTableModel(new Object[1][C], getTableHeaders(tableColumns))
        {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };
        return tableModel;
    }
    private String[] getTableHeaders(List<AjtColumnInfo<T>> tableColumns)
    {
        final String [] res = new String [tableColumns.size()];
        int cont = 0; for (AjtColumnInfo c : tableColumns) res [cont ++] = c.getHeader(); return res;
    }
    private String[] getTableTips(List<AjtColumnInfo<T>> tableColumns)
    {
        final String [] res = new String [tableColumns.size()];
        int cont = 0; for (AjtColumnInfo c : tableColumns) res [cont ++] = c.getTooltip(); return res;
    }


//    protected Double dialogGetDouble (String message , String title )
//    {
//      final String str = JOptionPane.showInputDialog(null, message , title , JOptionPane.QUESTION_MESSAGE);
//      if (str == null) return null;
//      return Double.parseDouble(str);
//    }
//    protected Integer dialogGetInteger (String message , String title )
//    {
//      final String str = JOptionPane.showInputDialog(null, message , title , JOptionPane.QUESTION_MESSAGE);
//      if (str == null) return null;
//      return Integer.parseInt(str);
//    }
//    protected String dialogGetString (String message , String title )
//    {
//      final String val = JOptionPane.showInputDialog(null, message , title , JOptionPane.QUESTION_MESSAGE);
//      return val;
//    }


    protected JComponent processMenu (AjtRcMenu popupInfo , List<T> visibleElementsInTable)
    {
        if (popupInfo.isSeparator())  { return new JPopupMenu.Separator(); }
        if (popupInfo.hasSubmenus())
        {
            final JMenu menu = new JMenu(popupInfo.getMenuMessage());
            menu.setEnabled(popupInfo.getIsMenuEnabledFunction().apply(visibleElementsInTable.size(), getSelectedRows().length));
            for (AjtRcMenu submenuInfo : popupInfo.getSubmenus())
            {
                final JComponent submenuItem = this.processMenu (submenuInfo , visibleElementsInTable);
                menu.add(submenuItem);
            }
            return menu;
        }


        final JMenuItem menu = new JMenuItem(popupInfo.getMenuMessage());
        menu.setEnabled(popupInfo.getIsMenuEnabledFunction().apply(visibleElementsInTable.size(), getSelectedRows().length));
        menu.addActionListener( e->
        {
            try
            {
                if (!popupInfo.isSlowAndNeedsInvokeLater())
                    popupInfo.getActionListener().accept(e);
                else
                    SwingUtilities.invokeLater(() -> popupInfo.getActionListener().accept(e));
            } catch(Exception ex)
            {
                ex.printStackTrace();
                ErrorHandling.showErrorDialog(ex.getMessage(), "Error");
                throw ex;
            }

        });
        return menu;
    }

    private Object[][] computeDataVector (List<AjtColumnInfo<T>> visibleTableColumns)
    {
        final int numColVisible = visibleTableColumns.size();
        final List<T> tableElements = getAllAbstractElementsInTable();

        /* Create data vector*/
        final int numRowsDataVector = tableElements.size() + (hasAggregationRow? 1 : 0);
        final Object[][] dataVector = new Object[numRowsDataVector][numColVisible];
        int rowCount = 0;
        final Number [] accumIfLastRow = new Number [numColVisible];
        for (AjtColumnInfo<T> col : visibleTableColumns)
            if (col.getLastRowAggregationRowType().isToAggregate())
                accumIfLastRow [col.getColumnIndexInTableModelWhenVissible()] = 0.0;
        for (T visibleElement : tableElements)
        {
            for (AjtColumnInfo<T> col : visibleTableColumns)
            {
                final Object val = col.getComputeNewValueAtFunction().apply(visibleElement);
                dataVector [rowCount][col.getColumnIndexInTableModelWhenVissible()] = val;
                if (this.hasAggregationRow && col.getLastRowAggregationRowType().isToAggregate())
                    accumIfLastRow [col.getColumnIndexInTableModelWhenVissible()] = col.getLastRowAggregationRowType().agg(val , accumIfLastRow [col.getColumnIndexInTableModelWhenVissible()]).doubleValue();
            }
            rowCount ++;
        }
        if (hasAggregationRow)
        {
            final LastRowAggregatedValue[] aggregatedData = new LastRowAggregatedValue[numColVisible];
            Arrays.fill(aggregatedData, new LastRowAggregatedValue());
            for (AjtColumnInfo<T> col : visibleTableColumns)
                if (col.getLastRowAggregationRowType().isToAggregate())
                {
                    final Number accumVal = accumIfLastRow[col.getColumnIndexInTableModelWhenVissible()];
                    if (accumVal == null)
                        assert false;
                    aggregatedData [col.getColumnIndexInTableModelWhenVissible()] = new LastRowAggregatedValue(accumVal);
                }
            dataVector [numRowsDataVector -1] = aggregatedData;
        }
        return dataVector;
    }

    private void printColumnInfo (String message)
    {
        System.out.println("--- PRINT COLUMN INFO: " + message);
        /* Print information */
        for (AjtColumnInfo col : this.getColumnsInfo(false))
        {
            if (getTableColumnObject(col).getFirst() == null)
                System.out.println("Column: " + col + ", not visible");
            else
            {
                final int indexPreviously = this.widthAndGlobalViewIndexIfPreviouslyVisible.containsKey(col)? this.widthAndGlobalViewIndexIfPreviouslyVisible.get(col).getSecond() : -1;
                System.out.println("Column: " + col + "Model index: " + col.getColumnIndexInTableModelWhenVissible()  + ", Unified view index NOW / PREVIOUS: " + getViewIndexInUnifiedView(col) + "/" + indexPreviously + ", user original index: " + col.getColumnIndexOriginallyUserDefined());
            }
        }
    }

    /**
     * @return THe table column and true if of fixed, false if scrollable. Null if not visible now
     */
    private Pair<TableColumn,Boolean> getTableColumnObject (AjtColumnInfo col)
    {
        try { final int indexInFixed = getFixedLeftTableColumnModel().getColumnIndex(col.getHeader()); return Pair.of(getFixedLeftTableColumnModel().getColumn(indexInFixed) , true); } catch (Exception e) { }
        try { final int indexInScrollable = getScrollableRightTableColumnModel().getColumnIndex(col.getHeader()); return Pair.of(getScrollableRightTableColumnModel().getColumn(indexInScrollable),false); } catch (Exception e) { }
        return Pair.of((TableColumn) null, (Boolean) null);
    }

    protected Optional<AjtRcMenu> getViewMenuAndSubmenus ()
    {
        final List<AjtColumnInfo<T>> tableColumnsVisibleAndNotVisible = getColumnsInfo(false);
        final SortedSet<String> viewTypesDefined = tableColumnsVisibleAndNotVisible.stream().filter(c->c.getViewTypesBelongTo() != null).map(c->c.getViewTypesBelongTo()).flatMap(c->c.stream()).collect(Collectors.toCollection(TreeSet::new));
        if (viewTypesDefined.isEmpty()) return Optional.empty();
        final List<AjtRcMenu> submenus = new ArrayList<> ();
        final List<String> allOptions = new ArrayList<> ();
        allOptions.add(null);
        allOptions.addAll(viewTypesDefined);
        for (String viewTypeDefined : allOptions)
            submenus.add(new AjtRcMenu(viewTypeDefined == null? "Default view" : viewTypeDefined,
                    (e)-> { control_currentViewTypeForColumns = viewTypeDefined; }  ,
                    (a,b)->true, null));
        final AjtRcMenu res =  new AjtRcMenu("View", null, (a,b)->true, submenus);
        return Optional.of(res);
    }

}
