package com.net2plan.gui.plugins.utils;

/**
 * Created by Jorge San Emeterio on 7/03/17.
 */
public enum NetworkDesignWindow
{
    network("View/Edit network state"),
    baseEqCatalog("Base Eq. catalogue"),
    ipEqCatalog("IP Eq. catalogue"),
    ipEqDeployment("Equipment deployment"),
    algorithmDesignWindowName("Design"),
    report("View reports");

    private final String text;
    NetworkDesignWindow (String text) { this.text = text; }

    @Override
    public String toString()
    {
        return text;
    }
}
