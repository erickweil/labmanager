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

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labmanager.server.swing.ClientStatusManager;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientData;
import br.erickweil.labmanager.server.swing.ColorControler;
import br.erickweil.labmanager.server.swing.PopupAcaoCliente;
import br.erickweil.labmanager.server.swing.SwingServer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableModel;

/**
 *
 * @author Usuario
 */
public class MapaCanvas extends JComponent implements MouseListener,MouseMotionListener,MouseWheelListener {

    

    private enum MouseAction {
    iddle,
    selecting,
    spanning,
    dragging,
    moving_background,
    scaling_background
    };
    
    private enum Tool {
        select,
        span
    }
    
    public static final int SIDEMENU_SELECT = 0;
    public static final int SIDEMENU_SPAN = 1;
    public static final int SIDEMENU_DELETE = 2;
    
    
    //public boolean deleting = false;
    private MouseAction mouseAction;
    private Tool toolSelected;
    private double camerax;
    private double cameray;
    private double umx;
    private double umy;
    private double mx;
    private double my;
    private double m_nc_x;
    private double m_nc_y;
    private List<MapNode> nodes;
    private int dragging = -1;
    private double spanning_offx;
    private double spanning_offy;
    private double backmove_offx;
    private double backmove_offy;
    SwingServer swingWindow;
    public boolean changed;
    protected int grid_size;
    private int select_startx = 0;
    private int select_starty = 0;
    private int select_endx = 0;
    private int select_endy = 0;
    
    // background img
    public static File backgroundPath;
    private BufferedImage background;
    private double background_aspect;
    public static double background_scale=1.0;
    public static double background_posx;
    public static double background_posy;
    
    public MapaCanvas(SwingServer swingWindow)
    {
        mouseAction = MouseAction.iddle;
        toolSelected = Tool.select;
        this.swingWindow = swingWindow;
        // Initialize img here.
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        nodes = MapLoader.loadMap(swingWindow.clients_manager, "test");
        if(nodes == null) nodes = new ArrayList<>();
        updateBackground();
    }
    
    public void save()
    {
        System.out.println("saved mapa.");
        MapLoader.saveMap(nodes, "test");
        changed = false;
    }
     
    public void removeScreens()
    {
        for(int i=0;i<nodes.size();i++)
        {
            if(nodes.get(i) instanceof PC)
            {
                PC p = (PC)nodes.get(i);
                p.lastscreen = null;
            }
        }
    }
    
    public void setScreenPC(BufferedImage screen, String uuid)
    {
        for(int i=0;i<nodes.size();i++)
        {
            if(nodes.get(i) instanceof PC)
            {
                if(((PC)nodes.get(i)).uuid.equals(uuid))
                {
                    PC p = (PC)nodes.get(i);
                    p.lastscreen = screen;
                    return;
                }
            }
        }
    }
    
    public boolean isPCVisible(String uuid)    
    {
        for(int i=0;i<nodes.size();i++)
        {
            if(nodes.get(i) instanceof PC)
            {
                if(((PC)nodes.get(i)).uuid.equals(uuid))
                {
                    PC p = (PC)nodes.get(i);
                    
                    int width = this.getWidth();
                    int height = this.getHeight();
                    double dgrid = (double) grid_size;
                    double dwidth = (double)width / dgrid;
                    double dheight = (double)height / dgrid;

                    double bound_x1 = camerax-1.0;
                    double bound_y1 = cameray-1.0;
                    double bound_x2 = camerax+dwidth+1.0;
                    double bound_y2 = cameray+dheight+1.0;
                    if(p.contained(bound_x1, bound_y1, bound_x2, bound_y2))
                    {
                        return true;
                    } else return false;
                }
            }
        }
        return false;
    }
    
    public int getShowScreenWidth()    
    {
        grid_size = swingWindow.sliderGridSize.getValue();
        for(int i=0;i<nodes.size();i++)
        {
            if(nodes.get(i) instanceof PC)
            {
                return ((PC)nodes.get(i)).getScreenDisplayWidth(grid_size);
            }
        }
        return -1;
    }
    
