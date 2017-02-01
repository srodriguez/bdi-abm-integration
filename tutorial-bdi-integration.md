---
layout: page
title: BDI integration HOWTO
permalink: /howto-bdi-integration/
---


<img alt="BDI-ABM software architecture" src="{{ site.baseurl }}/fig-tiers.png" width="849"/>

# Step 1: Implement the BDI interface

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
