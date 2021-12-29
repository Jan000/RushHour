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

package net.jkdev.rushhour.obj.impl;

import org.joml.Vector2i;
import org.joml.Vector2ic;

import net.jkdev.rushhour.FieldReference;
import net.jkdev.rushhour.RushHour;
import net.jkdev.rushhour.obj.WorldObject;
import net.jkdev.rushhour.obj.model.ModelWorldObject;
import net.jkdev.rushhour.ui.GUILevelDone;

public class MapObject extends ModelWorldObject{
	
	private static final float	NO_HOVER_VEHICLE_ALPHA	= 0.85F;
	private static final float	HOVER_VEHICLE_ALPHA		= 1.0F;
	
	private Vector2i hoveredField = new Vector2i(-1, -1);
	
	private final FieldReference[][] fields = new FieldReference[6][6];
	
	private FieldReference	movingVehicleRef	= null;
	private float			movingStartOffset;
	
	public MapObject(){}
	
	@Override
	public void render(RushHour game, double delta, float[] color){
		applyTransform(game.modelViewMatrix);
		game.updateMvp();
		
		if(movingVehicleRef != null){
			FieldReference ref = movingVehicleRef;
			if(ref.isHorizontal()){
				int z = ref.getLocation().y();
				int minX = ref.getLocation().x();
				int maxX = minX;
				for(int x = minX; x >= 0 && (fields[x][z] == null || fields[x][z] == ref); x--){
					minX = x;
				}
				for(int x = maxX; x < 6 && (fields[x][z] == null || fields[x][z] == ref); x++){
					maxX = x;
				}
				maxX -= ref.getVehicleUnitsX() - 1;
				ref.getVehicle().position.x = Math.min(maxX * RushHour.UNIT_SIZE_X + ref.getPositionXOffset() + RushHour.MAP_OFFSET.x(),
						Math.max(minX * RushHour.UNIT_SIZE_X + ref.getPositionXOffset() + RushHour.MAP_OFFSET.x(),
								game.yPlaneIntersection.x() - movingStartOffset));
			}else{
				int x = ref.getLocation().x();
				int minZ = ref.getLocation().y();
				int maxZ = minZ;
				for(int z = minZ; z >= 0 && (fields[x][z] == null || fields[x][z] == ref); z--){
					minZ = z;
				}
				for(int z = maxZ; z < 6 && (fields[x][z] == null || fields[x][z] == ref); z++){
					maxZ = z;
				}
				maxZ -= ref.getVehicleUnitsZ() - 1;
				if(minZ == 0 && x == RushHour.MAP_EXIT_X){
					minZ -= 2;
				}
				ref.getVehicle().position.z = Math.min(maxZ * RushHour.UNIT_SIZE_Z + ref.getPositionZOffset() + RushHour.MAP_OFFSET.z(),
						Math.max(minZ * RushHour.UNIT_SIZE_Z + ref.getPositionZOffset() + RushHour.MAP_OFFSET.z(),
								game.yPlaneIntersection.z() - movingStartOffset));
			}
		}
		
		getModel().render(game, delta, color);
	}

	private void mapVehiclePosition(FieldReference ref, int targetX, int targetZ){
		for(int x = 0; x < 6; x++){
			for(int z = 0; z < 6; z++){
				if(fields[x][z] == ref){
					fields[x][z] = null;
				}
			}
		}
		for(int x = targetX, maxX = targetX + ref.getVehicleUnitsX(); x < maxX; x++){
			for(int z = targetZ, maxZ = targetZ + ref.getVehicleUnitsZ(); z < maxZ; z++){
				fields[x][z] = ref;
			}
		}
	}
	
	private void setVehicleCoords(FieldReference ref, int targetX, int targetZ){
		ref.getVehicle().position.set(targetX * RushHour.UNIT_SIZE_X + RushHour.MAP_OFFSET.x() + ref.getPositionXOffset(),
				RushHour.MAP_OFFSET.y(), targetZ * RushHour.UNIT_SIZE_Z + RushHour.MAP_OFFSET.z() + ref.getPositionZOffset());
		ref.setLocation(targetX, targetZ);
	}
	
