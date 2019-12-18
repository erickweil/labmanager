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

public class ThreadSafeHandler_old<T> {
    private T data = null;
	private int needToRead;
    public long timeout = 1000;
	private synchronized void _setMessage(T data)
	{
		this.data = data;
		this.needToRead = waiting;
		this.notifyAll();
	}
	
	/**
	 * Envia uma mensagem a todos que estão escutando este Handler
	 * @param data objeto a ser enviado
	 * @throws InterruptedException
	 */
	public void sendMessage(T data) throws InterruptedException
	{
        long start = System.currentTimeMillis();
		_setMessage(data);
		while(needToRead > 0)
		{
			Thread.sleep(1);
            long elapsed = System.currentTimeMillis() - start;
            if(elapsed > timeout)
            {
                System.out.println("Can't send to ALL! needToRead:"+needToRead);
                return;
            }
		}
		System.out.println("Send To ALL --> '"+data+"' ");
	}
	
	private int waiting;
	private synchronized void setwaiting()
	{
		waiting++;
		//System.out.println("setwaiting() waiting:"+waiting+" needToRead:"+needToRead);
	}
	private synchronized void setawake()
	{
		waiting--;
		if(needToRead > 0) needToRead--;
		//System.out.println("setawake() waiting:"+waiting+" needToRead:"+needToRead);
	}
	
	/**
	 * Aguarda até que a próxima mensagem esteja pronta para ser lida,
	 * deve ser chamado dentro de um bloco synchronized(handler)
	 */
	public synchronized void waitMessage() {
		// TODO Auto-generated method stub
		try {
			this.setwaiting();
			this.wait();
			this.setawake();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			this.setawake();
			e.printStackTrace();
		}
	}
	
	/**
	 * retorna quantos estão esperando por mensagens
	 * @return
	 */
	public synchronized int getwaiting()
	{
		return waiting;
	}
	
    
	/**
	 * lê a mensagem atual do handler
	 * @return
	 */
	public synchronized T getMessage()
	{
		return data;
	}

}
