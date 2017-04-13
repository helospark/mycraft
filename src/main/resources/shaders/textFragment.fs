#version 130

out vec4 outColor;
uniform sampler2D sampler;

in vec2 texCoord;
in vec3 interpolatedColor;

void main() {
	vec4 color = texture(sampler, texCoord);
	color.rgb *= interpolatedColor;
	outColor = color;
	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
}