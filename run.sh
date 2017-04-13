#You may need to set absolute path, relative path sometimes does not work
export MAVEN_OPTS=-Djava.library.path=target/natives
mvn compile exec:java -Dexec.mainClass=com.helospark.mycraft.mycraft.App
