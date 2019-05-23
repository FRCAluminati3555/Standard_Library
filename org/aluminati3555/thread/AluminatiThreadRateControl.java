/**
 * Copyright (c) 2019 Team 3555
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Adapted from Team 195's 2019 robot code
 */

package org.aluminati3555.thread;

import org.aluminati3555.timer.AluminatiElapsedTimer;

import edu.wpi.first.hal.NotifierJNI;
import edu.wpi.first.wpilibj.Timer;

/**
 * This class allows for loop rates to be controlled
 * 
 * @author Caleb Heydon
 */
public class AluminatiThreadRateControl {
    private int notifier;
    private AluminatiElapsedTimer timer;

    @Override
    protected void finalize() {
        NotifierJNI.stopNotifier(notifier);
        NotifierJNI.cleanNotifier(notifier);
    }

    /**
     * Starts thread rate control
     */
    public synchronized void start() {
        timer.start();
    }

    /**
     * Call this in every loop at the end
     * 
     * @param ms The minimum time in milliseconds to wait
     */
    public synchronized void threadRateControl(int ms) {
        double timeRemaining = ((ms / 1000.0) - timer.getElapsedTime());

        if (timeRemaining > 0) {
            NotifierJNI.updateNotifierAlarm(notifier,
                    (long) (((Timer.getFPGATimestamp() + timeRemaining) * 2e6) - 150));
            NotifierJNI.waitForNotifierAlarm(notifier);
        }

        timer.start();
    }

    public AluminatiThreadRateControl() {
        notifier = NotifierJNI.initializeNotifier();
        timer = new AluminatiElapsedTimer();
    }
}
