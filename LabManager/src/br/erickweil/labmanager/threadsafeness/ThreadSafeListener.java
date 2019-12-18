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
package br.erickweil.labmanager.threadsafeness;

/**
 * É o canal de comunicação entre os eventos da Thread da Conexão e
 * A interface gráfica
 * handleEvent -> a interface implementa
 * sendEvent -> a thread da conexão usa para enviar um evento
 * @author Aluno
 */
public abstract class ThreadSafeListener<T> {
    
    public ThreadSafeListener()
    {
    }
    
    protected abstract void handleEvent(int event,Object ... args);
    
    public synchronized void sendEvent(int event,Object ... args)
    {
        try
        {
            handleEvent(event, args);
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public abstract void registerResponseListener(int msg_uuid, int nResponses,T listener);
}
