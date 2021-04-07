package io.github.agentsoz.nonmatsim;

/*-
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

import java.util.Map;

public class EventData {
    private final double time;
    private final String type;
    private final Map<String,String> attributes;

    public EventData(double time, String type, Map<String, String> attributes) {
        this.time = time;
        this.type = type;
        this.attributes = attributes;
    }

    public double getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
