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

import br.erickweil.labmanager.client.swing.ScreenLocker;

/**
 *
 * @author Usuario
 */
public class ScreenLockerTestv2 { 
    public static void main(String[] args) throws InterruptedException
    {
        ScreenLocker locker = new ScreenLocker();
        Thread.sleep(2000);
        for(int i=0;i <20;i++)
        {
            long time_start = System.currentTimeMillis();
        locker.startLocking(false);
            System.out.println("Time: "+(System.currentTimeMillis()-time_start));
        Thread.sleep(2000);
        locker.stopLocking();
        Thread.sleep(2000);
        }
        
        locker.dispose();
    }
}
