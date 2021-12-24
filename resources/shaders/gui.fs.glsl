#version 450 core

precision mediump float;

layout(location = 0) in vec2 fragUV;
layout(location = 1) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

layout(location = 1) uniform sampler2D tex;

void main(){
	outColor = fragColor * texture(tex, fragUV.st);
} 