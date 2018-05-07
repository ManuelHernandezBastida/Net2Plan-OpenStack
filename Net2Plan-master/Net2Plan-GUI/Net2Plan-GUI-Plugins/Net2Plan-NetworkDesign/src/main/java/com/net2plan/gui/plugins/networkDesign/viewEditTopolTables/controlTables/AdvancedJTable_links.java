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
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackLink;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackSubnet;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackUser;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;
import com.net2plan.gui.utils.JNumberField;
import com.net2plan.utils.Pair;

/**
 */
@SuppressWarnings("unchecked")
public class AdvancedJTable_links extends AdvancedJTable_networkElement<OpenStackLink>
{
    public AdvancedJTable_links(GUINetworkDesign callback)
    {
        super(callback, AJTableType.LINKS , true);
    }

    @Override
    public List<AjtColumnInfo<OpenStackLink>> getNonBasicUserDefinedColumnsVisibleOrNot()
    {

        final List<AjtColumnInfo<OpenStackLink>> res = new LinkedList<>();
        res.add(new AjtColumnInfo<OpenStackLink>(this, String.class, null, "Name", "Node name", null, n -> n.getId(), AGTYPE.NOAGGREGATION, null, null));



        return res;
    }


    @Override
    public List<AjtRcMenu> getNonBasicRightClickMenusInfo()
    {
        final List<AjtRcMenu> res = new ArrayList<>();

        return res;
    }


}
