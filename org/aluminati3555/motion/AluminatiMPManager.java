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

/**
 * Adapted from this code:
 * 
 * Phoenix Software License Agreement
 *
 * Copyright (C) Cross The Road Electronics.  All rights
 * reserved.
 * 
 * Cross The Road Electronics (CTRE) licenses to you the right to 
 * use, publish, and distribute copies of CRF (Cross The Road) firmware files (*.crf) and 
 * Phoenix Software API Libraries ONLY when in use with CTR Electronics hardware products
 * as well as the FRC roboRIO when in use in FRC Competition.
 * 
 * THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT
 * WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION, ANY WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT SHALL
 * CROSS THE ROAD ELECTRONICS BE LIABLE FOR ANY INCIDENTAL, SPECIAL, 
 * INDIRECT OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF
 * PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY OR SERVICES, ANY CLAIMS
 * BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE
 * THEREOF), ANY CLAIMS FOR INDEMNITY OR CONTRIBUTION, OR OTHER
 * SIMILAR COSTS, WHETHER ASSERTED ON THE BASIS OF CONTRACT, TORT
 * (INCLUDING NEGLIGENCE), BREACH OF WARRANTY, OR OTHERWISE
 */

package org.aluminati3555.motion;

import com.ctre.phoenix.motion.BufferedTrajectoryPointStream;
import com.ctre.phoenix.motion.MotionProfileStatus;
import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

import org.aluminati3555.data.AluminatiData;
import org.aluminati3555.input.AluminatiPigeon;
import org.aluminati3555.output.AluminatiTalonSRX;

/**
 * This class loads a motion profile
 * 
 * WARNING: This code is in development and has not been tested
 * 
 * @author Caleb Heydon
 */
public class AluminatiMPManager {
    /**
     * This method configures a talon for use with a center profile. Note that you
     * still need to configure the sensor phase.  See BobTrajectory wiki
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

    // Class members
    private AluminatiMP motionProfile;

    private AluminatiTalonSRX masterTalon;
    private AluminatiPigeon pigeon;

    private BufferedTrajectoryPointStream mpBuffer;
    private TalonSRXConfiguration talonConfig;

    private MotionProfileStatus mpStatus;

    private boolean invertGyro;

    /**
     * Returns true if the gyro is inverted
     */
    public boolean isGyroInverted() {
        return invertGyro;
    }

    /**
     * Loads the motion profile
     */
    private void loadMP() {
        mpBuffer = new BufferedTrajectoryPointStream();

        boolean forward = motionProfile.isForward();
        TrajectoryPoint point = new TrajectoryPoint();
        double[][] points = motionProfile.getPoints();

        for (int i = 0; i < points.length; i++) {
            double direction = forward ? 1 : -1;
            double position = points[i][0];
            double velocity = points[i][1];
            int duration = (int) points[i][2];

            double targetHeading = points[i][3];

            point.timeDur = duration;
            point.position = direction * position * AluminatiData.encoderUnitsPerRotation;
            point.velocity = direction * velocity * AluminatiData.encoderUnitsPerRotation;
            point.arbFeedFwd = 0;

            point.auxiliaryPos = targetHeading * AluminatiData.pigeonTurnUnitsPerDegree;
            point.auxiliaryVel = 0;
            point.auxiliaryArbFeedFwd = 0;

            point.profileSlotSelect0 = AluminatiData.primaryPIDSlot;
            point.profileSlotSelect1 = AluminatiData.auxPIDSlot;
            point.zeroPos = false;
            point.isLastPoint = ((i + 1) == points.length);
            point.useAuxPID = true;

            mpBuffer.Write(point);
        }
    }

    /**
     * Configures the master talon for the motion profile
     */
    private void configTalon() {
        talonConfig = new TalonSRXConfiguration();
        masterTalon.getAllConfigs(talonConfig);

        talonConfig.remoteFilter0.remoteSensorDeviceID = pigeon.getDeviceID();
        talonConfig.remoteFilter0.remoteSensorSource = RemoteSensorSource.Pigeon_Yaw;

        talonConfig.primaryPID.selectedFeedbackSensor = AluminatiData.encoderType;

        talonConfig.auxiliaryPID.selectedFeedbackSensor = FeedbackDevice.RemoteSensor0;

        talonConfig.slot0.kF = AluminatiData.encoderF;
        talonConfig.slot0.kP = AluminatiData.encoderP;
        talonConfig.slot0.kI = AluminatiData.encoderI;
        talonConfig.slot0.kD = AluminatiData.encoderD;
        talonConfig.slot0.integralZone = AluminatiData.iZone;
        talonConfig.slot0.closedLoopPeakOutput = AluminatiData.peakOutput;

        talonConfig.slot1.kF = AluminatiData.gyroF;
        talonConfig.slot1.kP = AluminatiData.gyroP * (invertGyro ? -1 : 1);
        talonConfig.slot1.kI = AluminatiData.gyroI * (invertGyro ? -1 : 1);
        talonConfig.slot1.kD = AluminatiData.gyroD * (invertGyro ? -1 : 1);
        talonConfig.slot1.integralZone = AluminatiData.iZone;
        talonConfig.slot1.closedLoopPeakOutput = AluminatiData.peakOutput;

        masterTalon.configAllSettings(talonConfig);

        masterTalon.setStatusFramePeriod(StatusFrame.Status_12_Feedback1, 5);
        masterTalon.setStatusFramePeriod(StatusFrame.Status_10_Targets, 5);
        masterTalon.setStatusFramePeriod(StatusFrame.Status_17_Targets1, 5);
    }

    /**
     * Starts the motion profile
     */
    public void startMP() {
        masterTalon.getSensorCollection().setQuadraturePosition(0, 100);
        pigeon.setYaw(0);

        masterTalon.startMotionProfile(mpBuffer, 25, ControlMode.MotionProfileArc);
    }

    /**
     * Returns true if the motion profile is complete
     * 
     * @return
     */
    public boolean isMPDone() {
        return masterTalon.isMotionProfileFinished();
    }

    /**
     * Returns true if the points are not being transmitted to the motor controller
     * fast enough
     */
    public boolean isMPUnderrun() {
        masterTalon.getMotionProfileStatus(mpStatus);
        return mpStatus.isUnderrun;
    }

    /**
     * Creata a new mp manager
     */
    public AluminatiMPManager(AluminatiMP motionProfile, AluminatiTalonSRX masterTalon, AluminatiPigeon pigeon,
            boolean invertGyro) {
        this.motionProfile = motionProfile;
        this.masterTalon = masterTalon;
        this.pigeon = pigeon;
        this.invertGyro = invertGyro;

        mpStatus = new MotionProfileStatus();

        loadMP();
        configTalon();
    }
}
