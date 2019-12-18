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
package br.erickweil.labmanager.client.slave;

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labmanager.client.protocol.LabProtocol;
import br.erickweil.labmanager.threadsafeness.ThreadSafeListener;
import java.util.HashMap;

/**
 *
 * @author Usuario
 */
public class SlaveStatusManager extends ThreadSafeListener<BroadcasterListener>{

    //BroadcasterListener listener;
    public HashMap<Integer,BroadcasterListener> response_listeners;

    
    public SlaveStatusManager()
    {        
        response_listeners = new HashMap<>();
    }
     
    @Override
    protected synchronized void handleEvent(int event, Object... args) {
        switch (event) {
            case SlaveApp.CLIENT_RESPONSE:
                int msg_uuid = (int) args[0];
                String status = (String) args[1];
                
                BroadcasterListener broadListener = response_listeners.remove(msg_uuid); // pega e já remove.
                                                                                         // para nao listenar mensagens que nao é para
                if(broadListener != null)
                {
                    if(args[2] == null)
                        args[2] = new LabProtocol.Response(msg_uuid,status);
                    
                    broadListener.onResponse(null,status,args[2]);
                }
                //else System.err.println("responseListener null na msg:"+msg_uuid);
                /*
                if(listener != null)
                {
                    //if( listener_msg_uuid == msg_uuid)
                    //{
                    if(args[2] == null)
                    {
                        listener.onResponse(null, status, new LabProtocol.Response(msg_uuid,status));
                    }
                    else
                    {
                        listener.onResponse(null, status, args[2]);
                    }

                        // para nao listenar mensagens que nao é para
                    //    listener = null;
                    //}
                    //else System.err.println("SlaveStatus uuid nao bateu:"+msg_uuid);
                }
                else System.err.println("SlaveStatus listener == null");
                */
            break;
        }
    }
    
        @Override
    public void registerResponseListener(int msg_uuid, int nResponses, BroadcasterListener listener) {
        this.response_listeners.put(msg_uuid, listener);
        //System.err.println("responseListener registering... na msg:"+msg_uuid);
        /*if(nResponses > 0)
        {
            this.response_waitcount.put(msg_uuid, nResponses);
        }
        else
        {
            this.response_waitcount.put(msg_uuid, connectedClients());
        }*/
    }
/*
    @Override
    public synchronized void registerResponseListener(int msg_uuid, int nResps, BroadcasterListener listener) {
        this.listener = listener;
    }*/
    
}
