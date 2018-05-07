package com.net2plan.gui.plugins.networkDesign.openStack;

import com.net2plan.interfaces.networkDesign.Link;
import java.util.List;

/**
 *
 * @author Manuel
 */
public class OpenStackUser extends OpenStackNetworkElement
{

    private String userId = "";
    private String userName = "";
    private String userDomainId = "";
    private String userEmail = "";
    private String userDescription = "";

    static OpenStackUser createFromNetPlan (OpenStackNet osn , Link l)
    {
        final OpenStackUser frqLink = new OpenStackUser(osn, l);
        return frqLink;
    }

    static OpenStackUser createFromAddUser (OpenStackNet osn ,String userId, String userName, String userDomainId, String userEmail, String userDescription)
    {
        final OpenStackUser res = new OpenStackUser(osn,null);
        res.userId= userId;
        res.userName=userName;
        res.userDomainId=userDomainId;
        res.userEmail=userEmail;
        res.userDescription=userDescription;

        return res;
    }

    private OpenStackUser (OpenStackNet osn , Link npLink)
    {
        super (osn , npLink , (List<OpenStackNetworkElement>) (List<?>) osn.list_osUsers);

    }

    @Override
    public String getId () { return this.userId; }

    public String getName () { return this.userName; }
    public String getDomainId () { return this.userDomainId; }
    public String getEmail () { return this.userEmail; }
    public String getDescription () { return this.userDescription; }

    OpenStackUser getOpenStackUser () { return this; }

    @Override
    public String get50CharactersDescription()
    {
        return "User-" + this.getId();
    }
}

