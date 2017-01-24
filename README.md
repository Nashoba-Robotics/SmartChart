As we've used SmartDashboard 2.0 (SFX) over the last two years, we've found that the built-in graphing utility just isn't good enough. Seeing this, we decided to make our own version. 

There are two versions, SmartChart and SmartChartDouble. The only difference between them is that SmartChartDouble graphs multiple variables simultaneously by sending a string with the variables combined with colons, while regular SmartChart graphs regular SmartDashboard number variables.

##SmartChart
####Advantages:
- Graphs based on time, instead of based on when a new data point is sent to the dashboard
- Graph can be reset
- Graph can export data to a csv file in the location USER\_HOME\_DIRECTORY/SmartDashboard/VARIABLE\_NAME.csv
- Graph can show multiple data sets simultaneously, by sending SmartDashboard a string with the format "[num1]:[num2]:[num3]:...". Eg. "1.0:2.3:5.6"
- Can zoom in on graph by clicking and dragging on the graph.

##Planned features:
- Scrolling

##Installation
Download the most recent release, and move the .jar to your USER_HOME_DIRECTORY/wpilib/tools/plugins/ folder.

##CameraExtension

This is a SmartDashboard (not SFX) USB Camera extension that flips the image upside down, so if you have an upside-down camera on your robot, the output is more useful for your drivers.

This is not tested with 2017 SmartDashboard, so it may not work, due to the changes that have happened with WPILib.
