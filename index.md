---
# You don't need to edit this file, it's empty on purpose.
# Edit theme's home layout instead if you wanna make some changes
# See: https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: home
---

# BDI-ABM Project Home

[BDI-ABM Project]: https://github.com/agentsoz/bdi-abm-integration
[bdi-abm-website]: http://agentsoz.github.io/bdi-abm-integration

The [BDI-ABM Project][] software realises a mechanism for integrating 
Belief-Desire-Intention (BDI) reasoning in agents within an
agent-based simulation (ABM). The concept is described
in the following papers:

*  Dhirendra Singh, Lin Padgham, Brian Logan. 
   Integrating BDI agents with Agent Based Simulation Platforms.
   Autonomous Agents and Multi-agent Systems, 
   Volume 30, Issue 6, pages 1050-1071, 2016. ([DOI](http://dx.doi.org/10.1007/s10458-016-9332-x))
   
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
<h2>Release v1.0.0 <img src="./fig-download.png" height="56"/></h2>
</div>


<h1 class="page-heading">Table of Contents</h1>
  
<ul class="toc-list">
  {% for post in site.html_pages %}
    <li>
      <a class="toc-link" href="{{ post.url | relative_url }}">{{ post.title  }}</a>
    </li>
  {% endfor %}
</ul>


