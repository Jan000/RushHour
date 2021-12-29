package net.jkdev.rushhour.model;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import com.mokiat.data.front.parser.MTLColor;
import com.mokiat.data.front.parser.MTLMaterial;

import net.jkdev.rushhour.TextureLoader;

/**
 * Mit Hilfe dieser Klasse werden die Wavefront Materialdaten in ein
 * OpenGL Uniform Buffer Object geladen, um diese in einem GLSL Shader Program
 * verwenden zu können.
 *
 * @author Jan Kiefer
 */
public class Material{
	
	private String name;
	
	IntBuffer	textures	= BufferUtils.createIntBuffer(5);
	int			glBuffer;
	
	public Material(String name){
		this.name = name;
	}
	
	public void load(ModelShader modelShader, TextureLoader textureLoader, String path, MTLMaterial mtl) throws IOException{
		MTLColor ambientColor = mtl.getAmbientColor(), diffuseColor = mtl.getDiffuseColor(), specularColor = mtl.getSpecularColor(),
				transmissionColor = mtl.getTransmissionColor();
		load(modelShader, textureLoader, path, new float[]{ambientColor.r, ambientColor.g, ambientColor.b},
				new float[]{diffuseColor.r, diffuseColor.g, diffuseColor.b}, new float[]{specularColor.r, specularColor.g, specularColor.b},
				new float[]{transmissionColor.r, transmissionColor.g, transmissionColor.b}, mtl.getSpecularExponent(), mtl.getDissolve(),
				mtl.getAmbientTexture(), mtl.getDiffuseTexture(), mtl.getSpecularTexture(), mtl.getSpecularExponentTexture(),
				mtl.getDissolveTexture());
	}
	
	public void load(ModelShader modelShader, TextureLoader textureLoader, String path, float[] ambientColor, float[] diffuseColor,
			float[] specularColor, float[] transmissionColor, float specularExponent, float dissolve, String ambientTexture,
			String diffuseTexture, String specularTexture, String specularExponentTexture, String dissolveTexture) throws IOException{
		glBuffer = glCreateBuffers();
		
		IntBuffer offsets = modelShader.uniBlockOffsets;
		ByteBuffer data = BufferUtils.createByteBuffer(offsets.get(10) + 1);
		data.position(offsets.get(0));
		data.putFloat(ambientColor[0]);
		data.putFloat(ambientColor[1]);
		data.putFloat(ambientColor[2]);
		data.position(offsets.get(1));
		data.putFloat(diffuseColor[0]);
		data.putFloat(diffuseColor[1]);
		data.putFloat(diffuseColor[2]);
		data.position(offsets.get(2));
		data.putFloat(specularColor[0]);
		data.putFloat(specularColor[1]);
		data.putFloat(specularColor[2]);
		data.position(offsets.get(3));
		data.putFloat(transmissionColor[0]);
		data.putFloat(transmissionColor[1]);
		data.putFloat(transmissionColor[2]);
		data.position(offsets.get(4));
		data.putFloat(dissolve);
		data.position(offsets.get(5));
		data.putFloat(specularExponent);
		data.position(offsets.get(6));
		data.put((byte) (ambientTexture == null ? 0 : 1));
		data.position(offsets.get(7));
		data.put((byte) (diffuseTexture == null ? 0 : 1));
		data.position(offsets.get(8));
		data.put((byte) (dissolveTexture == null ? 0 : 1));
		data.position(offsets.get(9));
		data.put((byte) (specularTexture == null ? 0 : 1));
		data.position(offsets.get(10));
		data.put((byte) (specularExponentTexture == null ? 0 : 1));
		data.flip();
		glNamedBufferData(glBuffer, data, GL_STATIC_DRAW);
		
		glCreateTextures(GL_TEXTURE_2D, textures);
		if(ambientTexture != null){
			textureLoader.loadTexture(path + ambientTexture, 0, textures);
		}else{
			glDeleteTextures(textures.get(0));
			textures.put(0, 0);
		}
		if(diffuseTexture != null){
			textureLoader.loadTexture(path + diffuseTexture, 1, textures);
		}else{
			glDeleteTextures(textures.get(1));
			textures.put(1, 0);
		}
		if(dissolveTexture != null){
			textureLoader.loadTexture(path + dissolveTexture, 2, textures);
		}else{
			glDeleteTextures(textures.get(2));
			textures.put(2, 0);
		}
		if(specularTexture != null){
			textureLoader.loadTexture(path + specularTexture, 3, textures);
		}else{
			glDeleteTextures(textures.get(3));
			textures.put(3, 0);
		}
		if(specularExponentTexture != null){
			textureLoader.loadTexture(path + specularExponentTexture, 4, textures);
		}else{
			glDeleteTextures(textures.get(4));
			textures.put(4, 0);
		}
		
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof Material && ((Material) obj).name.equals(name) || obj instanceof String && obj == name;
	}
}
