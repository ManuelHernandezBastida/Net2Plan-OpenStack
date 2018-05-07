package com.net2plan.gui.plugins.networkDesign.openStack;


import com.google.common.collect.Lists;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.openstack4j.model.network.ExternalGateway;
import org.openstack4j.model.network.HostRoute;
import org.openstack4j.model.network.IPVersionType;
import org.openstack4j.model.network.Ipv6AddressMode;
import org.openstack4j.model.network.Ipv6RaMode;
import org.openstack4j.model.network.NetworkType;
import org.openstack4j.model.network.Pool;
import org.openstack4j.model.network.State;
import org.openstack4j.model.network.Subnet;

/**
 *
 * @author Manuel
 */
public class OpenStackNet
{

    private final NetPlan np;


    final List<OpenStackNode> list_osNodes = new ArrayList<> ();
    final List<OpenStackLink> list_osLinks = new ArrayList<> ();
    final List<OpenStackUser> list_osUsers = new ArrayList<> ();
    final List<OpenStackNetwork> list_osNetworks = new ArrayList<> ();
    final List<OpenStackSubnet> list_osSubnets = new ArrayList<> ();

    final  NetPlan getNetPlan () { return np; }

    public OpenStackNet ()
    {
        this.np = new NetPlan ();
    }

    public static OpenStackNet buildFrqNetFromN2pFile(File n2pFile)
    {
        return new OpenStackNet (new NetPlan (n2pFile));
    }

    private OpenStackNet (NetPlan np)
    {
        this.np = np;

        for (Node npNode : np.getNodes())
            OpenStackNode.createFromNetPlan(this, npNode);
        for (Link npLink : np.getLinks())
            OpenStackLink.createFromNetPlan(this, npLink);

    }

