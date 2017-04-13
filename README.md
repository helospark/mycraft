# MyCraft

MyCraft (Minecraft clone) game Githubified for historical and safekeeping reasons
I wrote it as an excercise in Java programming couple of years ago.
Uses (mainly): Java, Spring, Maven, LWJGL

[![Video demonstration](https://img.youtube.com/vi/beOJJbvJm6k/0.jpg)](https://www.youtube.com/watch?v=beOJJbvJm6k)

## Compile and run

You will need Maven to compile:

      mvn clean install

Run with:

      export MAVEN_OPTS=-Djava.library.path=/FULL_PATH_TO/target/natives
      mvn compile exec:java -Dexec.mainClass=com.helospark.mycraft.mycraft.App

