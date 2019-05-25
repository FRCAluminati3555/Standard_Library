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

package org.aluminati3555.motion;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.StatusFrame;

import org.aluminati3555.data.AluminatiData;
import org.aluminati3555.input.AluminatiPigeon;
import org.aluminati3555.output.AluminatiTalonSRX;

/**
 * This class provides utilities for configuring motor controllers for motion
 * profiling
 * 
 * @author Caleb Heydon
 */
public class AluminatiMPManager {
    /**
     * This method configures a talon for use with a center profile. Note that you
     * still need to configure the sensor phase. See BobTrajectory wiki
     * 
     * @param left  The left master talon
     * @param right The right master talon
     * @param gyro
     */
    public static void configTalons(AluminatiTalonSRX left, AluminatiTalonSRX right, AluminatiPigeon gyro) {
        // Configure left talon
        left.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        left.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 5);

        // Configure right talon (master)
        right.configRemoteFeedbackFilter(left.getDeviceID(), RemoteSensorSource.TalonSRX_SelectedSensor, 0);
        right.configRemoteFeedbackFilter(gyro.getDeviceID(), RemoteSensorSource.GadgeteerPigeon_Yaw, 1);

        right.configSensorTerm(SensorTerm.Sum0, FeedbackDevice.RemoteSensor0);
        right.configSensorTerm(SensorTerm.Sum1, FeedbackDevice.QuadEncoder);
        right.configSelectedFeedbackSensor(FeedbackDevice.SensorSum, 0, 0);
        right.configSelectedFeedbackCoefficient(0.5, 0, 0);

        right.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor1, 1, 0);
        right.configSelectedFeedbackCoefficient(AluminatiData.pigeonTurnUnitsPerDegree, 1, 0);

        // Configure pid
        right.config_kF(0, AluminatiData.encoderF);
        right.config_kP(0, AluminatiData.encoderP);
        right.config_kI(0, AluminatiData.encoderI);
        right.config_kD(0, AluminatiData.encoderD);
        right.config_IntegralZone(0, AluminatiData.iZone);

        right.config_kF(1, AluminatiData.gyroF);
        right.config_kP(1, AluminatiData.gyroP);
        right.config_kI(1, AluminatiData.gyroI);
        right.config_kD(1, AluminatiData.gyroD);
        right.config_IntegralZone(1, AluminatiData.iZone);
    }
}
