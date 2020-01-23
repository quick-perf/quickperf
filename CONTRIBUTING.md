# Contributing to QuickPerf 
Thank you for your interest in contributing to QuickPerf! <br>

## 🚩 Table of contents
[Code of conduct](#Code-of-conduct) <br><br>
[Various types of contribution](#Various-types-of-contribution) <br><br>
[Submission guidelines](#Submission-guidelines) <br><br>
[Build QuickPerf](#Build-QuickPerf) <br><br>
[Test several JDK with Docker](#Test-several-JDK-with-Docker) <br><br>
[Technical resources](#Technical-resources) <br><br>
[Paris Hackergarten](#Paris-Hackergarten)<br><br>
[Legal stuff](#Legal-stuff)

## Code of conduct
Help us keep QuickPerf open and inclusive. We have adopted the Contributor Covenant Code of Conduct that we expect project participants to adhere to. Please read [our code of conduct](./CODE_OF_CONDUCT.md).

##  Various types of contribution
You can contribute in many ways to QuickPerf:
* [Report a bug](https://github.com/quick-perf/quickperf/issues/new?assignees=&labels=bug&template=bug_report.md&title=)
* [Request for new feature](https://github.com/quick-perf/quickperf/issues/new?assignees=&labels=enhancement&template=feature_request.md&title=)
* Work on an [issue](https://github.com/quick-perf/quickperf/issues)<br>
 You can start with [good first issues](https://github.com/quick-perf/quickperf/labels/%3Athumbsup%3A%20good%20first%20issue)
* Improve documentation
  * Submit a [documentation issue](https://github.com/quick-perf/quickperf/issues/new?assignees=&labels=%3Aledger%3A+documentation&template=documentation-improvement.md&title=)
  * Or submit a PR <br>
  To clone the doc repository: ```git clone https://github.com/quick-perf/doc.wiki.git```
* ...

## Submission guidelines
When you start to work on an issue, please leave a comment "*I start to work on this issue*", so we can assign it to you. Do not hesitate to ask for information about the issue.

Please rebase your PR on master (no merge). We prefer integrating PR by squashing all the commits and rebase it to master, if your PR has diverged and needs to integrate with master, please rebase on master but do not merge as it will prevent rebasing later on.

Need some help with Git or Github? Don't worry, you can watch [these videos](https://egghead.io/courses/how-to-contribute-to-an-open-source-project-on-github).

## Build QuickPerf

### JDK requirements
You need a JDK (OpenJDK, ...) >= 11 or an Oracle JDK >= 1.8.

### Steps to build QuickPerf
1) Clone the repository
 
   Execute ```git clone https://github.com/quick-perf/quickperf.git```.

2) Navigate to the *quickperf* repository

3) Build QuickPerf with Maven

    1. Use Maven installed locally
    
       You need Maven 3. 
       
       Execute ```mvn clean install```.
   
   2. Use a Maven wrapper
   
      The QuickPerf repository includes *Maven Wrapper* scripts (./mvnw or mvnw.bat) to build QuickPerf without having to install Maven locally.

      On Mac or Linux run ```./mvnw clean install```.

      On Windows run ```mvnw.cmd clean install```.
        
### Tips
  💡 To disable Spring Boot tests: ```mvn clean install -P -SpringBootTests```
  
  💡 To not build Spring modules: ```mvn clean install -P -default-spring,-SpringBootTests```

## Test several JDK with Docker

To quickly test multiple linux openjdk locally, you can execute the following command line:

```bash
$> docker container run --rm --name mvn-quickperf-cleaninstall -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/quickperf -w /usr/src/quickperf maven:3-jdk-11 mvn clean install
$> docker container run --rm --name mvn-quickperf-cleaninstall -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/quickperf -w /usr/src/quickperf maven:3-jdk-12 mvn clean install
```

You can look at [the versions available for the Maven image](https://hub.docker.com/_/maven) in the dockerhub.

The following command line allows to test a list of openjdk on unix systems :

```bash
$> for i in {11..12};  do echo "******** Testing with maven:3-jdk-$i **********"; docker container run --rm --name mvn-quickperf-cleaninstall -v $HOME/.m2:/root/.m2 -v $(pwd):/usr/src/quickperf -w /usr/src/quickperf maven:3-jdk-$i mvn clean install; if [[ $? != 0 ]]; then break; fi; done
```

It will fail as soon as one mvn clean install fails.

_Notice: Maven images are packaged only with openjdk not oraclejdk._

## Technical resources
* [Git and Github](https://egghead.io/courses/how-to-contribute-to-an-open-source-project-on-github)
* [Create an annotation](https://github.com/quick-perf/doc/wiki/Create-an-annotation)
* Debug annotations with [@DebugQuickPerf](https://github.com/quick-perf/doc/wiki/Core-annotations#DebugQuickPerf)

## Paris Hackergarten
You live in Paris? Jean Bisutti, a QuickPerf contributor, regularly attends [Paris Hackergarten](https://www.meetup.com/fr-FR/Paris-Hackergarten/). He can help you to contribute to QuickPerf during this event.

## Legal stuff
Project License: [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- You will only Submit Contributions where You have authored 100% of the content.
- You will only Submit Contributions to which You have the necessary rights. This means that if You are employed You have received the necessary permissions from Your employer to make the Contributions.
- Whatever content You Contribute will be provided under the Project License(s).