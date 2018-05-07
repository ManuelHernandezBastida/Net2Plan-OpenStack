package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JTable;

import com.google.common.base.Strings;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetworkElement;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_abstractElement.AGTYPE;
import com.net2plan.interfaces.networkDesign.Configuration;

public class AjtColumnInfo<T>
{
    private final String header;
    private final String tooltip;
    private final boolean isEditable;
    private int columnIndexOriginallyUserDefined;
    private int columnIndexInTableModelWhenVissible;
    private final BiConsumer<T,Object> setValueAtFunction; // receives p.e. UnidiIpDemand return double, the value to set
    private final Class valueShownIfNotAggregation;
    private final Function<T,Object> computeNewValueAtFunction; // receives p.e. UnidiIpDemand return double, the value to set
    private final AGTYPE lastRowAggregationRowType;
    private final Function<Object,Color> getSpetialBgColorIfNotSelected;
    private Function<Object,Color> getAllRowMandatorySpetialBgColorIfNotSelected;
    private DecimalFormat decimalFormatForDouble;
    private final SortedSet<String> viewTypesBelongsTo;
    private AjtColumnInfo<T> baseColumnForRowMandatorySpetialBgColorIfNotSelected;
    //    private final AdvancedJTable_networkElement ajTable;
    private final JTable ajTable;

    void setColumnIndexOriginallyUserDefined (int index)
    {
        this.columnIndexOriginallyUserDefined = index;
    }
    void setColumnIndexInTableModelWhenVissible (int index)
    {
        this.columnIndexInTableModelWhenVissible = index;
    }

    public SortedSet<String> getViewTypesBelongTo () { return viewTypesBelongsTo; }

    public Function<Object,Color> getGetAllRowMandatorySpetialBgColorIfNotSelected()
    {
        return getAllRowMandatorySpetialBgColorIfNotSelected;
    }

    public boolean isVisibleInView (String viewDefinedForTheTable)
    {
        if (getViewTypesBelongTo() == null) return true;
        if (viewDefinedForTheTable == null) return false;
        return getViewTypesBelongTo().contains(viewDefinedForTheTable);
    }

    public void setGetAllRowMandatorySpetialBgColorIfNotSelected(Function<Object,Color> getAllRowMandatorySpetialBgColorIfNotSelected, AjtColumnInfo<T> baseColumnForRowMandatorySpetialBgColorIfNotSelected)
    {
        this.getAllRowMandatorySpetialBgColorIfNotSelected = getAllRowMandatorySpetialBgColorIfNotSelected;
        this.baseColumnForRowMandatorySpetialBgColorIfNotSelected = baseColumnForRowMandatorySpetialBgColorIfNotSelected;
    }

    public AjtColumnInfo<T> getBaseColumnForRowMandatorySpetialBgColorIfNotSelected() { return this.baseColumnForRowMandatorySpetialBgColorIfNotSelected; }

    public Function<Object,Color> getGetSpecialBgColorIfNotSelected()
    {
        return getSpetialBgColorIfNotSelected;
    }



//    /**
//     * @return THe table column and true if of fixed, false if scrollable. Null if not visible now
//     */
//    public Pair<TableColumn,Boolean> getColumn ()
//    {
//    	try { final int indexInFixed = ajTable.getFixedLeftTableColumnModel().getColumnIndex(this.getHeader()); return Pair.of(ajTable.getFixedLeftTableColumnModel().getColumn(indexInFixed) , true); } catch (Exception e) { }
//    	try { final int indexInScrollable = ajTable.getScrollableRightTableColumnModel().getColumnIndex(this.getHeader()); return Pair.of(ajTable.getScrollableRightTableColumnModel().getColumn(indexInScrollable),false); } catch (Exception e) { }
//    	return Pair.of((TableColumn) null, (Boolean) null);
//    }

    public AjtColumnInfo(
            AdvancedJTable_networkElement ajTable ,
            Class valueShownIfNotAggregation ,
            Collection<String> viewTypesBelongsTo ,
            String header, String tooltip,
            BiConsumer<T,Object> setValueAtFunction ,
            Function<T,Object> computeNewValueAtFunction ,
            AGTYPE lastRowAggregationRowType,
            Function<Object,Color> getAllRowMandatorySpetialBgColorIfNotSelected ,
            Function<Object,Color> getSpetialBgColorIfNotSelected)
    {
        super();
        this.ajTable = ajTable;
        this.header = header;
        this.tooltip = tooltip;
        this.columnIndexOriginallyUserDefined = -1;
        this.columnIndexInTableModelWhenVissible = -1;
        this.setValueAtFunction = setValueAtFunction;
        this.valueShownIfNotAggregation = valueShownIfNotAggregation;
        this.isEditable = setValueAtFunction != null;
        this.computeNewValueAtFunction = computeNewValueAtFunction;
        this.lastRowAggregationRowType = lastRowAggregationRowType;
        this.getAllRowMandatorySpetialBgColorIfNotSelected = getAllRowMandatorySpetialBgColorIfNotSelected;
        this.getSpetialBgColorIfNotSelected = getSpetialBgColorIfNotSelected;
        this.decimalFormatForDouble = new DecimalFormat("#.##");
        this.viewTypesBelongsTo = viewTypesBelongsTo == null? null : new TreeSet<> (viewTypesBelongsTo);
    }

