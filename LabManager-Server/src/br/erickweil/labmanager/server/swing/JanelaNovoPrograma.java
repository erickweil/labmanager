/*
 * Copyright (C) 2018 Usuario
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

import br.erickweil.labamanger.common.BroadcasterMessage.Messages;
import br.erickweil.labamanger.common.Program;
import br.erickweil.labamanger.common.Program.Condition;
import br.erickweil.labamanger.common.Program.ConditionMatch;
import br.erickweil.labamanger.common.Program.ConditionVar;
import br.erickweil.labamanger.common.Program.StartType;
import br.erickweil.labamanger.common.WeilUtils;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author Usuario
 */
public class JanelaNovoPrograma extends javax.swing.JFrame implements ChangeListener {

    private final JanelaConfiguracoes janelaConfiguracoes;
    public static final int REGEX_EXATAMENTE = 0;
    public static final int REGEX_INICIACOM = 1;
    public static final int REGEX_TERMINACOM = 2;
    public static final int REGEX_CONTEM = 3;
    public static final int REGEX_REGEX = 4;
    
    
    /**
     * Creates new form JanelaNovoPrograma
     */
    public JanelaNovoPrograma(JanelaConfiguracoes janelaConfiguracoes) {
        initComponents();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.janelaConfiguracoes = janelaConfiguracoes;
        
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index));
        if(index == 1)
        {
            atualizar_condicoes();
        }
    }
    

    
    List<Condition> conditions;
    Program programEdit;
    public void inicializar(int roweditindex)
    {
        conditions = new ArrayList<>();
        
        
        this.jTabbedPane2.addChangeListener(this);
        this.table_conditions.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                selecionou_condicao(table_conditions.getSelectedRow());
            }
        });
        table_conditions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Program program = null;
        if(roweditindex >= 0)
        {
            DefaultTableModel model = (DefaultTableModel) janelaConfiguracoes.table_programas.getModel();
            String nome = (String)model.getValueAt(roweditindex, 0);
            List<Program> programs = janelaConfiguracoes.server.programs;
            
            for(Program p : programs)
            {
                if(p.name.equalsIgnoreCase(nome))
                {
                    program = p;
                    break;
                }
            }
            if(program != null)
            {
                if(program.stopConditions != null)
                conditions.addAll(Arrays.asList(program.stopConditions));
                input_addprograma_nome.setText(program.name);
                input_addprograma_caminho.setText(program.path);
                input_addprograma_janela.setText(program.window);
                input_addprograma_processo.setText(program.process);
                //combo_addprograma_regexjanela.setSelectedIndex(0);
                //combo_addprograma_regexprocesso.setSelectedIndex(0);
                combo_addprograma_inicializacao.setSelectedItem(program.start.toString());
                combo_addprograma_modo.setSelectedItem(program.mode.toString());
                
                button_addprograma.setText("Editar");
            }
            
            
        }
        
        if(program == null)
        {
            input_addprograma_nome.setText("");
            input_addprograma_caminho.setText("");
            input_addprograma_janela.setText("");
            input_addprograma_processo.setText("");
            //combo_addprograma_regexjanela.setSelectedIndex(0);
            //combo_addprograma_regexprocesso.setSelectedIndex(0);
            combo_addprograma_inicializacao.setSelectedIndex(0);
            combo_addprograma_modo.setSelectedIndex(0);
            
            button_addprograma.setText("Adicionar");
        }
        
        programEdit = program;
        
        atualizar_condicoes();
        table_conditions.setRowSelectionInterval(0, 0);
    }
    
    public void atualizar_condicoes()
    {
        DefaultTableModel model = (DefaultTableModel) table_conditions.getModel();
        while(model.getRowCount()>0) model.removeRow(0);
        
        if(conditions.size() == 0)
        {
            conditions.add(new Condition(ConditionVar.processo, ConditionMatch.exatamente, "", false));
        }
        
        if(combo_addprograma_modo.getSelectedItem().toString().equals("processo"))
        {
            conditions.set(0,new Condition(ConditionVar.processo,ConditionMatch.regex,input_addprograma_processo.getText(),false));
        }
        else if(combo_addprograma_modo.getSelectedItem().toString().equals("caminho"))
        {
            conditions.set(0,new Condition(ConditionVar.caminho,ConditionMatch.regex,input_addprograma_caminho.getText(),false));
        }
        else
        {
            conditions.set(0,new Condition(ConditionVar.janela,ConditionMatch.regex,input_addprograma_janela.getText(),false));
        }
        
        for(int i=0;i<conditions.size();i++)
        {
            model.addRow(conditions.get(i).toArray());
        }
        
        
    }
    
    public void selecionou_condicao(int row)
    {
        if(row < 1) // a linha 0 não pode ser editada
        {
            WeilUtils.disableAll(false,combo_cond_var,combo_cond_match,field_cond_value,btn_cond_save,btn_cond_remove,check_cond_inv);
            this.combo_cond_var.setSelectedIndex(0);
            this.combo_cond_match.setSelectedIndex(0);
            this.field_cond_value.setText("");
        }
        else
        {
            DefaultTableModel model = (DefaultTableModel) table_conditions.getModel();    
            
            WeilUtils.enableAll(false,combo_cond_var,combo_cond_match,field_cond_value,btn_cond_save,btn_cond_remove,check_cond_inv);
            this.combo_cond_var.setSelectedItem(model.getValueAt(row,0));
            this.combo_cond_match.setSelectedItem(model.getValueAt(row,1));
            this.field_cond_value.setText((String)model.getValueAt(row,2));
        }
        
        
    }

    private String combo_text(JComboBox c)
    {
        int index = c.getSelectedIndex();
        if(index != -1)
        {
            String selected = (String)c.getItemAt(index);
            if(selected != null && !selected.isEmpty())
            {
                return selected;
            }
        }
        return null;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        input_addprograma_caminho = new javax.swing.JTextField();
        input_addprograma_nome = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        input_addprograma_processo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        input_addprograma_janela = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        combo_addprograma_modo = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        combo_addprograma_inicializacao = new javax.swing.JComboBox();
        button_addprograma = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table_conditions = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        combo_cond_var = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        combo_cond_match = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        field_cond_value = new javax.swing.JTextField();
        btn_cond_save = new javax.swing.JButton();
        check_cond_inv = new javax.swing.JCheckBox();
        button_addcondition = new javax.swing.JButton();
        btn_cond_remove = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Detalhes do Programa"));
        jPanel1.setToolTipText("");

        input_addprograma_caminho.setText("C:/Windows/notepad\\.exe");
        input_addprograma_caminho.setToolTipText("Caso deseja que seja possível abrir esse programa, preencha um caminho de arquivo");

        input_addprograma_nome.setText("Notepad");
        input_addprograma_nome.setToolTipText("Coloque aqui o nome que irá aparecer na listagem dos programas.");
        input_addprograma_nome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                input_addprograma_nomeActionPerformed(evt);
            }
        });

        jLabel4.setText("Nome*:");

        jLabel6.setText("Caminho:");

        jLabel7.setText("Processo:");

        input_addprograma_processo.setText("Notepad.exe");
        input_addprograma_processo.setToolTipText("caso deseje que seja possivel matar o processo pelo nome do executável, preencha o nome do executável");

        jLabel2.setText("Janela:");

        input_addprograma_janela.setText("^.* \\- Bloco de Notas$");
        input_addprograma_janela.setToolTipText("caso deseje que seja possível matar o processo pelo nome da janela, preencha aqui o nome da janela");
        input_addprograma_janela.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                input_addprograma_janelaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel2))
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(input_addprograma_caminho, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                    .addComponent(input_addprograma_nome)
                    .addComponent(input_addprograma_processo)
                    .addComponent(input_addprograma_janela))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(input_addprograma_nome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(input_addprograma_caminho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(input_addprograma_processo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(input_addprograma_janela, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setText("Modo de Bloqueio:");

        combo_addprograma_modo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "processo", "janela", "caminho" }));
        combo_addprograma_modo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combo_addprograma_modoActionPerformed(evt);
            }
        });

        jLabel8.setText("Inicialização:");

        combo_addprograma_inicializacao.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "manual", "logon", "blacklist" }));

        button_addprograma.setText("Adicionar");
        button_addprograma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_addprogramaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(combo_addprograma_modo, 0, 137, Short.MAX_VALUE)
                            .addComponent(combo_addprograma_inicializacao, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(button_addprograma, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(combo_addprograma_modo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(combo_addprograma_inicializacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(button_addprograma)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Programa", jPanel3);

        table_conditions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "variável", "método de checagem", "valor", "inverter correspondência"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table_conditions.setShowHorizontalLines(false);
        jScrollPane1.setViewportView(table_conditions);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Editando Linha"));

        jLabel5.setText("Variável:");

        combo_cond_var.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "processo", "janela", "caminho", "pai" }));

        jLabel9.setText("Mét. Checagem:");

        combo_cond_match.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "exatamente", "comeca", "termina", "regex" }));

        jLabel10.setText("Valor:");

        btn_cond_save.setText("Salvar");
        btn_cond_save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cond_saveActionPerformed(evt);
            }
        });

        check_cond_inv.setText("Inverter");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10))
                .addGap(4, 4, 4)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(combo_cond_var, 0, 196, Short.MAX_VALUE)
                    .addComponent(field_cond_value))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(check_cond_inv))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combo_cond_match, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_cond_save, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(combo_cond_var, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(combo_cond_match, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(field_cond_value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(check_cond_inv)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_cond_save))))
        );

        button_addcondition.setText("Nova Linha");
        button_addcondition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_addconditionActionPerformed(evt);
            }
        });

        btn_cond_remove.setText("Excluir");
        btn_cond_remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cond_removeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(button_addcondition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_cond_remove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(button_addcondition)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_cond_remove)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Condições de Bloqueio", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void input_addprograma_nomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_input_addprograma_nomeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_input_addprograma_nomeActionPerformed

    private void button_addprogramaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_addprogramaActionPerformed
        //
        String name = input_addprograma_nome.getText();
        String path = input_addprograma_caminho.getText();
        String processo = input_addprograma_processo.getText();
        String janela = input_addprograma_janela.getText();
        StartType inicializacao = Program.StartType.manual;
        Program.BlockMode modo = Program.BlockMode.processo;


        String selected = combo_text(this.combo_addprograma_inicializacao);
        if(selected != null && !selected.isEmpty())
        {
            inicializacao = StartType.valueOf(selected);
        }

        selected = combo_text(this.combo_addprograma_modo);
        if(selected != null && !selected.isEmpty())
        {
            modo = Program.BlockMode.valueOf(selected);
        }
        
        /*int index = this.combo_addprograma_regexprocesso.getSelectedIndex();
        switch(index)
        {
            case REGEX_REGEX:        
            case REGEX_EXATAMENTE:   break;
            case REGEX_INICIACOM:   processo = "^"+processo+".*$"; break;
            case REGEX_TERMINACOM:  processo = "^.*"+processo+"$"; break;
            case REGEX_CONTEM:      processo = "^.*"+processo+".*$"; break;
        }
        
        index = this.combo_addprograma_regexjanela.getSelectedIndex();
        switch(index)
        {
            case REGEX_REGEX:        
            case REGEX_EXATAMENTE:   break;
            case REGEX_INICIACOM:   janela = "^"+janela+".*$"; break;
            case REGEX_TERMINACOM:  janela = "^.*"+janela+"$"; break;
            case REGEX_CONTEM:      janela = "^.*"+janela+".*$"; break;
        }*/

        
        if(programEdit == null)
        {
            Program p = new Program(name, path, processo, janela, inicializacao , false, modo);
            if(conditions.size() > 0)
            {
                p.stopConditions = new Condition[conditions.size()];
                p.stopConditions = conditions.toArray(p.stopConditions);
            }
            janelaConfiguracoes.server.programs.add(p);
        }
        else
        {
            programEdit.name = name;
            programEdit.path = path;
            programEdit.process = processo;
            programEdit.window = janela;
            programEdit.start = inicializacao;
            programEdit.mode = modo;
            if(conditions.size() > 0)
            {
                programEdit.stopConditions = new Condition[conditions.size()];
                programEdit.stopConditions = conditions.toArray(programEdit.stopConditions);
            }
        }
        
        janelaConfiguracoes.updateValues();
        janelaConfiguracoes.server.sendMessage("all", Messages.blacklist);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_button_addprogramaActionPerformed

    private void input_addprograma_janelaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_input_addprograma_janelaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_input_addprograma_janelaActionPerformed

    private void combo_addprograma_modoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combo_addprograma_modoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_combo_addprograma_modoActionPerformed

    private void button_addconditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_addconditionActionPerformed
        DefaultTableModel model = (DefaultTableModel) table_conditions.getModel();
        model.addRow(new Object[]{"Processo","Exatamente","",false});
        table_conditions.setRowSelectionInterval(table_conditions.getRowCount()-1, table_conditions.getRowCount()-1);
        
        conditions.add(new Condition(ConditionVar.processo,ConditionMatch.exatamente, "", false));
        
    }//GEN-LAST:event_button_addconditionActionPerformed

    private void btn_cond_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cond_saveActionPerformed
        if(table_conditions.getSelectedRow() > 0)
        {
            DefaultTableModel model = (DefaultTableModel) table_conditions.getModel();
            Condition cond = conditions.get(table_conditions.getSelectedRow());

            cond.var = ConditionVar.valueOf(this.combo_cond_var.getSelectedItem().toString());
            cond.match = ConditionMatch.valueOf(this.combo_cond_match.getSelectedItem().toString());
            cond.value = this.field_cond_value.getText();
            cond.inverse = this.check_cond_inv.isSelected();
        }
        atualizar_condicoes();
    }//GEN-LAST:event_btn_cond_saveActionPerformed

    private void btn_cond_removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cond_removeActionPerformed
        int rowSelec = table_conditions.getSelectedRow();
        if(rowSelec > 0)
        {
            DefaultTableModel model = (DefaultTableModel) table_conditions.getModel();
            conditions.remove(rowSelec);
            model.removeRow(rowSelec);
        }
        atualizar_condicoes();
    }//GEN-LAST:event_btn_cond_removeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_cond_remove;
    private javax.swing.JButton btn_cond_save;
    private javax.swing.JButton button_addcondition;
    private javax.swing.JButton button_addprograma;
    private javax.swing.JCheckBox check_cond_inv;
    private javax.swing.JComboBox combo_addprograma_inicializacao;
    private javax.swing.JComboBox combo_addprograma_modo;
    private javax.swing.JComboBox<String> combo_cond_match;
    private javax.swing.JComboBox<String> combo_cond_var;
    private javax.swing.JTextField field_cond_value;
    private javax.swing.JTextField input_addprograma_caminho;
    private javax.swing.JTextField input_addprograma_janela;
    private javax.swing.JTextField input_addprograma_nome;
    private javax.swing.JTextField input_addprograma_processo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable table_conditions;
    // End of variables declaration//GEN-END:variables
}
