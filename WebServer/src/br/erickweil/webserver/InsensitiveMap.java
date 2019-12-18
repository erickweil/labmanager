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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InsensitiveMap
{
	
	List<String> header_keys;
	
	HashMap<String,List<String>> header_values;
	
	public InsensitiveMap(){
		header_values = new HashMap<>();
		header_keys = new ArrayList<>();
	}
	
	private List<String> get_header_values(String key)
	{
		return header_values.get(key.toLowerCase());
	}
	
	public boolean containsKey(String key)
	{
		for(int i=0;i<header_keys.size();i++)
		{
			if(header_keys.get(i).equalsIgnoreCase(key))
			{
				return true;
			}
		}	
		return false;
	}
	
	private void put_header_values(String key,List<String> values)
	{
		if(!containsKey(key))
		{
			header_keys.add(key);
		}
		header_values.put(key.toLowerCase(), values);
	}
	
	public void addHeader(String key,String value)
	{
		if(containsKey(key))
		{
			get_header_values(key).add(value);
		}
		else
		{
			List<String> values = new ArrayList<>();
			values.add(value);
			put_header_values(key,values);
		}

		//System.out.println("Added "+key.toLowerCase()+":"+headers.get(key.toLowerCase()));
	}
	
	public void setHeader(String key,List<String> values)
	{
		put_header_values(key,values);
	}
	
	public void setHeader(String key,String value)
	{
		List<String> values = new ArrayList<>();
		values.add(value);
		put_header_values(key,values);
	}
	
	public List<String> getHeaderValues(String key)
	{
		return get_header_values(key);
	}
	
	public String getHeader(String key)
	{
		List<String> values = get_header_values(key);
		if(values != null && values.size() > 0)
		{
			return values.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public void delHeader(String key)
	{
		int index = -1;
		for(int i=0;i<header_keys.size();i++)
		{
			if(header_keys.get(i).equalsIgnoreCase(key))
			{
				index = i;
				break;
			}
		}
		if(index != -1)
		{
			header_keys.remove(index);
			header_values.remove(key.toLowerCase());
		}
	}
	
	public String[] getHeadersKeys()
	{
		String[] keys = new String[header_keys.size()];
		keys = header_keys.toArray(keys);
		return keys;
	}
}
