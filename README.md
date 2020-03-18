<div align="center">
<img src="https://pbs.twimg.com/profile_banners/926219963333038086/1518645789" alt="QuickPerf"/>
</div>

<div>
<blockquote>
<p><h3>QuickPerf is a testing library for Java providing annotations to quickly evaluate some performance properties.</h3></p>
</blockquote>
</div>

---
<p align="center">
  <a href="https://search.maven.org/search?q=org.quickperf">
    <img src="https://maven-badges.herokuapp.com/maven-central/org.quickperf/quick-perf/badge.svg"
         alt="Maven Central">
  </a>
  <a href="https://github.com/quick-perf/quickperf/blob/master/LICENSE.txt">
    <img src="https://img.shields.io/badge/license-Apache2-blue.svg"
         alt = "License">
  </a>  
  <a href="https://travis-ci.com/quick-perf/quickperf">
    <img src="https://travis-ci.com/quick-perf/quickperf.svg?branch=master"
         alt = "Build Status">
  </a>
    <a href="https://gitter.im/quickperf">
     <img src="https://img.shields.io/gitter/room/quick-perf/quickperf?color=orange"
          alt = "Gitter">
    </a>
    <a href="https://twitter.com/quickperf">
      <img src="https://img.shields.io/twitter/follow/QuickPerf.svg?label=Follow%20%40QuickPerf&style=social"
           alt = "Twitter Follow">
    </a>
</p>

---

## Usage
### JVM annotations

```java
    @MeasureHeapAllocation
    @HeapSize(value = 1, unit = AllocationUnit.GIGA_BYTE)
    @Test
    public void execute_batch() {
        ...
    }
```

