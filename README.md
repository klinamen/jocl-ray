# JOCLRay

JOCLRay is a 

## Features

- GPU 
- Object-oriented, dynamic scene description.
- Supported surface types: Sphere, Plane, Box.
- Supported light types: Point light, Spotlight with angle and attenuation.
- "Shading" renderer implementing Blinn-Phong shading model with shadows and arbitrary number of reflection bounces.
- "Visibility" renderer for visualizing intersection with view rays only.

## How to build

In order to build  JOCLRay you need JDK 10+ and Apache Maven. After cloning the repository, you can build JOCLRay from the repository root with
``` 
mvn package
```
This will build JOCLRay and package it to a JAR file with dependencies named `joclray-1.0-SNAPSHOT-jar-with-dependencies.jar` located in the `target` directory.

## Running JOCLRay
To run JOCLRay you need and at least one OpenCL-compatible device, and the appropriate drivers installed on your system.

Once JOCLRay is built, you can run it with
```
java -jar ./target/joclray-1.0-SNAPSHOT-jar-with-dependencies.jar
```

JOCLRay has a few command line options you can explore with the `--help` option.
