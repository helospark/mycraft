package com.helospark.mycraft.mycraft.services;

import org.lwjgl.util.vector.Vector3f;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import com.helospark.mycraft.mycraft.helpers.XmlHelpers;
import com.helospark.mycraft.mycraft.xml.XmlLoader;

@Component
public class GlobalParameters implements XmlLoader {
    public float inverseTextureUnitSize = 16.0f / 256.0f;
    public int initialWindowWidth = 800;
    public int initialWindowHeight = 600;
    public static final int NUMBER_OF_EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    public float fogNearDistance = 10.0f;
    public float fogFarDistance = 40.0f;
    public float fogFarDistanceSquared = fogFarDistance * fogFarDistance;
    public int desiredFps = 60;
    public boolean isFullScreen = false;
    public Vector3f fogColor = new Vector3f(0.6f, 0.6f, 0.6f);

    @Override
    public void parseXml(Element rootElement) {
        initialWindowWidth = XmlHelpers.parseIntegerValue(rootElement, "width");
        initialWindowHeight = XmlHelpers.parseIntegerValue(rootElement, "height");
        desiredFps = XmlHelpers.parseIntegerValue(rootElement, "desired-fps");
        isFullScreen = XmlHelpers.parseBooleanValue(rootElement, "is-fullscreen");
        fogNearDistance = (float) XmlHelpers.parseDoubleValue(rootElement, "fog-near-distance");
        fogFarDistance = (float) XmlHelpers.parseDoubleValue(rootElement, "fog-far-distance");
        fogFarDistanceSquared = fogFarDistance * fogFarDistance;
        fogColor = XmlHelpers.parseVector(rootElement, "fog-color");
    }
}
