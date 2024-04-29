# JFWMP Maven Plugin

![Maven Central](https://img.shields.io/maven-central/v/ru.zoommax/jfwmp-maven-plugin?style=plastic)
![GitHub](https://img.shields.io/github/license/ZooMMaX/jfwmp-maven-plugin?style=plastic)
[![GitHub issues](https://img.shields.io/github/issues/ZooMMaX/jfwmp-maven-plugin?style=plastic)](https://github.com/ZooMMaX/jfwmp-maven-plugin/issues)

## Overview
The JFWMP (Java Flutter Web Multi Project) Maven Plugin is a custom plugin designed to simplify the development of client-server solutions within a single project. It automates the process of compiling Flutter projects, copying the compiled projects to resources, and generating server classes for access to the application. It is specifically designed for projects that use Java and Maven. This plugin is particularly useful for developers who are working on multi-project solutions where they need to manage multiple Flutter web applications in a single Java project.

## Prerequisites

Before you can use the JFWMP Maven Plugin, you need to ensure that you have the following prerequisites installed and set up on your computer:

- Flutter (which includes the Dart SDK): You can download Flutter from the [official Flutter website](https://flutter.dev/docs/get-started/install). Follow the instructions provided on the website to install Flutter on your operating system.

- Environment Variables: After installing Flutter and Dart, you need to set up your environment variables. Add the path to your Flutter `bin` directory to your `PATH` environment variable. The process for setting environment variables depends on your operating system:

    - On Windows, you can follow the instructions provided in this [Microsoft documentation](https://docs.microsoft.com/en-us/windows/win32/procthread/environment-variables).

    - On Linux and macOS, you can add the following line to your `.bashrc`, `.bash_profile`, or `.zshrc` file (depending on your shell):

      ```bash
      export PATH="$PATH:`<path_to_flutter_directory>`/flutter/bin"
      ```

      Replace `<path_to_flutter_directory>` with the actual path to the directory where you installed Flutter.

After setting up the prerequisites, you can proceed to use the JFWMP Maven Plugin as described in the rest of this README.

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
            <version>1.0.0</version>
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