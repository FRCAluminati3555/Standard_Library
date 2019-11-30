# FRC Team 3555's Standard Java Library

This library provides an easy way to interface with WPILIB.  It also includes some helper classes for programming a robot.  The library includes various wrapper classes to ease programming and to make the robot more reliable (e.i. setting factory defaults).

![Robot.java](https://github.com/FRCAluminati3555/Standard_Library/raw/master/wiki/robot_class.png)

# Features
  - A static class that contains the library version and contains various settings that can be modified at runtime
  - A drive helper class adapted from Team 254's code: It supports both arcade and cheesy drive
  - A class to keep track of motor groups: It automatically sets the follower motors to follow the master
  - A drive helper class that facilitates shifting between high and low gear
  - A Pigeon gyro wrapper class
  - A joystick wrapper class which has methods for squared output
  - An Xbox controller wrapper class which also has methods for squared output
  - Motor and relay wrapper classes with which contain automatic current warnings with a common interface (Spark, Talon, TalonSRX, and VictorSPX)
  - A wrapper class for the pneumatics compressor which always has closedloop control enabled
  - Wrapper classes for both single and double solenoids
  - A custom robot superclass which allows for the timed robot delay to be intuitively adjusted
  - A wrapper class to allows multiple cameras to be started more easily
  - A wrapper class from the Limelight which supports all commonly used features
  - Support for detecting motor controller, encoder, and gyro failures
  - TalonSRX and VictorSPX firmware version validation
  - Motion profile arc following using a modified version of BobTrajectory
# Dependencies
  - Java 11+
  - WPILIB
  - CTRE Phoenix Library
# Examples
  - https://github.com/FRCAluminati3555/Standard_Library_Example
  
# Testing robot code in VS Code
```
@Override
  public void startCompetition() {
    robotInit();

    disabledInit();

    for (double i = 0; i < 3; i += 0.02) {
      robotPeriodic();
      disabledPeriodic();
      Timer.delay(0.02);
    }

    autonomousInit();
    robotMode = RobotMode.AUTONOMOUS;
    autoTask = new ModeExamplePath(robotState, driveSystem);

    while (true) {
      robotPeriodic();
      autonomousPeriodic();
      Timer.delay(0.02);
    }
  }
```
