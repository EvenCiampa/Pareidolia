JAVA_HOME=$(/usr/libexec/java_home -v 17)
export JAVA_HOME

if docker ps -a --format '{{.Names}}' | grep -Eq "^pareidolia_db\$" ; then
	docker stop pareidolia_db || exit 1
	# docker container rm pareidolia_db || exit 1
fi
docker compose up -d --wait || exit 1

mvn clean compile install -D maven.test.skip=true && java -jar target/Pareidolia.jar