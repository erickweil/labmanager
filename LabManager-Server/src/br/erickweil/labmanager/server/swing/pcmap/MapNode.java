/*
 * Copyright (C) 2018 Erick Leonardo Weil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.erickweil.labmanager.server.swing.pcmap;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class MapNode {
    public double px;
    public double py;
    protected int ipx;
    protected int ipy;
    double dx;
    double dy;
    protected int idx;
    protected int idy;
    public boolean selected;
    
    public MapNode(double px,double py,double dx,double dy)
    {
        this.px = px;
        this.py = py;
        this.dx = dx;
        this.dy = dy;
    }
    
    
    public boolean contained(double bx1,double by1,double bx2, double by2)
    {
        double ax1 = px-dx/2;
        double ay1 = py-dy/2;
        double ax2 = px+dx/2;
        double ay2 = py+dy/2;
        
        return ax1 >= bx1 && ay1 >= by1 && ax1 <= bx2 && ay1 <= by2 &&
               ax2 <= bx2 && ay2 <= by2 && ax2 >= bx1 && ay2 >= by1
                ;
                
    }
    
    public boolean inside(double x,double y)
    {
        return x >= (px-dx/2) && x < (px+dx/2) && y >= (py-dy/2) && y < (py+dy/2);
    }
    
    public void draw(Graphics g,boolean hover,double grid,double camx,double camy)
    {
        transformDimensions(grid,camx,camy);
        if(!hover)
        {
            g.setColor(Color.GRAY);
            g.fillRect(ipx, ipy, idx, idy);
            g.setColor(Color.BLACK);
            g.drawRect(ipx, ipy, idx, idy);
        }
        else
        {
            g.setColor(Color.CYAN);
            g.fillRoundRect(ipx-idx/8, ipy-idy/8, idx+idx/4, idy+idy/4,idx/4,idy/4);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(ipx, ipy, idx, idy);
        }
    }
    
    protected void transformDimensions(double grid,double camx,double camy)
    {
        this.ipx = (int)(((px-camx)-(dx/2))*grid);
        this.ipy = (int)(((py-camy)-(dy/2))*grid);
        this.idx = (int)(dx*grid);
        this.idy = (int)(dy*grid);
    }
    
    protected String get_tooltip()
    {
        return "Nodo Genérico.";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.px) ^ (Double.doubleToLongBits(this.px) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.py) ^ (Double.doubleToLongBits(this.py) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.dx) ^ (Double.doubleToLongBits(this.dx) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.dy) ^ (Double.doubleToLongBits(this.dy) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapNode other = (MapNode) obj;
        if (Double.doubleToLongBits(this.px) != Double.doubleToLongBits(other.px)) {
            return false;
        }
        if (Double.doubleToLongBits(this.py) != Double.doubleToLongBits(other.py)) {
            return false;
        }
        if (Double.doubleToLongBits(this.dx) != Double.doubleToLongBits(other.dx)) {
            return false;
        }
        if (Double.doubleToLongBits(this.dy) != Double.doubleToLongBits(other.dy)) {
            return false;
        }
        return true;
    }
    
    
}
