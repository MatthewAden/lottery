# 基础镜像
FROM openjdk:8-jre-slim

# 作者
MAINTAINER matthew

# 配置
ENV PARAMS=""

# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD target/Marketing-system-app-app.jar /lottery-app.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /lottery-app.jar $PARAMS"]