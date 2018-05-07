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
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackSubnet;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackUser;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;
import com.net2plan.gui.utils.JNumberField;
import com.net2plan.utils.Pair;

/**
 */
@SuppressWarnings("unchecked")
public class AdvancedJTable_subnets extends AdvancedJTable_networkElement<OpenStackSubnet>
{
    public AdvancedJTable_subnets(GUINetworkDesign callback)
    {
        super(callback, AJTableType.SUBNETS , true);
    }

    @Override
    public List<AjtColumnInfo<OpenStackSubnet>> getNonBasicUserDefinedColumnsVisibleOrNot()
    {

        final List<AjtColumnInfo<OpenStackSubnet>> res = new LinkedList<>();
        res.add(new AjtColumnInfo<OpenStackSubnet>(this, String.class, null, "Name", "Node name", null, n -> n.getId(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackSubnet>(this, String.class, null, "Site", "Site this node belongs to", null, n -> n.getName(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackSubnet>(this, String.class, null, "Up?", "Indicates whether the node is up or down (failed)", null, n -> n.getSubnetCidr(),
                AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackSubnet>(this, String.class, null, "X-pos", "Coordinate along x-axis (i.e. longitude)",
                null, n -> n.getSubnetGateway(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackSubnet>(this, String.class, null, "Y-pos", "Coordinate along y-axis (i.e. latitude)",
                null, n -> n.getSubnetNetworkId(), AGTYPE.NOAGGREGATION, null, null));


        return res;
    }


    @Override
    public List<AjtRcMenu> getNonBasicRightClickMenusInfo()
    {
        final List<AjtRcMenu> res = new ArrayList<>();

        return res;
    }


}
