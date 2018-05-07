package com.net2plan.gui.plugins.networkDesign.openStack;

import com.net2plan.interfaces.networkDesign.Node;
import java.awt.geom.Point2D;
import java.util.List;
import org.openstack4j.model.network.ExternalGateway;
import org.openstack4j.model.network.HostRoute;
import org.openstack4j.model.network.State;

/**
 *
 * @author Manuel
 */
public class OpenStackNode extends OpenStackNetworkElement
{
    final Node npNode;

    private String osn_nodeId = "";
    private String osn_nodeName = "";
    private String osn_nodeTenantId = "";

    static OpenStackNode createFromNetPlan (OpenStackNet osn , Node l)
    {
        final OpenStackNode frqLink = new OpenStackNode(osn, l);
        return frqLink;
    }

    static OpenStackNode createFromAddNode (OpenStackNet osn ,String nodeId,String nodeName,String nodeTenantId, State nodeStatus,boolean nodeIsAdminStateUp,boolean nodeDistributed,List<? extends HostRoute> nodeRoutes, ExternalGateway nodeExternalGatewayInfo)
    {
        final OpenStackNode res = new OpenStackNode(osn,null);
        res.osn_nodeId = nodeId;
        res.osn_nodeName = nodeName;
        res.osn_nodeTenantId = nodeTenantId;
        return res;
    }
    public OpenStackNode(OpenStackNet osn, Node npNode)
    {
        super (osn , npNode , (List<OpenStackNetworkElement>) (List<?>) osn.list_osNodes);
        this.npNode = npNode;
    }

    Node getNpNode () { return npNode; }

    @Override
    public String getId () { return osn_nodeId; }

    public Point2D getXYPositionMap () { return npNode.getXYPositionMap(); }
    public void setXYPositionMap (Point2D pos) { npNode.setXYPositionMap(pos); }
    public double getIngressCarriedTraffic() { return npNode.getIngressCarriedTraffic(); }
    public double getEgressCarriedTraffic() { return npNode.getEgressCarriedTraffic(); }
    public boolean isUp () { return npNode.isUp(); }
    public boolean isDown () { return npNode.isDown(); }
    public void setFailureState (boolean failureState) { npNode.setFailureState(failureState); }



    @Override
    public String get50CharactersDescription()
    {
        return "Node-" + this.getId();
    }
}
