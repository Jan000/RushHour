#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 color;

layout(location = 0) out vec2 fragUV;
layout(location = 1) out vec4 fragColor;

layout(location = 0) uniform mat4 projectionMatrix;

void main(){
	fragUV = texCoord;
	fragColor = color;
	gl_Position = projectionMatrix * vec4(pos.xy, 0.0, 1.0);
}