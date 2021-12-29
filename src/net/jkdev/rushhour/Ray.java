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

package net.jkdev.rushhour;

import org.joml.Vector3f;

/**
 * Mit Hilfe dieser Klasse wird mittels Ray-intersection bestimmt,
 * ob ein Strahl, welcher aus einem Positions- und Richtungsvektor besteht,
 * eine 3D-Fläche schneidet.
 *
 * @author Jan Kiefer
 */
public class Ray{
	
	private static final Vector3f[] vectorCache = new Vector3f[7];
	
	static{
		for(int i = 0; i < vectorCache.length; i++){
			vectorCache[i] = new Vector3f();
		}
	}
	
	public final Vector3f	position	= new Vector3f();
	public final Vector3f	direction	= new Vector3f();
	
	public Ray(){}
	
	public synchronized boolean intersectsQuad(Vector3f v1, Vector3f v2, Vector3f v3){
		Vector3f dS21 = vectorCache[0], dS31 = vectorCache[1], n = vectorCache[2], M = vectorCache[4], dMS1 = vectorCache[5],
				tmp = vectorCache[6];
		
		v2.sub(v1, dS21);
		v3.sub(v1, dS31);
		dS21.cross(dS31, n);
		
		float ndotdR = n.dot(direction);
		
		if(Math.abs(ndotdR) < 1e-6f){
			return false;
		}
		
		float t = -n.dot(position.sub(v1, tmp)) / ndotdR;
		
		direction.mul(t, M).add(position);
		
		M.sub(v1, dMS1);
		float u = dMS1.dot(dS21);
		float v = dMS1.dot(dS31);
		
		return u >= 0.0F && u <= dS21.dot(dS21) && v >= 0.0F && v <= dS31.dot(dS31);
	}
}
