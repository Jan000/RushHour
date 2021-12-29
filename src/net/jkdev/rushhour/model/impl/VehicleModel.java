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

package net.jkdev.rushhour.model.impl;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.jkdev.rushhour.RushHour;
import net.jkdev.rushhour.model.Material;
import net.jkdev.rushhour.model.Model;

/**
 * Diese Klasse erbt die {link {@link net.jkdev.rushhour.model.Model} Klasse
 * und erweitert diese mit den Dimensionen eines Fahrzeugmodells
 * in absoluten Werten und in Spielfeldeinheiten.
 *
 * @author Jan Kiefer
 */
public class VehicleModel extends Model{

	private boolean		loaded	= false;
	private float		width, height, length;
	private int			unitsX, unitsZ;
	private Vector3f	min, max;

	public VehicleModel(){}

	public boolean isLoaded(){
		return loaded;
	}

	public float getWidth(){
		return width;
	}

	public float getHeight(){
		return height;
	}

	public float getLength(){
		return length;
	}

	public int getUnitsX(){
		return unitsX;
	}

	public int getUnitsZ(){
		return unitsZ;
	}

	public Vector3fc getMin(){
		return min;
	}

	public Vector3fc getMax(){
		return max;
	}

	@Override
	public void load(int[] objIndices, int[] meshData, Material[] materials, FloatBuffer vertices, FloatBuffer normals,
			FloatBuffer texCoordData){
		super.load(objIndices, meshData, materials, vertices, normals, texCoordData);
		vertices.position(0);
		float maxX = vertices.get();
		float maxY = vertices.get();
		float maxZ = vertices.get();
		float minX = maxX;
		float minY = maxY;
		float minZ = maxZ;
		while(vertices.hasRemaining()){
			float x = vertices.get();
			float y = vertices.get();
			float z = vertices.get();

			maxX = Math.max(maxX, x);
			minX = Math.min(minX, x);

			maxY = Math.max(maxY, y);
			minY = Math.min(minY, y);

			maxZ = Math.max(maxZ, z);
			minZ = Math.min(minZ, z);
		}

		width = maxX - minX;
		height = maxY - minY;
		length = maxZ - minZ;

		min = new Vector3f(minX, minY, minZ);
		max = new Vector3f(maxX, maxY, maxZ);

		unitsX = Math.round(width / RushHour.UNIT_SIZE_X);
		unitsZ = Math.round(length / RushHour.UNIT_SIZE_Z);

		loaded = true;
	}

}
