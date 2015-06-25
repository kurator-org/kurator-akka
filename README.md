Kurator-Akka Framework
======================

The [kurator-akka](https://github.com/kurator-org/kurator-akka) repository hosts source code and examples for the **Kurator-Akka** scientific workflow framework.  This framework is being developed as part of the [Kurator project](http://wiki.datakurator.net/web/Kurator) and is designed to make it easy to develop and run high-performance data cleaning workflows.


**Kurator-Akka** is based on the [Akka actor toolkit and runtime](http://akka.io).  It aims to accelerate development of new data cleaning actors and to facilitate development of new workflows assembled from these actors.  **Kurator-Akka** supports actors implemented either in Java or Python, and the framework shields actor developers from the complexities of using the Akka API directly.  Workflows are specified using a YAML-based language that defines how data flows between the actors at run time.

Example actor and workflow
--------------------------

A *workflow* is a collection of actors configured to carry out some set of tasks.  An *actor* is a software component that receives data either from outside the workflow, or from other actors it is configured to listen to. Actors in **Kurator-Akka** may be defined either in Java or Python.

##### Java implementation of a Multiplier actor

The Java class below defines a simple actor for multiplying an integer by a configurable constant:

    import org.kurator.akka.AkkaActor;
    public class Multiplier extends AkkaActor {
        public int factor = 1;
        @Override public void handleData(Object i) {
        	broadcast((int)i * factor);
        }
    }

As shown above, a new Java actor can be implemented by overriding the `handleData()` method of the `org.kurator.akka.AkkaActor` class.  This method will be called by the **Kurator-Akka** framework each time the actor receives any data.  The `broadcast()` method is used within the `handleData()` method to send data (usually the results of performing some computation on the data received by the actor) to any other actors in the workflow configured to listen to this one.

##### Python implementation of the Multiplier actor

An implementation of the same actor in Python does not require a class to be defined:

    factor = 1
    def multiply(i):
        return factor * i

The **Kurator-Akka** framework calls the `multiply()` method on each data item received by the actor.  The value returned from the function is automatically broadcast to listeners.

##### YAML declaration of the Python version of the Multiplier actor

In addition to the Java or Python definition of an actor, an actor declaration authored in YAML is needed to make the actor available for use in workflows.  The following declares that the actor named `MultiplyByFactor` invokes the `multiply()` function defined in the file `multiplier.py`:

    imports:
    - classpath:/org/kurator/akka/types.yaml

    types:
    - id: Multiplier
      type: PythonFunctionActor
      properties:
        defaults:
          path: multiplier.py
          function: multiply

##### Defining a workflow that uses the Multiplier actor

With the above YAML saved to a file named `myactors.yaml`, the `MultiplyByFactor` actor can be used in a workflow also defined in YAML. The workflow below takes an input value from the command line, multiplies it by 2, and outputs the result:

    imports:

    - file:int_actors.yaml

    components:

    - id: MultiplyByTwoWorkflow
      type: Workflow
      properties:
        actors:
          - !ref ReadOneNumber
          - !ref MultiplyByTwo
          - !ref PrintProduct

    - id: ReadOneNumber
      type: StdinIntReader

    - id: MultiplyByTwo
      type: IntMultiplier
      properties:
        listensTo:
          - !ref ReadOneNumber
        parameters:
          factor: 2

    - id: PrintProduct
      type: IntPrinter
      properties:
        listensTo:
          - !ref MultiplyByTwo

The above declaration states the following: `MultiplyByTwo` is a workflow comprising three actors, `ReadOneNumber`, `MultiplyByTwo`, and `PrintProduct`. `MultiplyByTwo` listens to (receives its input from) `ReadOneNumber`, and `PrintProducts` receives its input in turn from `MultiplyByTwo`.  `MultiplyByTwo` is declared to be an instance of the `Multiplier` actor defined previously; this instance of `Multiplier` is configured to multiply each value it receives by a factor of 2.  The YAML declarations for the underlying actors `StdinNumberReader`, `Multiplier`, and `NumberPrinter` are all read from `myactors.yaml`.

The YAML definition of a workflow using Java implementations of each actor looks identical to a workflow using Python actors.  Java and Python actors can be used together in the same workflow without any limitations.


