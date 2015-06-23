Kurator-Akka Framework
======================

The Kurator-Akka repository hosts the core classes for a scientific workflow automation framework based on the [Akka actor toolkit and runtime](http://akka.io). This framework is being developed as part of the [Kurator project](http://wiki.datakurator.net/web/Kurator).

Overview
--------

The Kurator-Akka framework aims to accelerate development of new actors in Java or python by shielding developers from the complexities of using the Akka API.  The framework additinally facilitates development of new workflows using these actors. Instead of writing Java code to define and execute Akka workflows, workflows assembled from Kurator-Akka actors may be specified using a simple, YAML-based language.