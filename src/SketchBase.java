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
 *  File given for SketchBase. 
 *  
 *  1. Modified to support Point3D object instead of Point2D. Also changed
 *     drawLine to DDA for simplicity.
 *  
 *  2. Implemented depth-buffer algorithm by comparing the z value of the
 *  current point and the value store in the depthBuffer. Draw whichever
 *  is closer to camera.
 *  
 *  3. Added drawLine and drawTriangle for phong shading.
 *  
 */

//****************************************************************************
// SketchBase.  
//****************************************************************************
// Comments : 
//   Subroutines to manage and draw points, lines an triangles
//
// History :
//   Aug 2014 Created by Jianming Zhang (jimmie33@gmail.com) based on code by
//   Stan Sclaroff (from CS480 '06 poly.c)

import java.awt.image.BufferedImage;
import java.util.*;

public class SketchBase 
{
	public SketchBase()
	{
		// deliberately left blank
	}
	
	/**********************************************************************
	 * Draws a point.
	 * This is achieved by changing the color of the buffer at the location
	 * corresponding to the point. 
	 * 
	 * @param buff
	 *          Buffer object.
	 * @param p
	 *          Point to be drawn.
	 */
	public static void drawPoint(BufferedImage buff, Point3D p, float[][] depthBuffer)
	{
		// Implement depth buffer for rendering points
		if(p.x>=0 && p.x<buff.getWidth() && p.y>=0 && p.y < buff.getHeight()
				&& depthBuffer[(int)p.x][(int)p.y]<p.z) {
			depthBuffer[(int)p.x][(int)p.y]=p.z;
			buff.setRGB((int)p.x, (int)(buff.getHeight()-p.y-1), p.c.getRGB_int());	
		}		
	}
	
	/**********************************************************************
	 * Draws a line segment using Bresenham's algorithm, linearly 
	 * interpolating RGB color along line segment.
	 * This method only uses integer arithmetic.
	 * 
	 * @param buff
	 *          Buffer object.
	 * @param p1
	 *          First given endpoint of the line.
	 * @param p2
	 *          Second given endpoint of the line.
	 */
	public static void drawLine(BufferedImage buff, Point3D p1, Point3D p2, float[][] depthBuffer) {
		// draw line that supports float and Point3D with DDA
		// color interpolation
		
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		float dz = p2.z - p1.z;
		float steps;

		float x = p1.x, y = p1.y, z = p1.z;
		Point3D ret = new Point3D(p1.x, p1.y, p1.z, p1.c);
		
		if (Math.abs(dx) > Math.abs(dy)) {
			steps = Math.abs(dx);
		} 
		else {
			steps = Math.abs(dy);
		}

		float xInc = dx/steps;
		float yInc = dy/steps;
		float zInc = dz/steps;
		
		float dr = p2.c.r - p1.c.r;
		float dg = p2.c.g - p1.c.g;
		float db = p2.c.b - p1.c.b;

		float rInc = dr/steps;
		float gInc = dg/steps;
		float bInc = db/steps;
		
		float r = ret.c.r;
		float g = ret.c.g;
		float b = ret.c.b;

		drawPoint(buff, ret, depthBuffer);
		
		for (int k=0; k<steps; k++) {
			
			x += xInc;
			y += yInc;
			z += zInc;
			r += rInc;
			g += gInc;
			b += bInc;

			ret.x = Math.round(x);
			ret.y = Math.round(y);
			ret.z = Math.round(z);
			ret.c = new ColorType(r, g, b);

			drawPoint(buff, ret, depthBuffer);
		}

	}
	