    public void AddPC(String uuid)
    {
        for(int i=0;i<nodes.size();i++)
        {
            if(nodes.get(i) instanceof PC)
            {
                if(((PC)nodes.get(i)).uuid.equals(uuid))
                    return;
            }
        }
        Random r = new Random();
        changed = true;
        grid_size = swingWindow.sliderGridSize.getValue();
        nodes.add(new PC(swingWindow.clients_manager,uuid, r.nextInt(getWidth() / grid_size)+camerax, r.nextInt(getHeight() / grid_size)+cameray));
    }
    
    public void drawPC(Graphics g,int px,int py)
    {
        int b1 = 20;
        int b2 = 7;
        int w1 = 60;
        int h1 = 40;
        int gap = 5;
        int w2 = 40;
        int h2 = 10;
        g.setColor(Color.BLUE);
        g.fillRoundRect(px, py, w1, h1,b1,b1);
        g.setColor(Color.WHITE);
        g.fillRect(px+b2, py+b2, w1-b2*2, h1-b2*2);
        g.setColor(Color.BLUE);
        g.fillRoundRect((px + px+w1)/2 - w2/2, py+(h1+gap), w2, h2,b2,b2);
        g.setColor(Color.BLUE);
        g.fillRect((px + px+w1)/2 - 10/2, py+(h1), 10, gap);
    }
            
    public void update()
    {   
        this.repaint();
    }
    
