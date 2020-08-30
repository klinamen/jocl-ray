package io.klinamen.joclray.scene;

import io.klinamen.joclray.FloatVec4;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ElementSet<T extends Element> implements Iterable<T> {
    private final SortedMap<Integer, T> map;

    public ElementSet(SortedMap<Integer, T> map) {
        this.map = map;
    }

    public int[] getIdToIndex(){
        int[] index = new int[map.lastKey() + 1];
        int i=0;
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            index[entry.getKey()] = i++;
        }
        return index;
    }

    public int[] getIds(){
        return map.values().stream()
                .mapToInt(Element::getId)
                .toArray();
    }

    public float[] getFloats(Function<T, Float> propSelector){
        float[] out = new float[map.size()];
        int i=0;
        for (T item : map.values()) {
            out[i++] = propSelector.apply(item);
        }
        return out;
    }

    public float[] getFloatsById(Function<T, Float> propSelector){
        float[] out = new float[map.lastKey() + 1];
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            out[entry.getKey()] = propSelector.apply(entry.getValue());
        }
        return out;
    }

    public float[] getFloatVec4sById(Function<T, FloatVec4> propSelector){
        float[] out = new float[(map.lastKey() + 1) * FloatVec4.DIM];
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            FloatVec4 v = propSelector.apply(entry.getValue());
            System.arraycopy(v.getArray(), 0, out, entry.getKey() * FloatVec4.DIM, FloatVec4.DIM);
        }
        return out;
    }

    public float[] getFloatVec4s(Function<T, FloatVec4> propSelector){
        float[] out = new float[map.size() * FloatVec4.DIM];
        int i=0;
        for (T item : map.values()) {
            FloatVec4 v = propSelector.apply(item);
            System.arraycopy(v.getArray(), 0, out, i * FloatVec4.DIM, FloatVec4.DIM);
            i++;
        }
        return out;
    }

    public void copyFloats(int[] elementIds, float[] buffer, Function<T, Float> propSelector) {
        if (elementIds.length != buffer.length) {
            throw new IllegalArgumentException("Buffer lengths do not match!");
        }

        for (int i = 0; i < elementIds.length; i++) {
            int id = elementIds[i];
            T element = map.get(id);
            if (element != null) {
                buffer[i] = propSelector.apply(element);
            }
        }
    }

    public void copyFloatVec4s(int[] elementIds, float[] buffer, Function<T, FloatVec4> propSelector) {
        if (elementIds.length * FloatVec4.DIM != buffer.length) {
            throw new IllegalArgumentException("Buffer lengths do not match!");
        }

        for (int i = 0; i < elementIds.length; i++) {
            int id = elementIds[i];
            T element = map.get(id);
            if (element != null) {
                FloatVec4 source = propSelector.apply(element);
                System.arraycopy(source.getArray(), 0, buffer, i * FloatVec4.DIM, FloatVec4.DIM);
            }
        }
    }

    public int size(){
        return map.size();
    }

    public Collection<T> getElements(){
        return map.values();
    }

    @Override
    public Iterator<T> iterator() {
        return map.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        map.values().forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return map.values().spliterator();
    }
}