	public static void drawLineNormal(BufferedImage buff, Point3D p1, Point3D p2,
			float[][] depthBuffer, LightCombined light, Material mat, Point3D v) {
		// draw line that supports float and Point3D with DDA
		// normal interpolation
		
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		float dz = p2.z - p1.z;
		float steps;
		
		float x = p1.x, y = p1.y, z = p1.z;
		Point3D pk = new Point3D(p1.x, p1.y, p1.z, p1.c);
		pk.n = new Point3D(p1.n);

		if (Math.abs(dx) > Math.abs(dy)) {
			steps = Math.abs(dx);
		} 
		else {
			steps = Math.abs(dy);
		}

		float xInc = dx/steps;
		float yInc = dy/steps;
		float zInc = dz/steps;

		float dx_n = p2.n.x - p1.n.x;
		float dy_n = p2.n.y - p1.n.y;
		float dz_n = p2.n.z - p1.n.z;

		float nxInc = dx_n/steps;
		float nyInc = dy_n/steps;
		float nzInc = dz_n/steps;
		
		Point3D temp_n = new Point3D(pk.n);
		pk.c = light.applyLight(mat, v, temp_n, pk);

		drawPoint(buff, pk, depthBuffer);
		
		for (int k = 0; k < steps; k++) {
			
			x += xInc;
			y += yInc;
			z += zInc;
			temp_n.x += nxInc;
			temp_n.y += nyInc;
			temp_n.z += nzInc;

			pk.x = Math.round(x);
			pk.y = Math.round(y);
			pk.z = Math.round(z);
			
			ColorType color = light.applyLight(mat, v, temp_n, pk);
			pk.c = new ColorType(Math.round(color.getR_int()), Math.round(color.getG_int()), Math.round(color.getB_int()));
			
			drawPoint(buff, pk, depthBuffer);
		}

	}

	/**********************************************************************
	 * Draws a filled triangle. 
	 * The triangle may be filled using flat fill or smooth fill. 
	 * This routine fills columns of pixels within the left-hand part, 
	 * and then the right-hand part of the triangle.
	 *   
	 *	                         *
	 *	                        /|\
	 *	                       / | \
	 *	                      /  |  \
	 *	                     *---|---*
	 *	            left-hand       right-hand
	 *	              part             part
	 *
	 * @param buff
	 *          Buffer object.
	 * @param p1
	 *          First given vertex of the triangle.
	 * @param p2
	 *          Second given vertex of the triangle.
	 * @param p3
	 *          Third given vertex of the triangle.
	 * @param do_smooth
	 *          Flag indicating whether flat fill or smooth fill should be used.                   
	 */
	public static void drawTriangle(BufferedImage buff, Point3D p1, Point3D p2, Point3D p3, boolean do_smooth, float[][] depthBuff) {

		// sort the triangle vertices by ascending x value
		Point3D p[] = sortTriangleVerts(p1, p2, p3);
		
		int x;
		float y_a, y_b, z_a, z_b;
		float dy_a, dy_b, dz_a, dz_b;
		float dr_a=0, dg_a=0, db_a=0, dr_b=0, dg_b=0, db_b=0;

		Point3D side_a = new Point3D(p[0]), side_b = new Point3D(p[0]);
		
		if (!do_smooth) 
		{
			side_a.c = new ColorType(p1.c);
			side_b.c = new ColorType(p1.c);
		}

		y_b = p[0].y;
		z_b = p[0].z;
		dy_b = ((float)(p[2].y - p[0].y))/(p[2].x - p[0].x);
		dz_b = ((float)(p[2].z - p[0].z))/(p[2].x - p[0].x);

		if (do_smooth) 
		{
			// calculate slopes in r, g, b for segment b
			dr_b = ((float)(p[2].c.r - p[0].c.r))/(p[2].x - p[0].x);
			dg_b = ((float)(p[2].c.g - p[0].c.g))/(p[2].x - p[0].x);
			db_b = ((float)(p[2].c.b - p[0].c.b))/(p[2].x - p[0].x);
		}
		
		// if there is a left-hand part to the triangle then fill it
		if (p[0].x != p[1].x) 
		{
			y_a = p[0].y;
			z_a = p[0].z;
			dy_a = ((float)(p[1].y - p[0].y))/(p[1].x - p[0].x);
			dz_a = ((float)(p[1].z - p[0].z))/(p[1].x - p[0].x);

			if (do_smooth) 
			{
				// calculate slopes in r, g, b for segment a
				dr_a = ((float)(p[1].c.r - p[0].c.r))/(p[1].x - p[0].x);
				dg_a = ((float)(p[1].c.g - p[0].c.g))/(p[1].x - p[0].x);
				db_a = ((float)(p[1].c.b - p[0].c.b))/(p[1].x - p[0].x);
			}

			// loop over the columns for left-hand part of triangle
			// filling from side a to side b of the span
			for (x =(int)p[0].x; x<p[1].x; ++x) 
			{
				drawLine(buff, side_a, side_b, depthBuff);

				++side_a.x;
				++side_b.x;
				y_a += dy_a;
				y_b += dy_b;
				z_a += dz_a;
				z_b += dz_b;
				side_a.y = (int)y_a;
				side_b.y = (int)y_b;
				side_a.z = (int)z_a;
				side_b.z = (int)z_b;
				if (do_smooth) 
				{
					side_a.c.r += dr_a;
					side_b.c.r += dr_b;
					side_a.c.g += dg_a;
					side_b.c.g += dg_b;
					side_a.c.b += db_a;
					side_b.c.b += db_b;
				}
			}
		}

		// there is no right-hand part of triangle
		if (p[1].x == p[2].x)
			return;

		// set up to fill the right-hand part of triangle
		// replace segment a
		side_a = new Point3D(p[1]);
		if (!do_smooth)
			side_a.c = new ColorType(p1.c);

		y_a = p[1].y;
		z_a = p[1].z;
		dy_a = ((float)(p[2].y - p[1].y))/(p[2].x - p[1].x);
		dz_a = ((float)(p[2].z - p[1].z))/(p[2].x - p[1].x);
		if (do_smooth)
		{
			// calculate slopes in r, g, b for replacement for segment a
			dr_a = ((float)(p[2].c.r - p[1].c.r))/(p[2].x - p[1].x);
			dg_a = ((float)(p[2].c.g - p[1].c.g))/(p[2].x - p[1].x);
			db_a = ((float)(p[2].c.b - p[1].c.b))/(p[2].x - p[1].x);
		}

		// loop over the columns for left-hand part of triangle
		// filling from side a to side b of the span
		for (x=(int)p[1].x; x<=p[2].x; ++x) 
		{
			drawLine(buff, side_a, side_b, depthBuff);

			++side_a.x;
			++side_b.x;
			y_a += dy_a;
			y_b += dy_b;
			z_a += dz_a;
			z_b += dz_b;
			side_a.y = (int) y_a;
			side_b.y = (int) y_b;
			side_a.z = (int) z_a;
			side_b.z = (int) z_b;
			if (do_smooth) {
				side_a.c.r += dr_a;
				side_b.c.r += dr_b;
				side_a.c.g += dg_a;
				side_b.c.g += dg_b;
				side_a.c.b += db_a;
				side_b.c.b += db_b;
			}
		}
	}
	
