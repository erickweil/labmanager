/*
 * Copyright (C) 2018 Usuario
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

import br.erickweil.labamanger.common.files.TestEncrypt;
import br.erickweil.labmanager.cmd.CmdExec;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JTextArea;

/**
 *
 * @author Usuario
 */
public class OpensslHelper {
    

    
     
    public OpensslHelper()
    {

    }
    
    /*
     * 
    resumo:
    openssl genrsa -out server.key 4096
    openssl rsa -in server.key -out nserver.key
    openssl req -sha256 -new -key server.key -out server.csr
        Country Name (2 letter code) [AU]:BR
        State or Province Name (full name) [Some-State]:Rondonia
        Locality Name (eg, city) []:Vilhena
        Organization Name (eg, company) [Internet Widgits Pty Ltd]:Kcire Systems
        Organizational Unit Name (eg, section) []:Kcire
        Common Name (e.g. server FQDN or YOUR name) []:VHA-LABADS-PROF
        Email Address []:erick.weil@ifro.edu.br

        Please enter the following 'extra' attributes
        to be sent with your certificate request
        A challenge password []:1a2b3c4d
        An optional company name []:Kcire Systems

    openssl x509 -req -sha256 -days 1095 -in server.csr -signkey nserver.key -out server.crt
    openssl x509 -in server.crt -text -noout
    openssl pkcs12 -export -in server.crt -inkey server.key -certfile server.crt -out server_pkcs.p12
    keytool -importkeystore -srckeystore server_pkcs.p12 -srcstoretype pkcs12 -destkeystore server_keystore.jks -deststoretype JKS
    keytool -importcert -keystore client_trustore.jks -storepass 1a2b3c4d -file server.crt

    referencias:
    https://stackoverflow.com/questions/10175812/how-to-create-a-self-signed-certificate-with-openssl?rq=1
    http://xacmlinfo.org/2014/06/13/how-to-keystore-creating-jks-file-from-existing-private-key-and-certificate/
    https://www.openssl.org/docs/manmaster/man1/
    https://www.cloudera.com/documentation/enterprise/5-2-x/topics/cm_sg_create_key_trust.html
     
    https://ouestcode.com/journal/archive/2014-generate-self-signed-ssl-certificate-without-prompt-noninteractive-mode.html
    
    openssl genrsa -des3 -passout pass:p4ssw0rd -out server.pass.key 2048
    openssl rsa -passin pass:p4ssw0rd -in server.pass.key -out server.key
    rm server.pass.key
    openssl req -new -key server.key -out server.csr \
      -subj "/C=UK/ST=Warwickshire/L=Leamington/O=OrgName/OU=IT Department/CN=example.com"
    openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
    
    */
    
    public static void exec(JTextArea out,String ... args)
    {
        if(out != null)out.append(String.join(" ", args)+"\n");
        List<String> cmdResults = new ArrayList<>();
            
        cmdResults = CmdExec.readCmd(args);
        if(cmdResults == null || out == null) return;
        for(String line : cmdResults)
        {
            out.append("\t"+line+"\n");
        }
    }

    
    public static void genSelfSigned(
        String strBits,
        String strCity,
        String strCommonName,
        String strCompany,
        String strCountry,
        String strEmail,
        String strExpiration,
        String strOrgName,
        String strOrgUnit,
        String strProtocolo,
        String strState,
        String certificatePassword,
        JTextArea consoleOut
    )
    {
        
        try {
            if(!(new File("OpenSSL\\bin\\openssl.exe").exists()))
            {
                if(!(consoleOut == null))consoleOut.append("\n\nArquivo '"+new File("OpenSSL\\bin\\openssl.exe").getAbsolutePath()+"' não existe. não poderá criar certificado");
                return;
            }
            
            if(new File("clientProgram\\client_trustore.jks").exists())
            {
                if(!(consoleOut == null))consoleOut.append("\n\ndeletando client_trustore.jks para não causar inconsistências na geração do certificado");
                new File("clientProgram\\client_trustore.jks").delete();
            }
            
            
            String openssl = "OpenSSL\\bin\\openssl.exe";
            
            File dir = new File("tempkey");
            dir.mkdir();
            if(!dir.exists())
            {
                if(!(consoleOut == null))consoleOut.append("\n\nFalha ao criar diretório '"+dir.getAbsolutePath()+"'. não poderá criar certificado");
                return;
            }
            
            exec(consoleOut,openssl,"genrsa","-out","tempkey\\server.key",strBits);
            
            exec(consoleOut,openssl,"rsa","-in","tempkey\\server.key","-out","tempkey\\nserver.key");
            exec(consoleOut,openssl,"req","-sha256","-new","-key","tempkey\\server.key","-out","tempkey\\server.csr","-subj",
                "/C="+strCountry+
                    "/ST="+strState+
                    "/L="+strCity+
                    "/O="+strOrgName+
                    "/OU="+strOrgUnit+
                    "/CN="+strCommonName
            );
            exec(consoleOut,openssl,"x509",
                "-req",
                "-"+strProtocolo,
                "-days",strExpiration,
                "-in","tempkey\\server.csr",
                "-signkey","tempkey\\nserver.key",
                "-out","tempkey\\server.crt"
            );
            exec(consoleOut,openssl,"x509","-in","tempkey\\server.crt","-text","-noout");
            
            exec(consoleOut,openssl,"pkcs12","-export",
                "-in","tempkey\\server.crt",
                "-inkey","tempkey\\server.key",
                "-certfile","tempkey\\server.crt",
                "-out","tempkey\\server_pkcs.p12",
                "-passout","pass:1a2b3c4d"
            );
            
            exec(consoleOut,"keytool","-importkeystore",
                "-srckeystore","tempkey\\server_pkcs.p12",
                "-srcstoretype","pkcs12",
                "-destkeystore","tempkey\\server_keystore.jks",
                "-deststoretype","JKS",
                "-srcstorepass","1a2b3c4d",
                "-deststorepass","1a2b3c4d");
            
            exec(consoleOut,"keytool","-importcert",
                "-noprompt",
                "-keystore","clientProgram\\client_trustore.jks",
                "-storepass","1a2b3c4d",
                "-file","tempkey\\server.crt");
            
            
            
            byte[] serverKeystore = Files.readAllBytes(new File(dir,"server_keystore.jks").toPath());
            if(certificatePassword != null)
            {
                byte[] serverKeystore_aes = TestEncrypt.encryptWithPassw(serverKeystore, certificatePassword);
                Files.write(new File("server_keystore.jks.aes").toPath(), serverKeystore_aes);
            }
            else
            {
                Files.write(new File("server_keystore.jks").toPath(), serverKeystore);
            }
                
            
            // deletar diretorio tempkey
            //
            new File(dir,"server.crt").delete();
            new File(dir,"server_keystore.jks").delete();
            new File(dir,"server_pkcs.p12").delete();
            new File(dir,"server.key").delete();
            new File(dir,"nserver.key").delete();
            new File(dir,"server.csr").delete();
            dir.delete();
        } catch (Exception e) {
            e.printStackTrace();

            if(consoleOut != null)
            {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                consoleOut.append("\n\nExceção Inesperada:"+sw.toString());
            }
            
        }
    }
}
