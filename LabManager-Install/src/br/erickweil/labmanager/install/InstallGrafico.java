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
package br.erickweil.labmanager.install;

import br.erickweil.labamanger.common.files.FilesHelper;
import br.erickweil.labamanger.common.files.StatusReceiver;
import br.erickweil.labmanager.cmd.CmdExec;
import br.erickweil.labmanager.cmd.ProgramOpener;
import br.erickweil.labmanager.configurable.Configurable;
import br.erickweil.labmanager.configurable.SimpleLogger;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollBar;

/**
 *
 * @author professor
 */
public class InstallGrafico extends javax.swing.JFrame implements StatusReceiver {

    /**
     * Creates new form InstallGrafico
     */
    
   public static String[] _uninstall_directories = new String[]
   {
       "C:\\Program Files\\labmanager\\",
       "C:\\Program Files\\dnsproxy\\",
   };
   public static String[] _uninstall_keys = new String[]
   {
       "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\run\\LABMANAGER",
       "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\run\\DNSPROXY"
   };
   public static String[] _uninstall_services = new String[]
   {
       "nssm.exe,labmanager_master"
   };
   
   public static String _install_destination = "C:\\Program Files\\labmanager\\";
           
   public static String[] _install_directories_copy = new String[]
   {
       "clientProgram.zip,C:\\Program Files\\labmanager\\",
   };
   
   public static String[] _install_keys = new String[]
   {
       "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\run\\LABMANAGER,REG_SZ,C:\\Program Files\\labmanager\\svchost.exe"
   };
   public static String[] _install_services = new String[]
   {
       "C:\\Program Files\\labmanager\\nssm.exe,labmanager_master,C:\\Program Files\\labmanager\\labmanager_master.exe"
   };
   
   public static String[] _pre_install_cmds = new String[]
   {
   //    "netsh interface ip set address \"Local Area Connection\" dhcp",
   //    "netsh interface ip set dns \"Local Area Connection\" dhcp"
   };
   
   public static String[] _post_install_cmds = new String[]
   {
       //"netsh advfirewall firewall add rule name=\"LABMANAGER\" program=\"C:\\Program Files\\labmanager\\labmanager_master.exe\" protocol=tcp dir=in enable=yes action=allow profile=Private"
   };
           
   public static boolean _restartAfterKeyRemoval = false;
   //public static String _keyRemovalRestartCopy = "%APPDATA%/LabManager/temp/installerTemp";
   //public static String _keyRemovalRegEntryName = "LABMANAGERINSTALLER";
   
   public static boolean _restartAfterInstall = false;
   
   public static boolean _uninstall = true;
   public static boolean _install = true;
    
