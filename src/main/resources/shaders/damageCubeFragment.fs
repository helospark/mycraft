#version 130

out vec4 outColor;
//in vec3 interpolatedNormal;
//in vec3 interpolated_color;
uniform sampler2D sampler;
uniform float inverseTextureUnitSize;
uniform float textureX;

uniform int blockX;
uniform int blockY;
uniform int damage;

in vec2 texCoord;


void main() {
	vec2 originalCoordinate = vec2(blockX* inverseTextureUnitSize + texCoord.x * inverseTextureUnitSize, blockY* inverseTextureUnitSize + texCoord.y * inverseTextureUnitSize);
	vec2 newTexCoord = vec2(damage * inverseTextureUnitSize + texCoord.x * inverseTextureUnitSize, textureX + texCoord.y  * inverseTextureUnitSize);
	vec4 blockTexture = texture(sampler, originalCoordinate);
	vec4 crackTexture = texture(sampler, newTexCoord);
	
	outColor = blockTexture * crackTexture;
	
	//outColor = vec4(damage / 10.0, 0.0, 0.0, 1.0);
}