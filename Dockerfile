FROM ubuntu:18.04

ARG VERSION

ENV LANG="C.UTF-8"
ENV JAVA_HOME=/usr

RUN echo "deb mirror://mirrors.ubuntu.com/mirrors.txt bionic main restricted universe multiverse" > /etc/apt/sources.list && \
    echo "deb mirror://mirrors.ubuntu.com/mirrors.txt bionic-updates main restricted universe multiverse" >> /etc/apt/sources.list && \
    echo "deb mirror://mirrors.ubuntu.com/mirrors.txt bionic-security main restricted universe multiverse" >> /etc/apt/sources.list && \
    apt-get update && apt-get install -y --no-install-recommends nano sed openjdk-11-jdk

# set JDK path in sh
RUN export JAVA_HOME=$JAVA_HOME

RUN mkdir -p /app/

# copy resources
COPY ./dist /app

# make it executable
RUN chmod a+x /app/run.sh

EXPOSE 8080

WORKDIR /app

CMD ["/app/run.sh"]
