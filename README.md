## Vehicle Yielding Behavior Identification via Multi-Object Tracking

#### About

TBA

#### Quick Start (Ubuntu)

Install Oracle Java 8 on Ubuntu.

```sh
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer
$ sudo apt-get install oracle-java8-set-default
$ java -version
```

Install Git on Ubuntu.
```sh
$ sudo apt-get install git-core
```

Download or clone the repository to a local directory.
```sh
$ cd <local directory>
$ git clone https://github.com/nogrady/pedyield
```

Install Maven on Ubuntu.
```sh
$ sudo apt install maven
```

Build our project using Maven.
```sh
$ cd pedyield/
$ mvn clean compile package
```

Run the built -jar-with-dependencies.jar file.
```sh
$ cd target/
$ java -cp pedyield-0.0.1-SNAPSHOT-jar-with-dependencies.jar dzhuang.pedyield.detector.pedyield_detector -ip <> -iv <> [-t1 <>] [-t2 <>]
```
#### Params

| Params | Description |
| ------ | ------ |
| -ip | required, input file path of the pedestrian data |
| -iv | required, input file path of the vehicle data |
| -t1 | optional, the threshold of not yield |
| -t2 | optional, the threshold of yield |

#### Configuration
TBA

#### Calibration 
TBA