#version 130

uniform mat4 inProjectionMatrix;
uniform mat4 inViewMatrix;
uniform mat4 inModelMatrix;

uniform mat4 inModelViewProjectionMatrix;
in vec3 inPosition;
in vec2 inUv;
in float inLight;

out vec2 texCoord;
out float interpolatedLight;

#include "fog.vsi"

void main() {
	gl_Position = inModelViewProjectionMatrix * vec4(inPosition, 1.0);	
	texCoord = inUv;
	interpolatedLight = inLight;
	calculatePositionForFog();
}