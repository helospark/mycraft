#version 130

out vec4 outColor;
uniform sampler2D sampler;
uniform vec3 colorize;

in vec2 texCoord;


void main() {
	vec4 color = texture(sampler, texCoord);
	color.xyz += colorize;
	outColor = color;
	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
}