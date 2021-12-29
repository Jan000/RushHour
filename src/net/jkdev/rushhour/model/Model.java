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

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import com.mokiat.data.front.error.WFException;
import com.mokiat.data.front.parser.*;

import net.jkdev.rushhour.RushHour;
import net.jkdev.rushhour.TextureLoader;

/**
 * In dieser Klasse werden alle benötigten Daten eines 3D-Modells in OpenGL
 * hochgeladen in ein Vertex Buffer Object, welches mit einem Vertex Array Object verknüpft wird.
 * Außerdem managt diese Klasse die Erstellung und Freigebung von allen genutzten OpenGL Objekten.
 * Des Weiteren wird das Laden von Wavefront 3D-Modelldaten ermöglicht.
 *
 * @author Jan Kiefer
 */
public class Model{
	
	private int		vao, vbo;
	private int		vertexCount;
	private int[]	objIndices;
	
	private int[]		meshData;
	private Material[]	materials;
	
	public Model(){
		
	}
	
	public void load(int[] objIndices, int[] meshData, Material[] materials, FloatBuffer vertices, FloatBuffer normals,
			FloatBuffer texCoordData){
		this.objIndices = objIndices;
		this.meshData = meshData;
		this.materials = materials;
		
		vertexCount = vertices.capacity() / 3;
		int vao = this.vao = glCreateVertexArrays();
		
		int vbo = this.vbo = glCreateBuffers();
		
		int verticesSize = vertices.capacity() << 2;
		int normalsSize = normals.capacity() << 2;
		
		glNamedBufferData(vbo, vertices.capacity() + normals.capacity() + texCoordData.capacity() << 2, GL_DYNAMIC_DRAW);
		glNamedBufferSubData(vbo, 0, vertices);
		glNamedBufferSubData(vbo, verticesSize, normals);
		glNamedBufferSubData(vbo, verticesSize + normalsSize, texCoordData);
		
		glVertexArrayVertexBuffer(vao, 0, vbo, 0, 3 << 2);
		glVertexArrayVertexBuffer(vao, 1, vbo, verticesSize, 3 << 2);
		glVertexArrayVertexBuffer(vao, 2, vbo, verticesSize + normalsSize, 2 << 2);
		
		glVertexArrayAttribBinding(vao, 0, 0);
		glVertexArrayAttribFormat(vao, 0, 3, GL_FLOAT, false, 0);
		glVertexArrayBindingDivisor(vao, 0, 0);
		glEnableVertexArrayAttrib(vao, 0);
		
		glVertexArrayAttribBinding(vao, 1, 1);
		glVertexArrayAttribFormat(vao, 1, 3, GL_FLOAT, false, 0);
		glVertexArrayBindingDivisor(vao, 1, 0);
		glEnableVertexArrayAttrib(vao, 1);
		
		glVertexArrayAttribBinding(vao, 2, 2);
		glVertexArrayAttribFormat(vao, 2, 2, GL_FLOAT, false, 0);
		glVertexArrayBindingDivisor(vao, 2, 0);
		glEnableVertexArrayAttrib(vao, 2);
	}
	
	public int getObjectCount(){
		return objIndices.length;
	}
	
	public int getObjectIndex(int objectID){
		return objIndices[objectID];
	}
	
	public int getObjectSize(int objectID){
		return (objectID + 1 < objIndices.length ? objIndices[objectID + 1] : vertexCount) - objIndices[objectID];
	}
	
	public void destroy(){
		glDeleteBuffers(vbo);
		glDeleteVertexArrays(vao);
	}
	
	public void render(RushHour game, double delta){
		render(game, delta, RushHour.COLOR_WHITE);
	}
	
	public void render(RushHour game, double delta, int objectID){
		render(game, delta, objectID, RushHour.COLOR_WHITE);
	}
	
	public void render(RushHour game, double delta, float[] color){
		glUniform4fv(0, color);
		glBindVertexArray(vao);
		for(int i = 0; i < meshData.length; i += 4){
			int mtlIndex = meshData[i + 1];
			if(mtlIndex != -1){
				game.setModelMaterial(materials[mtlIndex]);
			}else{
				game.resetModelMaterial();
			}
			glDrawArrays(GL_TRIANGLES, meshData[i + 2], meshData[i + 3]);
		}
		glBindVertexArray(0);
	}
	
	public void render(RushHour game, double delta, int objectID, float[] color){
		glBindVertexArray(vao);
		glUniform4fv(0, color);
		for(int i = 0; i < meshData.length; i += 4){
			if(meshData[i] == objectID){
				int mtlIndex = meshData[i + 1];
				if(mtlIndex != -1){
					game.setModelMaterial(materials[mtlIndex]);
				}else{
					game.resetModelMaterial();
				}
				glDrawArrays(GL_TRIANGLES, meshData[i + 2], meshData[i + 3]);
			}
		}
		glBindVertexArray(0);
	}
	
