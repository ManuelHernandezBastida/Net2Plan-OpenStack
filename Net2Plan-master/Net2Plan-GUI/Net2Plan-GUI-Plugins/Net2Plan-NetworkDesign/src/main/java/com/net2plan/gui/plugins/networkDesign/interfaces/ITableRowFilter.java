/*******************************************************************************
 * Copyright (c) 2017 Pablo Pavon Marino and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 2-clause BSD License 
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/BSD-2-Clause
 *
 * Contributors:
 *     Pablo Pavon Marino and others - initial API and implementation
 *******************************************************************************/
package com.net2plan.gui.plugins.networkDesign.interfaces;

import com.google.common.collect.Sets;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNet;
import com.net2plan.gui.plugins.networkDesign.openStack.OpenStackNetworkElement;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane.AJTableType;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public abstract class ITableRowFilter
{
	protected final List<Link> vLinks;
	protected final List<Node> vNodes;
	protected final NetPlan netPlan;
	protected List<String> chainOfDescriptionsPreviousFiltersComposingThis;

	public enum FilterCombinationType { INCLUDEIF_AND, INCLUDEIF_OR };

	/* Baseline constructor: everything is filtered out */
	public ITableRowFilter (NetPlan netPlan)
	{
		this.netPlan = netPlan;
		this.vLinks = new ArrayList<> ();
		this.vNodes = new ArrayList<> ();
		this.chainOfDescriptionsPreviousFiltersComposingThis = new LinkedList<> ();
	}

	public abstract String getDescription ();

	public final List<Link> getVisibleLinks () { return Collections.unmodifiableList(vLinks); }
	public final List<Node> getVisibleNodes () { return Collections.unmodifiableList(vNodes); }

	public final boolean hasLinks (NetworkLayer layer) { return !vLinks.isEmpty(); }
	public final boolean hasNodes (NetworkLayer layer) { return !vNodes.isEmpty(); }

	public final int getNumberOfLinks (NetworkLayer layer) { return vLinks.size(); }
	public final int getNumberOfNodes (NetworkLayer layer) { return vNodes.size(); }

	public final void recomputeApplyingShowIf_ThisAndThat (ITableRowFilter that)
	{
		if (this.netPlan != that.netPlan) throw new RuntimeException();
		if (!this.netPlan.getNetworkLayers().equals(that.netPlan.getNetworkLayers())) throw new RuntimeException();
		vLinks.addAll((List<Link>) filterAnd(this.vLinks , that.vLinks));
		vNodes.addAll((List<Node>) filterAnd(this.vNodes , that.vNodes));
		chainOfDescriptionsPreviousFiltersComposingThis.add(that.getDescription());
	}

	public final void recomputeApplyingShowIf_ThisOrThat (ITableRowFilter that)
	{
		if (this.netPlan != that.netPlan) throw new RuntimeException();
		if (!this.netPlan.getNetworkLayers().equals(that.netPlan.getNetworkLayers())) throw new RuntimeException();
		vLinks.addAll((List<Link>) filterOr(this.vLinks , that.vLinks));
		vNodes.addAll((List<Node>) filterOr(this.vNodes , that.vNodes));
		chainOfDescriptionsPreviousFiltersComposingThis.add(that.getDescription());
	}


	private final List<? extends Object> filterAnd (List<? extends Object> l1 , List<? extends Object> l2)
	{
		final List<Object> resList = new LinkedList<> ();
		for (Object o : l1) if (l2.contains(o)) resList.add(o); // keep the same order as in List 1
		return resList;
	}

	private final List<? extends Object> filterOr (List<? extends Object> l1 , List<? extends Object> l2)
	{
		final List<Object> resList = new ArrayList<> (l1);
		for (Object o : l2) if (!l1.contains(o)) resList.add(o); // first the ones in l1, then the ones in l2-l1
		return resList;
	}

	protected final static List<? extends NetworkElement> orderSet (Set<? extends NetworkElement> set)
	{
		List res = new ArrayList<NetworkElement> (set);
		Collections.sort(res , new Comparator<NetworkElement> () { public int compare (NetworkElement e1 , NetworkElement e2) { return Integer.compare(e1.getIndex() , e2.getIndex()); }  } );
		return res;
	}
	protected final static List<Pair<Demand,Link>> orderSetFR (Set<Pair<Demand,Link>> set)
	{
		List res = new ArrayList<Pair<Demand,Link>> (set);
		Collections.sort(res , new Comparator<Pair<Demand,Link>> () { public int compare (Pair<Demand,Link> e1 , Pair<Demand,Link> e2) { return Integer.compare(e1.getFirst().getIndex() , e2.getFirst ().getIndex()); }  } );
		return res;
	}

	public final List<? extends OpenStackNetworkElement> getVisibleElements (OpenStackNet os, AJTableType ajTableType)
	{
		switch(ajTableType)
		{
			case NODES:
				return getVisibleNodes().stream().map(n->os.getOpenStackNetworkElementByInternalId(n.getId())).collect(Collectors.toList());
			case LINKS:
				return os.getOpenStackLinks();
			case USERS:
				return os.getOpenStackUsers();
			case NETWORKS:
				return os.getOpenStackNetworks();
			case SUBNETS:
				return os.getOpenStackSubnets();
			default:
				assert false;
				return new ArrayList<>();
		}
	}

	public static final List<? extends OpenStackNetworkElement> getAllElements (OpenStackNet os, AJTableType ajTableType)
	{
		switch(ajTableType)
		{
			case NODES:
				return os.getOpenStackNodes();
			case LINKS:
				return os.getOpenStackLinks();
			case USERS:
				return os.getOpenStackUsers();
			case NETWORKS:
				return os.getOpenStackNetworks();
			case SUBNETS:
				return os.getOpenStackSubnets();
			default:
				assert false;
				return new ArrayList<>();
		}
	}
}
