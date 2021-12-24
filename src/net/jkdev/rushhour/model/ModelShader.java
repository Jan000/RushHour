package net.jkdev.rushhour.model;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL45.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import net.jkdev.rushhour.ShaderProgram;

/**
 * Diese Klasse managt das OpenGL Shader Program zum Rendern von 3D-Modellen.
 * Es werden außerdem die Datenpositionen der Materialvariablen für den 
 * OpenGL Uniform Buffer ausgelesen und gespeichert, um diese mit der 
 * {@link net.jkdev.rushhour.model.Material} Klasse manipulieren zu können.
 * 
 * @author Jan Kiefer
 */
public class ModelShader extends ShaderProgram{

	public ModelShader(int program){
		super(program);
	}

	public IntBuffer uniBlockIndices, uniBlockOffsets;

	private Material defaultMaterial;

	public void init(){
		String[] mtlBlockNames = new String[]{"MaterialBlock.ambientColor", "MaterialBlock.diffuseColor", "MaterialBlock.specularColor",
				"MaterialBlock.transmissionColor", "MaterialBlock.dissolve", "MaterialBlock.specularExponent",
				"MaterialBlock.useTexAmbient", "MaterialBlock.useTexDiffuse", "MaterialBlock.useTexDissolve",
				"MaterialBlock.useTexSpecular", "MaterialBlock.useTexSpecularExp"};

		uniBlockIndices = BufferUtils.createIntBuffer(mtlBlockNames.length);
		glGetUniformIndices(program, mtlBlockNames, uniBlockIndices);
		uniBlockOffsets = BufferUtils.createIntBuffer(mtlBlockNames.length);
		glGetActiveUniformsiv(program, uniBlockIndices, GL_UNIFORM_OFFSET, uniBlockOffsets);
		uniBlockIndices.rewind();
	}

	public void defineDefaultMaterial(Material mtl){
		defaultMaterial = mtl;
	}

	public void setMaterial(Material mtl){
		glBindBufferBase(GL_UNIFORM_BUFFER, 0, mtl.glBuffer);
		for(int i = 0; i < mtl.textures.capacity(); i++){
			glBindTextureUnit(i, mtl.textures.get(i));
		}
	}

	public void setDefaultMaterial(){
		setMaterial(defaultMaterial);
	}
	
	public static ModelShader createProgram() {
		return new ModelShader(glCreateProgram());
	}
}
