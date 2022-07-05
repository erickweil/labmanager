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

/**
 * Classe que permite uma tarefa ser parada. auxilia execução de threads concorrentes.
 * @author Usuario
 */
public abstract class StoppableTask implements Runnable{
    private Thread thread;
    private volatile boolean running = false;
    
    protected abstract void on_stopTask();
    
    public synchronized boolean isRunning()
    {
        return running;
    }
    
    private synchronized void setRunning(boolean running)
    {
        this.running = running;
    }
    
    public void startTask()
    {
        if(isRunning()) return;
        
        this.thread = new Thread(this);
        this.thread.start();
    }
    
    public void stopTask()
    {
        if(!isRunning()) return;
        
        try{
            on_stopTask();
        }catch(Exception e){e.printStackTrace();}
        
        try
        {
            Thread.sleep(10);

            if(isRunning())
            {
                System.out.println("Teve que dar o interrupt na Tarefa");
                thread.interrupt();
                Thread.sleep(10);
                if(isRunning())
                {
                    System.out.println("Teve que dar o stop na Tarefa");
                    //thread.stop();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public final void run()
    {
        this.setRunning(true);
        try
        {
            runTask();
        }
        finally
        {
            this.setRunning(false);
        }
    }
    
    public abstract void runTask();
}
