web java -Dserver.port=$PORT $JAVA_OPTS -jar target/Get-it-0.0.1-SNAPSHOT.jar
heroku buildpacks:add --index 1 heroku/python
heroku ps:scale web=1
