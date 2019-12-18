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

import br.erickweil.labamanger.common.WeilUtils;
import br.erickweil.labmanager.server.CmdBroadcasterProtocol;
import br.erickweil.labmanager.server.swing.ClientStatusManager;
import br.erickweil.labmanager.server.swing.ClientStatusManager.ClientData;
import br.erickweil.labmanager.server.swing.ColorControler;
import br.erickweil.labmanger.filetransmission.FileUploaderTask;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Date;
import org.json.simple.JSONObject;

/**
 *
 * @author Usuario
 */
public class PC extends MapNode{
    String uuid;
    BufferedImage lastscreen;
    ClientStatusManager statusManager;
    //int temptest;
    
    public PC(ClientStatusManager statusManager,String uuid,double px, double py) {
        super(px, py, 0.625, 0.625);
        this.uuid = uuid;
        this.statusManager = statusManager;
    }
    
    @Override
    protected String get_tooltip()
    {
        ClientData clientData = statusManager.clients_uuidmap.get(uuid);
        if(clientData != null)
        {
            boolean locked = clientData.locked;
            String user = clientData.user;
            String ip = clientData.ip;
            int port = clientData.port;
            String uuid = clientData.uuid;
            String status = clientData.status.toString();
            Date since = clientData.connected_since;
            // info informada pelo clientcmdprotocol
            JSONObject client_info = clientData.client_info;
            String sistema_operacional = client_info.containsKey("OS") ? (String)client_info.get("OS") : "";
            String sistema_family = client_info.containsKey("OS_family") ? (String)client_info.get("OS_family") : "";
            String informed_IP = client_info.containsKey("IP") ? (String)client_info.get("IP") : "";
            String informed_MAC = client_info.containsKey("MAC") ? (String)client_info.get("MAC") : "";
            Date now = new Date();
            
            String percentCPU = "";
            String percentMem = "";
            boolean slaveOnline = false;
            // info do ping
            JSONObject ping_info = clientData.ping_info;
            if(ping_info != null)
            {
                //System.out.println("PING INFO NOT NULL!");
                percentCPU = ping_info.containsKey("cpuUsage") ? ping_info.get("cpuUsage")+"%" : "";
                long memTotal = ping_info.containsKey("memTotal") ? (long)ping_info.get("memTotal") : 0;
                long memFree = ping_info.containsKey("memFree") ? (long)ping_info.get("memFree") : 0;
                if(memTotal != 0)
                {
                    percentMem = (((float)(memTotal-memFree)/(float)memTotal)*100)+"%";
                }
                
                slaveOnline = ping_info.containsKey("slaveOnline") ? (boolean)ping_info.get("slaveOnline") : false;
            }
            
            String desde = WeilUtils.tempoDecorrido(now.getTime() - since.getTime());
            
            String extra = "";
            
            if(statusManager.serverWindow.uploaderHelper != null)
            {
                FileUploaderTask uploaderTask = statusManager.serverWindow.uploaderHelper.uploaderTask;

                if(   uploaderTask != null
                   && uploaderTask.isRunning()
                   && uploaderTask.uuidPresent(uuid))
                {
                    int progress = uploaderTask.getProgress(uuid);
                    double speed = uploaderTask.getMeanSpeed(uuid);
                    if(progress != 0 && progress != 100)
                    extra = "\nPorcentagem do download: "+progress+"% à "+WeilUtils.velocidadeDownload(speed);
                }
            }
        
            return user+" IP:"+ip+":"+port+"\n"+
                    (!sistema_family.isEmpty() ? sistema_operacional+" ("+sistema_family+") "+clientData.version+"\n" : clientData.version+"\n")+
                    (!informed_IP.isEmpty() ? "local IP:"+informed_IP+" MAC:"+informed_MAC+"\n" : "")+
                    
                   "UUID:"+uuid+"\n"+
                   "Status:"+status+
                   (percentCPU.isEmpty() ? "" : " CPU:"+percentCPU+" MEM:"+percentMem)+
                   (slaveOnline ? "" : " Slave Offline.")+
                   "\nConectado pela primeira vez a "+desde+extra;
        }
        else return "UUID:"+uuid +"\n"+
                   "Ainda não Conectou";
    }
    
    public int getScreenDisplayWidth(int grid)
    {
        transformDimensions(grid,0,0);
        return idx*3;
    }
    
