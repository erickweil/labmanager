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
 * Classe mais sensível do projeto. trabalha com o envio das mensagens da
 * thread da interface gráfica para todas as thread's de conexão ativas,
 * problemas que devem ser observados nessa classe:
 * - Memory Leak
 * - Thread Syncronization
 * 
 * como é utilizado pelas threads:
 * inicio:
 *    1. aguarda que uma nova mensagem chegue pelo método waitMessage()
 *    2. link é capturado pelo método getMessage()
 * sempre:
 *    3. lê a mensagem pelo método GetData() do DataLink
 *    4. captura o próximo link pelo método next() do DataLink
 *       4.1 aguarda a mensagem pelo método waitMessage() caso o next() retornar nulo.
 * @author Usuario
 * @param <T> 
 */
public class ThreadSafeHandler<T> {
    //private T data = null;
    /*
    private static final class DataLink<T>
    {
        private final T data;
        private DataLink<T> previous;
        private final long nonce;
        private int left_to_read;
        public DataLink(final long nonce,final int registered,final DataLink<T> previous,final T data)
        {
            this.nonce = nonce;
            this.data = data;
            this.previous = previous;
            this.left_to_read = registered;
        }
        
        public final T getData(){
            left_to_read--;
            return data;
        }
        
        public boolean canDelete()
        {
            if(left_to_read < 0)
            {
                System.err.println("ThreadSafeHandler mal implementado, left_to_read negativo:"+left_to_read);
            }
            return left_to_read <= 0;
        }
   
    }
    
    private DataLink<T> dataHolder = null;
    */
    
        public static class DataLink<T>
    {
        private final T data;
        private DataLink<T> next;
        public DataLink(T data)
        {
            this.data = data;
        }
        
        public synchronized T getData()
        {
            return this.data;
        }
        
        public synchronized DataLink<T> next()
        {
            return this.next;
        }
        
        private synchronized void setNext(DataLink<T> next)
        {
            this.next = next;
        }
    }
    
    private DataLink<T> head;
	private int needToRead;
    public long timeout = 1000;
    public boolean instant_send = false;
    private long nonce_count = 0;
    private int waiting;
    private final boolean LOG;

    public ThreadSafeHandler() {
        this.LOG = true;
    }
    
    public ThreadSafeHandler(boolean LOG) {
        this.LOG = LOG;
    }
    
    
	private synchronized void _addMessage(T data)
	{
		this.needToRead = waiting;
        
        DataLink<T> n = new DataLink<>(data);
        if(head != null)
        {
            head.setNext(n);
        }
        head = n;
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
		_addMessage(data);
        
        if(instant_send) return;
        
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
        if(LOG)
		System.out.println("Send To ALL --> '"+data+"' ");
	}
	
	
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
	 * lê a mensagem atual do handler
     * Deve ser usado na primeira vez apenas, depois acessa o next() do DataLink
	 * @return
	 */
    public synchronized DataLink<T> getMessage()
    {
        return head;
    }

}
