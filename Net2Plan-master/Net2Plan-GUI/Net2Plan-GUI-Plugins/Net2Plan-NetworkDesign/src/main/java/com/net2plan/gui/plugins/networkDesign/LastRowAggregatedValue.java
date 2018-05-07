package com.net2plan.gui.plugins.networkDesign;

public class LastRowAggregatedValue
{
    private String value;
    private InfNumberFormat numberFormat = new InfNumberFormat("##.##");

    public static final String EMPTY_VALUE = "---";

    public LastRowAggregatedValue()
    {
        value = EMPTY_VALUE;
    }
    public LastRowAggregatedValue(Number val)
    {
        this(val.doubleValue());
    }
    public LastRowAggregatedValue(int val)
    {
        this((double) val);
    }

    public LastRowAggregatedValue(double val)
    {
        final boolean isDecimal = val % 1 != 0;

        if (isDecimal)
            value = numberFormat.format(val);
        else
            value = numberFormat.format(((Number) val).intValue());
    }

    public LastRowAggregatedValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
