# 指定基础镜像，必须为第一个命令
FROM hwhaocool/java11:latest

MAINTAINER hwhaocool

# ENV 设置环境变量
ENV LC_ALL C.UTF-8

# EXPOSE 指定于外界交互的端口
EXPOSE 8080

# RUN：构建镜像时执行的命令， 创建工作目录文件夹
RUN mkdir /app && mkdir /app/jvmlogs

# 指定当前目标工作路径
WORKDIR /app/

# COPY 将本地文件复制到容器中
COPY target/warm-up-1.0.jar .

# ENTRYPOINT 配置容器，使其可执行化
ENTRYPOINT ["java", "-Duser.timezone=GMT+8", "-Djava.security.egd=file:/dev/./urandom", "-jar", "warm-up-1.0.jar"]