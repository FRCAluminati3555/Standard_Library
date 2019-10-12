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

package org.aluminati3555.drive;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team319.follower.FollowArc;
import com.team319.follower.FollowsArc;
import com.team319.follower.SrxTrajectory;

import org.aluminati3555.drive.AluminatiDriveHelper;
import org.aluminati3555.drive.AluminatiMotorGroup;
import org.aluminati3555.drive.AluminatiShifter;
import org.aluminati3555.input.AluminatiJoystick;
import org.aluminati3555.input.AluminatiPigeon;
import org.aluminati3555.input.AluminatiXboxController;
import org.aluminati3555.motion.AluminatiMPManager;
import org.aluminati3555.output.AluminatiCriticalDevice;
import org.aluminati3555.output.AluminatiTalonSRX;

/**
 * This system controls the drivetrain
 * 
 * @author Caleb Heydon
 */
public class AluminatiDrive implements AluminatiCriticalDevice, FollowsArc {
    // Class members
    private AluminatiMotorGroup left;
    private AluminatiMotorGroup right;
    private AluminatiPigeon gyro;

    private AluminatiDriveHelper driveHelper;
    private AluminatiShifter shifter;

    private FollowArc follower;

    private double controlCoefficient;
    private boolean inverted;

    /**
     * Returns the left motor group
     */
    public AluminatiMotorGroup getLeftGroup() {
        return left;
    }

    /**
     * Returns the right motor group
     */
    public AluminatiMotorGroup getRightGroup() {
        return right;
    }

    /**
     * Returns the gyro
     */
    public AluminatiPigeon getGyro() {
        return gyro;
    }

    /**
     * Returns true if the system is ok
     */
    public boolean isOK() {
        return (left.isOK() && right.isOK() && gyro.isOK() && left.isEncoderOK() && right.isEncoderOK());
    }

    /**
     * Returns the left master talon
     */
    public AluminatiTalonSRX getLeft() {
        return left.getMaster();
    }

    /**
     * Returns the right master talon
     */
    public AluminatiTalonSRX getRight() {
        return right.getMaster();
    }

    /**
     * Returns the encoder value
     */
    public double getDistance() {
        return right.getMaster().getSelectedSensorPosition();
    }

    /**
     * Returns the shifter
     */
    public AluminatiShifter getShifter() {
        return shifter;
    }

    /**
     * Returns the joystick coefficient
     */
    public double getJoystickCoefficient() {
        return controlCoefficient;
    }

    /**
     * Sets the joystick coefficient
     */
    public void setJoystickCoefficient(double controlCoefficient) {
        this.controlCoefficient = controlCoefficient;
    }

    /**
     * Returns true if the drive is inverted
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Sets the drive inverted
     */
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    /**
     * Starts a motion profile
     */
    public void startMP(SrxTrajectory path, boolean zeroGyro) {
        stopMP();
        follower = new FollowArc(this, path, path.flipped, false);

        if (zeroGyro) {
            gyro.zeroYaw();
        }

        follower.start();
    }

    /**
     * Starts a motion profile and zeros the gyro
     */
    public void startMP(SrxTrajectory path) {
        startMP(path, true);
    }

    /**
     * Returns true if the motion profile is done
     */
    public boolean isMPDone() {
        if (follower == null) {
            return false;
        }

        return follower.isFinished();
    }

    /**
     * Stops the mp
     */
    public void stopMP() {
        if (follower != null) {
            follower.end();
        }
    }

    /**
     * Returns the follower
     */
    public FollowArc getFollower() {
        return follower;
    }

    /**
     * Puts the drive in coast mode
     */
    public void coast() {
        left.coast();
        right.coast();
    }

    /**
     * Puts the drive in brake mode
     */
    public void brake() {
        left.brake();
        right.brake();
    }

    /**
     * Drives the robot using arcade drive
     */
    public void arcadeDrive(AluminatiJoystick joystick) {
        driveHelper.aluminatiDrive(-joystick.getSquaredY() * controlCoefficient,
                joystick.getSquaredX() * controlCoefficient, true, (shifter == null) ? true : shifter.isHigh());

        if (!inverted) {
            left.getMaster().set(ControlMode.PercentOutput, driveHelper.getLeftPower());
            right.getMaster().set(ControlMode.PercentOutput, driveHelper.getRightPower());
        } else {
            left.getMaster().set(ControlMode.PercentOutput, -driveHelper.getRightPower());
            right.getMaster().set(ControlMode.PercentOutput, -driveHelper.getLeftPower());
        }
    }

