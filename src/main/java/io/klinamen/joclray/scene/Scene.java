package io.klinamen.joclray.scene;

import com.google.common.collect.ImmutableSortedMap;
import io.klinamen.joclray.geom.Surface;
import io.klinamen.joclray.light.PointLight;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class Scene {
    private final Camera camera;

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

    public Scene add(PointLight light){
        return add(new LightElement(elements.size(), light));
    }

    public ElementSet<SurfaceElement<Surface>> getSurfaces(){
        return getSurfaceSetByType(Surface.class);
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
}
