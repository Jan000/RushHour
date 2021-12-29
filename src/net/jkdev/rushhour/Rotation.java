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

/**
 * Diese Klasse dient dazu, die möglichen Fahrzeugrotationen
 * in Radianten zu speichern und um testen zu können,
 * auf welcher Achse sich ein Fahrzeug bewegt.
 *
 * @author Jan Kiefer
 */
public class Rotation{
	
	public static final float	EAST	= 0.0F;
	public static final float	WEST	= (float) Math.PI;
	public static final float	SOUTH	= (float) (-Math.PI / 2.0D);
	public static final float	NORTH	= (float) (Math.PI / 2.0D);
	
	private Rotation(){}
	
	public static boolean isHorizontal(float rotation){
		return (int) (Math.abs(rotation) / NORTH) % 2 == 0;
	}
}
