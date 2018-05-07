package com.net2plan.gui.plugins.networkDesign.openStack;

import com.net2plan.interfaces.networkDesign.Link;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Manuel
 */
public class OpenStackLink extends OpenStackNetworkElement
{
    final Link npLink;

    private String linkId = "";
    private String originNodeId = "";
    private String originPort = "";
    private String destinationNodeId = "";
    private String destinationPort = "";
    private String osn_linkType = "";
    private String osn_linkState = "";


    static OpenStackLink createFromNetPlan (OpenStackNet onos , Link l)
    {
        final OpenStackLink frqLink = new OpenStackLink(onos, l);
        return frqLink;
    }
    static OpenStackLink createFromAddLink (OpenStackNode originNode , OpenStackNode destinationNode , String originPort , String destinationPort , Optional<String> linkType , Optional<String> linkState)
    {
        final OpenStackNet onos = originNode.getOpenStackNet();
        final Link l = onos.getNetPlan().addLink(originNode.getNpNode(), destinationNode.getNpNode(), 0,
                onos.getNetPlan().getNodePairHaversineDistanceInKm(originNode.getNpNode(), destinationNode.getNpNode()), 200000, null);

        final OpenStackLink res = new OpenStackLink(onos, l);

        res.linkId = "" + res.getInternalId();
        res.originNodeId = originNode.getId();
        res.destinationNodeId = destinationNode.getId();
        res.originPort = originPort;
        res.destinationPort = destinationPort;
        res.osn_linkType = linkType.isPresent()? linkType.get() : "";
        res.osn_linkState = linkState.isPresent()? linkState.get() : "";

        return res;
    }

    private OpenStackLink (OpenStackNet osn , Link npLink)
    {
        super (osn , npLink , (List<OpenStackNetworkElement>) (List<?>) osn.list_osLinks);
        this.npLink = npLink;
    }

    @Override
    public String getId () { return this.linkId; }

    //public OpenStackNode getOriginNode () { return this; }
    public String getOriginPort () { return this.originPort; }
    // public OpenStackNode getDestinationNode () { return npLink.getDestinationNode(); }
    public String getDestinationPort () { return this.destinationPort; }
    public String getType () { return this.osn_linkType; }
    public void setType (String type) { this.osn_linkType = type; }
    public String getState () { return this.osn_linkState; }
    public void setState (String state) { this.osn_linkState=state; }
    public double getLinkCapacity () { return this.npLink.getCapacity(); }
    public double getLinkOccupiedCapacity () { return this.npLink.getOccupiedCapacity(); }
    public void setLinkCapacity (double capacity) { this.npLink.setCapacity(capacity);}
    public double getLinkPropagationSpeedInKmPerSecond () { return this.npLink.getPropagationSpeedInKmPerSecond(); }
    public double getLinkLengthInKm () { return this.npLink.getLengthInKm(); }
    public void setLinkLengthInKm (double lengthInKm) { this.npLink.setLengthInKm(lengthInKm); }
    public void setLinkPropagationSpeedInKmPerSecond (double propagationSpeedInKmPerSecond) { this.npLink.setPropagationSpeedInKmPerSecond(propagationSpeedInKmPerSecond); }
    public boolean isUp () { return this.npLink.isUp(); }
    public boolean isDown () { return this.npLink.isDown(); }
    public double getLinkUtilization () { return this.npLink.getUtilization(); }
    public double getLinkCarriedTraffic () { return this.npLink.getCarriedTraffic(); }

    Link getNpLink () { return npLink; }



    @Override
    public String get50CharactersDescription()
    {
        return "Link-" + this.getOpenStackIndex();
    }
}

