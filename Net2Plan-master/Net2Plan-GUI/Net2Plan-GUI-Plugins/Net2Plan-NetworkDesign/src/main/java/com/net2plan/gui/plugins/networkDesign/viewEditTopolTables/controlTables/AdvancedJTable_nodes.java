package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.google.common.collect.Lists;
import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackUser;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;
//import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.dialog.AddMulticastFlowDialog;
//import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.dialog.CommonIPNodeDialogs;//
//import com.net2plan.gui.utils.IntegerInputDialog;
import com.net2plan.gui.utils.JNumberField;
//import com.net2plan.gui.utils.TextInputDialog;
import com.net2plan.utils.Pair;

/**
 */
@SuppressWarnings("unchecked")
public class AdvancedJTable_nodes extends AdvancedJTable_networkElement<OpenStackUser>
{
    public AdvancedJTable_nodes(GUINetworkDesign callback)
    {
        super(callback, AJTableType.NODES , true);
    }

    @Override
    public List<AjtColumnInfo<OpenStackUser>> getNonBasicUserDefinedColumnsVisibleOrNot()
    {

        final List<AjtColumnInfo<OpenStackUser>> res = new LinkedList<>();
        res.add(new AjtColumnInfo<OpenStackUser>(this, String.class, null, "Name", "Node name", null, n -> n.getId(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackUser>(this, String.class, null, "Site", "Site this node belongs to", null, n -> n.getName(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackUser>(this, String.class, null, "Up?", "Indicates whether the node is up or down (failed)", null, n -> n.getDomainId(),
                AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackUser>(this, String.class, null, "X-pos", "Coordinate along x-axis (i.e. longitude)",
                null, n -> n.getEmail(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackUser>(this, String.class, null, "Y-pos", "Coordinate along y-axis (i.e. latitude)",
                null, n -> n.getDescription(), AGTYPE.NOAGGREGATION, null, null));

        return res;
    }


    @Override
    public List<AjtRcMenu> getNonBasicRightClickMenusInfo()
    {
        final List<AjtRcMenu> res = new ArrayList<>();

        return res;
    }


}
