Kurator-Akka Framework
======================

The Kurator-Akka repository hosts the core classes for a scientific workflow automation framework based on the [Akka actor toolkit and runtime](http://akka.io). This framework is being developed as part of the [Kurator project](http://wiki.datakurator.net/web/Kurator).

Overview
--------

The Kurator-Akka framework aims to accelerate development of new actors in Java or Python by shielding developers from the complexities of using the Akka API.  The framework also facilitates development of new workflows assembled from these actors. Instead of writing Java code to define and execute each new Akka workflow, custom workflows may be specified using a simple, YAML-based language that defines how data flows between the actors at run time.

### Example actor and workflow

The Java class below defines a simple actor for multiplying an integer by a configurable constant:

    package myactors;
    import org.kurator.akka.AkkaActor;
    
    public class Multiplier extends AkkaActor {
        public int factor = 1;
        @Override public void handleData(Object value) {
            broadcast((Integer)value * factor);
        }
    }

The code above illustrates how a new Java actor can be implemented simply by overriding the `handleData()` method of the `org.kurator.akka.AkkaActor` class.  

An implementation of the same actor in Python does not require a class to be defined:

    factor = 1
    def multiply(value):
        return factor * value

In both cases a snippet of YAML is needed to make the code accessible to workflows using the actor.  The YAML needed to declare that an actor named `MultiplyByFactor` uses the `multiply()` function defined in the file `multiply_by_factor.py` looks like this:

    imports:
    - classpath:/org/kurator/akka/types.yaml

    types:
    - id: Multiplier
      type: PythonFunctionActor
      properties:
        defaults:
          path: multiplier.py
          function: multiply

With the the above YAML saved to a file named `myactors.yaml`, the `MultiplyByFactor` actor can be used in a simple workflow also defined in YAML. This workflow takes an input value,  multiplies it by 2, and outputs the result:

    imports:
    - file:myactors.yaml

    components:

    - id: MultiplyByTwo
      type: Workflow
      properties:
        actors:
          - !ref ReadOneNumber
          - !ref MultiplyByTwo
          - !ref PrintProduct

    - id: ReadOneNumber
      type: StdinNumberReader
      properties:
        fireOnce: true

    - id: MultiplyByTwo
	  type: Multiplier
      properties:
        listensTo:
          - !ref ReadOneNumber
        parameters:
          factor: 2

    - id: PrintProduct
      type: NumberPrinter
      properties:
        listensTo:
          - !ref MultiplyByTwo

