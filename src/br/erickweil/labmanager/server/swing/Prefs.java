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
package br.erickweil.labmanager.server.swing;

import br.erickweil.labamanger.common.files.JsonParser;
import br.erickweil.labmanager.server.ServerMain;
import br.erickweil.labmanager.server.swing.pcmap.MapaCanvas;
import java.awt.Frame;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author Usuario
 */
public class Prefs {
    public boolean changed;
    SwingServer sw;
    
    public Prefs(SwingServer swingServer)
    {
        this.sw = swingServer;
    }
    
    public void save()
    {
        try
        {
            changed = false;
            HashMap<String,String> prefs = new HashMap<String, String>();
            prefs.put("grid_size", ""+sw.sliderGridSize.getValue());
            prefs.put("grid_align", ""+sw.checkAlignOnGrid.isSelected());
            prefs.put("grid_show", ""+sw.checkShowGrid.isSelected());
            prefs.put("panel_tab", ""+sw.TabbedPane.getSelectedIndex());
            prefs.put("show_screen", ""+sw.checkShowScreen.isSelected());
            prefs.put("show_screenAmount", ""+sw.sliderScreenAmount.getValue());
            
            prefs.put("window_state", ""+sw.getExtendedState());
            prefs.put("window_x", ""+sw.getX());
            prefs.put("window_y", ""+sw.getY());
            prefs.put("window_w", ""+sw.getWidth());
            prefs.put("window_h", ""+sw.getHeight());
            
            prefs.put("pcmap_background", MapaCanvas.backgroundPath.getAbsolutePath());
            prefs.put("pcmap_backgroundscale", ""+MapaCanvas.background_scale);
            prefs.put("pcmap_backgroundx", ""+MapaCanvas.background_posx);
            prefs.put("pcmap_backgroundy", ""+MapaCanvas.background_posy);

            JsonParser.save(prefs, new File(ServerMain._preferencesdir+"prefs.json"));
            System.out.println("Preferences Saved.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void load()
    {
        try
        {
            changed = false;
            HashMap<String,String> prefs = JsonParser.load(new File(ServerMain._preferencesdir+"prefs.json"));
            if(prefs == null) return;
            
            sw.sliderGridSize.setValue(Integer.parseInt(prefs.get("grid_size")));
            sw.checkAlignOnGrid.setSelected(Boolean.parseBoolean(prefs.get("grid_align")));
            sw.checkShowGrid.setSelected(Boolean.parseBoolean(prefs.get("grid_show")));
            if(prefs.containsKey("show_screen"))
            sw.checkShowScreen.setSelected(Boolean.parseBoolean(prefs.get("show_screen")));
            if(prefs.containsKey("show_screenAmount"))
            sw.sliderScreenAmount.setValue(Integer.parseInt(prefs.get("show_screenAmount")));
            sw.TabbedPane.setSelectedIndex(Integer.parseInt(prefs.get("panel_tab")));
            
            int window_state = Integer.parseInt(prefs.get("window_state"));
            if((window_state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)
            {
                sw.setExtendedState(Frame.MAXIMIZED_BOTH);
            }
            else
            {
                int x = Integer.parseInt(prefs.get("window_x"));
                int y = Integer.parseInt(prefs.get("window_y"));
                int w = Integer.parseInt(prefs.get("window_w"));
                int h = Integer.parseInt(prefs.get("window_h"));
                sw.setBounds(x, y, w, h);
            }
            
            MapaCanvas.backgroundPath = new File((String)prefs.get("pcmap_background"));
            MapaCanvas.background_scale = Double.parseDouble(prefs.get("pcmap_backgroundscale"));
            MapaCanvas.background_posx = Double.parseDouble(prefs.get("pcmap_backgroundx"));
            MapaCanvas.background_posy = Double.parseDouble(prefs.get("pcmap_backgroundy"));
            
            System.out.println("Preferences Loaded.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void setChanged()
    {
        changed = true;
    }
}