📙 [All the JVM annotations](https://github.com/quick-perf/doc/wiki/JVM-annotations)  &nbsp;&nbsp; :mag_right: Examples with [JUnit4](https://github.com/quick-perf/quickperf-examples/blob/master/jvm-junit4/src/test/java/org/quickperf/jvm/JvmAnnotationsJunit4Test.java), [Junit5](https://github.com/quick-perf/quickperf-examples/blob/master/jvm-junit5/src/test/java/org/quickperf/jvm/JvmAnnotationsJunit5Test.java), [TestNG](https://github.com/quick-perf/quickperf-examples/blob/master/jvm-testng/src/test/java/org/quickperf/jvm/JvmAnnotationsTestNGTest.java) &nbsp;&nbsp; :mag_right: [Heap allocation of Apache Maven](https://github.com/quick-perf/maven-test-bench)

### **SQL annotations**

```java
    @ExpectSelect(1)
    @Test
    public void should_find_all_players() {
     ...
    }
```

```
[PERF] You may think that <1> select statement was sent to the database
       But in fact <10>...

💣 You may have even more select statements with production data.
Be careful with the cost of JDBC server roundtrips: https://blog.jooq.org/2017/12/18/the-cost-of-jdbc-server-roundtrips/
```

Auto-detection of Hibernate and Spring Data JPA:
```
💡 Perhaps you are facing a N+1 select issue
	* With Hibernate, you may fix it by using JOIN FETCH
	                                       or LEFT JOIN FETCH
	                                       or FetchType.LAZY
	                                       or ...
```
```
	* With Spring Data JPA, you may fix it by adding
		@EntityGraph(attributePaths = { "..." }) on repository method.
```

📙 [All the SQL annotations](https://github.com/quick-perf/doc/wiki/SQL-annotations)  &nbsp;&nbsp; :mag_right: [Spring Boot & JUnit 4](https://github.com/quick-perf/quickperf-examples/tree/master/springboot-junit4) &nbsp;&nbsp; :mag_right: [Spring Boot & JUnit 5](https://github.com/quick-perf/quickperf-examples/tree/master/springboot-junit5)

### View Devoxx Belgium talk
[![Devoxx Belgium talk](https://github.com/quick-perf/doc/blob/master/doc/images/Devoxx-Belgium.jpg?raw=true)](https://youtu.be/cEkoJL09kKI?t=5)

## Documentation
The full documentation is available [here](https://github.com/quick-perf/doc/wiki/QuickPerf).

## Something to ask us?

💬 Want to chat with us? Join us on [gitter](https://gitter.im/quickperf)

:octocat: You prefer to use a Github issue to ask a question? [Create a question issue](https://github.com/quick-perf/quickperf/issues/new?assignees=&labels=question&template=question.md&title=)

## Show your support
Please ⭐ this repository or [![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social&label=Tweet%20to%20support%20QuickPerf)](https://twitter.com/intent/tweet?text=You%20can%20use%20%40QuickPerf%20to%20quickly%20evaluate%20some%20Java%20performance%20properties%0Ahttps%3A%2F%2Fgithub.com%2Fquick-perf%2Fquickperf%0A) if this project helped you!

## Contributing
You are very welcome to contribute to QuickPerf! You can contribute in many ways. Some relatively easy things can be done. Other issues are more challenging. Each contribution is appreciated. Read our <a href="/CONTRIBUTING.md">contributing guide</a> to learn more.

## Contributors

Many thanks to all our contributors! 

<table>
    <tr>
        <td align="center">
            <a href="https://github.com/jeanbisutti">
                <img src="https://avatars1.githubusercontent.com/u/14811066?v=4" width="100px;"  alt="Jean Bisutti"/>
                <br/>
                <sub><b>Jean Bisutti</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Code">💻</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Tests">⚠</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Documentation">📖</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Design">🎨</a><br>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Examples">💡</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Reviewed Pull Requests">👀</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=jeanbisutti" title="Talks">📢</a>
        </td>
        <td align="center">
            <a href="https://github.com/guiRagh">
                <img src="https://avatars2.githubusercontent.com/u/47635364?v=4" width="100px;" alt="guiRagh"/>
                <br/>
                <sub><b>Guillaume Raghoumandan</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=guiRagh" title="Code">💻</a> 
            <a href="https://github.com/quick-perf/quickperf/commits?author=guiRagh" title="Tests">⚠</a>
        </td>
        <td align="center">
            <a href="https://github.com/pcavezzan">
                <img src="https://avatars2.githubusercontent.com/u/3405916?v=4" width="100px;" alt="Patrice CAVEZZAN"/>
                <br/>
                <sub><b>Patrice Cavezzan</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=pcavezzan" title="Code">💻</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=pcavezzan" title="Infrastructure">🚇</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=pcavezzan" title="Documentation">📖</a>
        </td>
        <td align="center">
            <a href="https://github.com/ablanchard">
                <img src="https://avatars1.githubusercontent.com/u/6951980?v=4" width="100px;"  alt="Alexandre Blanchard"/>
                <br/>
                <sub><b>Alexandre Blanchard</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=ablanchard" title="Bug reports">🐛</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=ablanchard" title="Code">💻</a>
        </td>        
        <td align="center">
            <a href="https://github.com/emcdow123">
                <img src="https://avatars1.githubusercontent.com/u/5025020?v=4" width="100px;" alt="Eric McDowell"/>
                <br/>
                <sub><b>Eric McDowell</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=emcdow123" title="Code">💻</a>
        </td>        
        <td align="center">
            <a href="https://github.com/waterfl0w">
                <img src="https://avatars1.githubusercontent.com/u/3315137?v=4" width="100px;" alt="Jan Krüger"/>
                <br/>
                <sub><b>Jan Krüger</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=waterfl0w" title="Code">💻</a>
        </td>
        <td align="center">
            <a href="https://github.com/loicmathieu">
                <img src="https://avatars1.githubusercontent.com/u/1819009?v=4" width="100px;" alt="Loïc Mathieu"/>
                <br/>
                <sub><b>Loïc Mathieu</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=loicmathieu" title="Code">💻</a>
        </td>
    </tr>
    <tr>
        <td align="center">
            <a href="https://github.com/danny95djb">
                <img src="https://avatars0.githubusercontent.com/u/6143158?v=4" width="100px;" alt="Daniel Bentley"/>
                <br/>
                <sub><b>Daniel Bentley</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=danny95djb" title="Infrastructure">🚇</a>
        </td> 
        <td align="center">           
            <a href="https://github.com/gaurav9822">
                <img src="https://avatars2.githubusercontent.com/u/5204384?v=4" width="100px;" alt="Gaurav Deshpande"/>
                <br/>
                <sub><b>Gaurav Deshpande</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=gaurav9822" title="Tests">⚠</a>
        </td>
        <td align="center">           
            <a href="https://github.com/rdm100">
                <img src="https://avatars2.githubusercontent.com/u/19872359?v=4" width="100px;" alt="rdm100"/>
                <br/>
                <sub><b>rdm100</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=rdm100" title="Documentation">📖</a>
        </td>
        <td align="center">
            <a href="https://github.com/Artus2b">
                <img src="https://avatars1.githubusercontent.com/u/3645691?v=4" width="100px;"  alt="Artus de Benque"/>
                <br/>
                <sub><b>Artus de Benque</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=ablanchard" title="Bug reports">🐛</a>
            <a href="https://github.com/quick-perf/quickperf/commits?author=ablanchard" title="Code">💻</a>
        </td>
		<td align="center">
            <a href="https://github.com/Minh-Trieu">
                <img src="https://avatars1.githubusercontent.com/u/12820973?v=4" width="100px;" alt="Minh-Trieu Ha"/>
                <br/>
                <sub><b>Minh-Trieu Ha</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=Minh-Trieu" title="Code">💻</a>
        </td>  		
        <td align="center">
            <a href="https://github.com/dialaya">
                <img src="https://avatars1.githubusercontent.com/u/254883?v=4" width="100px;" alt="Bakary Djiba"/>
                <br/>
                <sub><b>Bakary Djiba</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=dialaya" title="Code">💻</a>
        </td>                
        <td align="center">
            <a href="https://github.com/fabfas">
                <img src="https://avatars.githubusercontent.com/fabfas" width="100px;" alt="C Faisal"/>
                <br/>
                <sub><b>C Faisal</b></sub>
            </a>
            <br/>
            <a href="https://github.com/quick-perf/quickperf/commits?author=fabfas" title="Code">💻</a>
        </td>                
    </tr>
</table>
<a href = "https://allcontributors.org/docs/en/emoji-key">emoji key</a>

## Sponsors
Many thanks Zenika for sponsoring this project! <br><br>
[![with love by zenika](https://img.shields.io/badge/With%20%E2%9D%A4%EF%B8%8F%20by-Zenika-b51432.svg)](https://oss.zenika.com)

## License
[Apache License 2.0](/LICENSE.txt)