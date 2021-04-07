package io.github.agentsoz.bdiabm.data;

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


import java.io.Serializable;
import com.google.gson.Gson;

public class ActionPerceptContainer implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5415958031296122423L;
	
	private ActionContainer actionContainer;
	private PerceptContainer perceptContainer;
	
	public ActionPerceptContainer ()
	{
		actionContainer = new ActionContainer();
		perceptContainer = new PerceptContainer();
	}
	
	/**
	 * @return
	 * The action container
	 */
	public synchronized ActionContainer getActionContainer ()
	{
		return actionContainer;
	}
	
	/**
	 * @return
	 * The percept container
	 */
	public synchronized PerceptContainer getPerceptContainer()
	{
		return perceptContainer;
	}
	
	public synchronized boolean isEmpty() {
		return perceptContainer.isEmpty() && actionContainer.isEmpty();
	}

	@Override
	public synchronized String toString() {
		return (isEmpty()) ? "{}" : new Gson().toJson(this);
	}
}
