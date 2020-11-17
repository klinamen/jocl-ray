package io.klinamen.joclray.scene;

import com.google.common.collect.ImmutableSortedMap;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.light.PointLight;
import io.klinamen.joclray.util.FloatVec4;
import io.klinamen.joclray.util.IoR;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class Scene {
    private final Camera camera;

    private float ambientLightIntensity = 0;
    private FloatVec4 origin = new FloatVec4();

    private float worldIor = IoR.VACUUM;

    int nextId = 0;
    private final SortedMap<Integer, Element> elements = new TreeMap<>(Integer::compare);

    public Scene(Camera camera) {
        this.camera = camera;
    }

    private synchronized Scene add(Element element){
        elements.put(nextId++, element);
        return this;
    }

    public <T extends Surface> Scene add(T surface){
        return add(new SurfaceElement<>(nextId, surface));
    }

    public <T extends Surface> Scene add(String name, T surface){
        return add(new SurfaceElement<>(nextId, name, surface));
    }

    public Scene add(PointLight light){
        return add(new LightElement(elements.size(), light));
    }

    public Scene add(String name, PointLight light){
        return add(new LightElement(elements.size(), light));
    }

    public ElementSet<SurfaceElement<Surface>> getSurfaces(){
        return getSurfaceSetByType(Surface.class);
    }

    public ElementSet<SurfaceElement<? extends Surface>> getSurfaces(Predicate<Surface> filter){
        ImmutableSortedMap<Integer, SurfaceElement<? extends Surface>> surfaceMap = elements.values().stream()
                .filter(x -> x instanceof SurfaceElement)
                .map(x -> (SurfaceElement<? extends Surface>) x)
                .filter(x -> filter.test(x.getSurface()))
                .collect(ImmutableSortedMap.toImmutableSortedMap(Integer::compare, Element::getId, Function.identity()));
        return new ElementSet<>(surfaceMap);
    }

    public <T extends Surface> ElementSet<SurfaceElement<T>> getSurfaceSetByType(Class<T> clazz){
        ImmutableSortedMap<Integer, SurfaceElement<T>> surfaceMap = elements.values().stream().filter(x -> x instanceof SurfaceElement)
                .map(x -> (SurfaceElement<?>) x)
                .filter(x -> clazz.isInstance(x.getSurface()))
                .map(x -> (SurfaceElement<T>) x)
                .collect(ImmutableSortedMap.toImmutableSortedMap(Integer::compare, Element::getId, Function.identity()));

        return new ElementSet<>(surfaceMap);
    }

    public ElementSet<LightElement> getLightElements(){
        ImmutableSortedMap<Integer, LightElement> map = elements.values().stream().filter(x -> x instanceof LightElement)
                .map(x -> (LightElement) x)
                .collect(ImmutableSortedMap.toImmutableSortedMap(Integer::compare, Element::getId, Function.identity()));

        return new ElementSet<>(map);
    }

    public Element getElementById(int id){
        return elements.get(id);
    }

    public Camera getCamera() {
        return camera;
    }

    public FloatVec4 getOrigin() {
        return origin;
    }

    public Scene setOrigin(FloatVec4 origin) {
        this.origin = origin;
        return this;
    }

    public float getAmbientLightIntensity() {
        return ambientLightIntensity;
    }

    public Scene setAmbientLightIntensity(float ambientLightIntensity) {
        this.ambientLightIntensity = ambientLightIntensity;
        return this;
    }

    public float getWorldIor() {
        return worldIor;
    }

    public Scene setWorldIor(float worldIor) {
        this.worldIor = worldIor;
        return this;
    }
}
