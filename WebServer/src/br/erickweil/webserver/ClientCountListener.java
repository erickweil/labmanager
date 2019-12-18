/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.erickweil.webserver;

/**
 *
 * @author Usuario
 */
public interface ClientCountListener {
    public void onDisconnected();
    public void onConnected();
    public int getClientCount();
}
