/**
 * Copyright (c) 2020 Team 3555
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.aluminati3555.lib.drivers;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.Faults;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import org.aluminati3555.lib.data.AluminatiData;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * A wrapper class for the Talon FX
 * 
 * @author Caleb Heydon
 */
public class AluminatiTalonFX extends TalonFX
        implements AluminatiPoweredDevice, AluminatiCriticalDevice, AluminatiMotorController {
    // Fault buffer
    private Faults faults;

    private boolean firmwareOK;

    /**
     * Provides a useful string about the motor controller
     */
    @Override
    public String toString() {
        return "[TalonFX:" + this.getDeviceID() + "]";
    }

    /**
     * Empty method that warns drivers
     */
    public void follow(AluminatiSparkMax motorController) {
        DriverStation.reportWarning(this.toString() + " is unable to follow REV motor controllers", false);
    }

    /**
     * Returns true if the talon is ok
     */
    public boolean isOK() {
        this.getFaults(faults);
        boolean ok = (!faults.hasAnyFault() && firmwareOK);

        return ok;
    }

    /**
     * Verifies that a firmware version greater than or equal to the minimum is
     * installed
     */
    private void checkFirmwareVersion() {
        if (this.getFirmwareVersion() < AluminatiData.minTalonFXFirmwareVersion) {
            firmwareOK = false;
            DriverStation.reportWarning(this.toString() + " has too old of firmware (may not work)", false);
        } else {
            firmwareOK = true;
        }
    }

    /**
     * Initializes the talon
     */
    private void setupTalon() {
        // Check firmware version
        checkFirmwareVersion();

        // Restore factory settings
        this.configFactoryDefault();

        // Clear faults
        this.clearStickyFaults();

        // Clear mp warning
        this.clearMotionProfileHasUnderrun();

        // Configure deadband
        this.configNeutralDeadband(AluminatiData.deadband);

        // Disable
        this.set(ControlMode.PercentOutput, 0);
    }

    public AluminatiTalonFX(int canID) {
        super(canID);
        faults = new Faults();

        setupTalon();
    }
}
