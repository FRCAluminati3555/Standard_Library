# FRC Team 3555's Standard Java Library

This library provides an easy way to interface with WPILIB.  It also includes some helper classes for programming a robot.  The library includes various wrapper classes to ease programming and to make the robot more reliable (e.i. setting factory defaults).

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
  - TalonSRX firmware version validation
# Features in Development
  - A system to load motion profiles from GZIP files (position, velocity, duration, and heading)
  - A system to load the motion profiles onto a motor
  - A motion profile implementation for the Talon SRX with Pigeon gyro PID
# Dependencies
  - WPILIB
  - CTRE Phoenix Library
# Examples
  - https://github.com/FRCAluminati3555/Standard_Library_Example
