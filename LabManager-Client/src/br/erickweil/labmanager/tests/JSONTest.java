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

import br.erickweil.labmanager.client.ClientApp;
import br.erickweil.labmanager.client.ClientMain;
import br.erickweil.labmanager.cmd.CmdExec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.simple.JSONObject;

/**
 *
 * @author Usuario
 */
public class JSONTest {
    public static void main(String[] args) throws UnsupportedEncodingException
    {
        String user_name = CmdExec.getUser();
        if(user_name == null || user_name.isEmpty())
            user_name = System.getProperty("user.name");
        
        
        long json_elapsed = 0;
        long url_elapsed = 0;
        int Steps = 10;
        for(int i=0;i<Steps;i++)
        {
            long json_start = System.nanoTime();
            JSONObject obj = new JSONObject();
            obj.put("user_name", user_name);
            //obj.put("test", "áéíóúã~e~i~uõÇç!@#$%¨&*()");
            //obj.put("t\nest2\\\n", "\n\r\t\\\"\'");
            obj.put("uuid", ClientMain._uuid);
            String jsonInfo = obj.toJSONString();
            json_elapsed += System.nanoTime() - json_start;
            long url_start = System.nanoTime();
            String encoded = URLEncoder.encode(jsonInfo,"UTF-8")+"\n";
            url_elapsed += System.nanoTime() - url_start;
            if(i == 0)
            {
                System.out.println(jsonInfo);
                System.out.println(URLEncoder.encode(jsonInfo,"UTF-8")+"\n");
            }
        }
        
        System.out.println("JSON:"+((double)(json_elapsed)/1_000_000.0)/(double)Steps);
        System.out.println("URL:"+((double)(url_elapsed)/1_000_000.0)/(double)Steps);
    }
}
