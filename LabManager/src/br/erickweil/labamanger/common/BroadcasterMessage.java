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
package br.erickweil.labamanger.common;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 *<strong> Classe que abstrai a idéia de uma mensagem a ser transmistida ( broadcasted ) a um ou a todos os
 * clientes que estiverem escutando. </strong>

 * <p>o campo threadname identifica se é Todos ( Broadcast.All ) ou um cliente específico.</p>
 * 
 */
public class BroadcasterMessage {
    public static enum MessageResponse {
        none,
        text,
        binary
    };
    // deveria mudar depois para comunicação binária? api?
    public static enum Messages {
        start,
        stop,
        shutdown,
        restart,
        logoff,
        cancelshutdown,
        msg,
        lockscreen,
        unlockscreen,
        exit,
        ping,
        blacklist,
        download,
        browse,
        broadcast,
        admin_exec,
        exec,
        admin_download,
        printscreen,
        remotecontrol,
        windowlist;
        
        public MessageResponse responseType()
        {
            switch(this){
                case admin_exec:
                case exec:
                case ping:
                case windowlist: return MessageResponse.text;
                case printscreen: return MessageResponse.binary;
                
                default: return MessageResponse.none;
            }
        }
        
        public boolean executeAsMaster()
        {
            switch(this){
                case stop:
                case shutdown:
                case restart:
                case logoff:
                case cancelshutdown:
                case admin_exec:
                case admin_download:
                case blacklist:
                    return true;
                
                default: return false;
            }
        }
        
        public boolean executeAsMasterIfSlaveOffline()
        {
            switch(this){
                case ping:
                case download:
                    return true;
                
                default: return false;
            }
        }
        
        public boolean executeAsBoth()
        {
            switch(this){
                //case blacklist:
                //    return true;
                
                default: return false;
            }
        }
            
    };
    
    public static final String All = "all";
    
    public final String threadname;
    public final Messages cmd;
    public final String[] arguments;
    public final byte[] binary_data;
    public final int msg_uuid;

    public BroadcasterMessage(String threadname,Messages cmd,String[] arguments) {
        this.threadname = threadname;
        this.cmd = cmd;
        this.arguments = arguments;
        this.binary_data = null;
        this.msg_uuid = (new Random()).nextInt();
    }
    
    public BroadcasterMessage(String threadname,Messages cmd,String[] arguments,byte[] binary_data) {
        this.threadname = threadname;
        this.cmd = cmd;
        this.arguments = arguments;
        this.binary_data = binary_data;
        this.msg_uuid = (new Random()).nextInt();
    }
    
    @Override
    public String toString()
    {
        return threadname + " "+cmd+" "+Arrays.toString(arguments);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(threadname);
        hash = 59 * hash + Objects.hashCode(this.cmd);
        hash = 59 * hash + Arrays.deepHashCode(this.arguments);
        hash = 59 * hash + this.msg_uuid;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            throw new Exception("EQUALS DO BROADCASTERMESSAGE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BroadcasterMessage other = (BroadcasterMessage) obj;
        
        if(!this.threadname.equals(other.threadname)) return false;
        
        if(!this.cmd.equals(other.cmd)) return false;
        
        //if(this.hashCode() != other.hashCode()) return false;
        
        if(!Arrays.equals(this.arguments, other.arguments)) return false;
        
        if(!Arrays.equals(this.binary_data, other.binary_data)) return false;
        
        return true;
    }
    
}
