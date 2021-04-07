package io.github.agentsoz.bdimatsim;

/*
 * #%L
 * BDI-ABM Integration Package
 * %%
 * Copyright (C) 2014 - 2021 by its authors. See AUTHORS file.
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
import io.github.agentsoz.nonmatsim.BDIActionHandler;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ActionHandlerForReplanDriveto implements BDIActionHandler {
	private static final Logger log = LoggerFactory.getLogger(ActionHandlerForReplanDriveto.class ) ;

	private final MATSimModel model;
	public ActionHandlerForReplanDriveto(MATSimModel model ) {
		this.model = model;
	}
	@Override
	public ActionContent.State handle(String agentID, String actionID, Object[] args) {
		// assertions:
		MobsimAgent mobsimAgent = model.getMobsimAgentFromIdString(agentID) ;
		Gbl.assertNotNull(mobsimAgent) ;
		Gbl.assertIf( args.length >= 1 );
		Gbl.assertIf( args[0] instanceof MATSimModel.RoutingMode) ; // could have some default
		MATSimModel.RoutingMode routingMode = (MATSimModel.RoutingMode)args[0];
		if (WithinDayAgentUtils.isOnReplannableCarLeg(mobsimAgent)) {
			model.getReplanner().editTrips().replanCurrentTrip(mobsimAgent, 0.0, routingMode.name());
		}
		return ActionContent.State.PASSED;
	}
}
