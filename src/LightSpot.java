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
 *  LightSpot object. Added radical and angular attenuation to LightPoint object.
 *  
 */

import java.util.Random;

public class LightSpot extends LightCombined
{
	private Random rnd=new Random();
	private float a0,a1,a2,angle;
	public Boolean radical = false;
	public Boolean angular = false;
	
	public LightSpot(ColorType _c, Point3D _direction, Point3D _position)
	{
		color = new ColorType(_c);
		direction = new Point3D(_direction);
		position = new Point3D(_position); 
		a0 = 1;
		a1 = a2 = 0.0001f;
		angle = 1;
		light_on = true;
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	// v: viewing vector
	// n: face normal
	// s: pixel position to render
	public ColorType applyLight(Material mat, Point3D v, Point3D n, Point3D s){
		if (light_on) {
		ColorType res = new ColorType();
		
		// point light takes light position into consideration
		// l = norm(pl-ps)
		Point3D light = position.minus(s).normalize();
		double dot = light.dotProduct(n);
		
		// dot product between light direction and normal
		// light must be facing in the positive direction
		// dot <= 0.0 implies this light is facing away (not toward) this point
		// therefore, light only contributes if dot > 0.0 
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
			
			// radical attenuation
			if(radical) {
				float d = (float)Math.sqrt(Math.pow(light.x,2)+Math.pow(light.y,2)+Math.pow(light.z,2));
				res.r *= 1/(a0+a1*d+a2*(float)Math.pow(d,2));
				res.g *= 1/(a0+a1*d+a2*(float)Math.pow(d,2));
				res.b *= 1/(a0+a1*d+a2*(float)Math.pow(d,2));
			}
			
			// radical attenuation
			if(angular) {
				dot = light.dotProduct(direction);
				if (dot < Math.cos(Math.PI/4)) {
					res.r *= Math.pow(dot,angle);
					res.g *= Math.pow(dot,angle);
					res.b *= Math.pow(dot,angle);
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