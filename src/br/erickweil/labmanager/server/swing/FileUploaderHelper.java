/*
 * Copyright (C) 2019 Usuario
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

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.BroadcasterMessage.Messages;
import br.erickweil.labamanger.common.WeilUtils;
import br.erickweil.labmanager.server.ServerApp;
import br.erickweil.labmanager.server.ServerMain;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientData;
import br.erickweil.labmanger.filetransmission.FileUploaderTask;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Usuario
 */
public class FileUploaderHelper extends BroadcasterListener{
    public FileUploaderTask uploaderTask;
    
    private final SwingServer sw;
    private Messages action;
    private String download_action;
    private File selectedFile;
    private int port;
    private String filehash;
    List<String> uuids;
    HashMap<String,Boolean> clients_downloaded;
    @Override
    public void onResponse(String threadname, String status, Object data) {
        if(uuids.contains(threadname))
        {
            System.out.println("Enviando Arquivo um por um, Resposta: "+status);
            clients_downloaded.put(threadname, status.equals("OK"));
        }
        
        uploadToNext();
    }
                
    private void uploadToNext()
    {
        for(int i=0;i<uuids.size();i++)
        {
            String uuid = uuids.get(i);
            if(!clients_downloaded.containsKey(uuid) || !clients_downloaded.get(uuid))
            {
                ClientData client = this.sw.clients_manager.clients_uuidmap.get(uuid);
                if(client != null 
                        &&( client.status == ClientStatusManager.ClientStatus.Processando
                        || client.status == ClientStatusManager.ClientStatus.Aguardando)
                   )
                {
                    if(uploaderTask.getDownloadStart(uuid) == 0) // nao mandar fazer donwload cliente que já começou
                    {
                        System.out.println("Enviando Arquivo um por um, "+i+"/"+uuids.size());
                        this.sw.server.sendMessage(client.thread_name, action, download_action, selectedFile.getName() ,""+port, filehash, ServerMain._uploader_hash_protocol);
                        return;
                    }
                }
                else
                {
                    System.out.println("Enviando Arquivo um por um, não enviou para "+i+"/"+uuids.size()+": Desconectado.");
                }
            }
        }
        System.out.println("Enviando Arquivo um por um, terminou de enviar para "+uuids.size()+" clientes");
        
        //uploaderTask.stopTask();
    }
    
    public FileUploaderHelper(SwingServer server){
        this.sw = server;
    }
    
    public void upload(File _selectedFile,Messages _action,List<String> _uuids,String[] broadcast_selected)
    {   
       
                
        if(uploaderTask != null && uploaderTask.isRunning())
        {
            if(ServerMain._uploader_port == 0)
            {
                int dialogResult = JOptionPane.showConfirmDialog (null, "Já há um upload em progresso, deseja cancelar o upload anterior antes de começar o upload atual?","Atenção",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION)
                {
                    uploaderTask.stopTask();
                }
            }
            else
            {
                uploaderTask.stopTask();
            }
        }
        
        this.selectedFile = _selectedFile;
        this.action = _action;
        this.uuids = _uuids;
        this.clients_downloaded = new HashMap<>();
        

        this.download_action = "run";
        if(action == BroadcasterMessage.Messages.admin_download)
        {
            int dialogResult = JOptionPane.showConfirmDialog (null, "O arquivo a ser enviado é uma Atualização do Programa?","Atenção",JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION)
            {
                this.download_action = "run_after_exit";
            }
        }

        this.filehash = "none";
        if(ServerMain._uploader_use_hash)
        {
            try {
                System.out.println("calculando hash do arquivo a ser enviado...");
                this.filehash = WeilUtils.hashFile(selectedFile, ServerMain._uploader_hash_protocol);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }
        }

        this.uploaderTask = new FileUploaderTask(selectedFile,ServerMain._uploader_port,uuids);
        this.uploaderTask.setMaxSpeed(ServerMain._uploader_maxspeed);
        if(ServerMain._uploader_onebyone)
        {
            this.uploaderTask.register_onDownloadListener(this);
        }
        this.uploaderTask.startTask();

        this.port = ServerMain._uploader_port;
        if(ServerMain._uploader_port == 0)
        {
            for(int i=0;i<50 && this.port == 0;i++)
            {
                try
                {
                    Thread.sleep(50);
                    this.port = uploaderTask.getPort();
                }
                catch(Exception e){if(i == 0)e.printStackTrace();}
            }
        }

        
        //server.sendMessageToAll(broadcast_selected, action, download_action, selectedFile.getName() ,""+port, filehash, ServerMain._uploader_hash_protocol);
        if(ServerMain._uploader_onebyone)
        {
            uploadToNext();
        }
        else
        {
            this.sw.server.sendMessageToAll(broadcast_selected, action, download_action, selectedFile.getName() ,""+port, filehash, ServerMain._uploader_hash_protocol);
        }
    }

}
