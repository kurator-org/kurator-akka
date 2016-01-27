Kurator-Akka Framework
======================

The [kurator-akka](https://github.com/kurator-org/kurator-akka) repository hosts source code and examples for the core of the **Kurator-Akka** scientific workflow framework.  This software toolkit is being developed as part of the [Kurator project](http://wiki.datakurator.net/web/Kurator) and is designed to make it easy to develop and run high-performance data cleaning workflows.

**Kurator-Akka** is based on the [Akka actor toolkit and runtime](http://akka.io).  It aims to accelerate development of new data cleaning actors and to facilitate development of new workflows assembled from these actors.  **Kurator-Akka** supports actors implemented either in Java or Python, and the framework shields actor developers from the complexities of using the Akka API directly.  Workflows are specified using a YAML-based language that defines how data flows between the actors at run time.

The remainder of this README provides simple examples of actors and workflows implemented using Kurator-Akka, describes how to run example workflows at the command line, and outlines the setup required to develop with or extend the **Kurator-Akka** source code.  Detailed user documentation will be provided elsewhere.

The TDWG 2015 presentation [Data cleaning with the Kurator toolkit](http://www.slideshare.net/TimothyMcPhillips/data-cleaning-with-the-kurator-toolkit-bridging-the-gap-between-conventional-scripting-and-highperformance-workflow-automation) provides an overview of the Kurator project and tools described in this README.  

Example actor and workflow
--------------------------

A *workflow* is a collection of actors configured to carry out some set of tasks.  An *actor* is a software component that receives data either from outside the workflow, or from other actors within the same workflow. Actors in **Kurator-Akka** may be implemented either in Java or in Python. Actors in a **Kurator-Akka** workflow run concurrently in different threads.  

##### Java implementation of a Multiplier actor

The Java class below defines a simple actor type for multiplying an integer by a configurable constant:

    import org.kurator.akka.AkkaActor;
    public class Multiplier extends AkkaActor {
        public double factor = 1;
        
        @Override public void onData(Object i) {
        	broadcast((double)i * factor);
        }
    }

As shown above, a new Java actor type can be implemented by declaring a new Java class that overrides the `onData()` method of the `org.kurator.akka.AkkaActor` class.  This method will be called by the **Kurator-Akka** framework each an actor of this type receives any data.  The `broadcast()` method is used within the `onData()` method to send data (usually the results of performing some computation on the data received by the actor) to any other actors in the workflow configured to listen to this one.

An alternative approach to defining Java actors is to implement the actor as a Plain Old Java Object (POJO) and declare to the Kurator-Akka framework which public method is to be called for each received data object (by default the method named `onData` will be used).  This method must return the value to be broadcast to actors listing to this one.  A POJO implementation of the above Multiplier actor is as follows:

    public class Multiplier {
        public double factor = 1;

        public void multiply(Object i) {
        	return((double)i * factor);
        }
    }

No class inheritance is required when using using the POJO approach.  However, POJO actors have the limitation that only one value may be returned and communicated to other actors for each data item the actor receives.  In contrast, Java actors derived from `org.kurator.akka.AkkaActor` may call the `broadcast()` method multiple times within the `onData()` method and so produce multiple outputs per input data item received.

##### Python implementations of the Multiplier actor

An implementation of the same actor type in Python is analogous to the POJO approach above, but does not require a class to be defined:

    factor = 1
    def multiply(i):
        return factor * i

As for POJOs, the **Kurator-Akka** framework can be configured to call the `multiply()` method on each data item received by this actor.  The value returned from the function is automatically broadcast to listeners.  Unlike POJO actors, actors implemented as Python functions may produce a sequence of output data items for each input data item received by using the `yield` keyword instead of `return`.

Python actors also may be implemented using methods in a Python class.

##### YAML declaration of a Python implementation of the Multiplier actor

In addition to the Java or Python definition of an actor, an *actor type* declaration authored in YAML is needed to make the actor available for use in workflows.  The following declares that actors of type `Multiplier`, a subtype of actor type `PythonActor`, invoke the `multiply()` function defined in the file `multiplier.py`:

    types:

    - id: Multiplier
      type: PythonActor
      properties:
        script: multiplier.py
        onData: multiply

Analogous declaration must be provided for Java actors.


##### Defining a workflow that uses the Multiplier actor

With the above YAML saved to a file named `actors.yaml`, the Python-based`Multiplier` actor can be used in a workflow also defined in YAML. The workflow below accepts an input value from the user, multiplies it by 3, and outputs the result:

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

The above declaration states the following: `MultiplyByThreeWorkflow` is a workflow comprising three actors, `ReadOneNumber`, `MultiplyByThree`, and `PrintProduct`. `MultiplyByThree` listens to (receives its input from) `ReadOneNumber`, and `PrintProduct` receives its input in turn from `MultiplyByThree`.  `MultiplyByThree` is declared to be an instance of the `Multiplier` actor type defined previously; this instance of `Multiplier` is configured to multiply each value it receives by a factor of 3.  The YAML declarations for the underlying actor types `NumReader`, `Multiplier`, and `NumPrinter` are all imported from `actors.yaml`.

The YAML definition of a workflow using Java implementations of each actor looks identical to a workflow using Python actors.  Java actors and Python actors can be used together in the same workflow.

##### Inlining Python actors

**Kurator-Akka** allows the code for Python actors to be provided *inline*, i.e. within the workflow definition itself. No additional Python script file or YAML actor type declaration is needed in this case (the type of the actor is simply `PythonActor`).  For example, the block of YAML defining the `MultiplyByThree` actor in the workflow definition above depends on an additional YAML declaration for the `Multiplier` actor defined in the `actors.yaml` file, which in turn depends on a Python script file named `multiplier.py`.  Because the code for this actor is only a few lines long, it may be reasonable to define the actor entirely inline.  In other words, this block of YAML in the workflow:

    - id: MultiplyByThree
      type: Multiplier
      properties:
        listensTo:
          - !ref ReadOneNumber
        parameters:
          factor: 3

can be replaced with:

    - id: MultiplyByThree
      type: PythonActor
      properties:
        listensTo:
          - !ref ReadOneNumber
        onData: triple
        code: |
          def triple(n):
            return 3 * n

The Python code defining the `multiply()` function is now defined within the same YAML file that declares the workflow as a whole. Inlined Python actors are useful for implementing simple actors needed for specific workflows.


Setting up Kurator-Akka
-----------------------

The following describes how to set up an environment for writing your own actors and workflows, and executing them using **Kurator-Akka**.

#### Check installed version of Java
Kurator-Akka requires Java version 1.7 or higher. To determine the version of java installed on your computer use the `-version` option to the `java` command. For example,

    $ java -version
    java version "1.7.0_71"
    Java(TM) SE Runtime Environment (build 1.7.0_71-b14)
    Java HotSpot(TM) 64-Bit Server VM (build 24.71-b01, mixed mode)
    $

Instructions for installing Java may be found at [http://docs.oracle.com/javase/7/docs/webnotes/install/](http://docs.oracle.com/javase/7/docs/webnotes/install/).  If you plan to develop new actors in Java (not just in Python) be sure to install the JDK.  Otherwise a JRE is sufficient.

#### Download the Kurator-Akka jar

Kurator-Akka is distributed as a jar (Java archive) file that can be executed using the `java -jar` command. To download the most recent build of the latest **Kurator-Akka** code, navigate to the [Latest Successful Build](https://opensource.ncsa.illinois.edu/bamboo/browse/KURATOR-AKKA/latestSuccessful), click on the *Artifacts* tab, and download the *executable jar* artifact for the *kurator-akka* job.  The downloaded file will be named `kurator-akka-0.3-SNAPSHOT-jar-with-dependencies.jar`.

Note that while released distributions of **Kurator-Akka** are available for download from the [Kurator Software Releases](https://opensource.ncsa.illinois.edu/confluence/display/KURATOR/Software+Releases) page, the remainder of this README pertains to the latest development version available in the GitHub repository.

#### Install Kurator-Akka on your system

Once you have downloaded the **Kurator-Akka** jar, save the file in a convenient location and rename it to something like `kurator-akka.jar`.   **Kurator-Akka** can now be run using the `java -jar` command. The jar file includes several sample workflow scripts which can be accessed from the command line.

Test your installation by running the `hello.yaml` workflow. Assuming `kurator-akka.jar` is in your current working directory type:

    java -jar kurator-akka.jar -f classpath:/org/kurator/akka/samples/hello.yaml

If the Kurator-Akka jar is stored elsewhere, qualify `kurator-akka.jar` with the path to that file. For example, if you stored the jar file in the bin subdirectory of your home directory, running Kurator-Akka would look something like this on Linux or OS X:

    $ java -jar ~/bin/kurator-akka.jar -f classpath:/org/kurator/akka/samples/hello.yaml
    Hello World!
    $

On Windows platforms the command is similar:

    $ java -jar %USERPROFILE%\bin\kurator-akka.jar -f classpath:/org/kurator/akka/samples/hello.yaml
    Hello World!
    $

#### Define a short command for running Kurator-Akka at the prompt

If you are running **Kurator-Akka** on an Apple OSX or Linux system (or use Git Bash or Cygwin on Windows), you may define an *alias* to simplify running YesWorkflow at the command line. On Windows platforms you similarly may define a *doskey macro* for running **Kurator-Akka** at the prompt.

For example, if you have saved `kurator-akka.jar` to the bin subdirectory of your home directory, the following command will create a `bash` alias for running **Kurator-Akka** simply by typing `ka`:

    alias ka='java -jar ~/bin/kurator-akka.jar'

If you use `csh` or `tcsh` the command is:

    alias kurator-akka java -jar ~/bin/kurator-akka.jar

On Windows the command to create the `ka` doskey macro is:

     doskey ka=java -jar %USERPROFILE%\bin\kurator-akka.jar $*

On all platforms the `hello.yaml` demo now can be run using the short command:

    $ ka -f classpath:/org/kurator/akka/samples/hello.yaml
    Hello World!
    $


#### Extract the samples (optional)

If you would like to browse and edit the sample workflows included in the **Kurator-Akka** jar, type the following (qualifying the path to `kurator-akka.jar` as necessary) to extract its contents to your filesystem:

    jar xf kurator-akka.jar org/kurator/akka/samples

To run a script residing on the filesystem, you can use the file scheme:

    $ java -jar kurator-akka.jar -f file:org/kurator/akka/samples/hello.yaml
    Hello World!

The `file:` qualifier is optional, however. By default **Kurator-Akka** looks for workflows on your filesystem. So this will work, too:


    $ java -jar kurator-akka.jar -f org/kurator/akka/samples/hello.yaml
    Hello World!

Note that the path to `hello.yaml` above is relative (it does not start with a `/`).

Building Kurator-Akka
------------------

mvn package -DskipTests

TODO: How to get a fresh checkout to build without skipping tests.

TODO: Details

Development for Kurator-Akka
----------------------------

TODO: Documentation of development for framework, and for included Java actors.

TODO: Including Java libraries of actors

To make a Java class file available as a core Kurator-Akka actor that can be invoked 
from Kurator-Akka using a yaml workflow declaration, you will need to:  

(1) Create a java class (which either contains the desired functionality or wraps it), 
which extends AkkaActor.

(2) Add a type definition block to src/main/resources/org/kurator/akka/types.yaml 
giving an ID for the actor, and the classpath for the actor.

(3) Invoke the actor by ID in a workflow defined in a yaml file.

The Kurator-Akka class AkkaActor is an implementation of Akkal's UntypedActor.  Sybtypes
of AkkaActor implement specific Kurator workflow functionality throguh implementations 
of an onData(Object value) method.  Akka triggers actions by UntypedActors through the 
onRecieve(Object message) method, AkkaActor implements onRecieve() for its subclasses, 
allowing the Kurator-Akka framework to implement workflows as a series of actors connected
by inputs and outputs.

Developing new Python actors
----------------------------
You can develop new Python actors and use them within **Kurator-Akka** workflows using the `kurator-akka.jar` file (see the instructions above) and a local installation of Jython 2.7.0.  Jython is a Java implementation of the Python language and runtime.  Although `kurator-akka.jar` includes the core of the Jython system, specific actors may have dependencies on additional, 3rd-party Python packages.  These packages need to be installed locally and made available **Kurator-Akka**.  A local Jython installation is a convenient place to install 3rd-party packages; a standalone installation of Jython also is useful for confirming that these packages work correctly with Jython.

#### Install the Jython 2.7.0 distribution

* Download the [Jython 2.7 installer jar](http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7.0/jython-installer-2.7.0.jar) from the Jython.org [downloads page](http://www.jython.org/downloads.html). The downloaded jar file will be named `jython_installer-2.7.0.jar`.

* Follow the [Jython 2.7.0 Basic Install instructions](https://wiki.python.org/jython/InstallationInstructions#id2).  The installer can be started either by double-clicking the downloaded jar file (on Windows or OS X) or executing the downloaded jar at the command prompt using the `java -jar` command:



        java -jar jython_installer-2.7.0.jar

* Note the location of the Jython installation directory created by the installer.

#### Make the Jython package library available to Kurator

Define the environment variable `KURATOR_LOCAL_PYTHON_LIB` to provide the path to the `Lib` subdirectory of the newly installed Jython 2.7.0 distribution. **Kurator-Akka** uses this directory to locate 3rd-party Python packages that specific actors depend upon.

In a bash shell the environment variable can be assigned with the following command if Jython was installed to the `jython2.7.0` directory within your home directory:

    export KURATOR_LOCAL_PYTHON_LIB=$HOME/jython2.7.0/Lib/

On Windows it is easiest to define the variable using the Advanced system settings Environment Variables dialog:

    Control Panel -> System -> Advanced system settings -> Advanced -> Environment Variables

**Kurator-Akka** now will have access to all Python packages installed to your Jython installation.  Jython 2.7.0 includes the `pip` tool (in the `bin` subdirectory of the Jython installation) which makes it easy to install 3rd-party Python packages and to install their dependencies automatically.




