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
package br.erickweil.labmanager.tests;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Usuario
 */
public class BottomMenu {
    
    public static class Painel extends JPanel{
        @Override
        public void paintComponent( Graphics g ){
            super.paintComponent( g );
            int pixel=0;

            for(pixel=0 ; pixel <= getHeight() ; pixel += 10){
                g.drawLine(0, pixel, pixel, getHeight());
                }

            for(pixel=getHeight() ; pixel >=0 ; pixel -= 10){
                g.drawLine(0, pixel, getHeight() - pixel, 0);
            }

            for(pixel=0 ; pixel <= getHeight() ; pixel +=10){
                g.drawLine(getWidth(), pixel, getWidth() - pixel, getHeight());
            }

            for(pixel=getHeight() ; pixel >=0 ; pixel -= 10){
                g.drawLine(getWidth(), pixel, getWidth() - (getHeight() - pixel), 0);
            }

        }
    }
    
    public static void main(String[] args)
    {
        JFrame janela = new JFrame("Meu primeiro frame em Java");
        Painel meuPainel = new Painel();
		
		janela.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		janela.add(meuPainel);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
                   
        int screen_width = screenSize.width;
        int screen_height = screenSize.height;
        
        int taskbarheight = screen_height - GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
        int taskbarwidth = screen_width;
 
        
        janela.setLocation(0, screen_height-taskbarheight);
        
        janela.setSize(taskbarwidth,taskbarheight);
        janela.setUndecorated(true);
        janela.setAlwaysOnTop(true);
        janela.setVisible(true);
        
    }
}