    public DecimalFormat getDecimalFormatForDouble () { return decimalFormatForDouble; }
    public void setNumDecimalValues (int numDecimalValues)
    {
        if (numDecimalValues < 0) return;
        this.decimalFormatForDouble = new DecimalFormat("#." + Strings.repeat("#", numDecimalValues));
    }

    public AGTYPE getLastRowAggregationRowType()
    {
        return lastRowAggregationRowType;
    }

    public String getHeader()
    {
        return header;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public boolean isEditable()
    {
        return isEditable;
    }

    public int getColumnIndexInTableModelWhenVissible()
    {
        return columnIndexInTableModelWhenVissible;
    }
    public int getColumnIndexOriginallyUserDefined()
    {
        return columnIndexOriginallyUserDefined;
    }
    public BiConsumer<T,Object> getSetValueAtFunction()
    {
        return setValueAtFunction;
    }

    public Class getValueShownIfNotAggregation()
    {
        return valueShownIfNotAggregation;
    }

    public Function<T,Object> getComputeNewValueAtFunction()
    {
        return computeNewValueAtFunction;
    }

    public static AjtColumnInfo<OpenStackNetworkElement> createIdColumn (AdvancedJTable_networkElement<? extends OpenStackNetworkElement> ajTable)
    {
        return new AjtColumnInfo<OpenStackNetworkElement>(ajTable, Long.class, null, "Id", "Unique identifier (never repeated in any other network element)", null , e->e.getId() , AGTYPE.NOAGGREGATION , null , null);
    }
    public static AjtColumnInfo<OpenStackNetworkElement> createIndexColumn (AdvancedJTable_networkElement<? extends OpenStackNetworkElement> ajTable)
    {
        return new AjtColumnInfo<>(ajTable, Integer.class, null , "Index", "Index (consecutive integer starting in zero)", null , e->e.getOpenStackIndex(), AGTYPE.NOAGGREGATION , null , null);
    }
    public static AjtColumnInfo<OpenStackNetworkElement> createTagsColumn (AdvancedJTable_networkElement<? extends OpenStackNetworkElement> ajTable)
    {
        return new AjtColumnInfo<>(ajTable, String.class, null , "Tags", "User-defined tags associated to this element", null , e->e.getId() , AGTYPE.NOAGGREGATION , null , null);
    }
    public static AjtColumnInfo<OpenStackNetworkElement> createAttributesColumn (AdvancedJTable_networkElement<? extends OpenStackNetworkElement> ajTable)
    {
        return new AjtColumnInfo<>(ajTable, String.class, null , "Attributes", "User-defined attributes associated to this element", null , e->e.getId() , AGTYPE.NOAGGREGATION , null , null);
    }

    public static Function<Object,Color> NonZeroRedZeroNone = (v) -> ((Number) v).doubleValue() > Configuration.precisionFactor? Color.RED : null;
    public static Function<Object,Color> NonZeroRedZeroGreen = (v) -> ((Number) v).doubleValue() > Configuration.precisionFactor? Color.RED : Color.GREEN;
    public static Function<Object,Color> LessOneNoneOneYellowHigherRed = (v) -> { final double val = ((Number) v).doubleValue(); if (val<1) return null; if (Math.abs(val - 1.0)<Configuration.precisionFactor) return Color.YELLOW; else return Color.RED; } ;
    public static Function<Object,Color> NonZeroRedZeroGreenCollection = (v) -> ((Collection<?>) v).size() > Configuration.precisionFactor? Color.RED : Color.GREEN;
    public static Function<Object,Color> FalseRedTrueNone = (v) -> ((Boolean) v).booleanValue() ? null : Color.RED;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ajTable == null) ? 0 : ajTable.hashCode());
        result = prime * result + ((header == null) ? 0 : header.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AjtColumnInfo other = (AjtColumnInfo) obj;
        if (ajTable == null) {
            if (other.ajTable != null)
                return false;
        } else if (!ajTable.equals(other.ajTable))
            return false;
        if (header == null) {
            if (other.header != null)
                return false;
        } else if (!header.equals(other.header))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "AjtColumnInfo [header=" + header + "]";
    }


}
