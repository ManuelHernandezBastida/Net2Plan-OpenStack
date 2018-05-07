package com.net2plan.gui.plugins.networkDesign.openStack;

import com.net2plan.interfaces.networkDesign.Resource;
import java.util.List;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.State;
import org.openstack4j.model.network.Subnet;

/**
 *
 * @author Manuel
 */
public class OpenStackNetwork extends OpenStackNetworkElement
{
    private String networkId = "";
    private String networkName = "";
    private String networkProviderPhyNet = "";
    private String networkProviderSegID ="";
    private String networkTenantId = "";
    static OpenStackNetwork createFromNetPlan (OpenStackNet osn , Resource n)
    {
        final OpenStackNetwork osNetwork = new OpenStackNetwork(osn, n);
        return osNetwork;
    }

    static OpenStackNetwork createFromAddNetwork (OpenStackNet osn ,String networkId,String networkName,State networkStatus,NetworkType networkType,List<? extends Subnet> networkNeutronSubnets,String networkProviderPhyNet,String networkProviderSegID,List <String> networkSubnets,String networkTenantId,boolean networkIsAdminStateUp,boolean networkIsRouterExternal, boolean networkIsShared)
    {
        final OpenStackNetwork res = new OpenStackNetwork(osn,null);
        res.networkId= networkId;
        res.networkName=networkName;
        res.networkProviderPhyNet=networkProviderPhyNet;
        res.networkProviderSegID=networkProviderSegID;
        res.networkTenantId=networkTenantId;

        return res;
    }

    public OpenStackNetwork(OpenStackNet osn, Resource npDummyResource)
    {
        super (osn , npDummyResource , (List<OpenStackNetworkElement>) (List<?>) osn.list_osNetworks);
    }


    @Override
    public String getId () { return this.networkId; }
    public String getName () { return this.networkName; }
    public String getNetworkProviderPhyNet () { return this.networkProviderPhyNet; }
    public String getNetworkProviderSegID () { return this.networkProviderSegID; }
    public String getNetworkTenantId () { return this.networkTenantId; }

    OpenStackNetwork getOpenStackNetwork () { return this; }

    @Override
    public String get50CharactersDescription()
    {
        return this.getId();
    }
}
