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

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse speichert die z-Koordinate des roten Hauptfahrzeuges
 * und eine Liste aller Fahrzeugdaten auf dem Spielfeld.
 *
 * @author Jan Kiefer
 */
public class Level{
	
	public int						mainVehicleZ;
	public final List<LevelData>	dataList	= new ArrayList<>();
	
	public Level(){}
	
	public Level addData(int x, int z, int xUnits, int zUnits, boolean horizontal){
		dataList.add(new LevelData(x, z, xUnits, zUnits, horizontal));
		return this;
	}
	
	public Level addMainVehicleData(int z){
		mainVehicleZ = z;
		return this;
	}
	
	public static class LevelData{
		
		public int		x, z, xUnits, zUnits;
		public boolean	horizontal;
		
		public LevelData(int x, int z, int xUnits, int zUnits, boolean horizontal){
			this.x = x;
			this.z = z;
			this.xUnits = xUnits;
			this.zUnits = zUnits;
			this.horizontal = horizontal;
		}
	}
}