    @Override
    public void draw(Graphics g,boolean hover,double grid,double camx,double camy)
    {
        transformDimensions(grid,camx,camy);
        ClientData clientData = statusManager.clients_uuidmap.get(uuid);
        
        boolean slaveOnline = false;

        
        boolean locked = false;
        String user = "Unknown";
        boolean old = false;
        boolean veryold = false;
        //String ip = "0.0.0.0";
        //int port = 0;
        //String status = "Nao Conectou";
        if(clientData != null)
        {
            locked = clientData.locked;
            user = clientData.user;
            // info do ping
            JSONObject ping_info = clientData.ping_info;
            if(ping_info != null)
            {
                slaveOnline = ping_info.containsKey("slaveOnline") ? (boolean)ping_info.get("slaveOnline") : false;
            }
            //ip = clientData.ip;
            //port = clientData.port;
            //status = clientData.status.toString();
            if(!CmdBroadcasterProtocol.ULTIMA_VERSAO.equals(clientData.version))
            {
                String prot = CmdBroadcasterProtocol.ULTIMA_VERSAO.replaceAll("[a-z]$", "");
                String client_prot = clientData.version.replaceAll("[a-z]$", "");
                if(!prot.equals(client_prot))
                {
                    veryold = true;
                }
                else
                {
                    old = true;
                }
            }
        }
        int chw = g.getFontMetrics().charWidth('-');
        int chh = g.getFontMetrics().getHeight();
        int charOff = (user.length()*chw/2);
        
        
        
        // selecao
        int pad = idx/4;
        int left = charOff;
        int bottom = (int)(idx*0.8);
        
        int selx1 = ipx-left-pad;
        int sely1 = ipy-pad;
        int selx2 = ipx+idx+left+pad;
        int sely2 = ipy+idy+pad+bottom;
        if(!hover && selected)
        {
            g.setColor(ColorControler.PC_SELECTEDFILL);
            g.fillRoundRect(selx1,sely1,selx2 - selx1,sely2 - sely1,pad,pad);
            g.setColor(ColorControler.PC_SELECTEDDRAW);
            g.drawRoundRect(selx1,sely1,selx2 - selx1,sely2 - sely1,pad,pad);
        }
        else if(hover && !selected)
        {
            g.setColor(ColorControler.PC_HOVERFILL);
            g.fillRoundRect(selx1,sely1,selx2 - selx1,sely2 - sely1,pad,pad);
            g.setColor(ColorControler.PC_HOVERDRAW);
            g.drawRoundRect(selx1,sely1,selx2 - selx1,sely2 - sely1,pad,pad);
        }
        else if(hover && selected)
        {
            g.setColor(ColorControler.PC_HOVER_SELECTEDFILL);
            g.fillRoundRect(selx1,sely1,selx2 - selx1,sely2 - sely1,pad,pad);
            g.setColor(ColorControler.PC_HOVER_SELECTEDDRAW);
            g.drawRoundRect(selx1,sely1,selx2 - selx1,sely2 - sely1,pad,pad);
        }
        
        

        
            
        // detalhes
        g.setColor(ColorControler.PC_TEXT);
        g.drawString(user, ipx - charOff, ipy+ idy + chh);
        //if(hover)
        //{
        //    g.drawString(ip+":"+port, ipx-charOff, ipy+ idy + chh*2);
        //    g.drawString(status, ipx-charOff, ipy+ idy + chh*3);
        //}
        
        // nodo
        
        
        if(clientData == null)
        g.setColor(ColorControler.PC_NULL);
        else if(clientData.status == ClientStatusManager.ClientStatus.Desconectado)
        g.setColor(ColorControler.PC_DISCONNECTED);
        else if(clientData.status == ClientStatusManager.ClientStatus.Processando)
        g.setColor(ColorControler.PC_PROCESSING);
        else if(clientData.status == ClientStatusManager.ClientStatus.Aguardando)
        {
            if(old)
            {
                g.setColor(ColorControler.PC_VERYOLDFILL);
            }
            else if(veryold)
            {
                g.setColor(ColorControler.PC_VERYOLDFILL);
            }
            else
            {
                g.setColor(ColorControler.PC_WAITING);
            }
        }
        else if(clientData.status == ClientStatusManager.ClientStatus.Iniciando)
        g.setColor(ColorControler.PC_START);
        
        

        
        if(lastscreen != null && clientData.status != ClientStatusManager.ClientStatus.Desconectado)
        {
            int backgroundx = ipx-idx;
            int backgroundy = ipy-idy/2;
            int backgroundw = idx*3; 
            double background_aspect = (double)lastscreen.getWidth()/(double)lastscreen.getHeight();
            int backgroundh = (int)(backgroundw / background_aspect);
            //System.out.println(backgroundx+","+ backgroundy+","+ backgroundw+","+ backgroundh);
            
            g.fillRect(backgroundx-3, backgroundy-3, backgroundw+6, backgroundh+6);
            g.setColor(ColorControler.PC_DRAW);
            g.drawRect(backgroundx-3, backgroundy-3, backgroundw+6, backgroundh+6);
            g.drawImage(lastscreen, backgroundx, backgroundy, backgroundw, backgroundh, null);
            

        }
        else
        {
            g.fillRect(ipx, ipy, idx, idy);
            
            g.setColor(ColorControler.PC_DRAW);
            g.drawRect(ipx, ipy, idx, idy);
        }
        
        // cadeado
        if(locked)
        {
            g.setColor(Color.RED);
            int lock_px = ipx+(3*idx/4);
            int lock_py = ipy-idy/4;
            int lock_dx = idx/2;
            int lock_dy = idy/2;
            g.fillRect(lock_px,lock_py,lock_dx,lock_dy);
            g.fillArc(lock_px, lock_py-lock_dy/2, lock_dx, lock_dy, 0, 180);
            g.setColor(ColorControler.MAPA_FUNDO);
            g.fillArc(lock_px+2, lock_py-lock_dy/2+2, lock_dx-4, lock_dy-4, 0, 180);
        }
        
        // alerta
        if(clientData != null && clientData.status != ClientStatusManager.ClientStatus.Desconectado)
        {
            if(!slaveOnline)
            {
                
                int alert_px0 = ipx-(idx/4);
                int alert_py0 = ipy-(idy/4);

                int alert_px1 = alert_px0-idx/3;
                int alert_py1 = ipy+idy/2;

                int alert_px2 = alert_px0+idx/3;
                int alert_py2 = ipy+idy/2;
                g.setColor(Color.YELLOW);
                g.fillPolygon(
                        new int[]{
                    alert_px0,
                    alert_px1,        
                    alert_px2,        
                }, 
                        new int[]{
                    alert_py0,
                    alert_py1,        
                    alert_py2,
                }, 3);
                g.setColor(Color.BLACK);
                g.drawPolygon(
                        new int[]{
                    alert_px0,
                    alert_px1,        
                    alert_px2,        
                }, 
                        new int[]{
                    alert_py0,
                    alert_py1,        
                    alert_py2,
                }, 3);
                
                g.drawString("!", alert_px0 - chw/2, (alert_py0+alert_py1)/2 + chh/2);
            }
        }
        
        if(statusManager.serverWindow.uploaderHelper != null)
        {
        FileUploaderTask uploaderTask = statusManager.serverWindow.uploaderHelper.uploaderTask;
        if (uploaderTask != null
                && uploaderTask.isRunning()
                && uploaderTask.uuidPresent(uuid)) {
            int progress = uploaderTask.getProgress(uuid);
            if (progress != 0 && progress != 100) {
                drawProgressArrow(g,progress);
            }
        }
        }
    }
    
