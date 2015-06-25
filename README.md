Kurator-Akka Framework
======================

The [kurator-akka](https://github.com/kurator-org/kurator-akka) repository hosts source code and examples for the **Kurator-Akka** scientific workflow framework.  This software toolkit is being developed as part of the [Kurator project](http://wiki.datakurator.net/web/Kurator) and is designed to make it easy to develop and run high-performance data cleaning workflows.


**Kurator-Akka** is based on the [Akka actor toolkit and runtime](http://akka.io).  It aims to accelerate development of new data cleaning actors and to facilitate development of new workflows assembled from these actors.  **Kurator-Akka** supports actors implemented either in Java or Python, and the framework shields actor developers from the complexities of using the Akka API directly.  Workflows are specified using a YAML-based language that defines how data flows between the actors at run time.

Example actor and workflow
--------------------------

A *workflow* is a collection of actors configured to carry out some set of tasks.  An *actor* is a software component that receives data either from outside the workflow, or from other actors within the same workflow that it is configured to listen to. Actors in **Kurator-Akka** may be defined either in Java or Python.

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

    - id: Multiplier
      type: PythonFunctionActor
      properties:
        defaults:
          script: multiplier.py
          function: multiply

##### Defining a workflow that uses the Multiplier actor

With the above YAML saved to a file named `actors.yaml`, the `MultiplyByFactor` actor can be used in a workflow also defined in YAML. The workflow below takes an input value from the command line, multiplies it by 3, and outputs the result:

    imports:

    - file:actors.yaml

    components:

    - id: MultiplyByThreeWorkflow
      type: Workflow
      properties:
        actors:
          - !ref ReadOneNumber
          - !ref MultiplyByThree
          - !ref PrintProduct

    - id: ReadOneNumber
      type: NumReader

    - id: MultiplyByThree
      type: Multiplier
      properties:
        listensTo:
          - !ref ReadOneNumber
        parameters:
          factor: 3

    - id: PrintProduct
      type: NumPrinter
      properties:
        listensTo:
          - !ref MultiplyByThree

The above declaration states the following: `MultiplyByThreeWorkflow` is a workflow comprising three actors, `ReadOneNumber`, `MultiplyByThree`, and `PrintProduct`. `MultiplyByThree` listens to (receives its input from) `ReadOneNumber`, and `PrintProducts` receives its input in turn from `MultiplyByThree`.  `MultiplyByThree` is declared to be an instance of the `Multiplier` actor defined previously; this instance of `Multiplier` is configured to multiply each value it receives by a factor of 3.  The YAML declarations for the underlying actors `NumReader`, `Multiplier`, and `NumPrinter` are all imported from `actors.yaml`.

The YAML definition of a workflow using Java implementations of each actor looks identical to a workflow using Python actors.  Java and Python actors can be used together in the same workflow.

##### Inlining Python actors

**Kurator-Akka** allows the code for Python actors to be provided *inline*, i.e. within the workflow definition itself. No additional Python script file is needed in this case.  For example, the block of YAML defining the `MultiplyByThree` actor in the workflow definition above depends on an additional YAML declaration for the `Multiplier` actor defined in the `actors.yaml` file, which in turn depends on a Python script file named `multiplier.py`.  Because the code for this actor is only a few lines long, it may be reasonable to define the actor entirely inline.  In other words, thus block of YAML in the workflow:

    - id: MultiplyByThree
      type: Multiplier
      properties:
        listensTo:
          - !ref ReadOneNumber
        parameters:
          factor: 3

can be replaced with:

    - id: MultiplyByThree
      type: PythonFunctionActor
      properties:
        listensTo:
          - !ref ReadOneNumber
        defaults:
          function: triple
          code: |
            def triple(n):
              return 3 * n

The Python code defining the multiply() function is now defined within the same YAML file that declares the workflow as a whole. Inlined Python actors are useful for implementing simple actors needed for specific workflows.