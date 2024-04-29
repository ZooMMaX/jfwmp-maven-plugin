# JFWMP Maven Plugin

![Maven Central](https://img.shields.io/maven-central/v/ru.zoommax/jfwmp-maven-plugin?style=plastic)
![GitHub](https://img.shields.io/github/license/ZooMMaX/jfwmp-maven-plugin?style=plastic)
[![GitHub issues](https://img.shields.io/github/issues/ZooMMaX/jfwmp-maven-plugin?style=plastic)](https://github.com/ZooMMaX/jfwmp-maven-plugin/issues)

## Overview
The JFWMP (Java Flutter Web Multi Project) Maven Plugin is a custom plugin designed to simplify the development of client-server solutions within a single project. It automates the process of compiling Flutter projects, copying the compiled projects to resources, and generating server classes for access to the application. It is specifically designed for projects that use Java and Maven. This plugin is particularly useful for developers who are working on multi-project solutions where they need to manage multiple Flutter web applications in a single Java project.
## Parameters
The plugin accepts the following parameters:

- `apiPath`: The API path for the server. Default is `/api/v1/miniapp`.
- `port`: The port number for the server. Default is `8080`. See [SimpleServer library](https://github.com/ZooMMaX/SimpleServer) documentation for more information on the `port` parameter
- `threads`: The number of threads for the server. Default is `0` (use available processors - 1). See [SimpleServer library](https://github.com/ZooMMaX/SimpleServer) documentation for more information on the `threads` parameter
- `createStartServerMethod`: A boolean value indicating whether to create a start server method. Default is `true`.
- `createUnpackMethod`: A boolean value indicating whether to create an unpack method. Default is `true`.
- `https`: A boolean value indicating whether to use HTTPS. Default is `false`.

## Generated Files
The plugin generates the following files:

- `Server.java`: This file contains the server class with endpoints for each file in the Flutter project. It is located in the `flutters` package.
- `Unpacker.java`: This file contains the unpacker class with unpack methods for each Flutter project. It is also located in the `flutters` package.

These files are automatically generated and should not be manually modified, as they are overwritten each time the plugin is run.

## Usage
To use the plugin, add it to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>ru.zoommax</groupId>
            <artifactId>jfwmp-maven-plugin</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <configuration>
                <!-- Add your configuration here -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

You can customize the plugin's behavior by adding configuration parameters:

```xml
<configuration>
    <apiPath>/custom/api/path</apiPath>
    <port>8081</port>
    <threads>4</threads>
    <createStartServerMethod>false</createStartServerMethod>
    <createUnpackMethod>false</createUnpackMethod>
    <https>true</https>
</configuration>
```

## Directory Structure
The plugin expects the following directory structure:

- The Flutter projects should be located in a directory named `flutter` in the project's base directory.
- The `src/main/resources` directory is used to store the compiled Flutter projects.
- The `src/main/java/your_package/flutters` directory is used to store the generated `Server.java` and `Unpacker.java` files.

## Checks
The plugin checks for the presence of the `SimpleServer` dependency in the `pom.xml` file. If it is not found, an exception is thrown.

## Important Note
The plugin modifies the `index.html` file of the Flutter project to insert the base path. Therefore, it is important not to manually modify this file after the plugin has been run.