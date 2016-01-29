package io.github.agentsoz.bushfire;

/*
 * #%L
 * BDI-ABM Integration Package
 * %%
 * Copyright (C) 2014 - 2015 by its authors. See AUTHORS file.
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

import io.github.agentsoz.abmjill.JillModel;
import io.github.agentsoz.bdiabm.ABMServerInterface;
import io.github.agentsoz.bdiabm.Agent;
import io.github.agentsoz.bdiabm.data.AgentDataContainer;
import io.github.agentsoz.bdimatsim.moduleInterface.data.SimpleMessage;
import io.github.agentsoz.bushfire.datamodels.Region;
import io.github.agentsoz.bushfire.datamodels.RegionSchedule;
import io.github.agentsoz.bushfire.jill.agents.BasicResident;
import io.github.agentsoz.bushfire.jill.agents.EvacController;
import io.github.agentsoz.bushfire.shared.ActionID;
import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BushfireApplication class handles passing percepts and actions to and
 * from the resident agents, passing simulation setup data (locations, routes
 * etc) and agent plan choices to the visualiser, broadcasting alerts to the
 * agents, and managing the EvacController and relief schedule data.
 **/
public class BushfireApplication extends JillModel implements DataClient,
		DataSource {

	final Logger logger = LoggerFactory.getLogger("");
	private BdiConnector bdiConnector;
	protected EvacController controller;
	protected HashMap<String, Object[]> agentsDriveToActions = new HashMap<String, Object[]>();

	/**
	 * Used only when byPassController is enabled
	 */
	static ArrayList<String> regionsToEvacuate = new ArrayList<String>();

	/**
	 * Used only when byPassController is enabled
	 */
	static boolean startedEvac = false;

	/**
	 * Input arguments sent to {@link BushfireMain}. This should be passed to
	 * JillModel.
	 */
	private String[] inputArgs;

	/**
	 * Data server instance This is used to get the fire alert from
	 * {@link FireModule}, and to pass information to visualiser.
	 */
	protected DataServer dataServer;

	public BushfireApplication(String[] args) {
		this.inputArgs = args;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The init method is overridden here for three reasons. 1. to setup data
	 * server. 2. to publishGeography to unity visualiser. 3. to assign jill
	 * agents the names that are used by MATSim
	 */
	@Override
	public boolean init(AgentDataContainer agentDataContainer,
			io.github.agentsoz.bdiabm.data.AgentStateList agentList,
			ABMServerInterface abmServer, Object[] params) {
		String[] agentNames = (String[]) params;

		boolean result = super.init(agentDataContainer, agentList, abmServer,
				inputArgs);
		if (result) {
			this.setupDataServer();
			this.publishGeography();

			int index = 0;

			for (Agent agent : getAllAgents()) {
				if (agent instanceof BasicResident) {
					((BasicResident) agent).setName(agentNames[index++]);
					((BasicResident) agent).init(bdiConnector, getKids(""),
							getRels(""));
				}
			}
		} else {
			logger.error("Failed to create Jill agents");
			System.exit(0);
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void takeControl(AgentDataContainer adc) {
		// TODO: remove this
		logger.info("==================" + dataServer.getTime());

		// Iterate through all stored DRIVE_TO actions and if their waiting time
		// is reached package the action.
		Object[] agentsInDriveToList = agentsDriveToActions.keySet().toArray();

		for (Object agentName : agentsInDriveToList) {
			Object[] parameters = agentsDriveToActions.get((String) agentName);

			if ((Integer) parameters[3] == 0) {
				agentsDriveToActions.remove(agentName);
				packageDriveToAction((String) agentName, parameters);
			} else {
				parameters[3] = (Integer) parameters[3] - 1;
				agentsDriveToActions.remove(agentName);
				agentsDriveToActions.put((String) agentName, parameters);
			}
		}

		// setAgentsDepartureEvents is useful only when the byPassController is
		// enabled.
		this.setAgentsDepartureEvents(adc);

		// Take control about the packaged actions in previous steps.
		super.takeControl(adc);
	}

	/**
	 * This method is useful only when the byPassController is enables.
	 * 
	 * @param adc
	 */
	protected void setAgentsDepartureEvents(AgentDataContainer adc) {
		// on receiving the fire alert, insert a global percept into the data
		// container
		if (!regionsToEvacuate.isEmpty()) {
			adc.getOrCreate("global").getPerceptContainer()
					.put(FireModule.FIREALERT, regionsToEvacuate.clone());
			logger.debug("added fire alert percept");
			regionsToEvacuate.clear();
		}
	}

	/**
	 * Setting up the data server.
	 */
	private void setupDataServer() {
		dataServer = DataServer.getServer("Bushfire");
		dataServer.subscribe(this, DataTypes.FIRE_ALERT);
		dataServer.subscribe(this, DataTypes.EVAC_BROADCAST);
		dataServer.subscribe(this, DataTypes.MATSIM_AGENT_UPDATES);
		dataServer.registerSource("time", this);

		bdiConnector = new BdiConnector(this);
		boolean controllerFound = false;

		for (Agent agent : getAllAgents()) {
			if (agent instanceof EvacController) {
				((EvacController) agent).setName("controller");
				controller = (EvacController) agent;
				controllerFound = true;
				break;
			}
		}

		if (!controllerFound) {
			Log.error("Controller is not available");
			System.exit(-1);
		}

	}

	/**
	 * Implemented here so we can cleanly close our log files.
	 */
	@Override
	public void finish() {
		logger.info("shut down");
		EvacuationReport.close();
		super.finish();
	}

	public void promptControllerInput() {
		SimpleMessage msg = new SimpleMessage();
		msg.name = DataTypes.UI_PROMPT;
		String prompt = "Specify input for EvacController agent?";
		String[] options = new String[] { "Yes", "No" };
		msg.params = new Object[] { prompt, options };
		dataServer.publish(DataTypes.UI_PROMPT, msg);
	}

	public void processActions(BasicResident agent, String actionID,
			Object[] parameters) {

		Region region = getResidetsHomeRegion(agent.getName());

		// BDI agent has completed all of its goals - publish update
		if ((String) parameters[0] == "done" && dataServer != null) {
			SimpleMessage message = new SimpleMessage();
			message.name = "done";
			message.params = new Object[] { agent.getName(), "done" };
			dataServer.publish(DataTypes.BDI_AGENT_UPDATES, message);
			logger.debug("agent {}(id:{}) made it to relief centre",
					agent.getName(), agent.getId());
			region.agentEvacuated();
			return;
		}

		// translate abstract destination names to actual network coordinates
		// before packaging
		if ((String) parameters[0] == ActionID.DRIVETO) {
			logger.debug("agent {}(id:{}) will drive to {} after {} seconds",
					agent.getName(), agent.getId(), (String) parameters[2],
					Integer.toString((int) parameters[3]));
			agentsDriveToActions.put(agent.getName(), parameters);
		}
	}

	protected void packageDriveToAction(String agentName, Object[] parameters) {
		Region region = getResidetsHomeRegion(agentName);

		double[] destination = (double[]) parameters[1];
		String destinationName = (String) parameters[2];

		logger.debug("agent (name:{}) driving to {}, coordinates: {}",
				agentName, destinationName, destination);
		region.agentStart();

		super.packageAgentAction(agentName, ActionID.DRIVETO, parameters);

		if (dataServer != null) {
			// publish new agent actions
			SimpleMessage message = new SimpleMessage();
			message.name = "updateAgentBDI";
			message.params = new Object[] { agentName, parameters[0],
					parameters[1], parameters[2] };
			dataServer.publish(DataTypes.BDI_AGENT_UPDATES, message);
		}
	}

	protected Region getResidetsHomeRegion(String agentName) {
		BasicResident resident = ((BasicResident) getAgentByName(agentName));
		return Config.getRegion(resident.homeRegion);
	}

	/**
	 * The visualiser module will store this data, and send it to the visualiser
	 * when the visualiser requests it TODO: Should go under visualiser module
	 */
	void publishGeography() {

		logger.debug("publishing LOCATION, ROUTE, REGION, and IMAGE");

		for (String location : Config.getLocations()) {
			dataServer
					.publish(DataTypes.LOCATION, Config.getLocation(location));
		}
		for (String route : Config.getRoutes()) {
			dataServer.publish(DataTypes.ROUTE, Config.getRoute(route));
		}
		for (String region : Config.getRegionsByName()) {
			dataServer.publish(DataTypes.REGION, Config.getRegion(region));
		}
		dataServer.publish(DataTypes.IMAGE, Config.getImage());
	}

	/**
	 * Publish the evacuation schedules of regions to the data server.
	 * 
	 * @param rs
	 */
	public void publishSchedule(RegionSchedule rs) {
		dataServer.publish(DataTypes.EVAC_SCHEDULE, rs);
	}

	/**
	 * Listens for fire alerts and sets received flag to true
	 */
	@Override
	public boolean dataUpdate(double time, String dataType, Object data) {

		switch (dataType) {

		case DataTypes.FIRE_ALERT: {

			logger.debug("received fire alert");

			if (Config.getBypassController()) {

				logger.debug("bypassing controller, broadcasting evac message");
				regionsToEvacuate = new ArrayList<String>(
						Config.getRegionsByName());
				logger.debug("number of regions to evacuate is {}",
						regionsToEvacuate.size());
			} else {
				logger.debug("telling controller to start fire response");
				controller.startFireResponse();
			}

			return true;
		}
		case DataTypes.EVAC_BROADCAST: {

			logger.debug("received evacuation broadcast");
			startedEvac = true;
			return true;
		}
		// establish starting location and home region for each agent
		case DataTypes.MATSIM_AGENT_UPDATES: {
			logger.debug("received matsim agent updates");
			for (SimpleMessage msg : (SimpleMessage[]) data) {

				String name = (String) msg.params[0];
				BasicResident agent = (BasicResident) getAgentByName(name);

				if (agent != null && agent.getStartLocation() == null) {

					double lat = (double) msg.params[1];
					double lon = (double) msg.params[2];
					logger.debug("agent {}(id:{}) start location is {},{}",
							name, agent.getId(), lon, lat);

					agent.startLocation = new double[] { lat, lon };
					Region r = Config.getRegion(lat, lon);

					if (r != null) {

						agent.homeRegion = r.getName();
						r.agents.add(agent);
						logger.debug("agent {}(id:{}) is in region {}", name,
								agent.getId(), agent.homeRegion);
						dataServer.publish(DataTypes.REGION_ASSIGNMENT,
								new Object[] { agent.getName(),
										agent.homeRegion });
					} else {
						logger.warn(
								"Agent {}(id:{}) was not assigned to any region",
								name, agent.getId());
					}
				}
			}
		}
		}

		return false;
	}

	/**
	 * Whether a particular agent should have children to pickup according to
	 * the percentage of agents with children to pickup.
	 * 
	 * @param name
	 * @return
	 */
	public boolean getKids(String name) {
		return new Random().nextDouble() < Config.getProportionWithKids();
	}

	/**
	 * Whether a particular agent should have relations to pickup according to
	 * the percentage of agents with relations to pickup.
	 * 
	 * @param name
	 * @return
	 */
	public boolean getRels(String name) {
		return new Random().nextDouble() < Config.getProportionWithRelatives();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getNewData(double time, Object parameters) {
		return null;
	}

	/**
	 * Returns the simulation time read from data server.
	 * 
	 * @return
	 */
	public double getSimTime() {
		return dataServer.getTime();
	}
}