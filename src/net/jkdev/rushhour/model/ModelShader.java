/**
MIT License

Copyright (c) 2021 Jan Kiefer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package net.jkdev.rushhour.model;

import static org.lwjgl.opengl.GL20.*;
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

	public static ModelShader createProgram(){
		return new ModelShader(glCreateProgram());
	}
}