    /**
     * Drives the robot using arcade drive
     */
    public void arcadeDrive(AluminatiXboxController controller) {
        driveHelper.aluminatiDrive(-controller.getSquaredY() * controlCoefficient,
                controller.getSquaredX() * controlCoefficient, true, (shifter == null) ? true : shifter.isHigh());

        if (!inverted) {
            left.getMaster().set(ControlMode.PercentOutput, driveHelper.getLeftPower());
            right.getMaster().set(ControlMode.PercentOutput, driveHelper.getRightPower());
        } else {
            left.getMaster().set(ControlMode.PercentOutput, -driveHelper.getRightPower());
            right.getMaster().set(ControlMode.PercentOutput, -driveHelper.getLeftPower());
        }
    }

    /**
     * Drives the robot using cheesy drive
     */
    public void cheesyDrive(AluminatiJoystick joystick, int cheesyDriveButton) {
        driveHelper.aluminatiDrive(-joystick.getSquaredY() * controlCoefficient,
                joystick.getSquaredX() * controlCoefficient, joystick.getRawButton(cheesyDriveButton),
                (shifter == null) ? true : shifter.isHigh());

        if (!inverted) {
            left.getMaster().set(ControlMode.PercentOutput, driveHelper.getLeftPower());
            right.getMaster().set(ControlMode.PercentOutput, driveHelper.getRightPower());
        } else {
            left.getMaster().set(ControlMode.PercentOutput, -driveHelper.getRightPower());
            right.getMaster().set(ControlMode.PercentOutput, -driveHelper.getLeftPower());
        }
    }

    /**
     * Drives the robot using cheesy drive
     */
    public void cheesyDrive(AluminatiXboxController controller, int cheesyDriveButton) {
        driveHelper.aluminatiDrive(-controller.getSquaredY() * controlCoefficient,
                controller.getSquaredX() * controlCoefficient, controller.getRawButton(cheesyDriveButton),
                (shifter == null) ? true : shifter.isHigh());

        if (!inverted) {
            left.getMaster().set(ControlMode.PercentOutput, driveHelper.getLeftPower());
            right.getMaster().set(ControlMode.PercentOutput, driveHelper.getRightPower());
        } else {
            left.getMaster().set(ControlMode.PercentOutput, -driveHelper.getRightPower());
            right.getMaster().set(ControlMode.PercentOutput, -driveHelper.getLeftPower());
        }
    }

    /**
     * Manuall uses arcade drive (no helper) with no squared output, control coefficient, or
     * drive inversion
     */
    public void manualArcadeDrive(double x, double y) {
        left.getMaster().set(ControlMode.PercentOutput, -y + x);
        right.getMaster().set(ControlMode.PercentOutput, -y - x);
    }

    /**
     * Zeros the encoders
     */
    public void zeroEncoders() {
        left.getMaster().setSelectedSensorPosition(0);
        right.getMaster().getSensorCollection().setQuadraturePosition(0, 0);
    }

    public AluminatiDrive(AluminatiMotorGroup left, AluminatiMotorGroup right, AluminatiPigeon gyro) {
        this.left = left;
        this.right = right;
        this.gyro = gyro;

        driveHelper = new AluminatiDriveHelper();
        driveHelper.aluminatiDrive(0, 0, true, true);

        controlCoefficient = 1;

        // Configure
        AluminatiMPManager.configTalons(left.getMaster(), right.getMaster(), gyro);

        left.getMaster().set(ControlMode.PercentOutput, driveHelper.getLeftPower());
        right.getMaster().set(ControlMode.PercentOutput, driveHelper.getRightPower());
    }

    public AluminatiDrive(AluminatiMotorGroup left, AluminatiMotorGroup right, AluminatiPigeon gyro,
            AluminatiShifter shifter) {
        this(left, right, gyro);
        this.shifter = shifter;
    }
}
