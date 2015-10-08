# deLog

deLog provides a simple method for logging things to disk and console in a structured way.  

#How to use

All methods are static and accessed through the ```Logger``` class.  
  
Change your log file path with ```setLogFile()```, the default is "log.log"  
Set your logging level with ```setLogLevel("NONE|ERROR|WARNING|INFO|DEBUG")```   
or have more fine control via ```setDebugEnabled()``` and its counterparts.  
```setAll2StdOut(true)``` lets you see your log message on the console, no need for ```System.out.printf()```!  

And that is pretty much all of it, search the javadoc for more details.
