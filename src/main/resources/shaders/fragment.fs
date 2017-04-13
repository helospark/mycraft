#version 130

out vec4 outColor;
//in vec3 interpolatedNormal;
//in vec3 interpolated_color;
uniform sampler2D sampler;

in vec2 texCoord;
in float interpolatedLight;

#include "fog.fsi"


void main() {
	vec4 color = texture(sampler, texCoord) * interpolatedLight;
	outColor = vec4(calculateFogColor(color.xyz), color.a);
	//outColor = texture(sampler, texCoord) * interpolatedLight;
	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
}