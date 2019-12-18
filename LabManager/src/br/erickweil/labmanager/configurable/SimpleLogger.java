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
package br.erickweil.labmanager.configurable;

//import javax.swing.JOptionPane;
//import javax.swing.JPanel;

public class SimpleLogger implements Logger {

	@Override
	public void LOG(String msg) {
		// TODO Auto-generated method stub
		System.out.println(msg);
	}

	@Override
	public void Erro(Object Origem, String estado, String solucao,
			Throwable erro) {
		// TODO Auto-generated method stub
        System.out.println(estado+"\n Solução:"+solucao+"\n Origem:"+Origem);
		//final JPanel panel = new JPanel();
		//panel.setLocation((int)(Math.random()*1000.0),(int)(Math.random()*500.0));
	    //JOptionPane.showMessageDialog(panel,estado+"\n Solução:"+solucao+"\n Origem:"+Origem, "Erro", JOptionPane.ERROR_MESSAGE);
	}

}
