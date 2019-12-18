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
import br.erickweil.labamanger.common.BroadcasterMessage.Messages;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.Program.StartType;
import br.erickweil.labmanager.server.ServerApp;
import br.erickweil.labmanager.server.ServerMain;
import br.erickweil.labmanager.server.swing.pcmap.MapNode;
import br.erickweil.labmanager.server.swing.pcmap.PC;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Usuario
 */
public class JanelaConfiguracoes extends javax.swing.JFrame implements MouseListener {

    /**
     * Creates new form JanelaConfiguracoes
     */
    ServerApp server;
    private JanelaNovoPrograma janelaNovoPrograma;
    public JanelaConfiguracoes(ServerApp server) {
        initComponents();
        this.server = server;
        
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        updateValues();
        
        table_programas.getModel().addTableModelListener(
        new TableModelListener() 
        {
        boolean selfupdate = false;
        @Override
        public void tableChanged(TableModelEvent evt) 
        {
            if(selfupdate) return;
            selfupdate = true;
            try{
            if(evt.getType() == TableModelEvent.UPDATE)
            {
                int col = evt.getColumn();
                int row = evt.getFirstRow();
                DefaultTableModel model = (DefaultTableModel) table_programas.getModel();
                if(model.getValueAt(row, col) instanceof Boolean)
                {
                    boolean value = (boolean) model.getValueAt(row, col);
                    //server.programs.get(row).start = Program.StartType.manual;
                    // proibir
                    if(col == 1)
                    {
                        if(value)
                        {
                            if(server.programs.get(row).start != Program.StartType.manual)
                            {
                                model.setValueAt(false, row, 2);
                            }
                            server.programs.get(row).start = Program.StartType.blacklist;
                        }
                        else
                        {
                            server.programs.get(row).start = Program.StartType.manual;
                        }
                    }
                    // logon
                    else if(col == 2)
                    {
                        if(value)
                        {
                            if(server.programs.get(row).start != Program.StartType.manual)
                            {
                                model.setValueAt(false, row, 1);
                            }
                            server.programs.get(row).start = Program.StartType.logon;
                        }
                        else
                        {
                            server.programs.get(row).start = Program.StartType.manual;
                        }
                    }
                    // offline
                    else if(col == 3)
                    {
                        server.programs.get(row).block_offline = value;
                    }
                    server.sendMessage("all", BroadcasterMessage.Messages.blacklist);
                    //updatePrograms();
                }
            }
            }
            finally
            {
                selfupdate = false;
            }
        }
        });
        
        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent we)
        { 
            server.SaveConfigs();
        }
        });
        
        // http://www.codejava.net/java-se/swing/jtable-popup-menu-example
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemEdit = new JMenuItem("Editar");
        menuItemEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table_programas.getSelectedRow();
                System.out.println(selectedRow);
                AbrirJanelaNovoPrograma(selectedRow);
            }
        });
        
        JMenuItem menuItemRemove = new JMenuItem("Deletar");
        menuItemRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table_programas.getSelectedRow();
                String nome = (String)table_programas.getValueAt(selectedRow, 0);
                
                List<Program> programs = server.programs;
                for(int i=0;i<programs.size();i++)
                {
                    if(programs.get(i).name.equalsIgnoreCase(nome))
                    {
                        programs.remove(i);
                        updateValues();
                        server.sendMessage("all", BroadcasterMessage.Messages.blacklist);
                        break;
                    }
                }
            }
        });
        

        popupMenu.add(menuItemEdit);
        popupMenu.add(menuItemRemove);
        
        //JTable table = new JTable();
        // set data model for the table...

        // sets the popup menu for the table
        this.table_programas.setComponentPopupMenu(popupMenu);
        this.table_programas.addMouseListener(this);
        
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
                // selects the row at which point the mouse is clicked
        Point point = e.getPoint();
        int currentRow = table_programas.rowAtPoint(point);
        table_programas.setRowSelectionInterval(currentRow, currentRow);
    }
    
    public void updateValues()
    {
        DefaultTableModel model = (DefaultTableModel) table_programas.getModel();
        while(model.getRowCount()>0) model.removeRow(0);
        for(Program p : server.programs)
        {
            model.addRow(new Object[]{p.name,p.start == Program.StartType.blacklist,p.start == Program.StartType.logon,p.block_offline}); 
        }
        
    }
    
    public void AbrirJanelaNovoPrograma(int roweditindex)
    {
        if(janelaNovoPrograma == null)
        {
            janelaNovoPrograma = new JanelaNovoPrograma(this);
            janelaNovoPrograma.setVisible(true);
            janelaNovoPrograma.inicializar(roweditindex);
        }
        else
        {
            janelaNovoPrograma.setVisible(true);
            janelaNovoPrograma.requestFocus();
            janelaNovoPrograma.inicializar(roweditindex);
        }
    }
    
            

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_programas = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        table_programas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Nome", "Proibir", "Abrir ao Inicializar", "Bloquear Offline"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        table_programas.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(table_programas);

        jButton1.setText("Cadastrar Programa");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Programas", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         AbrirJanelaNovoPrograma(-1);
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    protected javax.swing.JTable table_programas;
    // End of variables declaration//GEN-END:variables

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
}
