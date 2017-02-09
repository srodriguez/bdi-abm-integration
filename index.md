---
# You don't need to edit this file, it's empty on purpose.
# Edit theme's home layout instead if you wanna make some changes
# See: https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: home
---

# BDI-ABM project home

[BDI-ABM Project]: https://github.com/agentsoz/bdi-abm-integration
[bdi-abm-website]: http://agentsoz.github.io/bdi-abm-integration

The [BDI-ABM Project][] software realises a mechanism for integrating
Belief-Desire-Intention (BDI) reasoning in agents within an
agent-based simulation (ABM). The software framework provides the
infrastructure that allows off-the-shelf
BDI and ABM systems to be combined. In this arrangement, conceptually,
the *body* of the agent resides in the physical environment of the ABM while
the *brain* is programmed separately within the BDI system.

<img alt="BDI-ABM conceptual architecture" src="{{ site.baseurl }}/fig-arch.png" width="849"/>

The concept is described in the following papers:

*  Dhirendra Singh, Lin Padgham, Brian Logan.
   Integrating BDI agents with Agent Based Simulation Platforms.
   Autonomous Agents and Multi-agent Systems,
   Volume 30, Issue 6, pages 1050-1071, 2016. ([DOI](http://dx.doi.org/10.1007/s10458-016-9332-x))

*  Lin Padgham and Dhirendra Singh. Making MATSim Agents Smarter with the
   Belief-Desire-Intention Framework (Chapter 31).
   Horni, A., Nagel, K. and Axhausen, K.W. (eds.). The Multi-Agent
   Transport Simulation MATSim. Ubiquity Press, London, 2016.
   ([DOI](http://dx.doi.org/10.5334/baw))

*  Lin Padgham, Kai Nagel, Dhirendra Singh, Qingyu Chen.
   Integrating BDI Agents into a MATSim Simulation.
   Frontiers in Artificial Intelligence and Applications 263 (ECAI 2014),
   pages 681-686, 2014. ([DOI](http://dx.doi.org/10.3233/978-1-61499-419-0-681))


The [BDI-ABM Project][] is maintained by the
[RMIT Agents Group](https://sites.google.com/site/rmitagents/).
For questions about this project, please contact Professor
[Lin Padgham](http://goanna.cs.rmit.edu.au/~linpa/).
<!--
Technical questions about the code can be directed to
Dhirendra Singh {% include icon-github.html username="dhixsingh" %}.
-->

<div class="rounded-btn"
onclick="location.href='https://github.com/agentsoz/bdi-abm-integration/releases/v1.0.0';">
<h2>Release v1.0.0 <img src="{{ site.baseurl }}/fig-download.png" height="56"/></h2>
</div>



# How to use this software

The project code [resides on GitHub][BDI-ABM Project].

A BDI-ABM application consists of three layers. A generic first layer
(`./integrations/bdi-abm`) manages the high level interaction and message
passing between the BDI and the ABM system. A second platform specific
layer realises the connection between a specific BDI platform (such as
JACK, i.e., `./integrations/abm-jack`), and a specific ABM system (such
as MATSim, i.e., `./integrations/bdi-matsim`). Finally, a third application
layer puts these together along with domain specific code (for instance
`./examples/bushfire`). The following figure shows the arrangement.


<img alt="BDI-ABM software architecture" src="{{ site.baseurl }}/fig-tiers.png" width="849"/>

Overall, the repository consists of *integrations* and *examples*. Integrations
are platform specific and live in `./integrations`. Examples are domain
specific, and live in `./examples`. The following integrations
are provided:

Integration   | Directory                   | Description
:-------------|:----------------------------|:----------------------------
BDI-ABM       | `./integrations/bdi-abm`    | BDI-ABM communication and data layer
BDI-GAMS      | `./integrations/bdi-gams`   | Integration for [GAMS](http://www.gams.com)
BDI-MATSim    | `./integrations/bdi-matsim` | Integration for [MATSim](http://www.matsim.org)
BDI-Repast    | `./integrations/bdi-repast` | Integration for [Repast Simphony](http://repast.sourceforge.net) <br/><span class="msg-warn">OUTDATED SINCE REPAST 2.1</span>
ABM-GORITE    | `./integrations/abm-gorite` | Integration for [GORITE](https://en.wikipedia.org/wiki/GORITE) <br/><span class="msg-warn">UNAVAILABLE DUE TO PROPRIETARY LICENSE</span>
ABM-JACK      | `./integrations/abm-jack`   | Integration for [JACK](http://aosgrp.com/products/jack)
ABM-Jadex     | `./integrations/abm-jadex`  | Integration for [Jadex](https://www.activecomponents.org/) <br/><span class="msg-warn">OUTDATED SINCE JADEX 3.0</span>
              |                             |

Integrations are pulled together to build application examples. The following
examples are provided:

Example             | Directory                  | Description
:-------------------|:---------------------------|:----------------------------
Bushfire Evacuation | `./examples/bushfire`      | Uses JACK and MATSim
Conservation Ethics | `./examples/conservation`  | Uses JACK and GAMS
Taxi Service        | `./examples/taxi`          | Uses GORITE and MATSim<br/><span class="msg-warn">UNAVAILABLE</span>
Child Vaccination   | `./examples/vaccination`   | Uses JACK and a custom Python-based ABM
Humans and Zombies  | `./examples/zombies`       | Uses Jadex and Repast<br/><span class="msg-warn">UNAVAILABLE</span>
                    |                            |



# Build dependencies


* [Java Development Kit (JDK) 1.8](http://en.wikipedia.org/wiki/Java_Development_Kit)

* [Apache Maven 3.3.x+](maven.apache.org)

* [Eclipse with built-in Maven support](https://www.eclipse.org) (for development only)


* Some of the integrations (e.g., JACK, GAMS) require third-party
  libraries to be installed in your local Maven repository. See
  the respective READMEs (`./integrations/*/README.md`) for details.
  *The project will not build unless these dependencies have been
   resolved.*

# Compiling

This software is managed by the Apache Maven software management tool. You
can build the software in two ways.

* Examples can all be built from the command line.
  Ensure that you have Maven installed.
  Check the corresponding README (`./examples/*/README.md`) for
  instructions on how to build an example. Essentially this involves executing
  the following build command for the generic layer, BDI integration, ABM
  integration, and the example.

  ```
  mvn clean install
  ```

* Each integration and example also contains an Eclipse project. First
  ensure that you have an appropriate Eclipse version that supports Maven.
  Then import the existing project
  (e.g., `./integrations/bdi-abm/.project`) into Eclipse, and it should
  build without any additional configuration.



# License


BDI-ABM Integration Package
Copyright (C) 2014, 2015 by its authors. See AUTHORS file.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

For contact information, see AUTHORS file.


# Related pages

<ul class="toc-list">
  {% for post in site.html_pages %}
    <li>
      <a class="toc-link" href="{{ post.url | relative_url }}">{{ post.title  }}</a>
    </li>
  {% endfor %}
</ul>


