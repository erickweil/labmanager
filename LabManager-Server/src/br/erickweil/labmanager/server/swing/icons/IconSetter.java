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
package br.erickweil.labmanager.server.swing.icons;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *
 * @author Usuario
 */
public class IconSetter {
    public static Class<IconSetter> MyClass = IconSetter.class;
    public static HashMap<String,HashMap<String,Icon>> loadedIcons;
    
    public static final String[] ButtonNames = {
        "shutdown",
        "restart",
        "logoff",
        "lockscreen",
        "unlockscreen",
        "broadcast",
        "printscreen",
        "browse",
        "msg",
        "download",
        "start",
        "stop",
        "exit"
    };

    public static final String[] ButtonTooltips = {
        "Desligar",
        "Reiniciar",
        "Fazer Logoff",
        "Bloquear Tela",
        "Desbloquear Tela",
        "Transmitir Tela",
        "Tirar PrintScreen",
        "Navegar",
        "Mensagem",
        "Enviar Arquivo",
        "Abrir Programa",
        "Fechar Programa",
        "Desconectar do Servidor"
    };
    
    public static JButton createButton(String buttonName,String toolTip,java.awt.event.ActionListener listener)
    {
        JButton button = new JButton();
        setIcon(buttonName, button);
        button.setToolTipText(toolTip);
        button.setBorder(null);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setMaximumSize(new java.awt.Dimension(32, 32));
        button.setMinimumSize(new java.awt.Dimension(32, 32));
        button.setPreferredSize(new java.awt.Dimension(32, 32));
        button.addActionListener(listener);
        return button;
    }
    
    public static void setIcon(String buttonName,AbstractButton button)
    {
        try
        {
            HashMap<String,Icon> icons = null;
            
            if(loadedIcons == null)
            {
                loadedIcons = new HashMap<>();
            }
            
            if(!loadedIcons.containsKey(buttonName))
            {
                icons = new HashMap<>();
                Icon t = load(buttonName+"-default.png");
                if(t != null) icons.put("default",t);

                t = load(buttonName+"-pressed.png");
                if(t != null) icons.put("pressed",t);

                t = load(buttonName+"-rollover.png");
                if(t != null) icons.put("rollover",t);

                t = load(buttonName+"-disabled.png");
                if(t != null) icons.put("disabled",t);

                t = load(buttonName+"-selected.png");
                if(t != null) icons.put("selected",t);
                
                if(icons.isEmpty())
                {
                    System.err.println("nenhum icone carregado para '"+buttonName+"'");
                    return;
                }
                
                loadedIcons.put(buttonName, icons);
            }
            else
            {
                icons = loadedIcons.get(buttonName);
            }

            if(icons.containsKey("default")) button.setIcon(icons.get("default"));
            if(icons.containsKey("pressed")) button.setPressedIcon(icons.get("pressed"));
            if(icons.containsKey("rollover")) button.setRolloverIcon(icons.get("rollover"));
            if(icons.containsKey("disabled")) button.setDisabledIcon(icons.get("disabled"));
            if(icons.containsKey("selected")) button.setSelectedIcon(icons.get("selected"));

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static Icon load(String buttonName)
    {
        URL resource = MyClass.getResource(buttonName);
        if(resource == null)
        {
            //System.err.println("Falha ao carregar ícone '"+buttonName+", caminho não encontrado");
            return null;
        }
        Image image = Toolkit.getDefaultToolkit().getImage(resource);
        Icon icon = new ImageIcon(image);
        return icon;
    }
}
