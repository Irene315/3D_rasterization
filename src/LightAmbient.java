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
 *  LightAmbient object. Only consider ambient illumination model.
 *  
 */

public class LightAmbient extends LightCombined
{
	
	public LightAmbient(ColorType _c, Point3D _direction)
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
		
		if(mat.ambient)
		{
			
			res.r += (float)(mat.ka.r*color.r);
			res.g += (float)(mat.ka.g*color.g);
			res.b += (float)(mat.ka.b*color.b);

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
