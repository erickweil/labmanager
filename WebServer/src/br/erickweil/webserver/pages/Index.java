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

public class Index extends ServerPage{

	public Index(ServerHttp server) {
		super(server);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void get() throws IOException {
		// TODO Auto-generated method stub
		echo("<!DOCTYPE html>\n");
		echo("<html>\n");
		echo("<head>\n");
			echo("<meta charset=\"utf-8\">\n");
			echo("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
			echo("<!--[if lt IE 9]>\n");
			echo("	<script src=\"http://html5shiv.googlecode.com/svn/trunk/html5.js\"></script>\n");
			echo("<![endif]-->\n");
			echo("<title>Java WebServer</title>\n");
		echo("</head>\n");
		echo("<body>\n");
			echo("<h1>Server funfando, né?</h1>\n");
		echo("</body>\n");
		echo("</html>\n");

	}



}
