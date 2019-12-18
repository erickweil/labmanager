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
package br.erickweil.webserver.pages;

import java.io.BufferedWriter;
import java.io.IOException;

import br.erickweil.webserver.ServerHttp;
import br.erickweil.webserver.ServerPage;

public class PageError extends ServerPage{
	int error;
	public PageError(ServerHttp server,int error)
	{
		super(server);
		this.error = error;
	}
	@Override
	public void get() throws IOException {
		// TODO Auto-generated method stub
		echo("<!DOCTYPE html>");
		echo("<html>");
		echo("<head>");
			echo("<meta charset=\"utf-8\">");
			echo("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
			echo("<!--[if lt IE 9]>");
			echo("	<script src=\"http://html5shiv.googlecode.com/svn/trunk/html5.js\"></script>");
			echo("<![endif]-->");
			echo("<title>Java WebServer</title>");
		echo("</head>");
		echo("<body>");
			echo("<h1>Erro "+error+"</h1>");
		echo("</body>");
		echo("</html>");
	}
	

}
