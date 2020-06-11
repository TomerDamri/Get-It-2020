web java -Dserver.port=$PORT $JAVA_OPTS -jar target/Get-it-0.0.1-SNAPSHOT.jar
heroku buildpacks:add --index 1 heroku/python
pip install youtube_transcript_api
pip install -r requirements.txt
heroku ps:scale web=1
