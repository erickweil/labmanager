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

import br.erickweil.labamanger.common.files.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Configurable {
	private Class<?> my_This;
	private Map<String,Field> my_FieldMap;
	public File my_configFile;
	public static String Solucao_Erroaoler = "o arquivo de configuração não foi encontrado, ou algo impediu sua leitura, dessa forma todas as configurações desse arquivo continuarão com valor padrão, isso pode causar problemas se o valor padrão não for o correto para sua máquina, como por exemplo um caminho de arquivo, um endereço de ip, usuário e senha do banco de dados, etc..., para resolver isso você deve reinstalar esse programa, e configurá-lo novamente, ou chame o criador desse programa ";
	public static String Solucao_ErroParse= "erro interno ao tentar entender o arquivo, talvez o arquivo esteja corrompido ou contém um conteúdo inesperado , reinstale o programa, ou instale uma versão mais recente dele, chame o criador desse programa para resolver esse problema";
	public static String Solucao_CampoInexistente = "uma configuração tentou setar um valor em um campo que não existe, talvez o arquivo esteja corrompido, reinstale o programa, ou instale uma versão mais recente dele, chame o criador desse programa para resolver esse problema";
	public static String Solucao_ErroAoSetar = "Não foi possivel aplicar o valor no campo, isso pode acontecer se por acaso o nome do campo que essa configuração se refere, acidentalmente foi marcado como privado ou se o tipo do valor não é o adequado, instale uma versão mais recente do programa onde esse problema pode ter sido resolvido, ou chame o criador desse programa para resolver esse problema";
	public static String Solucao_ValorInvalido = "não foi possível extrair corretamente o nome do campo e que valor ele deveria receber, dessa forma esse campo continuará com valor padrão, isso pode causar problemas se o valor padrão não for o correto para sua máquina, como por exemplo um caminho de arquivo, um endereço de ip, usuário e senha do banco de dados, etc..., para resolver isso você deve reinstalar esse programa, e configurá-lo novamente, ou chame o criador desse programa";
	private final Logger logger;
	public Configurable(Logger logger,Class<?> classe,String filename)
	{
		this.logger = logger;
		Configurations.configs.add(this);
		try 
		{
		//my_This = (Class<? extends Configurable>) this.getClass();
		my_This = classe;
		Field[] fields = my_This.getFields();
		HashMap<String,Field> tempmap = new HashMap<String,Field>();
		for(int i =0;i<fields.length;i++)
		{
			String nome = fields[i].getName();
			if(nome.startsWith("_")&&Modifier.isStatic(fields[i].getModifiers()))
			{
			tempmap.put(nome.substring(1).toLowerCase(), fields[i]);
			}
		}
		my_FieldMap = new TreeMap<String, Field>(tempmap);
		boolean Carregou=false;
		my_configFile = new File("configs/"+filename+".txt");
		if(my_configFile!=null)
		{
			if(my_configFile.exists())
			{
				ParseAll();
				Carregou=true;
			}
		}
		if(!Carregou)
		{
			logger.LOG("Erro ao Ler as Configurações:Arquivo de configuracao não encontrado, criando um padrão em:"
					+ "\n"+my_configFile);
			Save();
		}
		}
		catch (Exception e) 
		{
			logger.Erro(this,"Erro ao Ler as Configurações de "+filename+" :"+e.getMessage(),Solucao_Erroaoler,e);
			e.printStackTrace();
		}
	}
	public void ParseAll()
	{
		BufferedReader reader=null;
		try 
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(my_configFile)));
			String line;
			while((line = reader.readLine())!=null)
			{
				try {
					if(!line.trim().startsWith("//") && !line.trim().isEmpty())
					ParseLine(reader,line);
				} catch (Exception e) {
					logger.Erro(this,"Erro na linha "+line+" :"+e.getMessage(),Solucao_ErroParse,e);
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			logger.Erro(this,"Erro ao Ler as Configurações:Erro de arquivo nao encontrado ao tentar ler o arquivo :"+my_configFile,Solucao_Erroaoler,e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.Erro(this,"Erro ao Ler as Configurações:Erro IOException ao tentar ler o arquivo :"+my_configFile,Solucao_Erroaoler,e);
			e.printStackTrace();
		}
		finally{
			try {
			if(reader!=null)
			reader.close();
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	private void ParseLine(BufferedReader reader,String line) throws IOException
	{
		int index_equals =line.indexOf("=");
		String field_name = line.substring(0,index_equals).toLowerCase().trim();
		String field_value = line.substring(index_equals+1,line.length()).trim();
		//System.out.println("field:"+field_name+" value:"+field_value);
        if(ValidFieldName(field_name))
		{
			Field f = my_FieldMap.get(field_name);
            if(f!=null)
            {
                String type = Tipo(f);
                if(type.endsWith("[]"))
                {
                    type = type.substring(0, type.length()-2);
                    List<Object> arr = new ArrayList<>();
                    while(true)
                    {
                        line = reader.readLine();
                        if(line == null)
                        {
                            logger.Erro(this,"Erro ao Ler as Configurações:Array incompleto:"+field_name,Solucao_ValorInvalido,null);
                        }
                        else if(line.trim().equals(";"))
                        {
                            break;
                        }
                        else
                        {
                            line = line.trim();
                            if(!line.isEmpty() && !line.startsWith("//"))
                            {
                                Object value = ParseField(type, field_name,line.trim());
                                arr.add(value);
                            }
                        }
                    }
                    Object final_array = fillArray(type, arr);
                    if(final_array != null)
                        SetValue(f,final_array);
                }
                else
                {
                    Object value = ParseField(type, field_name,field_value);
                    if(value != null)
                        SetValue(f,value);
                }
            }
            else
            {
                logger.Erro(this,"Erro ao Ler as Configurações:Nome de campo inexistente:"+field_name,Solucao_CampoInexistente,null);
            }
        }
		else
		{
			logger.Erro(this,"Erro ao Ler as Configurações:Nome de campo inválido:"+field_name,Solucao_ValorInvalido,null);
		}
    }
    
    private Object ParseField(String type,String field_name,String field_value)
	{
            switch(type)
            {
            case "java.lang.String":
            if(ValidStringValue(field_value))
            {
                String v = field_value.substring(1, field_value.length()-1).replaceAll("[^\\\\]\\\\n","\n").replace("\\\\","\\");
                return v;
            }
            else
                logger.Erro(this,"Erro ao Ler as Configurações:Valor Inválido para "+type+" '"+field_value+"' no campo "+field_name,Solucao_ValorInvalido,null);
            break;
            case "int":
                if(ValidIntegerValue(field_value))
                {
                    int v = Integer.parseInt(field_value);
                    return v;
                }
                else
                    logger.Erro(this,"Erro ao Ler as Configurações:Valor Inválido para "+type+" '"+field_value+"' no campo "+field_name,Solucao_ValorInvalido,null);
                break;
            case "double":
                if(ValidFloatValue(field_value))
                {
                    double v = Double.parseDouble(field_value);
                    return v;
                }
                else
                    logger.Erro(this,"Erro ao Ler as Configurações:Valor Inválido para "+type+" '"+field_value+"' no campo "+field_name,Solucao_ValorInvalido,null);
                break;
            case "float":
                if(ValidFloatValue(field_value))
                {
                    float v = Float.parseFloat(field_value);
                    return v;
                }
                else
                    logger.Erro(this,"Erro ao Ler as Configurações:Valor Inválido para "+type+" '"+field_value+"' no campo "+field_name,Solucao_ValorInvalido,null);
                break;
            case "boolean":
                if(ValidBooleanValue(field_value))
                {
                    switch(field_value)
                    {
                    case "true":
                    case "1":
                    case "sim":
                    case "ligado":
                    case "on": return true;
                    case "false":
                    case "0":
                    case "mentira":
                    case "desligado":
                    case "off": return false;
                    //"(true|false|1|0|sim|nao|verdade|mentira|ligado|desligado|on|off)"
                    }
                }
                else
                    logger.Erro(this,"Erro ao Ler as Configurações:Valor Inválido para "+type+" '"+field_value+"' no campo "+field_name,Solucao_ValorInvalido,null);
                break;
            default:
                logger.Erro(this,"Erro ao Ler as Configurações:Tipo de campo inexistente:"+type,Solucao_ValorInvalido,null);
                return null;
            }

        return null;
	}
    
    private Object fillArray(String type,List<Object> list)
    {
         switch(type)
         {
            case "int":
            {
                int[] arr = new int[list.size()];
                for(int i=0;i<list.size();i++){arr[i] = (int)list.get(i);}
            return arr;
            }
            case "float":
            {
                float[] arr = new float[list.size()];
                for(int i=0;i<list.size();i++){arr[i] = (float)list.get(i);}
            return arr;
            }
            case "double":
            {
                double[] arr = new double[list.size()];
                for(int i=0;i<list.size();i++){arr[i] = (double)list.get(i);}
            return arr;
            }
            case "boolean":
            {
                boolean[] arr = new boolean[list.size()];
                for(int i=0;i<list.size();i++){arr[i] = (boolean)list.get(i);}
            return arr;
            }
            case "java.lang.String":
            {
                String[] arr = new String[list.size()];
                arr = list.toArray(arr);
            return arr;
            }
         }
         return null;
    }
    
	public void Save()
	{
		if(!my_configFile.exists())
		{
			try {
				if(!my_configFile.getParentFile().exists())
				{
					my_configFile.getParentFile().mkdirs();
				}
				my_configFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		System.out.println("configurações :"+my_configFile.getAbsolutePath());
		Set<String> keys = my_FieldMap.keySet();
		String[] array = new String[keys.size()];
	    array = keys.toArray(array);
	    
	    BufferedWriter writer=null;
		try 
		{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(my_configFile)));
			for(int i=0;i<array.length;i++)
			{
				Field f = my_FieldMap.get(array[i]);
				//if(f.getName().startsWith("_")&&Modifier.isStatic(f.getModifiers()))
				//{
					String fname = f.getName().substring(1);
					try 
					{
						Object value = f.get(null);
						if(value!=null)
						{
                            if(value instanceof String[])
							{
                                writer.write(fname+" =");
                                writer.newLine();
                                String[] arr = (String[]) value;
                                for(String s : arr)
                                {
                                    writer.write("\""+s.replace("\\","\\\\").replace("\n","\\n")+"\"");
                                    writer.newLine();
                                }
                                writer.write(";");
                                writer.newLine();
                            }
                            else if(value instanceof int[])
							{
                                writer.write(fname+" =");
                                writer.newLine();
                                int[] arr = (int[]) value;
                                for(int s : arr)
                                {
                                    writer.write(""+s);
                                    writer.newLine();
                                }
                                writer.write(";");
                                writer.newLine();
                            }
                            else if(value instanceof float[])
							{
                                writer.write(fname+" =");
                                writer.newLine();
                                float[] arr = (float[]) value;
                                for(float s : arr)
                                {
                                    writer.write(""+s);
                                    writer.newLine();
                                }
                                writer.write(";");
                                writer.newLine();
                            }
                            else if(value instanceof double[])
							{
                                writer.write(fname+" =");
                                writer.newLine();
                                double[] arr = (double[]) value;
                                for(double s : arr)
                                {
                                    writer.write(""+s);
                                    writer.newLine();
                                }
                                writer.write(";");
                                writer.newLine();
                            }
                            else if(value instanceof boolean[])
							{
                                writer.write(fname+" =");
                                writer.newLine();
                                boolean[] arr = (boolean[]) value;
                                for(boolean s : arr)
                                {
                                    writer.write(""+s);
                                    writer.newLine();
                                }
                                writer.write(";");
                                writer.newLine();
                            }
                            else if(value instanceof String)
							{
								writer.write(fname+" = \""+value.toString().replace("\n","\\n")+"\"");
								writer.newLine();
							}
							else
							{
								writer.write(fname+" = "+value.toString());
							    writer.newLine();
							}
						}
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
			if(writer!=null)
				writer.close();
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	    
	    

	}
	private String Tipo(Field f)
	{
		return f.getType().getCanonicalName();
	}
	private void SetValue(Field f,Object v)
	{
		try {
			System.out.println("SETANDO: "+f.getName()+" = "+v);
			f.set(null, v);
		} catch (IllegalArgumentException e) {
			logger.Erro(this,"Erro ao Setar as Configurações:"+e.getMessage(),Solucao_ErroAoSetar ,e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.Erro(this,"Erro ao Setar as Configurações:"+e.getMessage(),Solucao_ErroAoSetar ,e);
			e.printStackTrace();
		}
	}
	private boolean ValidFieldName(String name)
	{
		return name!=null && !name.isEmpty();
	}
	private boolean ValidStringValue(String value)
	{
		return value!=null && !value.isEmpty() && value.startsWith("\"")&&value.endsWith("\"");
	}
	private boolean ValidIntegerValue(String value)
	{
		return value!=null && !value.isEmpty() && value.matches("[+-]?[0-9]+");
	}
	private boolean ValidFloatValue(String value)
	{
		return value!=null && !value.isEmpty() && value.matches("[+-]?[0-9]*\\.?[0-9]+");
	}
	private boolean ValidBooleanValue(String value)
	{
		return value!=null && !value.isEmpty() && value.matches("(true|false|1|0|sim|nao|verdade|mentira|ligado|desligado|on|off)");
	}
}
