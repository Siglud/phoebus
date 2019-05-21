FROM openjdk:11-jre-slim
WORKDIR /usr/src/use
RUN echo -e '#!/bin/sh\nset -e\nexec $@' > /usr/src/use/entrypoint.sh &&
    cd /usr/src/app && chmod +x entrypoint.sh
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
COPY build/libs/*.jar /usr/src/use/app.jar
CMD java -jar -Xmx200M app.jar --spring.profiles.active=dev