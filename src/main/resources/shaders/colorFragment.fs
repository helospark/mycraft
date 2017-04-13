#version 130

out vec4 outColor;
//in vec3 interpolatedNormal;
//in vec3 interpolated_color;
uniform sampler2D sampler;

in vec3 interpolatedColor;


void main() {
	outColor = vec4(interpolatedColor, 1.0);
}