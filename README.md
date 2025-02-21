# ReplayMOP
Deterministic replay of concurrent Java programs using monitoring oriented programming. 

The ultimate goal of this project is to come up with a specification language that can describe the runtime behavior of a given program at the non-deterministic points of its execution, and efficiently enforcing the specified behavior at runtime without any modification to java virtual machine. 

This way, a developer can specify exactly how a program should behave during the runtime without any modification to the source code. An automatic tool can also observe execution of a program and generate such an specification.


## Build:
1. Install [Maven](http://maven.apache.org/). 
2. Install [RV-Predict](https://runtimeverification.com/predict/).
3. Run `mvn install:install-file -Dfile=path/to/rv-predict/lib/rv-predict.jar -DgroupId=com.runtimeverification.rvpredict -DartifactId=root -Dversion=1.3-SNAPSHOT -Dpackaging=jar`
4. Go to the project's root and run `mvn package`

### If you want to build your own `aspectjweaver.jar`
Use this version of aspectj: https://github.com/eclipse-aspectj/aspectj/releases/tag/V1_8_9

To build the jars
```bash
ant jar
# located at aj-build/dist/tools/lib/aspectjweaver.jar
```

## Use:
ReplayMOP get as input a replay specification (.rs) and outputs a java intrumentation agent (.jar). You can then run your program with the agent and get the specified behavior. For example if you normally run your program this way: 
```bash
java MyClass
```
You only need to change it to this:
```bash
java -javaagent:/path/to/generated/agent.jar -cp /path/to/aspectjweaver.jar:. MyClass
```

But first you have to generate `agent.jar`.

If you want to write an specification manually, refer to the examples (e.g [this](examples/basic/Example1/Example1.rs)) to get familiar with the language. In order to generate the agent from a specication run:
```bash
# generate aspect.aj
$ java -jar target/replaymop-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./spec.rs
# generate agent.jar from aspect.aj
$ java -cp target/replaymop-0.0.1-SNAPSHOT-jar-with-dependencies.jar replaymop.utils.AJAgentGenerator ./GeneratedAspect.aj
```

If everything goes well, it generates the agent (`agent.jar`)

