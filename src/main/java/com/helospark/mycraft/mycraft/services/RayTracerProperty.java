package com.helospark.mycraft.mycraft.services;

import java.util.ArrayList;
import java.util.List;

import com.helospark.mycraft.mycraft.mathutils.IntVector;
import com.helospark.mycraft.mycraft.raytracer.RayTracerType;

public class RayTracerProperty {
    private RayTracerType[] types;
    private List<IntVector> ignoreAtPositions = new ArrayList<>();
    private final int ownerToIgnore;

    public RayTracerProperty(RayTracerType[] types, int ownerToIgnore) {
        this.types = types;
        this.ownerToIgnore = ownerToIgnore;
    }

    public void addIgnored(IntVector vector) {
        ignoreAtPositions.add(vector);
    }

    public int getOwnerIdToIgnore() {
        return ownerToIgnore;
    }

    public RayTracerType[] getTypes() {
        return types;
    }

    public void setTypes(RayTracerType[] types) {
        this.types = types;
    }

    public List<IntVector> getIgnoreAtPositions() {
        return ignoreAtPositions;
    }

    public void setIgnoreAtPositions(List<IntVector> ignoreAtPositions) {
        this.ignoreAtPositions = ignoreAtPositions;
    }

}