	public static void drawTriangleNormal(BufferedImage buff, Point3D p1, Point3D p2, Point3D p3, 
			float[][] depthBuffer, Point3D n1, Point3D n2, Point3D n3, LightCombined light, Material mat, Point3D v) {
		
		// add normal to each vertex and sort the triangle vertices by ascending x value
		p1.n = n1;
		p2.n = n2;
		p3.n = n3;
		Point3D p[] = sortTriangleVerts(p1, p2, p3);
		
		int x;
		float y_a, y_b, dy_a, dy_b;
		float z_a, z_b, dz_a, dz_b;
		float dnx_a = 0, dny_a = 0, dnz_a = 0, dnx_b = 0, dny_b = 0, dnz_b = 0;

		Point3D side_a = new Point3D(p[0]), side_b = new Point3D(p[0]);
		
		side_a.n = new Point3D(p[0].n);
		side_b.n = new Point3D(p[0].n);

		y_b = p[0].y;
		z_b = p[0].z;
		dy_b = ((float) (p[2].y - p[0].y)) / (p[2].x - p[0].x);
		dz_b = ((float) (p[2].z - p[0].z)) / (p[2].x - p[0].x);

		// calculate slopes in r, g, b for segment b
		dnx_b = ((float) (p[2].n.x - p[0].n.x)) / (p[2].x - p[0].x);
		dny_b = ((float) (p[2].n.y - p[0].n.y)) / (p[2].x - p[0].x);
		dnz_b = ((float) (p[2].n.z - p[0].n.z)) / (p[2].x - p[0].x);

		// if there is a left-hand part to the triangle then fill it
		if (p[0].x != p[1].x) {
			y_a = p[0].y;
			z_a = p[0].z;
			dy_a = ((float) (p[1].y - p[0].y)) / (p[1].x - p[0].x);
			dz_a = ((float) (p[1].z - p[0].z)) / (p[1].x - p[0].x);

			dnx_a = ((float) (p[1].n.x - p[0].n.x)) / (p[1].x - p[0].x);
			dny_a = ((float) (p[1].n.y - p[0].n.y)) / (p[1].x - p[0].x);
			dnz_a = ((float) (p[1].n.z - p[0].n.z)) / (p[1].x - p[0].x);

			// loop over the columns for left-hand part of triangle
			// filling from side a to side b of the span
			for (x = (int)p[0].x; x < p[1].x; ++x) {
				drawLineNormal(buff, side_a, side_b, depthBuffer, light, mat, v);

				++side_a.x;
				++side_b.x;
				y_a += dy_a;
				y_b += dy_b;
				z_a += dz_a;
				z_b += dz_b;
				side_a.y = (int) y_a;
				side_b.y = (int) y_b;
				side_a.z = (int) z_a;
				side_b.z = (int) z_b;

				side_a.n.x += dnx_a;
				side_b.n.x += dnx_b;
				side_a.n.y += dny_a;
				side_b.n.y += dny_b;
				side_a.n.z += dnz_a;
				side_b.n.z += dnz_b;
						
				side_a.c = light.applyLight(mat, v, side_a.n, side_a);
				side_b.c = light.applyLight(mat, v, side_b.n, side_b);
			}
		}

		// there is no right-hand part of triangle
		if (p[1].x == p[2].x)
			return;

		// set up to fill the right-hand part of triangle
		// replace segment a
		side_a = new Point3D(p[1]);
		side_a.n = new Point3D(p[1].n);

		y_a = p[1].y;
		z_a = p[1].z;
		dy_a = ((float) (p[2].y - p[1].y)) / (p[2].x - p[1].x);
		dz_a = ((float) (p[2].z - p[1].z)) / (p[2].x - p[1].x);
		
		// calculate slopes in r, g, b for replacement for segment a
		dnx_a = ((float) (p[2].n.x - p[1].n.x)) / (p[2].x - p[1].x);
		dny_a = ((float) (p[2].n.y - p[1].n.y)) / (p[2].x - p[1].x);
		dnz_a = ((float) (p[2].n.z - p[1].n.z)) / (p[2].x - p[1].x);

		// loop over the columns for right-hand part of triangle
		// filling from side a to side b of the span
		for (x = (int)p[1].x; x <= p[2].x; ++x) {
			drawLineNormal(buff, side_a, side_b, depthBuffer, light, mat, v);

			++side_a.x;
			++side_b.x;
			y_a += dy_a;
			y_b += dy_b;
			z_a += dz_a;
			z_b += dz_b;
			side_a.y = (int) y_a;
			side_b.y = (int) y_b;
			side_a.z = (int) z_a;
			side_b.z = (int) z_b;
			
			side_a.n.x += dnx_a;
			side_b.n.x += dnx_b;
			side_a.n.y += dny_a;
			side_b.n.y += dny_b;
			side_a.n.z += dnz_a;
			side_b.n.z += dnz_b;
					
			side_a.c = light.applyLight(mat, v, side_a.n, side_a);
			side_b.c = light.applyLight(mat, v, side_b.n, side_b);
		}
	}
	
	/**********************************************************************
	 * Helper function to bubble sort triangle vertices by ascending x value.
	 * 
	 * @param p1
	 *          First given vertex of the triangle.
	 * @param p2
	 *          Second given vertex of the triangle.
	 * @param p3
	 *          Third given vertex of the triangle.
	 * @return 
	 *          Array of 3 points, sorted by ascending x value.
	 */
	private static Point3D[] sortTriangleVerts(Point3D p1, Point3D p2, Point3D p3)
	{
	    Point3D pts[] = {p1, p2, p3};
	    Point3D tmp;
	    int j=0;
	    boolean swapped = true;
	         
	    while (swapped) 
	    {
	    	swapped = false;
	    	j++;
	    	for (int i = 0; i < 3 - j; i++) 
	    	{                                       
	    		if (pts[i].x > pts[i + 1].x) 
	    		{                          
	    			tmp = pts[i];
	    			pts[i] = pts[i + 1];
	    			pts[i + 1] = tmp;
	    			swapped = true;
	    		}
	    	}                
	    }
	    return(pts);
	}
}
