FROM openjdk:11-jre-slim
WORKDIR /usr/src/use
RUN echo '#!/bin/sh\nset -e\nexec $@' > /usr/src/use/entrypoint.sh && chmod +x /usr/src/use/entrypoint.sh
ENTRYPOINT ["/usr/src/use/entrypoint.sh"]
COPY build/libs/*.jar /usr/src/use/app.jar
CMD java -jar -Xmx200M app.jar --spring.profiles.active=dev