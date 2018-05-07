package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetwork;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;

/**
 */
@SuppressWarnings("unchecked")
public class AdvancedJTable_networks extends AdvancedJTable_networkElement<OpenStackNetwork>
{
    public AdvancedJTable_networks(GUINetworkDesign callback)
    {
        super(callback, AJTableType.NETWORKS , true);
    }

    @Override
    public List<AjtColumnInfo<OpenStackNetwork>> getNonBasicUserDefinedColumnsVisibleOrNot()
    {

        final List<AjtColumnInfo<OpenStackNetwork>> res = new LinkedList<>();
        res.add(new AjtColumnInfo<OpenStackNetwork>(this, String.class, null, "Name", "Node name", null, n -> n.getId(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackNetwork>(this, String.class, null, "Site", "Site this node belongs to", null, n -> n.getName(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackNetwork>(this, String.class, null, "Up?", "Indicates whether the node is up or down (failed)", null, n -> n.getNetworkProviderPhyNet(),
                AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackNetwork>(this, String.class, null, "X-pos", "Coordinate along x-axis (i.e. longitude)",
                null, n -> n.getNetworkProviderSegID(), AGTYPE.NOAGGREGATION, null, null));
        res.add(new AjtColumnInfo<OpenStackNetwork>(this, String.class, null, "Y-pos", "Coordinate along y-axis (i.e. latitude)",
                null, n -> n.getNetworkTenantId(), AGTYPE.NOAGGREGATION, null, null));


        return res;
    }


    @Override
    public List<AjtRcMenu> getNonBasicRightClickMenusInfo()
    {
        final List<AjtRcMenu> res = new ArrayList<>();

        return res;
    }


}
