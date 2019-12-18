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

import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientData;
import br.erickweil.labmanager.server.swing.icons.IconSetter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 *
 * @author Usuario
 */
public class PopupAcaoCliente extends JPopupMenu {
        public PopupAcaoCliente(final SwingServer swingWindow,ClientData[] clientClicked){
        JMenuItem header;
        if(clientClicked.length == 1)
        header = new JMenuItem("Agir em "+clientClicked[0].user);
        else
        header = new JMenuItem("Agir com os Selecionados");
        
        this.add(header);
        this.addSeparator();
        for(int i=0;i< IconSetter.ButtonNames.length;i++)
        {
            final String buttonName = IconSetter.ButtonNames[i];
            final String buttonTooltip= IconSetter.ButtonTooltips[i];
            
            if(buttonName.equals("broadcast")) continue;

            if(clientClicked.length == 1 && (
                    (clientClicked[0].locked && IconSetter.ButtonNames[i].equals("lockscreen"))
                || (!clientClicked[0].locked && IconSetter.ButtonNames[i].equals("unlockscreen"))
                || clientClicked[0].status == ClientStatusManager.ClientStatus.Desconectado)
              )
            {
                //menuItem.setEnabled(false);
                continue;
            }
            
            JMenuItem menuItem;
            menuItem = new JMenuItem(buttonTooltip);
            IconSetter.setIcon(buttonName, menuItem);

            menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            List<String> threadNames = new ArrayList<String>();
                            for(ClientData c : clientClicked)
                            {
                                if(c != null && c.status != ClientStatusManager.ClientStatus.Desconectado)
                                {
                                    threadNames.add(c.thread_name);
                                }
                            }
                            String[] threadNames_arr = new String[threadNames.size()];
                            threadNames_arr = threadNames.toArray(threadNames_arr);
                            swingWindow.ToolbarAction(BroadcasterMessage.Messages.valueOf(buttonName),threadNames_arr);
                            //}
                        }
            });
            this.add(menuItem);
        }
    }
        
    public PopupAcaoCliente(final SwingServer swingWindow,ClientData clientClicked){
        this(swingWindow,new ClientData[]{clientClicked});
    }

    
    static class PopupClickListener extends MouseAdapter {
        SwingServer swingWindow;
        public PopupClickListener(SwingServer swingWindow)
        {
            this.swingWindow = swingWindow;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Component c = e.getComponent();
            if (e.isPopupTrigger()) {
                if(c != null)
                {
                    System.out.println(c.getClass().getCanonicalName());
                    System.out.println(c.toString());
                    if(c instanceof JTable)
                    {
                        JTable table = (JTable) c;
                        int row = table.rowAtPoint(e.getPoint());
                        TableModel model = table.getModel();
                        //System.out.println(model.getValueAt(row, 0)+" "+model.getValueAt(row, 1)+model.getValueAt(row, 2));
                        ClientStatusManager.ClientData clientClicked = swingWindow.clients_manager.getClientAt(row);
                        System.out.println(clientClicked.thread_name+" "+clientClicked.user);
                        PopupAcaoCliente popup = new PopupAcaoCliente(swingWindow,clientClicked);
                        popup.show(e.getComponent(),x, y);
                    }
                }
                
            }
        }
    }
}
