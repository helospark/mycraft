#version 130

uniform mat4 inModelViewProjectionMatrix;
in vec3 inPosition;
in vec2 inUv;
in vec3 inColor;

varying vec3 interpolatedColor;

out vec2 texCoord;

void main() {
	gl_Position = inModelViewProjectionMatrix * vec4(inPosition, 1.0);	
	texCoord = inUv;
	interpolatedColor = inColor;
}