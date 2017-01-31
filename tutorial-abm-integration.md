---
layout: page
title: ABM integration HOWTO
permalink: /howto-abm-integration/
---

In this HOWTO we will step through the development of a custom ABM
integration, i.e., JAVA code for a new ABM System Integration box as shown in
the figure below.

<img alt="BDI-ABM software architecture" src="{{ site.baseurl }}/fig-tiers.png" width="849"/>

The system we will integrate is [GAMS](http://www.gams.com), a system for
modeling linear, nonlinear and mixed integer optimization problems. The
complete working code for the GAMS integration can be found
[here (v1.0.0)](https://github.com/agentsoz/bdi-abm-integration/tree/v1.0.0/integrations/bdi-gams).
It is used in the conservation ethics application
[here (v1.0.0)](https://github.com/agentsoz/bdi-abm-integration/tree/v1.0.0/examples/conservation).

This tutorial assumes intermediate Java development knowledge. We use Eclipse
for our Java development, but feel free to use your favourite IDE.

## Step #1: Implement the ABM interface

The first step in building a new ABM integration is to create a new Java class
that implements the generic layer ABM Interface `io.github.agentsoz.bdiabm.ABMServerInterface`.
This interface has only two functions, and the most basic (empty) implementation
is given below.

```java
public class GAMSModel implements ABMServerInterface {

    @Override
    public void takeControl(AgentDataContainer agentDataContainer) {
    }

    @Override
    public Object queryPercept(String agentID, String perceptID) {
        return null;
    }
}
```

The `takeControl` function is where data exchange between the BDI and ABM
systems occurs, and is what will be called (by your application) at each
simulation cycle. The `agentDataContainer` object is what holds the data for
all the agents. Essentially it is a map, with agent name as key, and an
`ActionPerceptContainer` object as value, that in turn stores the agent's
actions (in class `ActionContainer`) and percepts ()in class `PerceptContainer`).
You may wish to inspect the top level
`io.github.agentsoz.bdiabm.data.AgentDataContainer` class further to understand
the structure details. This map is passed back and forth between the BDI and ABM
systems, each updating it as the simulation progresses.

The second function `queryPercept` is used, by the BDI system, to make ad-hoc
queries in the ABM. This is explained in the papers, but is essentially used
for data transfer efficiency. It is often the case that certain information
required in the BDI system is context dependent and need not be shipped as a
percept on each cycle (using the `agentDataContainer`).
For instance, an agent may require information about traffic conditions on the
road only when it is preparing to drive to its destination, and not at any
other time. In this case, the BDI agent would request this information from
the ABM, when needed, using the `queryPercept` function.

## Step #2: Integrate the ABM system

This is the part where we hook up the external ABM system to our new class --
the very purpose of this exercise. Typically this would involve API calls to
the underlying ABM, either directly through Java if the ABM has a compatible
API, or else through an indirect means, such as using the Java Native Interface
(JNI) to communicate to a native ABM application, or using a network-based
connection.

In our case, fortunately, GAMS provides a nice Java interface (JAR) which makes
it a relatively straightforward exercise to control the GAMS system through our
class.

To begin, we must first ensure that the GAMS JAR is available at compile and
run time. The configuration for this will vary depending on your development
set up. We use Eclipse and Maven, so making the JAR available is a matter of
 installing it in the local Maven repository, so that we can access it via the
 project's POM file. (Sorry if this sounds like jibbersih to non-Maven users.)
 This is simple to do, and for details see the
 [GAMS integration README file](https://github.com/agentsoz/bdi-abm-integration/blob/v1.0.0/integrations/bdi-gams/README.md).

 Next is to ensure that the GAMS system is initialised correctly when the
 application starts. We will do this in the constructor of our new class:

```java
public GAMSModel(String GAMSDir, String file) {
    wsInfo = new GAMSWorkspaceInfo();
    wsInfo.setSystemDirectory(GAMSDir);
    // create GAMSWorkspace "ws" with user-specified system directory and the default working directory
    // (the directory named with current date and time under System.getProperty("java.io.tmpdir"))
    ws = new GAMSWorkspace(".", GAMSDir,  GAMSGlobals.DebugLevel.KEEP_FILES);
    // create GAMSJob from file
    job = ws.addJobFromFile(file);
}
```
The details of code are less important here. Its purpose is mainly to initialise
the GAMS system and get it ready to execute the code from the given GAMS
code `file`.

What remains is a way to *run* the configured GAMS code at each simulation
cycle, each time passing in a new set of input values, and collecting at the
end of the run, a new set of output values. We write a function to do
just that:

```java
public void run(HashMap<String,String> opts, ArrayList<String> input, ArrayList<String> output) {
    // run GAMSJob
    try {
        GAMSOptions opt = ws.addOptions();
        for (String key: opts.keySet()) {
            opt.defines(key, opts.get(key));
        }
      	job.run(opt);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

```

Again the details are not critical, but suffice it to say that it does what we
said it would.

# Step #3: Build and test

The complete integration code is surprisingly simple. You can see the final
completed GAMS integration class
[GAMSModel here](https://github.com/agentsoz/bdi-abm-integration/blob/v1.0.0/integrations/bdi-gams/src/main/java/io/github/agentsoz/bdigams/GAMSModel.java).

The final step is to make sure it all builds, and importantly works. You will
likely write some unit tests, and stub classes to do that.

# Next steps

Once your ABM integration is ready, and assuming you have a BDI integration you
can use (or you have created your own using the
[BDI integration HOWTO](howto-bdi-integration)), you can then start building
your new application. See the [Custom BDI-ABM application HOWTO](howto-bdi-abm-application)
for more details on that. Finally, to see how the GAMS integration is used in
an example application, see the
[`AuctioneerModel`](https://github.com/agentsoz/bdi-abm-integration/blob/v1.0.0/examples/conservation/src/main/java/io/github/agentsoz/conservation/AuctioneerModel.java)
class of the conservation ethics application.

