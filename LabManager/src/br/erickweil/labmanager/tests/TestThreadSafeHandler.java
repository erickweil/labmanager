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
package br.erickweil.labmanager.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import br.erickweil.webserver.ReaderWriter;

public class TestThreadSafeHandler {

	private String txt = "";
	
	private int needToRead;
	private synchronized void _setMessage(String txt)
	{
		this.txt = txt;
		this.needToRead = waiting;
		this.notifyAll();
	}
	
	private void sendMessage(String txt) throws InterruptedException
	{
		_setMessage(txt);
		while(needToRead > 0)
		{
			Thread.sleep(1);
		}
		System.out.println("Send To ALL");
	}
	
	private int waiting;
	public synchronized void setwaiting()
	{
		waiting++;
		//System.out.println("setwaiting() waiting:"+waiting+" needToRead:"+needToRead);
	}
	public synchronized void setawake()
	{
		waiting--;
		if(needToRead > 0) needToRead--;
		//System.out.println("setawake() waiting:"+waiting+" needToRead:"+needToRead);
	}
	public synchronized int getwaiting()
	{
		return waiting;
	}
	
	public synchronized String getMessage()
	{
		//needToRead--;
		return txt;
	}
	
    public static void main(String[] args) throws IOException, InterruptedException {
    	TestThreadSafeHandler theDemo = new TestThreadSafeHandler();

    	long nano = System.nanoTime();
    	for(int i=0;i<100;i++)
    	{
    		TestThread th = new TestThread("THREAD "+i,theDemo,nano);
    		//synchronized (theDemo) {
				th.start();
			//}
    	}
    	
    	for(int i=0;i<50;i++)
    	{
    		theDemo.sendMessage("THREAD "+i);
    	}
        //new TestThread("THREAD 2",theDemo,nano);
        //new TestThread("THREAD 3",theDemo,nano);
        

		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while (!(line = inputReader.readLine()).equals("EXIT")) {
			theDemo.sendMessage(line);
		}
		theDemo.sendMessage(null);
        
    }

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
}

class SOP {
    public static void print(String s) {
        System.out.println(s);
    }
}

class TestThread extends Thread {
    String name;
    TestThreadSafeHandler theDemo;
    long nano;
    public TestThread(String name,TestThreadSafeHandler theDemo,long nano) {
        this.theDemo = theDemo;
        this.name = name;
        this.nano = nano;
    }
    
    public void print(String txt)
    {
    	long ns = System.nanoTime() - nano;
    	String time = String.format("%,d", ns); 
    	System.out.println(name+" time:"+time+" -> "+txt);
    }
    
    @Override
    public void run() {

			String txt;
			synchronized(theDemo)
			{
				while((txt = theDemo.getMessage()) != null)
				{
					print(txt);
					if(txt.equals(name)) break;
					//theDemo.setwaiting();
					//theDemo.wait();
					//theDemo.setawake();
					theDemo.waitMessage();
				}
			}
			
			print("Terminou");

    }
}