    public static OpenStackNet buildOpenStackNetFromServer(String serverIp, String serverPort, String userName, String password)
    {
        try
        {
            final OpenStackNet res = new TopologyCreator(serverIp, serverPort, userName, password).getOpenStackNet();
            return res;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    public OpenStackLink addOpenStackLink (OpenStackNode originNode, OpenStackNode destinationNode, String originPort, String destinationPort, Optional<String> linkType, Optional<String> linkState)
    {
        final OpenStackLink res = OpenStackLink.createFromAddLink(originNode, destinationNode, originPort, destinationPort, linkType, linkState);
        list_osLinks.add(res);
        return res;
    }
    public OpenStackUser addOpenStackUser (String userId, String userName, String userDomainId, String userEmail, String userDescription)
    {
        final OpenStackUser res = OpenStackUser.createFromAddUser(this , userId, userName, userDomainId, userEmail, userDescription);
        list_osUsers.add(res);
        return res;
    }

    public OpenStackNetwork addOpenStackNetwork(String networkId,String networkName,State networkStatus,NetworkType networkType,List<? extends Subnet> networkNeutronSubnets,String networkProviderPhyNet,String networkProviderSegID,List <String> networkSubnets,String networkTenantId,boolean networkIsAdminStateUp,boolean networkIsRouterExternal, boolean networkIsShared)
    {
        final OpenStackNetwork res = OpenStackNetwork.createFromAddNetwork(this , networkId, networkName, networkStatus, networkType, networkNeutronSubnets,networkProviderPhyNet,networkProviderSegID,networkSubnets,networkTenantId,networkIsAdminStateUp,networkIsRouterExternal,networkIsShared);
        list_osNetworks.add(res);
        return res;
    }

    public OpenStackSubnet addOpenStackSubnet (String subnetId,String subnetName,List<? extends Pool> subnetAllocationPools,String subnetCidr,List<String> subnetDnsNames,String subnetGateway,List<? extends HostRoute> subnetHostRoutes,IPVersionType subnetIpVersion,Ipv6AddressMode subnetIpv6AddressMode,Ipv6RaMode subnetIpv6RaMode,String subnetNetworkId,String subnetTenantId,boolean subnetIsDHCPEnabled)
    {
        final OpenStackSubnet res = OpenStackSubnet.createFromAddSubnet(this,subnetId, subnetName, subnetAllocationPools, subnetCidr, subnetDnsNames, subnetGateway,subnetHostRoutes,subnetIpVersion,subnetIpv6AddressMode,subnetIpv6RaMode,subnetNetworkId,subnetTenantId,subnetIsDHCPEnabled);
        list_osSubnets.add(res);
        return res;
    }

    public OpenStackNode addOpenStackNode(String nodeId,String nodeName,String nodeTenantId, State nodeStatus,boolean nodeIsAdminStateUp,boolean nodeDistributed,List<? extends HostRoute> nodeRoutes, ExternalGateway nodeExternalGatewayInfo)
    {
        final OpenStackNode res = OpenStackNode.createFromAddNode(this,nodeId, nodeName,nodeTenantId, nodeStatus, nodeIsAdminStateUp, nodeDistributed, nodeRoutes,nodeExternalGatewayInfo);
        list_osNodes.add(res);
        return res;
    }


    public String getTopologyName () { return np.getNetPlan().getNetworkName(); }
    public String getTopologyDescription () { return np.getNetPlan().getNetworkDescription(); }
    public void setTopologyName (String name) { this.np.getNetPlan().setNetworkName(name); }
    public String getLinkCapacityUnitsName () { return np.getNetPlan().getLinkCapacityUnitsName(); }
    public void setLinkCapacityUnitsName (String name) { np.getNetPlan().setLinkCapacityUnitsName(name); }
    public String getDemandTrafficUnitsName () { return np.getNetPlan().getDemandTrafficUnitsName(); }
    public void setDemandTrafficUnitsName (String name) { np.getNetPlan().setDemandTrafficUnitsName(name); }

    public List<OpenStackNode> getOpenStackNodes () { return Collections.unmodifiableList(list_osNodes); }
    public List<OpenStackLink> getOpenStackLinks () { return Collections.unmodifiableList(list_osLinks); }
    public List<OpenStackUser> getOpenStackUsers () { return Collections.unmodifiableList(list_osUsers); }
    public List<OpenStackNetwork> getOpenStackNetworks () { return Collections.unmodifiableList(list_osNetworks); }
    public List<OpenStackSubnet> getOpenStackSubnets () { return Collections.unmodifiableList(list_osSubnets); }


    public OpenStackNetworkElement getOpenStackNetworkElementByInternalId (long internalId)
    {
        final List<OpenStackNetworkElement> allOpenStackNetworkElements = Lists.newArrayList();
        allOpenStackNetworkElements.addAll(list_osNodes);
        allOpenStackNetworkElements.addAll(list_osLinks);
        allOpenStackNetworkElements.addAll(list_osUsers);
        allOpenStackNetworkElements.addAll(list_osNetworks);
        allOpenStackNetworkElements.addAll(list_osSubnets);

        Optional<OpenStackNetworkElement> element = allOpenStackNetworkElements.stream().filter(n->n.getInternalId() == internalId).findFirst();
        if (element.isPresent()) return element.get();
        else return null;
    }


    public OpenStackNetworkElement getOpenStackNetworkElementByOpenStackId (String openStackId)
    {
        final List<OpenStackNetworkElement> allOpenStackNetworkElements = Lists.newArrayList();
        allOpenStackNetworkElements.addAll(list_osNodes);
        allOpenStackNetworkElements.addAll(list_osLinks);
        allOpenStackNetworkElements.addAll(list_osUsers);
        allOpenStackNetworkElements.addAll(list_osNetworks);
        allOpenStackNetworkElements.addAll(list_osSubnets);

        Optional<OpenStackNetworkElement> element = allOpenStackNetworkElements.stream().filter(n->n.getId() == openStackId).findFirst();
        if (element.isPresent()) return element.get();
        else return null;
    }

    public void distributeTopologyOverCircle()
    {
        // Node list
        final List<OpenStackNode> nodeList = getOpenStackNodes();

        final int numNodes = nodeList.size();
        double index = 0.0;

        for (OpenStackNode node : nodeList)
        {
            final double xCoord = Math.sin(Math.toRadians((360 * index) / numNodes));
            final double yCoord = -Math.cos(Math.toRadians((360 * index) / numNodes));

            node.setXYPositionMap(new Point2D.Double(xCoord, yCoord));

            index++;
        }



    }
}