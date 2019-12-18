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
package br.erickweil.labmanager.threadsafeness;

import br.erickweil.labamanger.common.BroadcasterListener;
import br.erickweil.labamanger.common.BroadcasterMessage;
import br.erickweil.labamanger.common.UUIDable;
import java.io.IOException;

/**
 *
 * @author Usuario
 */
public class BlockingListenerHelper extends BroadcasterListener{
    
    private UUIDable response;
    private boolean responseArrived;
    private boolean waiting;
    private int uuidWaiting;
    public static final boolean LOG_msgs = false;
    private synchronized void setResponseArrived(UUIDable r)
    {
        if(waiting && r != null)
        {
            if(r.getUUID() == uuidWaiting)
            {
                response = r;
                responseArrived = true;
            }
            else
            {
                System.err.println("DESCARTADA RESPOSTA '"+r+"' uuid:"+r.getUUID());
            }
        }
    }
    private synchronized boolean hasresponseArrived()
    {
        return responseArrived;
    }
    private synchronized UUIDable getresponseArrived()
    {
        if(LOG_msgs)
            System.out.println("Taken:"+response.getUUID());
        UUIDable r = response;
        
        response = null;
        responseArrived = false;
        
        waiting = false;
        return r;
    }
    private synchronized void setWaiting(int uuid)
    {
        if(LOG_msgs)
        System.out.println("Waiting:"+uuid);
        waiting = true;
        uuidWaiting = uuid;
        
        response = null;
        responseArrived = false;
    }
    
    
    public UUIDable sendAndWaitResponse(final BroadcasterMessage msg, int nResponses,final ThreadSafeHandler<BroadcasterMessage> handler,final ThreadSafeListener<BroadcasterListener> status, long timeout) throws IOException
    {
        status.registerResponseListener(msg.msg_uuid,nResponses,this);
        //final BroadcasterMessage msg = new BroadcasterMessage("thread",BroadcasterMessage.Messages.valueOf(cmd),args,binary_msg);
        
        //if((response.responseType() != BroadcasterMessage.MessageResponse.none) || forceListener)
        //{
            // diz q ta esperando a resposta para essa msg
            setWaiting(msg.msg_uuid);
        //}
        
        
        // envia a msg
        try {
            handler.sendMessage(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        //if((response.responseType() != BroadcasterMessage.MessageResponse.none) || forceListener)
        //{
            try {
                long time_start = System.currentTimeMillis();
                while(!hasresponseArrived() && (System.currentTimeMillis() - time_start) < timeout)
                {
                    Thread.sleep(5);
                }
                
                if(!hasresponseArrived())
                {
                    System.err.println("msg not asnwered:"+msg.cmd);
                    return null;
                }
                
                // pega a resposta que chegou
                UUIDable resp = getresponseArrived();
                
                
                return resp;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            return null;
        //}
        //else
        //{
        //    if(forward)
        //    {
        //        writeln_url("OK");
        //    }
        //    return true;
        //}
    }

    @Override
    public void onResponse(String threadname, String status, Object data) {
        if(data == null)
        {
            System.err.println("UNKNOW RESPONSE:"+status);
        }
        else if(status.equals("OK"))
        {
            //System.out.println("Response Arrived: "+status);
            setResponseArrived((UUIDable)data);
        }
        else
        {
            System.err.println("STATUS NAO OK:"+status);

            setResponseArrived((UUIDable)data);
        }
    }
}
