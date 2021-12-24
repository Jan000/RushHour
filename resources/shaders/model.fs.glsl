#version 450 core

layout(location = 0) uniform vec4 color = vec4(1.0, 1.0, 1.0, 1.0);
layout(location = 1) uniform vec3 lightPos;
layout(location = 2) uniform vec3 lightColor;
layout(location = 3) uniform bool lightingEnabled;

layout(binding = 0) uniform sampler2D tex_ambient;
layout(binding = 1) uniform sampler2D tex_diffuse;
layout(binding = 2) uniform sampler2D tex_dissolve;
layout(binding = 3) uniform sampler2D tex_specular;
layout(binding = 4) uniform sampler2D tex_specularExponent;

layout(std140, binding = 0) uniform MaterialBlock{
	vec3 ambientColor;
	vec3 diffuseColor;
	vec3 specularColor;
	vec3 transmissionColor;
	float dissolve;
	float specularExponent;
	bool useTexAmbient;
	bool useTexDiffuse;
	bool useTexDissolve;
	bool useTexSpecular;
	bool useTexSpecularExp;
} material;

layout(location = 0) in VS_OUT {
	vec3 vertex;
	vec3 normal;
	vec2 texCoord;
} fs_in;

layout(location = 0) out vec4 outColor;

void main(){
	if(lightingEnabled){
		float alpha = 1.0;
		
		// ambient
		float ambientStrength = 0.15;
		vec3 ambient = ambientStrength * material.ambientColor;    
		
		 // diffuse 
		vec3 norm = normalize(fs_in.normal);
		vec3 lightDir = normalize(lightPos - fs_in.vertex);
		float diff = max(dot(norm, lightDir), 0.0);
		vec3 diffuse = diff * material.diffuseColor;
		
		if(material.useTexDiffuse){
			vec4 texDiff = texture(tex_diffuse, fs_in.texCoord);
			diffuse *= texDiff.xyz;
			alpha *= texDiff.w;
		}
		
		// specular
		float specularStrength = 0.6;
		vec3 viewDir = normalize(-fs_in.vertex);
		vec3 reflectDir = reflect(-lightDir, norm);  
		float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
		vec3 specular = specularStrength * spec * material.specularColor; 
		
		vec3 result = (ambient + diffuse + specular);
		outColor = vec4(result, alpha) * color;
	}else{
		if(material.useTexDiffuse){
			outColor = texture(tex_diffuse, fs_in.texCoord) * color;
		}else{
			outColor = vec4(material.diffuseColor, 1.0) * color;
		}
	}
}