As we've used SmartDashboard 2.0 (SFX), we've found that the built-in graphing utility just isn't good enough.

####Advantages:
- Graphs based on time, instead of based on when a new data point is sent to the dashboard
- Graph can be reset
- Graph can export data to a csv file in the location USER\_HOME\_DIRECTORY/SmartDashboard/VARIABLE\_NAME.csv
- Graph can show multiple data sets simultaneously, by sending SmartDashboard a string with the format "[num1]:[num2]:[num3]". Eg. "1.0:2.3:5.6"
- If not showing multiple data sets simultaneously, can zoom in on graph by clicking and dragging on the graph.

##Planned features:
- Zooming for SmartChartDouble
- Scrolling

To install, download the most recent release, and move the .jar to your USER\_HOME\_DIRECTORY/wpilib/tools/plugins/ folder.