	public static <T extends Model> T loadOBJ(ModelShader modelShader, TextureLoader textureLoader, String path, Class<T> type,
			OBJModel model, Map<String, MTLLibrary> materialMap){
		List<Float> vertices = new ArrayList<>(model.getVertices().size() * 3);
		List<Float> normals = new ArrayList<>();
		List<Float> texCoords = new ArrayList<>();
		List<Integer> objIndexList = new ArrayList<>();
		List<Integer> meshDataList = new ArrayList<>();
		List<Material> materialList = new ArrayList<>();
		
		for(MTLLibrary mtllib : materialMap.values()){
			for(MTLMaterial mtl : mtllib.getMaterials()){
				Material material = new Material(mtl.getName());
				try{
					material.load(modelShader, textureLoader, path, mtl);
					materialList.add(material);
				}catch(IOException e){
					RushHour.handleError(e);
				}
			}
		}
		
		Vector3f normal = new Vector3f();
		
		for(OBJObject object : model.getObjects()){
			objIndexList.add(vertices.size() / 3);
			for(OBJMesh mesh : object.getMeshes()){
				int meshVerticesIndex = vertices.size() / 3;
				meshDataList.add(objIndexList.size() - 1);
				materialIndex: {
					String meshMaterialName = mesh.getMaterialName();
					for(int i = 0; i < materialList.size(); i++){
						if(materialList.get(i).getName().equals(meshMaterialName)){
							meshDataList.add(i);
							break materialIndex;
						}
					}
					meshDataList.add(-1);
				}
				meshDataList.add(meshVerticesIndex);
				for(OBJFace face : mesh.getFaces()){
					normal.set(0.0F, 0.0F, 0.0F);
					List<OBJDataReference> refList = face.getReferences();
					boolean hasNormals = false;
					for(int i = 0, len = refList.size(); i < len; i++){
						OBJDataReference ref = refList.get(i);
						OBJVertex vertexCurrent = model.getVertex(ref);
						
						vertices.add(vertexCurrent.x);
						vertices.add(vertexCurrent.y);
						vertices.add(vertexCurrent.z);
						
						if(ref.hasTexCoordIndex()){
							OBJTexCoord texCoord = model.getTexCoord(ref);
							texCoords.add(texCoord.u);
							texCoords.add(1.0F - texCoord.v);
						}else{
							texCoords.add(0.0F);
							texCoords.add(0.0F);
						}
						
						if(ref.hasNormalIndex()){
							hasNormals = true;
							OBJNormal n = model.getNormal(ref);
							normals.add(n.x);
							normals.add(n.y);
							normals.add(n.z);
						}else{
							OBJVertex vertexNext = model.getVertex(refList.get((i + 1) % len));
							normal.add((vertexCurrent.y - vertexNext.y) * (vertexCurrent.z + vertexNext.z),
									(vertexCurrent.z - vertexNext.z) * (vertexCurrent.x + vertexNext.x),
									(vertexCurrent.x - vertexNext.x) * (vertexCurrent.y + vertexNext.y));
						}
					}
					
					if(!hasNormals){
						normal.normalize();
						
						for(int i = 0; i < refList.size(); i++){
							normals.add(normal.x);
							normals.add(normal.y);
							normals.add(normal.z);
						}
					}
				}
				meshDataList.add(vertices.size() / 3 - meshVerticesIndex);
			}
		}
		
		int[] meshData = meshDataList.stream().mapToInt(i -> i).toArray();
		int[] objIndices = objIndexList.stream().mapToInt(i -> i).toArray();
		Material[] materials = materialList.toArray(new Material[0]);
		
		FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.size());
		vertices.forEach(f -> vertexData.put(f));
		vertexData.position(0);
		
		FloatBuffer normalData = BufferUtils.createFloatBuffer(normals.size());
		normals.forEach(f -> normalData.put(f));
		normalData.position(0);
		
		FloatBuffer texCoordData = BufferUtils.createFloatBuffer(texCoords.size());
		texCoords.forEach(f -> texCoordData.put(f));
		texCoordData.position(0);
		
		T instance = null;
		try{
			instance = type.getConstructor().newInstance();
		}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e){
			RushHour.handleError(e);
		}
		
		instance.load(objIndices, meshData, materials, vertexData, normalData, texCoordData);
		
		return instance;
	}
	
	public static <T extends Model> T loadOBJ(ModelShader modelShader, TextureLoader textureLoader, String path, Class<T> type)
			throws IOException, WFException{
		int i = path.lastIndexOf('/');
		if(i == -1){
			i = path.lastIndexOf('\\');
		}
		String directoryPath = i != 0 ? path.substring(0, i + 1) : "";
		
		try(InputStream in = ClassLoader.getSystemResourceAsStream(path)){
			IOBJParser parser = new OBJParser();
			OBJModel model = parser.parse(in);
			
			Map<String, MTLLibrary> materials = new HashMap<>();
			IMTLParser mtlParser = new MTLParser();
			
			for(String materialLibraryName : model.getMaterialLibraries()){
				try(InputStream mtlIn = ClassLoader.getSystemResourceAsStream(directoryPath + materialLibraryName)){
					MTLLibrary material = mtlParser.parse(mtlIn);
					materials.put(materialLibraryName, material);
				}
			}
			
			return loadOBJ(modelShader, textureLoader, directoryPath, type, model, materials);
		}
	}
}
