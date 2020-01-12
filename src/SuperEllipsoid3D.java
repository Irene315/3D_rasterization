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
 *  SuperEllipsoid3D object with 1 mesh. 
 *  Reference: https://en.wikipedia.org/wiki/Superellipsoid
 *  
 */

public class SuperEllipsoid3D {
	
	private Point3D center;
	private float rx, ry, rz;
	private float n, t;
	private int stacks,slices;
	public Mesh3D mesh;
	
	public SuperEllipsoid3D(float _x, float _y, float _z, float _rx, float _ry, float _rz, float _n, float _t, int _stacks, int _slices)
	{
		center = new Point3D(_x,_y,_z);
		rx = _rx;
		ry = _ry;
		rz = _rz;
		n = _n;
		t = _t;
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
	
	public void set_radius(float _rx, float _ry, float _rz)
	{
		rx = _rx;
		ry = _ry;
		rz = _rz;
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
	// using the current parameters for the sphere
	private void fillMesh()
	{
		// ****************Implement Code here*******************//
		int i, j;
		float theta, phi;
		float d_theta = (float)(2*Math.PI)/(float)(stacks-1);
		float d_phi = (float)(Math.PI)/(float)(slices-1);
		float cos_theta, sin_theta;
		float cos_phi, sin_phi;
		
		for(i=0, theta=(float)(-Math.PI); i<stacks; i++, theta+=d_theta) {
			cos_theta = (float)Math.cos(theta);
			sin_theta = (float)Math.sin(theta);
			
			for(j=0, phi=(float)(-0.5*Math.PI); j<slices; j++, phi+=d_phi) {
				cos_phi = (float)Math.cos(phi);
				sin_phi = (float)Math.sin(phi);

				mesh.v[i][j].x=center.x+rx*Math.signum(cos_phi)*((float)Math.pow(Math.abs(cos_phi), n))
						*Math.signum(cos_theta)*((float) Math.pow(Math.abs(cos_theta), t));
				mesh.v[i][j].y=center.y+ry*Math.signum(cos_phi)*((float)Math.pow(Math.abs(cos_phi), n))
						*Math.signum(sin_theta)*((float) Math.pow(Math.abs(sin_theta), t));
				mesh.v[i][j].z=center.z+rz*Math.signum(sin_phi) * ((float) Math.pow(Math.abs(sin_phi), n));
				
				mesh.n[i][j].x = cos_phi*cos_theta;
				mesh.n[i][j].y = cos_phi*sin_theta;
				mesh.n[i][j].z = sin_phi;

			}
			
			
			
		}		
	}
}
