/*
 * 
 * Name: Xinyuan Zhang
 * Class: CS480
 * 
 * Assignment 4
 * Due: 2019/12/10
 * Problem Number: /
 * 
 * Description: 
 *  LightCombined object. Store all lights defined in an arraylist.
 *  
 */

import java.util.*;

public class LightCombined extends Light {
	public ArrayList<LightCombined> allLights;
	public boolean light_on;
	
	public LightCombined() {
		this.allLights = new ArrayList<LightCombined>();
		this.light_on = true;
	}
	
	public void addLight(LightCombined l) {
		allLights.add(l);
	}
	
	public void removeLight(LightCombined l) {
		allLights.remove(l);
	}
	
	public ColorType applyLight(Material mat, Point3D v, Point3D n, Point3D p) {
		ColorType res = new ColorType();
		ColorType temp = new ColorType();
		
		// loop through all lights in array to show accumulated effects
		for (LightCombined l: allLights) {
			
			temp = l.applyLight(mat, v, n, p);
			res.r += temp.r;
			res.g += temp.g;
			res.b += temp.b;
			
		}
		
		res.clamp();
		
		return(res);
	}

}
