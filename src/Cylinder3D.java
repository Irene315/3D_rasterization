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
 *  Cylinder3D object with 1 mesh. 
 *  
 */

public class Cylinder3D {
	
	private Point3D center;
	private float rx, ry;
	private int stacks,slices,u;
	public Mesh3D mesh;
	
	public Cylinder3D(float _x, float _y, float _z, float _rx, float _ry, int _stacks, int _slices, int _u)
	{
		center = new Point3D(_x,_y,_z);
		rx = _rx;
		ry = _ry;
		u = _u;
		stacks = _stacks;
		slices = _slices;
		initMesh();
	}
	
	public void set_center(float _x, float _y, float _z)
	{
		center.x=_x;
		center.y=_y;
		center.z=_z;
		fillMesh();  // update the triangle mesh
	}
	
	public void set_radius(float _rx, float _ry)
	{
		rx = _rx;
		ry = _ry;
		fillMesh(); // update the triangle mesh
	}
	
	public void set_stacks(int _stacks)
	{
		stacks = _stacks;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public void set_slices(int _slices)
	{
		slices = _slices;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public int get_n()
	{
		return slices;
	}
	
	public int get_m()
	{
		return stacks;
	}

	private void initMesh()
	{
		mesh = new Mesh3D(stacks, slices);
		fillMesh();  // set the mesh vertices and normals
	}
		
	// fill the triangle mesh vertices and normals
	// using the current parameters for the cylinder
	private void fillMesh()
	{
		int i, j;
		float theta, phi;
		float d_theta = (float)(2*Math.PI)/(float)(slices-1);
		float d_phi = u*(float)(Math.PI)/(float)(stacks-1);
		float cos_theta, sin_theta;
		float cos_phi, sin_phi;
		Point3D du = new Point3D();
		Point3D dv = new Point3D();
		
		for(i=0, theta=(float)(-Math.PI); i<slices; i++, theta+=d_theta) {
			cos_theta = (float)Math.cos(theta);
			sin_theta = (float)Math.sin(theta);
			
			for(j=0, phi=(float)(-0.5*Math.PI); j<stacks; j++, phi+=d_phi) {
				cos_phi = (float)Math.cos(phi);
				sin_phi = (float)Math.sin(phi);
				
				mesh.v[i][j].x = center.x+rx*cos_theta;
				mesh.v[i][j].y = center.y+ry*sin_theta;
				mesh.v[i][j].z = center.z+phi;
				
				du.x = -rx*sin_theta;
				du.y = ry*cos_theta;
				du.z = 0;
				
				dv.x = 0;
				dv.y = 0;
				dv.z = 1;
				
				mesh.n[i][j] = du.crossProduct(dv).normalize();
			}
			
			// fill the top and bottom caps
			mesh.v[i][0] = new Point3D(center.x, center.y, center.z);
			mesh.n[i][0] = new Point3D(0, 0, -1);
			mesh.v[i][slices-1] = new Point3D(center.x, center.y, center.z+u*(float)(Math.PI));
			mesh.n[i][slices-1] = new Point3D(0, 0, 1);
			
		}		
	}
}
