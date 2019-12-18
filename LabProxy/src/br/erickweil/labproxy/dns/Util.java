/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.labproxy.dns;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Usuario
 */
public class Util {
    public static String horaBonita()
    {
                try {
        // data/hora atual
        LocalDateTime agora = LocalDateTime.now();

        // formatar a data
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataFormatada = formatterData.format(agora);

        // formatar a hora
        DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        String horaFormatada = formatterHora.format(agora);
        
        return horaFormatada+" "+dataFormatada;
        }
	catch (Exception e) {
		e.printStackTrace();
		return "HH:mm:ss dd/MM/yyyy";
	}
    }
}
