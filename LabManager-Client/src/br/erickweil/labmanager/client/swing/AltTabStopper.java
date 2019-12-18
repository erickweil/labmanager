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
package br.erickweil.labmanager.client.swing;

import br.erickweil.labmanager.client.ClientMain;
import br.erickweil.labmanager.client.swing.ScreenLocker;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 *
 * @author Usuario
 */
public class AltTabStopper implements Runnable {

        private volatile boolean hastopped = false;
        public Thread thread;
        private ScreenLocker frame;

        public AltTabStopper(ScreenLocker frame) {
            this.frame = frame;
        }
        
        public static AltTabStopper create(ScreenLocker frame) {
            AltTabStopper stopper = new AltTabStopper(frame);
            stopper.thread = new Thread(stopper, "Alt-Tab Stopper");
            stopper.thread.start();
            return stopper;
        }

        @Override
        public void run() {
            if(ClientMain._testing){
                hastopped = true;
                return;
            }
            try {
                Robot robot = new Robot();
                while (!Thread.currentThread().isInterrupted()) {
                    robot.keyRelease(KeyEvent.VK_CONTROL);
                    robot.keyRelease(KeyEvent.VK_DELETE);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    robot.keyRelease(KeyEvent.VK_TAB);
                    robot.keyRelease(KeyEvent.VK_WINDOWS);
                    frame.focus();
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                    }
                    synchronized(frame)
                    {
                        if(!frame.isLocking())
                        {
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally
            {
                hastopped = true;
            }
       }
        
       public synchronized boolean hasStopped() {
            return hastopped;
       }
}
