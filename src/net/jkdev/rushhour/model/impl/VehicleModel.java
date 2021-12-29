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
