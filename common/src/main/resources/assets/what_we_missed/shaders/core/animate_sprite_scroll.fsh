#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:animation_sprite.glsl>

uniform sampler2D Sprite;

layout(location = 0) in float fAnimationProgress;
layout(location = 1) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 scrolledCoord = vec2(texCoord0.x, fract(texCoord0.y + fAnimationProgress));
    fragColor = textureLod(Sprite, scrolledCoord, MipMapLevel);
}
