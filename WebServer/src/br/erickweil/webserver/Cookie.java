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
package br.erickweil.webserver;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class Cookie {
	
	String name;
	String value;
	String domain;
	String path;
	Date expires;
	boolean httpOnly;
	boolean secure;
	
	public Cookie()
	{
		
	}
	
	public static HashMap<String,String> decodeCookies(String CookieString)
	{
		HashMap<String, String> cookies = new LinkedHashMap<>();
		String[] cookies_split;
		if(CookieString.contains(";"))
		{
			cookies_split = CookieString.split(";");
		}
		else
		{
			cookies_split = new String[]{CookieString};
		}
		
		for(int i=0;i<cookies_split.length;i++)
		{
			if(cookies_split[i].contains("="))
			{
				String[] key_value = cookies_split[i].split("=");
				String key = key_value[0].trim();
				String value = key_value[1].trim();
				cookies.put(key.toLowerCase(), value);
			}
			else
			{
				cookies.put(cookies_split[i].trim().toLowerCase(), null);
			}
		}
		
		return cookies;
	}
	
	public static String encodeCookies(HashMap<String,String> cookies)
	{
		StringBuilder result = new StringBuilder();
		
		Set<String> keyset = cookies.keySet();
		String[] keys = new String[keyset.size()];
		keys = keyset.toArray(keys);
		boolean primeiro = true;
		for(int i=0;i<keys.length;i++)
		{
			String key = keys[i];
			String value = cookies.get(key);
			if(!primeiro)
			{
				result.append("; ");
			}
			if(value != null)
			{
				result.append(key);
				result.append("=");
				result.append(value);
			}
			else
			{
				result.append(key);
			}
			primeiro = false;
		}
		return result.toString();
	}
	
}
