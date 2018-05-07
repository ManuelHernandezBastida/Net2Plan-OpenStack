package com.net2plan.gui.plugins.networkDesign.openStack;


import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.Node;
import java.util.ArrayList;
import java.util.List;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.identity.v3.User;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Router;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;

/**
 *
 * @author Manuel
 */
class TopologyCreator
{
    private final OSClientV3 os;

    TopologyCreator(String openstackIPAddress, String user, String password, String project)
    {
        os  = OSFactory.builderV3()
                .endpoint("http://"+openstackIPAddress+"/identity/v3")
                .credentials(user,password,Identifier.byName("Default"))
                .scopeToProject(Identifier.byName(project), Identifier.byId("default"))
                .authenticate();
    }


    OpenStackNet getOpenStackNet()
    {
        /* Empty NetPlan */
        final OpenStackNet osn = new OpenStackNet();

        /* Get elements */
        final List<User> users = (List<User>) os.identity().users().list();
        final List<Network> networks = (List<Network>) os.networking().network().list();
        final List<Subnet> subnets = (List<Subnet>) os.networking().subnet().list();
        final List<Router> routers = (List<Router>) os.networking().router().list();
        final List<Link> links = new ArrayList<>();


        /* Create users objects */
        for (User user : users)
            osn.addOpenStackUser(user.getId(), user.getName(), user.getDomainId(), user.getEmail(), user.getDescription());

        /* Create networks objects */
        for (Network net : networks)
            osn.addOpenStackNetwork(net.getId(),net.getName(),net.getStatus(),net.getNetworkType(),net.getNeutronSubnets(),net.getProviderPhyNet(),net.getProviderSegID(),net.getSubnets(),net.getTenantId(),net.isAdminStateUp(),net.isRouterExternal(),net.isShared());

        /* Create subnets objects */
        for (Subnet subnet : subnets)
            osn.addOpenStackSubnet(subnet.getId(),subnet.getName(),subnet.getAllocationPools(),subnet.getCidr(),subnet.getDnsNames(),subnet.getGateway(),subnet.getHostRoutes(),subnet.getIpVersion(),subnet.getIpv6AddressMode(),subnet.getIpv6RaMode(),subnet.getNetworkId(),subnet.getTenantId(),subnet.isDHCPEnabled());

        /*Create links objects
        for (Link link : links)
            osn.addOpenStackLink(link.getId(),link.getCapacity(),subnet.getAllocationPools(),subnet.getCidr(),subnet.getDnsNames(),subnet.getGateway(),subnet.getHostRoutes(),subnet.getIpVersion(),subnet.getIpv6AddressMode(),subnet.getIpv6RaMode(),subnet.getNetworkId(),subnet.getTenantId(),subnet.isDHCPEnabled());
        */
        /* Create links objects */
        for (Router router : routers)

            osn.addOpenStackNode(router.getId(), router.getName(), router.getTenantId(), router.getStatus(),router.isAdminStateUp(),router.getDistributed(),router.getRoutes(),router.getExternalGatewayInfo());

        if (routers.isEmpty()) throw new Net2PlanException("The OpenStack topology is empty");


        osn.distributeTopologyOverCircle();

        return osn;
    }
}

