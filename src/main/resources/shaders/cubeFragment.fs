#version 130

out vec4 outColor;
//in vec3 interpolatedNormal;
//in vec3 interpolated_color;
uniform sampler2D sampler;
uniform float inverseTextureUnitSize;
uniform float textureX;
uniform float textureY;

in vec2 texCoord;


void main() {
	vec2 newTexCoord = vec2(textureX + texCoord.x * inverseTextureUnitSize, textureY + texCoord.y * inverseTextureUnitSize);
	outColor = texture(sampler, newTexCoord);
	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
}