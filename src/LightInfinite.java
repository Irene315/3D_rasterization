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
 *  File given for LightInfinte object. 
 *  
 *  Added boolean condition light_on to decide whether light should be casted.
 *  
 */

//****************************************************************************
//       Infinite light source class
//****************************************************************************
// History :
//   Nov 6, 2014 Created by Stan Sclaroff
//
import java.util.Random;

public class LightInfinite extends LightCombined
{
	private Random rnd=new Random();
	
	public LightInfinite(ColorType _c, Point3D _direction)
	{
		color = new ColorType(_c);
		direction = new Point3D(_direction);
		light_on = true;
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	// v: viewing vector
	// n: face normal
	public ColorType applyLight(Material mat, Point3D v, Point3D n, Point3D p){
		if (light_on) {
		ColorType res = new ColorType();
		
		// ****************Implement Code here*******************//
		// dot product between light direction and normal
		// light must be facing in the positive direction
		// dot <= 0.0 implies this light is facing away (not toward) this point
		// therefore, light only contributes if dot > 0.0 
		double dot = direction.dotProduct(n);
		if(dot>0.0)
		{
			// diffuse component
			if(mat.diffuse)
			{
				res.r += (float)(dot*mat.kd.r*color.r);
				res.g += (float)(dot*mat.kd.g*color.g);
				res.b += (float)(dot*mat.kd.b*color.b);
			}
			// specular component
			if(mat.specular)
			{
				Point3D r = direction.reflect(n);
				dot = r.dotProduct(v);
				if(dot>0.0)
				{
					res.r += (float)(dot*mat.ks.r*Math.pow(color.r,mat.ns));
					res.g += (float)(dot*mat.ks.g*Math.pow(color.g,mat.ns));
					res.b += (float)(dot*mat.ks.b*Math.pow(color.b,mat.ns));
				}
			}
			
			// clamp so that allowable maximum illumination level is not exceeded
			res.clamp();
		}
		return(res);
		}
		
		else {
			return new ColorType(0, 0, 0);
		}
	}


}