    public InstallGrafico() throws InterruptedException {
        initComponents();
        new Thread(new Runnable() {
            public void run() {
                try {
                    runInstaller();
                } catch (InterruptedException ex) {
                    Logger.getLogger(InstallGrafico.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    public void runInstaller() throws InterruptedException
    {
        try {
            SimpleLogger logger = new SimpleLogger();
            new Configurable(logger, InstallGrafico.class, "config");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
       
        try
        {
            int percent = jProgressBar1.getMaximum()/10;
            jProgressBar1.setValue(percent);
            
            for(String s : _pre_install_cmds)
            {
                UniversalInstaller.exec(this,"cmd","/c",s);
            }
            
            if(_uninstall)
            {
                
                status.setText("Desinstalando...");
                UniversalInstaller.deleteRegistry(this, _uninstall_keys);
                jProgressBar1.setValue(percent*3);
                
                status.setText("Parando os processos...");
                
                status.setText("Desinstalando.");
                UniversalInstaller.unregisterService(this, _uninstall_services);
                
                Thread.sleep(100);
                
                if(_restartAfterKeyRemoval)
                {
                    //File thisFolder = new File("anything.txt").getAbsoluteFile().getParentFile();
                    //File tempCopy = ProgramOpener.parsePath(_keyRemovalRestartCopy);
                    //File thisFile = ProgramOpener.parsePath(".*\\.exe");
                    //FilesHelper.copyDirectory(this, thisFolder, tempCopy);
                    //CmdExec.execCmd("reg",
                    //        "add","HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\run",
                    //        "/v",_keyRemovalRegEntryName,
                    //        "/t","REG_SZ",
                    //        "/d",thisFile.getAbsolutePath(),
                    //        "/f");
                    
                    CmdExec.restart();
                }
                
                status.setText("Desinstalando..");
                UniversalInstaller.deleteFiles(this, _uninstall_directories);
                jProgressBar1.setValue(percent*5);
            }
            
            if(_install)
            {
                status.setText("Instalando.");
                Thread.sleep(1000);
                UniversalInstaller.copyDirectories(this, _install_directories_copy);
                jProgressBar1.setValue(percent*7);
                status.setText("Instalando..");
                UniversalInstaller.addRegistry(this, _install_keys);
                jProgressBar1.setValue(percent*9);
                status.setText("Instalando...");
                UniversalInstaller.registerService(this, _install_services);
            
                status.setText("Validando Instalação");

                sendMessage("Verificando.");
                Thread.sleep(1000);

                boolean sucess = true;

                sendMessage("\nChecando Diretórios:");
                if(UniversalInstaller.checkDirectories(this, _install_directories_copy))
                sendMessage("Copiado diretórios com sucesso!");
                else{sendMessage("Erro ao copiar diretórios.");sucess=false;}

                sendMessage("\nChecando Registro:");
                if(UniversalInstaller.checkRegistry(this, _install_keys))
                sendMessage("Criadas chaves do registro com sucesso!");
                else{sendMessage("Erro ao criar chaves do registro.");sucess=false;}

                sendMessage("\nChecando Serviços:");
                if(UniversalInstaller.checkService(this, _install_services))
                sendMessage("Registrado serviço com sucesso!");
                else{sendMessage("Erro ao registrar o serviço.");sucess=false;}
                if(sucess)
                {
                    for(String s : _post_install_cmds)
                    {
                        UniversalInstaller.exec(this,"cmd","/c",s);
                    }

                    
                    suscessfull();
                    if(_restartAfterInstall)
                    {
                        CmdExec.restart();
                    }
                }
                else
                {
                    failed("Instalação falhou, verifique os logs.");
                }
            }
        } catch (IOException ex) {
           Logger.getLogger(InstallGrafico.class.getName()).log(Level.SEVERE, null, ex);
           
            failed(ex.getMessage());
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

        button = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        status = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textarea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Labmanager 12.11.17.10.07 - Programa de Instalação");

        button.setText("Cancelar");
        button.setEnabled(false);
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Aguarde enquando a instalação é feita.");

        status.setText("iniciando");

        textarea.setColumns(20);
        textarea.setRows(5);
        jScrollPane2.setViewportView(textarea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(button)
                    .addComponent(status))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_buttonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InstallGrafico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InstallGrafico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InstallGrafico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InstallGrafico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new InstallGrafico().setVisible(true);
                } catch (InterruptedException ex) {
                    Logger.getLogger(InstallGrafico.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public synchronized void suscessfull()
    {
        jProgressBar1.setValue(jProgressBar1.getMaximum());
        status.setText("COMPLETADO.");
        button.setEnabled(true);
        button.setText("Finalizar");
    }
    
    public synchronized void failed(String reason)
    {
        jProgressBar1.setValue(jProgressBar1.getMaximum()/2);
        status.setText("FALHA:"+reason);
        button.setEnabled(true);
        button.setText("Fechar");
    }
    
    @Override
    public synchronized void sendMessage(String msg)
    {
        status.setText(msg);
       textarea.append(msg+"\n");
       JScrollBar vertical = jScrollPane2.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
       jProgressBar1.setValue((jProgressBar1.getValue()+5) % jProgressBar1.getMaximum());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel status;
    private javax.swing.JTextArea textarea;
    // End of variables declaration//GEN-END:variables
}
