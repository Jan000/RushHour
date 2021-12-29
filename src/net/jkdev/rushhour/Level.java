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