    private void drawProgressArrow(Graphics g, int progress)
    {
        g.setColor(Color.BLUE);
        int trilarg = (idx*8)/10;
        int tricenterx =ipx + idx + trilarg/2;
        int tricentery =ipy + idy/2 + trilarg/2;
        

        int tax = tricenterx;
        int tay = tricentery;
        
        int tbx = tricenterx - trilarg/2;
        int tby = tricentery - trilarg/2;
        
        g.drawLine(tax,tay,tbx,tby);
        
        int tcx = tbx +trilarg/4;
        int tcy = tby;
        
        g.drawLine(tbx,tby,tcx,tcy);
        
        int tdx = tcx;
        int tdy = tcy - trilarg/2;
        
        g.drawLine(tcx,tcy,tdx,tdy);
        
        int tex = tdx+trilarg/2;
        int tey = tdy;
        
        g.drawLine(tdx,tdy,tex,tey);
        
        int tfx = tex;
        int tfy = tey + trilarg/2;
        
        g.drawLine(tex,tey,tfx,tfy);
        
        int tgx = tfx+trilarg/4;
        int tgy = tfy;
        
        g.drawLine(tfx,tfy,tgx,tgy);
        
        g.drawLine(tgx,tgy,tax,tay);
        
        if(progress < 50)
        {
            int aprogress = 50-progress;
            int inter_ba_x = (tbx*progress + tax*aprogress)/50;
            int inter_ba_y = (tby*progress + tay*aprogress)/50;
            
            int inter_ga_x = (tgx*progress + tax*aprogress)/50;
            int inter_ga_y = (tgy*progress + tay*aprogress)/50;
            
            g.fillPolygon(
                    new int[]{
                tricenterx,
                inter_ba_x,        
                inter_ga_x,        
            }, 
                    new int[]{
                tricentery,
                inter_ba_y,        
                inter_ga_y,
            }, 3);
        }
        else
        {
            g.fillPolygon(
                    new int[]{
                tricenterx,
                tricenterx - trilarg/2,        
                tricenterx + trilarg/2,        
            }, 
                    new int[]{
                tricentery,
                tricentery - trilarg/2,        
                tricentery - trilarg/2,
            }, 3);
                        
            int qheight = ((trilarg/2)*(progress-50))/50;
            g.fillRect(tricenterx-trilarg/4, tricentery-trilarg/2-qheight, trilarg/2, qheight+1);
        }
    }
    
}