	public boolean addVehicle(VehicleObject vehicle, float rotation, int unitX, int unitZ){
		FieldReference ref = new FieldReference(vehicle, rotation);
		for(int x = unitX, maxX = unitX + ref.getVehicleUnitsX(); x < maxX; x++){
			for(int z = unitZ, maxZ = unitZ + ref.getVehicleUnitsZ(); z < maxZ; z++){
				if(fields[x][z] != null){
					return false;
				}
			}
		}
		setVehicleCoords(ref, unitX, unitZ);
		mapVehiclePosition(ref, unitX, unitZ);
		return true;
	}
	
	public boolean isVehicleMoving(){
		return movingVehicleRef != null;
	}
	
	public void removeVehicle(VehicleObject vehicle){
		for(int x = 0; x < 6; x++){
			for(int z = 0; z < 6; z++){
				if(fields[x][z] != null && fields[x][z].getVehicle() == vehicle){
					fields[x][z] = null;
				}
			}
		}
	}
	
	public void removeAllVehicles(){
		for(int x = 0; x < 6; x++){
			for(int z = 0; z < 6; z++){
				fields[x][z] = null;
			}
		}
	}
	
	public Vector2ic getVehicleLocation(VehicleObject vehicle){
		for(int x = 0; x < 6; x++){
			for(int z = 0; z < 6; z++){
				if(fields[x][z] != null && fields[x][z].getVehicle() == vehicle){
					return fields[x][z].getLocation();
				}
			}
		}
		return null;
	}
	
	public VehicleObject getVehicleAt(Vector2ic location){
		return getVehicleAt(location.x(), location.y());
	}
	
	public VehicleObject getVehicleAt(int fieldX, int fieldZ){
		FieldReference ref = fields[fieldX][fieldZ];
		return ref != null ? ref.getVehicle() : null;
	}
	
	public FieldReference getField(Vector2ic location){
		return getField(location.x(), location.y());
	}
	
	public FieldReference getField(int unitX, int unitZ){
		return fields[unitX][unitZ];
	}
	
	public void onMouseClick(RushHour game){
		if(isFieldHovered()){
			FieldReference hoveredFieldRef = this.getField(hoveredField);
			if(hoveredFieldRef != null){
				movingVehicleRef = hoveredFieldRef;
				movingStartOffset = hoveredFieldRef.isHorizontal() ? game.yPlaneIntersection.x() - hoveredFieldRef.getVehicle().position.x
						: game.yPlaneIntersection.z() - hoveredFieldRef.getVehicle().position.z;
			}
		}
	}
	
	public void onMouseRelease(RushHour game){
		if(movingVehicleRef != null){
			FieldReference fieldRef = movingVehicleRef;
			if(fieldRef.isHorizontal()){
				int x = Math.round((fieldRef.getVehicle().position.x - RushHour.MAP_OFFSET.x() - fieldRef.getPositionXOffset())
						/ RushHour.UNIT_SIZE_X);
				int z = fieldRef.getLocation().y();
				setVehicleCoords(fieldRef, x, z);
				mapVehiclePosition(fieldRef, x, z);
			}else{
				int x = fieldRef.getLocation().x();
				int z = Math.round((fieldRef.getVehicle().position.z - RushHour.MAP_OFFSET.z() - fieldRef.getPositionZOffset())
						/ RushHour.UNIT_SIZE_Z);
				
				if(x == RushHour.MAP_EXIT_X && z < 0){
					game.gui.showView(new GUILevelDone());
				}else{
					setVehicleCoords(fieldRef, x, z);
					mapVehiclePosition(fieldRef, x, z);
				}
			}
			movingVehicleRef = null;
		}
	}
	
	public void onCursorMove(RushHour game, double cursorX, double cursorY){}
	
	public void onHover(RushHour game, int fieldX, int fieldZ){
		boolean wasHoveredBefore = isFieldHovered();
		hoveredField.set(fieldX, fieldZ);
		
		if(game.isDebug() && isFieldHovered()){
			System.out.println("Spielfeld: x=" + hoveredField.x + ", z=" + hoveredField.y);
		}
		
		if(!isVehicleMoving()){
			if(wasHoveredBefore){
				for(WorldObject wo : game.worldObjects){
					if(wo instanceof VehicleObject){
						((VehicleObject) wo).color[3] = NO_HOVER_VEHICLE_ALPHA;
					}
				}
			}
			
			if(isFieldHovered()){
				FieldReference field = getField(fieldX, fieldZ);
				if(field != null){
					field.getVehicle().color[3] = HOVER_VEHICLE_ALPHA;
				}
			}
		}
	}
	
	public boolean isFieldHovered(){
		return hoveredField.x != -1;
	}
	
}