    public void updateBackground()
    {
        try {
            if(backgroundPath == null)
            backgroundPath = new File("pcmap.png");
            background = ImageIO.read(backgroundPath);
            background_aspect = (double)background.getWidth()/(double)background.getHeight();
        } catch (IOException ex) {
            System.out.println("Erro ao abrir a imagem '"+backgroundPath.getAbsolutePath()+"'");
            ex.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        // Draws the image to the canvas
        //System.out.println(this.getWidth()+" "+this.getHeight());
        
        // infos
        grid_size = swingWindow.sliderGridSize.getValue();
        boolean show_grid = swingWindow.checkShowGrid.isSelected();
        
        int width = this.getWidth();
        int height = this.getHeight();
        
        //g.setFont(Font.getFont(Font.MONOSPACED));
        
        g.setColor(ColorControler.MAPA_FUNDO);
        g.fillRect(0, 0, width, height);
        
        if(background != null)
        {
            int backgroundx = PosToScreenx(background_posx);
            int backgroundy = PosToScreeny(background_posy);
            int backgroundw = PosToScreenx((background_posx + 20.0) * background_scale) - backgroundx;
            int backgroundh = (int)(backgroundw / background_aspect);
            //System.out.println(backgroundx+","+ backgroundy+","+ backgroundw+","+ backgroundh);
            g.drawImage(background, backgroundx, backgroundy, backgroundw, backgroundh, null);
        }
        
        //boolean snap_on_grid = swingWindow.checkAlignOnGrid.isSelected();
        
        
        
        int offx = (int)((0-camerax)*grid_size)%grid_size;
        int offy = (int)((0-cameray)*grid_size)%grid_size;
        if(grid_size > 0 && show_grid)
        {
            g.setColor(ColorControler.MAPA_GRADE);
            int grid_x = width/grid_size;
            
            for(int x=0;x<=grid_x+1;x++)
            {
                g.drawLine(x*grid_size + offx, 0, x*grid_size + offx, height);
            }
            int grid_y = height/grid_size;
            for(int y=0;y<=grid_y+1;y++)
            {
                g.drawLine(0,y*grid_size + offy,width, y*grid_size + offy);
            }
        }
        
        //g.setColor(Color.BLACK);
        //g.drawRect(0, 0, this.getWidth()-1, this.getHeight()-1);
        double dgrid = (double) grid_size;
        double dwidth = (double)width / dgrid;
        double dheight = (double)height / dgrid;
        
        double bound_x1 = camerax-1.0;
        double bound_y1 = cameray-1.0;
        double bound_x2 = camerax+dwidth+1.0;
        double bound_y2 = cameray+dheight+1.0;
        for(int i=0;i<nodes.size();i++)
        {
            MapNode n = nodes.get(i);
            if(n.contained(bound_x1, bound_y1, bound_x2, bound_y2))
            n.draw(g,n.inside(mx, my),dgrid,camerax,cameray);
        }
        
        //selecao
        
        if(toolSelected == Tool.select && mouseAction == MouseAction.selecting)
        {
            int s1x = select_startx;
            int s1y = select_starty;
            int s2x = select_endx;
            int s2y = select_endy;
            if(s1x > s2x)
            {
                s1x = select_endx;
                s2x = select_startx;
            }
            if(s1y > s2y)
            {
                s1y = select_endy;
                s2y = select_starty;
            }
            g.setColor(ColorControler.MAPA_SELECFILL);
            g.fillRect( s1x,s1y,s2x - s1x, s2y - s1y);
            g.setColor(ColorControler.MAPA_SELECDRAW);
            g.drawRect( s1x,s1y,s2x - s1x, s2y - s1y);
        }
        
        // menus
        
        int menu_itemw = 32;
        int menu_cols = 1;
        int menu_rows = 3;
        int menuh = height/2 - (menu_itemw*menu_rows)/2;
        String[] menu_label = new String[] { "Sel","Span","Del"};
        g.setColor(ColorControler.MAPA_MENUFILL);
        g.fillRect(0, menuh, menu_itemw*menu_cols, menu_itemw*menu_rows);
        g.setColor(ColorControler.MAPA_MENUDRAW);
        g.drawRect(0, menuh, menu_itemw*menu_cols, menu_itemw*menu_rows);
        
        g.setColor(ColorControler.MAPA_TOOLTIPTEXT);
        for(int i=0;i<menu_rows;i++)
        {
            g.drawLine(0,menuh+menu_itemw*i,menu_itemw,menuh+menu_itemw*i);
            g.drawString(menu_label[i], 7, menuh + 18 + menu_itemw*i);
        }
        
        // eu sei o que estou fazendo com esses equals em double
        // eu sei o que estou fazendo com esses equals em double
        // eu sei o que estou fazendo com esses equals em double
        // sei?
        if(umx == mx && umy == my)
        {
            for(int i=0;i<nodes.size();i++)
            {
                MapNode n = nodes.get(i);
                if(n.inside(mx, my))
                {
                    drawTooltipBox(n,g);
                    break;
                }
            }
        }
        
        umx = mx;
        umy = my;
    }
    
    public int clickedItemOnMenu()
    {
        int height = this.getHeight();
        int menu_itemw = 32;
        int menu_cols = 1;
        int menu_rows = 3;
        int menuh = height/2 - (menu_itemw*menu_rows)/2;
        
        int sx = PosToScreenx(mx);
        int sy = PosToScreeny(my);
        for(int row=0;row<menu_rows;row++)
        {
            int mx1 = 0;
            int my1 = row*menu_itemw + menuh;
            int mx2 = menu_itemw;
            int my2 = (row+1)*menu_itemw + menuh;
            
            boolean inside = sx >= mx1 && sx < mx2 && sy >= my1 && sy < my2;
            if(inside)
            {
                return row;
            }
        }
        return -1;
    }

    public void drawTooltipBox(MapNode n,Graphics g)
    {
        String[] tooltipText = n.get_tooltip().split("\n");
        int greaterline = g.getFontMetrics().stringWidth(tooltipText[0]);
        for(int k = 1;k < tooltipText.length;k++)
        {
            int t = g.getFontMetrics().stringWidth(tooltipText[k]);
            if(t > greaterline)
            greaterline = t;
        }
        int screen_mx = PosToScreenx(mx);
        int screen_my = PosToScreeny(my);
        int chh = g.getFontMetrics().getHeight();
        g.setColor(ColorControler.MAPA_TOOLTIPFILL);
        g.fillRect(
                screen_mx, 
                screen_my+20,
                greaterline +20, 
                tooltipText.length*chh+10);
        g.setColor(ColorControler.MAPA_TOOLTIPDRAW);
        g.drawRect(
                screen_mx, 
                screen_my+20,
                greaterline +20, 
                tooltipText.length*chh+10);
        g.setColor(ColorControler.MAPA_TOOLTIPTEXT);
        for(int k=0;k < tooltipText.length;k++)
        {
            g.drawString(tooltipText[k], screen_mx + 10, screen_my + 40 + k*chh);
        }   
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        //
    }

    // ... other MouseListener methods ... //

    @Override
    public void mousePressed(MouseEvent e) {
       transformMouse(e.getX(),e.getY());
       //if( maybeShowPopup(e) ) return;
       
       if( SwingUtilities.isRightMouseButton(e))
       {
            spanning_offx = mx;
            spanning_offy = my;
       }
       
       if( !SwingUtilities.isLeftMouseButton(e)) return;
       
       int menuItem = clickedItemOnMenu();
       if( menuItem != -1)
       {
           switch(menuItem)
           {
               case SIDEMENU_SELECT:
                   this.toolSelected = Tool.select;
                   this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
               break;
               case SIDEMENU_SPAN:
                   this.toolSelected = Tool.span;
                   this.setCursor(new Cursor(Cursor.HAND_CURSOR));
               break;
           }
           System.out.println("Clicked on:"+menuItem);
           return;
       }
       
        switch(mouseAction)
        {
            case iddle:
                for(int i=0;i<nodes.size();i++)
                {
                    if(nodes.get(i).inside(mx, my))
                    {
                        dragging = i;
                        break;
                    }
                }
                if(dragging == -1)
                {
                    if(toolSelected == Tool.span)
                    {
                        mouseAction = MouseAction.spanning;
                        spanning_offx = mx;
                        spanning_offy = my;
                    }
                    else if(toolSelected == Tool.select)
                    {
                        mouseAction = MouseAction.selecting;
                        select_startx = e.getX();
                        select_starty = e.getY();
                        select_endx = select_startx;
                        select_endy = select_starty;
                    }
                }
                else
                {
                    mouseAction = MouseAction.dragging;
                }
            break;
            case dragging:
                System.err.println("ESTADO INCONSISTENTE: pressionando mouse while dragging");
                mouseAction = MouseAction.iddle;
            break;
            case spanning:
                System.err.println("ESTADO INCONSISTENTE: pressionando mouse while spanning");
                mouseAction = MouseAction.iddle;
            break;
            case moving_background:
                spanning_offx = mx;
                spanning_offy = my;
                backmove_offx = background_posx;
                backmove_offy = background_posy;
            break;
            case scaling_background:
                spanning_offx = mx;
                spanning_offy = my;
                backmove_offx = background_scale;
            break;
        }

        
       //if(deleting && dragging != -1)
       //{
       //    if( nodes.get(dragging) instanceof PC)
       //    {
       //     PC pcClicked = (PC) nodes.get(dragging);       
       //     ClientStatusManager.ClientData clientClicked = swingWindow.clients_manager.clients_uuidmap.get(pcClicked.uuid);
       //     if(clientClicked == null)
       //     {
       //         System.out.println("removed");
       //         nodes.remove(dragging);
       //         dragging = -1;
       //     }
       //     
       //    }
       //}
       

       
       
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        transformMouse(e.getX(),e.getY());        
       if( SwingUtilities.isRightMouseButton(e))
       {
            if(mouseAction == MouseAction.spanning)
            {
                mouseAction = MouseAction.iddle;
                return;
            }
       }
       
       if( maybeShowPopup(e) ) return;
       
       if( !SwingUtilities.isLeftMouseButton(e)) return;
        
        switch(mouseAction)
        {
            case iddle:
                
            break;
            case dragging:
                mouseAction = MouseAction.iddle;
                if(swingWindow.checkAlignOnGrid.isSelected())
                {
                    changed = true;
                    MapNode n = nodes.get(dragging);
                    
                    TryAlignOnGrid(n.px,n.py,dragging);
                    if(n.selected)
                    {
                        for(int i=0;i<nodes.size();i++)
                        {
                            if(nodes.get(i).selected && i != dragging)
                            {
                                TryAlignOnGrid(nodes.get(i).px,nodes.get(i).py,i);
                            }
                        }
                    }
                    update();
                    //double newpx = Math.round(mx);
                    //double newpy = Math.round(my);

                    // nao alinhar na grade se estiver no mesmo espaço outro pc
                    //boolean overlap = false;
                    //for(int i=0;i<nodes.size();i++)
                    //{
                    //    if(nodes.get(i).inside(newpx, newpy) && i != dragging)
                    //    {
                    //        overlap = true;
                    //        break;
                    //    }
                    //}

                    //if(!overlap)
                    //{
                    //    n.px = newpx;
                    //    n.py = newpy;
                    //    update();
                    //}
                }
                
                dragging = -1;
            break;
            case spanning:
                mouseAction = MouseAction.iddle;
            break;
            case moving_background:
                swingWindow.prefs.setChanged();
                mouseAction = MouseAction.iddle;
            break;
            case scaling_background:
                swingWindow.prefs.setChanged();
                mouseAction = MouseAction.iddle;
            break;
            case selecting:
                selectPcs(e.isShiftDown()||e.isControlDown(),e.isAltDown());
                mouseAction = MouseAction.iddle;
            break;
        }
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        transformMouse(e.getX(),e.getY());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mx = -1;
        my = -1;
        
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
       if( SwingUtilities.isRightMouseButton(e))
       {
           transformMouse(e.getX(),e.getY());
            double d = (spanning_offx - m_nc_x)*(spanning_offx - m_nc_x)+
                       (spanning_offy - m_nc_y)*(spanning_offy - m_nc_y);
            if( d > 1.0)
            {
                
                mouseAction = MouseAction.spanning;
            }
            if(mouseAction == MouseAction.spanning)
            {
                
                camerax = spanning_offx - m_nc_x;
                cameray = spanning_offy - m_nc_y;
                //System.out.println(mx+","+my);
                update();
                return;
            }
       }
       
       if( !SwingUtilities.isLeftMouseButton(e)) return;
        switch(mouseAction)
        {
            case iddle:
                
            break;
            case dragging:
                transformMouse(e.getX(),e.getY());
                changed = true;
                MapNode n = nodes.get(dragging);

                double offx = mx - n.px;
                double offy = my - n.py;
                
                n.px += offx;
                n.py += offy;
                
                if(n.selected)
                {
                    for(int i=0;i<nodes.size();i++)
                    {
                        if(nodes.get(i).selected && i != dragging)
                        {
                            nodes.get(i).px += offx;
                            nodes.get(i).py += offy;
                        }
                    }
                }

                update();
            break;
            case spanning:
                transformMouse(e.getX(),e.getY());
                camerax = spanning_offx - m_nc_x;
                cameray = spanning_offy - m_nc_y;
                //System.out.println(mx+","+my);
                update();
            break;
            case moving_background:
                transformMouse(e.getX(),e.getY());
                background_posx = backmove_offx - (spanning_offx - mx);
                background_posy = backmove_offy - (spanning_offy - my);
                //System.out.println(mx+","+my);
                update();
            break;
            case scaling_background:
                transformMouse(e.getX(),e.getY());
                background_scale = backmove_offx *  (1.0 - ((spanning_offx - mx) / 20.0));
                //System.out.println(mx+","+my);
                update();
            break;
            case selecting:
                select_endx = e.getX();
                select_endy = e.getY();
                selectPcs(e.isShiftDown()||e.isControlDown(),e.isAltDown());
                //System.out.println(mx+","+my);
                update();
            break;
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        //System.out.println("Scroll Wheel: "+e.getWheelRotation());
        //System.out.println(e.getScrollType()== MouseWheelEvent.WHEEL_UNIT_SCROLL ? 
        //        "Unit:"+e.getUnitsToScroll() : 
        //        "Block:"+e.getWheelRotation());
        //System.out.println("precise:"+e.getPreciseWheelRotation());
        transformMouse(e.getX(),e.getY());
        
        double prev_mx = this.mx;
        double prev_my = this.my;
        
        float notches = (float)e.getPreciseWheelRotation() * -5.0f;
        
        int newGridSize = (int)((grid_size * 1.0f) * (1.0f + (notches/50.0f)));
        if(newGridSize == grid_size)
        {
            if(notches > 0) newGridSize++;
                    else newGridSize--;
        }
        swingWindow.sliderGridSize.setValue(newGridSize);
        grid_size = swingWindow.sliderGridSize.getValue();
        
        transformMouse(e.getX(),e.getY());
        
        this.camerax += prev_mx - this.mx;
        this.cameray += prev_my - this.my;
        
        
        //System.out.println(newGridSize+":"+grid_size);
        
        
        
        update();
    }
    
    private void selectPcs(boolean shiftdown,boolean altdown)
    {
        double s1x = screenToPosx(select_startx);
        double s1y = screenToPosy(select_starty);
        double s2x = screenToPosx(select_endx);
        double s2y = screenToPosy(select_endy);
        if(s1x > s2x)
        {
            double t = s1x;
            s1x = s2x;
            s2x = t;
        }
        if(s1y > s2y)
        {
            double t = s1y;
            s1y = s2y;
            s2y = t;
        }
        
        //System.out.println(s1x+","+s1y+" "+s2x+","+s2y);
        for(MapNode n : nodes)
        {
            if(n.contained(s1x, s1y, s2x, s2y))
            {
                n.selected = !altdown;
            }
            else
            {
                if(!shiftdown && !altdown)
                {
                    n.selected = false;
                }
            }
        }
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        transformMouse(e.getX(),e.getY());
    }

    private double screenToPosx(int c)
    {
        double nc = ((double)c)/(double)grid_size;
        return nc + camerax;
    }
    private double screenToPosy(int c)
    {
        double nc = ((double)c)/(double)grid_size;
        return nc + cameray;
    }
    private int PosToScreenx(double c)
    {
        double nc = c - camerax;
        return (int)(nc * grid_size);
    }
    private int PosToScreeny(double c)
    {
        double nc = c - cameray;
        return (int)(nc * grid_size);
    }
    
    private void TryAlignOnGrid(double px,double py,int node)
    {
        double newpx = Math.round(px);
        double newpy = Math.round(py);

        // nao alinhar na grade se estiver no mesmo espaço outro pc
        for(int i=0;i<nodes.size();i++)
        {
            if(nodes.get(i).inside(newpx, newpy) && i != node)
            {
                return;
            }
        }
        
        nodes.get(node).px = newpx;
        nodes.get(node).py = newpy;
    }
    
    
    private void transformMouse(int x,int y)
    {
        this.m_nc_x = ((double)x)/(double)grid_size;
        this.m_nc_y = ((double)y)/(double)grid_size;
        
        this.mx = m_nc_x + camerax;
        this.my = m_nc_y + cameray;
    }
    
    public void centerCamera()
    {
        grid_size = swingWindow.sliderGridSize.getValue();
        double median_x = 0.0;
        double median_y = 0.0;
        for(int i=0;i<nodes.size();i++)
        {
            median_x += nodes.get(i).px;
            median_y += nodes.get(i).py;
        }
        camerax = (median_x/(double)nodes.size()) - ((double)this.getWidth()/2.0)/ (double)grid_size;
        cameray = (median_y/(double)nodes.size()) - ((double)this.getHeight()/2.0)/ (double)grid_size;
    }

    private boolean maybeShowPopup(MouseEvent e) {
        transformMouse(e.getX(),e.getY());
        if (e.isPopupTrigger()) {
            MapNode nodeClicked = null;
            for(int i=0;i<nodes.size();i++)
            {
                if(nodes.get(i).inside(mx, my))
                {
                    nodeClicked = nodes.get(i);
                    break;
                }
            }
            if(nodeClicked == null || !(nodeClicked instanceof PC))
            {
                JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem menuItemEdit = new JMenuItem("Trocar Plano de Fundo");
                menuItemEdit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //FileDialog fileDialog = new FileDialog(swingWindow,"Escolha uma Imagem",FileDialog.LOAD);
                        //fileDialog.setVisible(true);
                        //if(fileDialog.getFile() != null)
                        //{
                        JFileChooser jfc;
                        jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                        int returnValue = jfc.showOpenDialog(null);
                        // int returnValue = jfc.showSaveDialog(null);

                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = jfc.getSelectedFile();

                            backgroundPath = selectedFile;
                            swingWindow.prefs.setChanged();
                            updateBackground();
                        }
                        
                    }
                });
                popupMenu.add(menuItemEdit);
                
                JMenuItem menuItemMove = new JMenuItem("Mover Plano de Fundo");
                menuItemMove.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mouseAction = MouseAction.moving_background;
                    }
                });
                popupMenu.add(menuItemMove);
                
                JMenuItem menuItemScale = new JMenuItem("Escalonar Plano de Fundo");
                menuItemScale.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mouseAction = MouseAction.scaling_background;
                    }
                });
                popupMenu.add(menuItemScale);
                
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
                return true;
            }
            else
            {
                PC pcClicked = (PC) nodeClicked;
                final ClientStatusManager.ClientData clientClicked = swingWindow.clients_manager.clients_uuidmap.get(pcClicked.uuid);
                //ClientData clientClicked = pcClicked.clientData;
                if (clientClicked != null) {
                    if(!pcClicked.selected)
                    {
                        System.out.println(clientClicked.thread_name + " " + clientClicked.user);
                        PopupAcaoCliente popup = new PopupAcaoCliente(swingWindow, clientClicked);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                        return true;
                    }
                    else
                    {
                        List<ClientData> allclients = new ArrayList<>();
                        for(MapNode n : nodes)
                        {
                            if(n.selected && n instanceof PC)
                            {
                                ClientData c = swingWindow.clients_manager.clients_uuidmap.get(((PC)n).uuid);
                                if(c != null) allclients.add(c);
                            }
                        }
                        ClientData[] allclients_arr = new ClientData[allclients.size()];
                        allclients_arr = allclients.toArray(allclients_arr);
                        System.out.println("Clicado em "+allclients_arr.length+" clientes");
                        PopupAcaoCliente popup = new PopupAcaoCliente(swingWindow, allclients_arr);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                        return true;
                    }
                }
                else
                {
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem menuItemEdit = new JMenuItem("Excluir");
                    final MapNode nodeClickedf = nodeClicked;
                    menuItemEdit.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(nodeClickedf.selected)
                            {
                                List<MapNode> toRemove = new ArrayList<>();
                                for(MapNode n : nodes)
                                {
                                    if(n.selected)toRemove.add(n);
                                }
                                for(MapNode n : toRemove)
                                {
                                    nodes.remove(n);
                                    changed = true;
                                }
                            }
                            else
                            {
                                nodes.remove(nodeClickedf);
                                changed = true;
                            }
                            
                        }
                    });


                    popupMenu.add(menuItemEdit);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    return true;
                }
            }
        } else return false;
    }
                
        

}
