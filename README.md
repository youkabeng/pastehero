# Paste Hero

This is a gui app that helps you manage your clipboard activities.    
With it you can access to previous copied data easily.    
Use it to boost up your productivity!

![gui](gui.png)

#### Requirements

Now the app only runs on linux platform (Dbus is enabled). (Support for Windows is coming)

Maven
Java 1.8+


#### Packaging

```
mvn package
```

#### Run

```
nohup java -Xms128m -Xmx256m -XX:+UseG1GC \
    -Djdk.gtk.version=3 \ 
    -cp pastehero-1.0-SNAPSHOT-jar-with-dependencies.jar me.phph.app.pastehero.App > .pastehero.log 2>&1 &
```



