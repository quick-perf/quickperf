# Contributing to QuickPerf
Thank you for your interest in contributing to QuickPerf! <br>
Feel free to open an issue to suggest a new feature or report a bug. Also don't hesitate to work on an existing issue. When you start to work on an issue, please leave a comment "I start to work on this issue", so we can assign it to you.

## Build
You need the following setup:
* Maven 3
* JDK (OpenJDK, ...) >= 11 or Oracle JDK >= 1.8

To build:
* Clone the repository: git clone https://github.com/quick-perf/quickperf.git
* Navigate to the *quickperf* repository
* mvn clean install <br>
  To disable Spring Boot tests: mvn clean install -P -SpringBootTests

## Tips

To quickly test multiple linux openjdk locally, you can run thanks to docker the following commandline:

```bash
$> docker container run --rm --name mvn-quickperf-cleaninstall -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/quickperf -w /usr/src/quickperf maven:3-jdk-11 mvn clean install
$> docker container run --rm --name mvn-quickperf-cleaninstall -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/quickperf -w /usr/src/quickperf maven:3-jdk-12 mvn clean install
```

See to dockerhub [the versions available for maven image](https://hub.docker.com/_/maven).

And the following commandline enable us to test a list of openjdk on unix systems :

```bash
$> for i in {11..12};  do echo "******** Testing with maven:3-jdk-$i **********"; docker container run --rm --name mvn-quickperf-cleaninstall -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/quickperf -w /usr/src/quickperf maven:3-jdk-$i mvn clean install; if [[ $? != 0 ]]; then break; fi; done
```

It will fail as soon as one mvn clean install fails.

_notice: maven image are packaged only with openjdk not oraclejdk (required for jdk 1.8)._

## Legal stuff
Project License: [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- You will only Submit Contributions where You have authored 100% of the content.
- You will only Submit Contributions to which You have the necessary rights. This means that if You are employed You have received the necessary permissions from Your employer to make the Contributions.
- Whatever content You Contribute will be provided under the Project License(s).
