Kurator-Akka Framework
======================

The [kurator-akka](https://github.com/kurator-org/kurator-akka) repository hosts source code for the **Kurator-Akka** workflow engine component of the Kurator workflow automation toolkit. This software toolkit is being developed as part of the [Kurator project](http://wiki.datakurator.net/wiki/Kurator) and is designed to make it easy to develop and run high-performance data cleaning workflows.

**Kurator-Akka** is based on the [Akka actor toolkit and runtime](http://akka.io).  It aims to accelerate development of new data cleaning actors and to facilitate development of new workflows assembled from these actors.  **Kurator-Akka** supports actors implemented either in Java or Python, and the framework shields actor developers from the complexities of using the Akka API directly.  Workflows are specified using a YAML-based language that defines how data flows between the actors at run time.

The remainder of this README provides simple examples of actors and workflows that employ Kurator-Akka, describes how to run example workflows at the command line, and outlines the setup required to develop with or extend the **Kurator-Akka** source code.  Detailed user documentation will be provided elsewhere.

The TDWG 2015 presentation [Data cleaning with the Kurator toolkit](http://www.slideshare.net/TimothyMcPhillips/data-cleaning-with-the-kurator-toolkit-bridging-the-gap-between-conventional-scripting-and-highperformance-workflow-automation) provides an overview of the Kurator project and tools described in this README.  Also see the list of publications on the [Kurator project wiki](https://wiki.datakurator.net/wiki/Kurator#Publications).

Citing Kurator-Akka
-------------------

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1068311.svg)](https://doi.org/10.5281/zenodo.1068311)

Obtaining Kurator-Akka
----------------------

An executable jar is available in the [current release](https://github.com/kurator-org/kurator-akka/releases/tag/v1.0.1)

Also see the [Kurator-Web](http://wiki.datakurator.org/wiki/Kurator-Web_User_Documentation) application for running Kurator-Akka workflows through a web interface.

Kurator-Akka is available as a dependency in maven central:

    <dependency>
       <groupId>org.datakurator</groupId>
       <artifactId>kurator-akka</artifactId>
       <version>1.0.1</version>
    </dependency>

Example actor and workflow
--------------------------

A *workflow* is a collection of actors configured to carry out some set of tasks.  An *actor* is a software component that receives data either from outside the workflow, or from other actors within the same workflow; and either communicates the results of its computations to other actors, or saves the results outside the running workflow.  Actors in a **Kurator-Akka** workflow run concurrently in different threads.

**Kurator-Akka** simplifies use of the underlying Akka actor framework by providing the `KuratorActor` class as an alternative to Akka's `UntypedActor` class.  A sub-class of `UntypedActor`, `KuratorActor` makes scientific-workflow specific capabilities available to actor and workflow authors, most critically through the `onData()` event handling method.  When Akka triggers events handled by  the `UntypedActor.onRecieve()` method, the `KuratorActor` implementation of `onRecieve()` makes calls to the `onData()` methods defined by Kurator-Akka actors.  **Kurator-Akka** further provides automated means for orchestrating actors in ways compatible with computational pipelines typically used to automate scientific workflows.  For example, **Kurator-Akka** correctly times actor initialization and actor shutdown events so that workflow authors need not concern themselves with these otherwise complex--yet critical--workflow management details.

Actors in **Kurator-Akka** may be implemented either in Java or in Python.


##### Java implementation of a Multiplier actor

The Java class below defines a simple actor for multiplying an integer by a configurable constant:

    import org.kurator.akka.KuratorActor;

    public class Multiplier extends KuratorActor {
        public double factor = 1;
        @Override public void onData(Object i) {
        	broadcast((double)i * factor);
        }
    }

As shown above, a new Java actor can be implemented by declaring a new Java class that overrides the `onData()` method of the `org.kurator.akka.KuratorActor` base class.  This method will be called by the **Kurator-Akka** framework each time the actor receives any data.  The call to `broadcast()` within the `onData()` method sends data (usually the results of performing some computation on the data received by the actor) to any other actors in the workflow configured to listen to this one.

An alternative approach to defining a **Kurator-Akka** Java actor is to implement the actor as a Plain Old Java Object (POJO) and declare to the Kurator-Akka framework which public method is to be called for each received data object. By default the method named `onData()` will be used. This method must be a function that returns the value to be broadcast to other actors.  A POJO implementation of the above Multiplier actor is as follows:

    public class Multiplier {
        public double factor = 1;
        public void multiply(Object i) {
        	return((double)i * factor);
        }
    }

No class inheritance is required when using using the POJO approach.  However, POJO actors have the limitation that only one output data item may be returned and communicated to other actors for each input data item the actor receives.  In contrast, Java actors derived from `org.kurator.akka.KuratorActor` may call the `broadcast()` method multiple times within the `onData()` method and so produce multiple outputs per input.

Both approaches for Java actors support event handlers in addition to `onData()`. These include `onInitialize()`, called before any actor produces output; `onStart()`, which allows actors to produce output data before any input data is received; and `onEnd()`, which is called after an actor has handled the last data item it will receive during a workflow run.

##### Python implementations of the Multiplier actor

An implementation of the same actor in Python is analogous to the POJO approach above, with the difference that the input data handler can, but does not need to be, defined as a method in a class. Any name may be used for the input data handing function; `on_data()` is assumed by default.

Thus, the following Python snippet can serve as a valid actor implementaion:

    factor = 1
    def multiply(i):
        return factor * i

And methods defined in Python classes work as well:

    class MultiplierActor(object):
        def __init__(self):
            self.factor = 1
        def multiply(i):
            return self.factor * i


As for POJOs, the **Kurator-Akka** framework can be configured to call the `multiply()` function on each data item received by either of the above implementations of the actor.  The value returned from the function is automatically broadcast to listeners.  Unlike POJO actors, actors implemented as Python functions alternatively may produce a sequence of output data items for each input data item received by using the `yield` keyword instead of `return`.

Similar to Java actors, Python actors may provide `on_initialize()`, `on_start()`, and `on_end()` event handlers (and may name these functions arbitrarily).

##### YAML declaration of a Python implementation of the Multiplier actor

In addition to the Java or Python definition of an actor, an *actor type declaration* authored in YAML is needed to make the actor available for use in workflows.  The following declares that actors of type `Multiplier`, a subtype of `PythonActor`, invoke the `multiply()` function defined in the file `multiplier.py` to handle incoming data:

    types:
    - id: Multiplier
      type: PythonActor
      properties:
        script: multiplier.py
        onData: multiply

The argument to the `script:` property is an absolute path to the Python file defining the function, or a path relative to the directory in which the workflow is invoked.  A  less fragile way of defining a Python actor is to make the Python script part of a Python package, and use the `module` property to refer to the script.  For example, if `multiplier.py` is in the `math.operators` package, then the following declaration will work:

    types:
    - id: Multiplier
      type: PythonActor
      properties:
        module: math.operators.multiplier
        onData: multiply

A declaration corresponding to a Python class implementation of the actor defined in a `math.actors` package would be as follows:

    types:
    - id: Multiplier
      type: PythonClassActor
      properties:
        pythonClass: math.actors.MultiplierActor
        onData: multiply


Analogous declarations must be provided for Java actors as well.


##### Defining a workflow that uses the Multiplier actor

With either of the above YAML snippets saved to a file named `actors.yaml`, the Python-based `Multiplier` actor can be used in a workflow also defined in YAML. The workflow below accepts an input value from the user, multiplies it by 3, and outputs the result:

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


Preparing to run Kurator-Akka
-----------------------------

This section describes how to set up an environment for writing your own actors and workflows, and executing them using **Kurator-Akka**. Instructions for building and extending the **Kurator-Akka** framework itself are provided following this section.

#### Check installed version of Java
**Kurator-Akka** requires Java version 1.8.0 or higher. To determine the version of java installed on your computer use the `-version` option to the `java` command. For example,

    $ java -version
    java version "1.8.0_65"
    Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
    Java HotSpot(TM) 64-Bit Server VM (build 25.65-b01, mixed mode)


Instructions for installing Java may be found at [http://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html](http://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html).  If you plan to develop new actors in Java (not just in Python) be sure to install the JDK.  Otherwise a JRE is sufficient.

#### Download the Kurator-Akka jar

**Kurator-Akka** is distributed as an executable jar (Java archive) file that can be invoked using the `java -jar` command. To download the most recent build of the latest **Kurator-Akka** code, navigate to the [Latest Successful Build](https://opensource.ncsa.illinois.edu/bamboo/browse/KURATOR-AKKA/latestSuccessful), click on the *Artifacts* tab, and download the *executable jar* artifact for the *kurator-akka* job.  The downloaded file will be named `kurator-akka-0.3-SNAPSHOT-jar-with-dependencies.jar`.

#### Install Kurator-Akka on your system

Once you have downloaded the **Kurator-Akka** jar, save the file in a convenient location and rename it to something like `kurator-akka.jar`.   **Kurator-Akka** can now be run using the `java -jar` command. The jar file includes several sample workflow scripts which can be accessed from the command line.

Test your installation by running the `hello.yaml` workflow. Assuming `kurator-akka.jar` is in your current working directory type:

    java -jar kurator-akka.jar -f classpath:/org/kurator/akka/samples/hello.yaml

If the Kurator-Akka jar is stored elsewhere, qualify `kurator-akka.jar` with the path to that file. For example, if you stored the jar file in the bin subdirectory of your home directory, running Kurator-Akka would look something like this on Linux or OS X:

    $ java -jar ~/bin/kurator-akka.jar -f classpath:/org/kurator/akka/samples/hello.yaml
    Hello World!

On Windows platforms the command is similar:

    $ java -jar %USERPROFILE%\bin\kurator-akka.jar -f classpath:/org/kurator/akka/samples/hello.yaml
    Hello World!

#### Define a short command for running Kurator-Akka at the prompt

If you are running **Kurator-Akka** on an Apple OSX or Linux system (or use Git Bash or Cygwin on Windows), you may define an *alias* to simplify running **Kurator-Akka** at the command line. On Windows platforms you similarly may define a *doskey macro* for running **Kurator-Akka** at the prompt.

For example, if you have saved `kurator-akka.jar` to the bin subdirectory of your home directory, the following command will create a `bash` alias for running **Kurator-Akka** simply by typing `ka`:

    alias ka='java -jar ~/bin/kurator-akka.jar'

If you use `csh` or `tcsh` the command is:

    alias kurator-akka java -jar ~/bin/kurator-akka.jar

On Windows the command to create the `ka` doskey macro is:

     doskey ka=java -jar %USERPROFILE%\bin\kurator-akka.jar $*

On all platforms the `hello.yaml` demo now can be run using the short command:

    $ ka -f classpath:/org/kurator/akka/samples/hello.yaml
    Hello World!

#### Extract and run sample workflows (optional)

If you would like to browse and edit the sample workflows included in the **Kurator-Akka** jar, type the following (qualifying the path to `kurator-akka.jar` as necessary) to extract its contents to your filesystem:

    jar xf kurator-akka.jar org/kurator/akka/samples

To run a script residing on the filesystem, you can use the `file:` qualifier instead of `classpath:`:

    $ java -jar kurator-akka.jar -f file:org/kurator/akka/samples/hello.yaml
    Hello World!

The `file:` qualifier is optional, however. By default **Kurator-Akka** looks for workflows on your filesystem. So this will work, too:


    $ java -jar kurator-akka.jar -f org/kurator/akka/samples/hello.yaml
    Hello World!

Note that the path to `hello.yaml` above is relative (it does not start with a `/`).


Developing new Java actors
-------------------------------
New Java actors can be implemented using standard Java development tools.  Simply make sure that the `kurator-akka.jar` file is on the CLASSPATH when compiling your actors, and that the compiled `.class` files are on the CLASSPATH when running **Kurator-Akka**.

For example, if your new actor is defined in the file `MyActor.java`, and `kurator-akka.jar` is stored in `~/bin`, the following command will compile your actor to produce `MyActor.class`:

    javac -classpath ~/bin/kurator-akka.jar MyActor.java

Note that including `kurator-akka.jar` on the classpath during compilation is unnecessary if your actor is a POJO, i.e. not a subclass of `org.kurator.akka.KuratorActor`.

Developing new Python actors
--------------------------------------
New Python actors can be used in **Kurator-Akka** workflows without any manual compilation step. Python actors that depend only on packages provided as part of a standard Python distribution can be developed without any preparation other than downloading the **Kurator-Akka** jar file.  The `kurator-akka.jar` also includes within it all of the 3rd-party Python packages requierd by the basic actors provided in the jar.


However, if you develop new Python actors that depend on 3rd-party packages not included in the  **Kurator-Akka** jar file, you will need to install these packages to a local **Jython** installation.  Jython is a Java implementation of the Python language and runtime, and comes with the same package management tools as Python, including `pip`.

#### Install the Jython 2.7.1b3 distribution to support 3rd party Python packages

* Download the [Jython 2.7.1.b3 installer jar](http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7.1b3/jython-installer-2.7.1b3.jar). The downloaded jar file will be named `jython_installer-2.7.1b3.jar`.

* The Jython installer can be started either by double-clicking the downloaded jar file (on Windows or OS X) or executing the downloaded jar at the command prompt using the `java -jar` command:

        java -jar jython_installer-2.7.1b3.jar

* Note the location of the Jython installation directory created by the installer.

#### Install dependencies for native actor support

As an alternative to the actors that used the Java based Python interpreter Jython, native actor support is provided for embedded Python and R.

On Linux, install the native dependencies for R and Python via:

    sudo apt-get install r-base python python-dev
    
On MacOS, install the native dependencies via:

    brew install python r

#### Make installed Python packages available to Kurator

Define the environment variable `JYTHONHOME` to indicate the path to the newly installed Jython 2.7.0 distribution. **Kurator-Akka** uses this variable to locate 3rd-party Python packages that specific actors depend upon.

In a bash shell the environment variable can be assigned with the following command (assuming, for example, that Jython was installed to the `jython2.7.1b3` directory within your home directory):

    export JYTHONHOME=$HOME/jython2.7.1b3/

On Windows it is easiest to define the variable using the Advanced system settings Environment Variables dialog:

    Control Panel -> System -> Advanced system settings -> Advanced -> Environment Variables

**Kurator-Akka** now will have access to all Python packages installed to your Jython installation.  Jython 2.7 includes the `pip` tool (in the `bin` subdirectory of the Jython installation) which makes it easy to install 3rd-party Python packages and to install their dependencies automatically.  For example, this following command installs the `suds-jurko` package which subsequently can be imported by Python actors:

    $JYTHON_HOME/bin/pip install suds-jurko

#### Make your own Python code available to **Kurator-Akka**

To make your own Python code available to **Kurator-Akka** specify the directory (or directories) containing your Python packages using the `JYTHONPATH` variable.  For example, if you have defined a Python function `add_two_numbers()` in a file named `adders.py`, and placed this Python module (file) in the `$HOME/packages/math/operators` directory, you can make your function available for use in an actor via the `math.operators.adders` module by defining `JYTHONPATH` to include the `$HOME/packages/` directory and then declaring the `module` and `onData` properties of the actor as follows:  

    types:
    - id: Adder
      type: PythonClassActor
      properties:
        module: math.operators.adders
        onData: add_two_numbers

Note that Python packages always require that a file named `__init__.py` be present in each directory comprising the package.  In the above example, the directories `$HOME/packages/math` and `$HOME/packages/math/operators` must each contain a file named `__init__.py`. These files may be empty.

For native actor support set the `PYTHONPATH` environment variable to the same value.

Instructions for Kurator-Akka workflow engine developers
---------------------------
If you would like to build, modify, or extend the **Kurator-Akka** workflow engine you may set up your development environment as follows.

#### JDK and Maven configuration

The **Kurator-Akka** framework is built using Maven 3. Before building **Kurator-Akka** confirm that the `mvn` command is in your path, that your version of Maven is at least 3.0.5, and that a JDK version 1.8.0 (or higher) is found by Maven:

    $ mvn --version
    Apache Maven 3.3.3 (7994120775791599e205a5524ec3e0dfe41d4a06; 2015-04-22T04:57:37-07:00)
    Maven home: /Users/tmcphill/DropBox/Library/Java/apache-maven-3.3.3
    Java version: 1.8.0_65, vendor: Oracle Corporation
    Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/jre
    Default locale: en_US, platform encoding: UTF-8
    OS name: "mac os x", version: "10.11.3", arch: "x86_64", family: "mac"
    $

JDK 7 and Maven 3 downloads and detailed installation instructions can be found at the following links:

- [Instructions for installing and configuring JDK 1.7](http://docs.oracle.com/javase/7/docs/webnotes/install/) (Oracle Java Documentation)
- [Instructions for installing and configuring Maven 3](http://maven.apache.org/download.cgi) (Apache Maven Project)


#### Project directory layout

**Kurator-Akka** adopts the default organization of source code, resources, and tests as defined by Maven.  See [maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html](http://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) for more information.  The most important directories are listed below:

Directory            | Description
---------------------|-----------
src/main/java        | Source code to be built and packaged for distribution.
src/main/resources   | Resource files to be packaged with production code.
src/test/java        | Source code for unit tests. Not included in packaged distributions.
src/test/resources   | Resource files available to tests. Not included in packaged distributions.
target               | Destination directory for packaged distributions (jar files) built by maven.
target/classes       | Compiled java classes for source code found under src/main/java.
target/test-classes  | Compiled java classes for test code found under src/test/java.
target/dependency    | Automatically resolved and downloaded dependencies (jars) that will be included in the standalone distribution.
target/apidocs/      | Local build of Javadoc documentation.
packages             | Custom Python packages to be included in the distribution.

Note that the final directory, `packages` contains all of the Python source files developed and delivered with the **Kurator-Akka** engine.  Any new packages added to this directory are automatically made available to **Kurator-Akka**.


#### Building and testing with maven

**Kurator-Akka** can be built and tested from the command line using the following commands:

Maven command | Description
--------------|------------
mvn clean     | Delete the target directory including all compiled classes.
mvn compile   | Download required dependencies and compile source code in src/main/java.  Only those source files changes since the last compilation or clean are built.
mvn test      | Compile the classes in src/test/java and run all tests found therein. Peforms *mvn compile* first.
mvn package   | Package the compiled classes in target/classes and files found in src/main/resources in two jar files, **kurator-akka-0.3-SNAPSHOT.jar** and **kurator-akka-0.3-SNAPSHOT-jar-with-dependencies.jar**.  The latter also contains all jar dependencies. Performs *mvn compile* and *mvn test* first, and will not perform packaging step if any tests fail. Use the `-DskipTests` option to bypass tests.
mvn javadoc:javadoc | Build Javadoc documentation. The `mvn package` command also builds Javadoc.

#### Continuous integration with Bamboo

All code is built and tests run automatically on a build server at NCSA whenever changes are committed to directories used by maven.  Please confirm that the automated build and tests succeed after committing changes to code or resource files (it may take up to two minutes for a commit-triggered build to start).  Functional tests depend on the scripts in src/main/resources and are likely to fail if not updated following changes to these scripts.

Site                  | Url
----------------------| ---
Build history         | https://opensource.ncsa.illinois.edu/bamboo/browse/KURATOR-AKKA
Last build            | https://opensource.ncsa.illinois.edu/bamboo/browse/KURATOR-AKKA/latest
Last successful build | https://opensource.ncsa.illinois.edu/bamboo/browse/KURATOR-AKKA/latestSuccessful

The link to the latest successful build is useful for obtaining the most recently built jar file without building it yourself.  Follow the link to the [last successful build](https://opensource.ncsa.illinois.edu/bamboo/browse/KURATOR-AKKA "last successful build"), click the Artifacts tab, then download the executable jar.

<!---

Development for Kurator-Akka
----------------------------

TODO: Documentation of development for framework, and for included Java actors.

TODO: Including Java libraries of actors

To make a Java class file available as a core Kurator-Akka actor that can be invoked
from Kurator-Akka using a yaml workflow declaration, you will need to:  

(1) Create a java class (which either contains the desired functionality or wraps it),
which extends KuratorActor.

(2) Add a type definition block to src/main/resources/org/kurator/akka/types.yaml
giving an ID for the actor, and the classpath for the actor.

(3) Invoke the actor by ID in a workflow defined in a yaml file.

--->
