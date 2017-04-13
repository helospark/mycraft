uniform float fogEnd;
uniform float fogStart;
uniform vec3 fogColor;
in vec4 positionForFog;

vec3 calculateFogColor(vec3 currentColor) {
	float distance = length(positionForFog.xyz / positionForFog.w);
	float factor = (fogEnd - distance) / (fogEnd - fogStart);
	return mix(fogColor, currentColor, clamp(factor, 0.0, 1.0));
}