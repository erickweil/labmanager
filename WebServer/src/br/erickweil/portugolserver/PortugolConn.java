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
package br.erickweil.portugolserver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.TimeoutException;

public class PortugolConn {
	final String file_input;
	final String file_output;
	public long timeout = -1;
	public long sleeptime = 10;
	public PortugolConn(String file_input,String file_output)
	{
		this.file_input = file_input;
		this.file_output = file_output;
		//try_read();
	}
	
	public void enviar_mensagem(String msg) throws TimeoutException
	{
		try {
			boolean writed = false;
			long started_write = System.currentTimeMillis();
			do
			{
				if(try_write(msg))
					return;
				
				Thread.sleep(sleeptime);
				if(timeout > 0 && (System.currentTimeMillis()-started_write) > timeout)
				{
					throw new TimeoutException();
				}
			}
			while(!writed);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	int last_output_msg_code = -1;
	public String ler_mensagem() throws TimeoutException
	{
		try {
			String msg = null;
			long started_read = System.currentTimeMillis();
			do
			{
				Thread.sleep(sleeptime);
				msg = try_read();
				if(timeout > 0 && (System.currentTimeMillis()-started_read) > timeout)
				{
					throw new TimeoutException();
				}
			}
			while(msg == null);
			return msg;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private String try_read()
	{
		 String t = Readtxt(file_output);
		 if(t == null)
		 {
			 System.err.println("Mensagem Nula");
			 return null;
		 }
		 // a mensagem não é válida ainda
		 if(!t.contains("\r\n") || !t.endsWith("\r\nFIM\r\n")) 
		 {
			 System.err.println("Mensagem Incompleta");
			 return null;
		 }
		 // le o header da mensagem, que é um número que incrementa a cada mensagem
		 // se não é um número, ou se é igual ao último, descarta a mensagem
		 String txt_msg_code = t.substring(0,t.indexOf("\r\n"));
		 if(!txt_msg_code.matches("^\\d+$"))
		{
			 System.err.println("Identificador Inválido");
			 return null;
		}
		 int msg_code = Integer.parseInt(txt_msg_code);
		 if(msg_code == last_output_msg_code)
		 {
			 //System.out.println("Identificador Antigo");
			 return null;
		 }
		 last_output_msg_code = msg_code;
		 
		 // numero\r\nmensagemmensagemmensagem\r\nFIM\r\n
		 String actual_msg = t.substring(t.indexOf("\r\n")+2,t.length()-7);
		 
		 return actual_msg;
	}
	
	int input_msg_code = 0;
	private boolean try_write(String msg)
	{
		String t = input_msg_code+"\n"+msg+"\nFIM\n";
		
		try {
			Writetxt(file_input,t);
			input_msg_code++;
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
	}
	
	private static void Writetxt(String path,String content) throws IOException
	{
		Files.write(new File(path).toPath(), content.getBytes());
	}
	
	private static String Readtxt(String path)
	{
		String content=null;

		try {
			//content = new String(Files.readAllBytes(Paths.get(path)));
			File arquivo = new File(path);
			if(arquivo.exists())
			{
				content = new String(Files.readAllBytes(arquivo.toPath()),Charset.forName("Cp1252"));
			}
			else
			{
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
}
