---
layout: page
title: BDI integration HOWTO
permalink: /howto-bdi-integration/
---

In this HOWTO we will implement a BDI System Integration as shown in the diagram
below. The BDI system we will integrate is
[Jill](https://github.com/agentsoz/jill), a new BDI system we are
developing (it is still in alpha) that is geared towards social simulation.

You can access the full Jill integration code
[here](https://github.com/agentsoz/bdi-abm-integration/blob/master/integration/abm-jill/src/main/java/io/github/agentsoz/JillModel.java).
*Note that this code is not available in the latest release version--it was
developed more recently and will be included in the next release.*

<img alt="BDI-ABM software architecture" src="{{ site.baseurl }}/fig-tiers.png" width="849"/>

# Step 1: Implement the BDI interface

The first step in developing a custom BDI interface is to define a new class that
implements `io.github.agentsoz.bdiabm.BDIServerInterface`, as shown below.

```java
public  class JillModel implements BDIServerInterface {

    public JillModel() {}

    @Override
    public boolean init(AgentDataContainer agentDataContainer,
        AgentStateList agentList,
        ABMServerInterface abmServer,
        Object[] params)
    {
        // Parse the command line options
        ArgumentsLoader.parse((String[])params);
        // Load the configuration
        config = ArgumentsLoader.getConfig();
        // Now initialise Jill with the loaded configuration
        try {
            Main.init(config);
        } catch(Exception e) {
            Log.error("While initialising JillModel: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        Main.start(config);
    }

    @Override
    public void finish() {
        Main.finish();
    }

    @Override
    public void takeControl(AgentDataContainer agentDataContainer) {
        // ...
    }

}
```

Our `JillModel` class implements the four functions provided by the
`BDIServerInterface` interface.

The `init` function is called at initialisation, and prior to the
simulation's first step. The implementation
shown above basically initialises Jill (`Main.init`) using the provided
arguments (`params`, that in turn would have been passed as command line
arguments to the application, and are first parsed by Jill using
`ArgumentsLoader.parse`).

The `start` function is called just prior to the simulation's first step, and
after the model has been initialised (so after the call to `init`). Typically
the `init` function will initialise all the BDI agents in the system, while
the `start` will set them running, such as by posting any startup goals. You
may find it helpful to explore the `Main.init` and `Main.start` classes in Jill
to get a sense of what each doesn exactly.

The `finish` is called just prior to the simulation terminating, and provides
an opportunity to perform any finalisation tasks, such as closing any open
connections, writing logs, and so on.

The `takeControl` function is where data exchange between the BDI and ABM
systems occurs, and is what will be called (by your application) at each
simulation cycle. The `agentDataContainer` object is what holds the data for
all the agents. Essentially it is a map, with agent name as key, and an
`ActionPerceptContainer` object as value, that in turn stores the agent's
actions (in class `ActionContainer`) and percepts (in class `PerceptContainer`).
You may wish to inspect the top level
`io.github.agentsoz.bdiabm.data.AgentDataContainer` class further to understand
the structure details. This map is passed back and forth between the BDI and ABM
systems, each updating it as the simulation progresses.

The `takeControl` implementation in all BDI integrations is conceptually the
same. Its purpose is to deliver all the data received via the
`agentDataContainer`. to the addressed agents in the BDI system. This involves
the following:

*  Deliver any `BROADCAST` percepts to *all* agents;
*  deliver all per-agent percepts (`PerceptContainer`) to the addressed agent
   by calling its `handlePercept` function;
   (see [`io.github.agentsoz.bdiabm.Agent.handlePercept`]())
*  update, per-agent, the status' of all its actions (received in `ActionContainer`)
   by calling its `updateAction` function
   (see [`io.github.agentsoz.bdiabm.Agent.updateAction`]()); and
*  finally wait (blocking) until all BDI agents have finished their activities
   (during which agents will progress on their executing plans, and may post
   BDI actions that get added by the infrastructure to the `agentDataContainer`),
   before returning control back to the calling function.

The full implementation of `JillModel.takeControl` is given below.

```java
	@Override
	// send percepts to individual agents
	public void takeControl(AgentDataContainer agentDataContainer) {

		Log.trace("Received " + agentDataContainer);

		if (agentDataContainer == null || agentDataContainer.isEmpty()) {
			Log.debug("Received empty container, nothing to do.");
			return;
		}
		nextContainer = agentDataContainer;

		boolean global = false;
		HashMap<String, Object> globalPercepts = new HashMap<String, Object>();

		try {
			PerceptContainer gPC = agentDataContainer.get(BROADCAST)
					.getPerceptContainer();
			String[] globalPerceptsArray = gPC.perceptIDSet().toArray(
					new String[0]);
			for (int g = 0; g < globalPerceptsArray.length; g++) {
				String globalPID = globalPerceptsArray[g];
				Object gaParameters = gPC.read(globalPID);
				globalPercepts.put(globalPID, gaParameters);
				global = true;
			}
		}
		// no global agent
		catch (NullPointerException npe) {
			global = false;
		}
		// post global percepts to all agents - this was moved out of the below
		// while loop
		// since not all agents will have an ActionPerceptContainer when program
		// starts
		if (global) {
			Iterator<Map.Entry<String, Object>> globalEntries = globalPercepts
					.entrySet().iterator();
			while (globalEntries.hasNext()) {
				Map.Entry<String, Object> gme = globalEntries.next();
				String gPerceptID = gme.getKey();
				Object gParameters = gme.getValue();
				for (int i = 0; i < GlobalState.agents.size(); i++) {
					((Agent) GlobalState.agents.get(i)).handlePercept(
							gPerceptID, gParameters);
				}
			}
		}

		Iterator<Entry<String, ActionPerceptContainer>> i = agentDataContainer
				.entrySet().iterator();
		// For each ActionPercept (one for each agent)
		while (i.hasNext()) {
			Map.Entry<String, ActionPerceptContainer> entry = (Map.Entry<String, ActionPerceptContainer>) i
					.next();
			if (entry.getKey().equals(BROADCAST)) {
				continue;
			}
			ActionPerceptContainer apc = entry.getValue();
			PerceptContainer pc = apc.getPerceptContainer();
			ActionContainer ac = apc.getActionContainer();
			if (!pc.isEmpty()) {
				Set<String> pcSet = pc.perceptIDSet();
				String[] pcArray = pcSet.toArray(new String[0]);

				for (int pcI = 0; pcI < pcArray.length; pcI++) {
					String perceptID = pcArray[pcI];
					Object parameters = pc.read(perceptID);
					try {
						int id = Integer.parseInt(entry.getKey());
						((Agent) GlobalState.agents.get(id)).handlePercept(
								perceptID, parameters);
					} catch (Exception e) {
						Log.error("While sending percept to Agent "
								+ entry.getKey() + ": " + e.getMessage());
					}
				}
				// now remove the percepts
				pc.clear();
			}
			if (!ac.isEmpty()) {
				Iterator<String> k = ac.actionIDSet().iterator();
				// for each action, update the agent action state
				while (k.hasNext()) {

					String actionID = k.next();
					// convert from state definition in bdimatsim to definition
					// in
					// jack part
					State state = State.valueOf(ac.get(actionID).getState()
							.toString());
					Object[] params = ac.get(actionID).getParameters();
					ActionContent content = new ActionContent(params, state, actionID);
					try {
						int id = Integer.parseInt(entry.getKey());
						((Agent) GlobalState.agents.get(id)).updateAction(
								actionID, content);
					} catch (Exception e) {
						Log.error("While updating action status for Agent "
								+ entry.getKey() + ": " + e.getMessage());
					}

					// remove completed states
					if (!(state.equals(State.INITIATED) || state
							.equals(State.RUNNING))) {
						ac.remove(actionID);
					}
				}
			}
		}
		// Wait until idle
		Main.waitUntilIdle();
	}

```

That's it. That is all that is required to build a custom BDI integration.


# Next steps

To see how the Jill integration is used in
an example application, see the
[`LandholderModel`](https://github.com/agentsoz/bdi-abm-integration/blob/master/examples/conservation/src/main/java/io/github/agentsoz/conservation/LandholderModel.java)
class of the conservation ethics application. (*Note that that code exists
on the latest master branch, and not in release v1.0.0.*)

To build your own BDI-ABM application, you will also require a suitable ABM
integration. If there is no [existing ABM integration]({{ site.baseurl }}) you
can use, then you may wish to create your own using the
[ABM integration HOWTO]({{ site.baseurl }}/howto-abm-integration).
You can then start building your new application. See the
[custom BDI-ABM application HOWTO]({{ site.baseurl }}/howto-bdi-abm-application)
for more details on that.

<br/><br/>
*Last updated by Dhirendra Singh on 02 Feb 2017*
