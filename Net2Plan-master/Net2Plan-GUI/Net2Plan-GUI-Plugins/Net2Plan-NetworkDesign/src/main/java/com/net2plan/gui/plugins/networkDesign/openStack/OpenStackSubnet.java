package com.net2plan.gui.plugins.networkDesign.openStack;


import com.net2plan.interfaces.networkDesign.Resource;
import java.util.List;
import org.openstack4j.model.network.HostRoute;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Ipv6AddressMode;
import org.openstack4j.model.network.Ipv6RaMode;
import org.openstack4j.model.network.Pool;

/**
 *
 * @author Manuel
 */
public class OpenStackSubnet extends OpenStackNetworkElement
{
    private String subnetId = "";
    private String subnetName = "";
    private String subnetCidr = "";
    private String subnetGateway = "";
    private String subnetNetworkId = "";

    static OpenStackSubnet createFromNetPlan (OpenStackNet osn , Resource n)
    {
        final OpenStackSubnet osSubnet = new OpenStackSubnet(osn, n);
        return osSubnet;
    }

    static OpenStackSubnet createFromAddSubnet (OpenStackNet osn ,String subnetId,String subnetName,List<? extends Pool> subnetAllocationPools,String subnetCidr,List<String> subnetDnsNames,String subnetGateway,List<? extends HostRoute> subnetHostRoutes,IPVersionType subnetIpVersion,Ipv6AddressMode subnetIpv6AddressMode,Ipv6RaMode subnetIpv6RaMode,String subnetNetworkId,String subnetTenantId,boolean subnetIsDHCPEnabled)
    {

        final OpenStackSubnet res = new OpenStackSubnet(osn,null);
        res.subnetId = subnetId;
        res.subnetName =subnetName;
        res.subnetCidr = subnetCidr;
        res.subnetGateway = subnetGateway;
        res.subnetNetworkId =subnetNetworkId;

        return res;
    }

    public OpenStackSubnet(OpenStackNet onos, Resource npDummyResource)
    {
        super (onos , npDummyResource , (List<OpenStackNetworkElement>) (List<?>) onos.list_osSubnets);

    }

    @Override
    public String getId () { return this.subnetId; }
    public String getName () { return this.subnetName; }
    public String getSubnetCidr () { return this.subnetCidr; }
    public String getSubnetGateway () { return this.subnetGateway; }
    public String getSubnetNetworkId () { return this.subnetNetworkId; }

    OpenStackSubnet getOpenStackSubnet () { return this; }

    @Override
    public String get50CharactersDescription()
    {
        return this.getId();
    }
}

