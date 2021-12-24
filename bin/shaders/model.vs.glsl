#version 450 core

layout(location = 0) in vec3 inVertex;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTexCoord;

layout(std430, binding = 0) readonly buffer MVPBuffer
{
	mat4 mvp;
};

layout(location = 0) out VS_OUT {
	vec3 vertex;
	vec3 normal;
	vec2 texCoord;
} vsOut;

void main(){
	vsOut.vertex = inVertex;
	vsOut.normal = inNormal;
	vsOut.texCoord = inTexCoord;
	gl_Position = mvp * vec4(inVertex, 1.0);
}