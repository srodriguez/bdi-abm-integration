package io.github.agentsoz.bushfire.jill.agents;

/*
 * #%L
 * BDI-ABM Integration Package
 * %%
 * Copyright (C) 2014 - 2016 by its authors. See AUTHORS file.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.agentsoz.bdiabm.data.ActionContent;
import io.github.agentsoz.bdiabm.data.ActionContent.State;
import io.github.agentsoz.bushfire.FireModule;
import io.github.agentsoz.bushfire.bdi.IBdiConnector;
import io.github.agentsoz.bushfire.datamodels.ReliefCentre;
import io.github.agentsoz.bushfire.jill.goals.EvacHouse;
import io.github.agentsoz.jill.lang.Agent;
import io.github.agentsoz.jill.lang.AgentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * @author Sewwandi Perera
 *
 */
@AgentInfo(hasGoals = { /* "io.github.agentsoz.bushfire.jill.goals.EvacHouse", */
"io.github.agentsoz.abmjill.genact.EnvironmentAction" })
public class BasicResident extends Agent implements
		io.github.agentsoz.bdiabm.Agent {

	final Logger logger = LoggerFactory.getLogger("");
	public boolean kidsNeedPickup;
	public boolean relsNeedPickup;
	public String homeRegion;
	public double[] currentDestination;
	public double[] currentStartLocation;
	public double[] startLocation;
	public IBdiConnector bdiConnector;
	public List<Coordinate> evacRoute;
	public List<String> waypointNames;
	public int evacDelay;

	public BasicResident(String name) {
		super(name);
	}

	public void init(IBdiConnector bdiConnector, boolean getKids,
			boolean getRels) {
		this.bdiConnector = bdiConnector;
		this.kidsNeedPickup = getKids;
		this.relsNeedPickup = getRels;
		logger.debug("Agent " + getName() + "(id:" + getId()
				+ ") initialised; {} ;", (getKids ? "has kids; " : "no kids; ")
				+ (getRels ? "has relatives" : "no relatives"));
		currentDestination = new double[2];
		Random r = new Random();
		evacDelay = r.nextInt(1800);
	}

	public void setDestination(double[] dest) {
		currentStartLocation = currentDestination;
		currentDestination = dest;
	}

	public double[] getStartLocation() {
		return startLocation;
	}

	public void updateActionState(String actionID, State state, Object[] params) {
		logger.info("============================== post updateActionState");
		// TODO:postEvent(updateaction_p.postEvent(actionID, state, params));
	}

	public double getCurrentTime() {
		return bdiConnector.getCurrentTime();
	}

	public void fireAlert(List<Coordinate> evacRoute, List<String> waypointNames) {
		this.evacRoute = evacRoute;
		this.waypointNames = waypointNames;
		post(new EvacHouse("Evac House"));
	}

	@Override
	public void init(String[] args) {

	}

	@Override
	public void start() {

	}

	@Override
	public void handlePercept(String perceptID, Object parameters) {
		if (bdiConnector.shouldByPassController()) {
			ArrayList<String> params = (ArrayList<String>) parameters;

			if (perceptID.equals(FireModule.FIREALERT)
					&& params.contains(homeRegion)) {
				List<Coordinate> waypoints = new ArrayList<Coordinate>();
				List<String> waypointNames = new ArrayList<String>();

				ReliefCentre randomRS = bdiConnector.getRandomEvacPoint();

				waypointNames.add(randomRS.getLocation());
				waypoints.add(randomRS.getLocationCoords());
				fireAlert(waypoints, waypointNames);
			}
		}
	}

	@Override
	public void packageAction(String actionID, Object[] parameters) {
		bdiConnector.processAction(this, actionID, parameters);
	}

	@Override
	public void updateAction(String actionID, ActionContent content) {
		updateActionState(actionID, content.getState(), content.getParameters());
	}

	@Override
	public void kill() {
		super.finish();
	}

	public String getRegion() {
		return homeRegion;
	}

}
