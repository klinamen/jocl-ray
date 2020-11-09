# JOCLRay
JOCLRay is an attempt to implement basic ray tracing on GPU via OpenCL kernels, yet maintaining a high-level interface to describe the scene to be rendered and trying to keep the design modular.

## Features
- Ray casting, ray-object intersections, and shading run on GPU as OpenCL kernels.
- Object-oriented, dynamic scene description.
- Supported surface types: Sphere, Plane, Box.
- Supported light types: Point light, Spotlight with angle and attenuation.
- "Visibility" renderer for visualizing intersection with view rays only.
- "Shading" distribution ray-tracing renderer implementing Blinn-Phong lighting model with shadows, and configurable sampling resolution for glossy reflection and transmission.
- Octrees acceleration structure.
- Anti-aliasing with configurable sampling resolution.
- Thin-lens depth of field with configurable sampling resolution.
- Path tracing (implicit).
- Ashikhmin-Shierley and Lambertian BRDFs.
- HDR rendering and various tone-mapping operators.

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

## Sample scenes

### Scene 1
![Image of Scene1](./sample-images/scene1.png)

- FOV (grad): 50
- 1 primary ray per-pixel
- Lights: 3 point lights, 1 spot light
- Objects: 8
- Reflection bounces: 4

Rendering times on my laptop (Intel(R) Core(TM) i7-8550U CPU @ 1.80GHz, 16GB RAM, NVIDIA GeForce MX150) for both NVIDIA and Intel GPU. Reported rendering time is the average over 10 sequential runs, discarding the first one in which compilation of OpenCL kernels happens.

GPU         | Rendering Time
----------- | -------------
NVIDIA GeForce MX150    | 1.3s
Intel UHD Graphics 620  | 3.4s


### Scene 2
![Image of Scene2](./sample-images/scene2.png)

- FOV (grad): 30
- 1 primary ray per-pixel
- Lights: 1 point lights, 2 spot lights
- Objects: 10
- Reflection bounces: 4

Rendering times on my laptop (Intel(R) Core(TM) i7-8550U CPU @ 1.80GHz, 16GB RAM, NVIDIA GeForce MX150) for both NVIDIA and Intel GPU. Reported rendering time is the average over 10 sequential runs, discarding the first one in which compilation of OpenCL kernels happens.

GPU         | Rendering Time
----------- | -------------
NVIDIA GeForce MX150    | 1.6s
Intel UHD Graphics 620  | 3.5s

### Scene 3
![Image of Scene3](./sample-images/scene3.png)

- FOV (grad): 48
- 1 primary ray per-pixel
- Lights: 1 point light, 3 spot lights
- Objects: 8
  - Utah Teapot (6,320 tris)
  - Stanford Bunny low-poly (4,968 tris)
- Reflection bounces: 4
- Acceleration structure: Octrees

Rendering times on my laptop (Intel(R) Core(TM) i7-8550U CPU @ 1.80GHz, 16GB RAM, NVIDIA GeForce MX150) for both NVIDIA and Intel GPU. Reported rendering time is the average over 10 sequential runs, discarding the first one in which compilation of OpenCL kernels happens.

GPU         | Rendering Time
----------- | -------------
NVIDIA GeForce MX150    | 19.5s

### Scene 4
![Image of Scene4](./sample-images/scene4.png)

- FOV (grad): 60
- Image plane sampling (anti-aliasing): 16 rays per-pixel (4x4), jittered
- Reflection rays sampling (glossy reflections): 64 rays
- Transmission rays sampling (glossy transmission): 64 rays
- Lens-space sampling (depth of field): 1 ray (no DoF)
- Objects: 16
  - 4 x Utah Teapot (6,320 tris) 
- Reflection/Transmission splits max depth: 3
- Acceleration structure: Octrees

Rendering times on my laptop (Intel(R) Core(TM) i7-8550U CPU @ 1.80GHz, 16GB RAM, NVIDIA GeForce MX150) for both NVIDIA and Intel GPU. Reported rendering time is the average over 10 sequential runs, discarding the first one in which compilation of OpenCL kernels happens.

GPU         | Rendering Time
----------- | -------------
NVIDIA GeForce MX150    | 6.9h

#### Depth of Field
![Image of Scene4 with DoF](./sample-images/scene4_dof.png)
To show depth of field effect, the same scene was rendered with an aperture size of 1.25 and a focal length of 22. 

- FOV (grad): 60
- Focal length: 22
- Aperture size: 1.25
- Image plane sampling (anti-aliasing): 4 rays per-pixel (2x2), jittered
- Reflection rays sampling (glossy reflections): 4 rays
- Transmission rays sampling (glossy transmission): 4 rays
- Lens-space sampling (depth of field): 16 rays
- Objects: 16
  - 4 x Utah Teapot (6,320 tris) 
- Reflection/Transmission splits max depth: 3
- Acceleration structure: Octrees

GPU         | Rendering Time
----------- | -------------
NVIDIA GeForce MX150    | 2.2h

### Scene 5
![Image of Scene4](./sample-images/scene5.png)

- FOV (grad): 50
- Implicit (brute-force) GPU Path Tracing: 16,384 samples per pixel, 4 bounces.
- 1 spherical light.
- Ashikhmin-Shirley BRDF
- Lambertian BRDF
- Hable's filmic tone-mapping (https://64.github.io/tonemapping/#filmic-tone-mapping-operators)
- Acceleration structure: Octrees

Rendering times on my laptop (Intel(R) Core(TM) i7-8550U CPU @ 1.80GHz, 16GB RAM, NVIDIA GeForce MX150.

GPU         | Rendering Time
----------- | -------------
NVIDIA GeForce MX150    | 90 min

## Limitations and Future Work
- Extend the renderer to make use of multiple OpenCL devices.
- Surfaces in the scene are grouped by type and an intersection kernel is scheduled on the GPU for each group. Thus, each GPU thread calculates intersections for a `<ray, element>` pair. However, currently, there is no reduction step to merge the results and the memory access model may lead to the corruption of the buffer holding intersections results.
- Shadow rays generation and their intersections are calculated on the GPU, whereas the results are merged on CPU into a light-intensity map that associates an intensity value to each `<ray,light>`. Try to move the whole process to GPU and find a more compact data structure to represent the contribution of each light for each primary ray.
- Parallelize shading across lights.
- Camera, lights and all the objects are axis-aligned, as there is no support for transformations.
- Instancing
- Simplify code for wrappers and buffer management.
- Unit tests
- ...