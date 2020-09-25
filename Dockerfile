# 指定基础镜像，必须为第一个命令
FROM registry.cn-shenzhen.aliyuncs.com/cuishiwen/geek-java8:v5

MAINTAINER yellowtail

# 更新时间
ENV REFRESHED_AT 2019-10-07

# ENV 设置环境变量
ENV LC_ALL C.UTF-8

# EXPOSE 指定于外界交互的端口
EXPOSE 8080

#build时接受的参数，用来指定spring boot的profile
ARG envType=dev
ENV envType ${envType}

# 拷贝代码
# RUN：构建镜像时执行的命令， 创建工作目录文件夹
RUN mkdir /app

# 指定当前目标工作路径
WORKDIR /app/

# COPY 将本地文件复制到容器中
COPY target/top-1.0.jar .

# ENTRYPOINT 配置容器，使其可执行化
ENTRYPOINT ["java", "-Duser.timezone=GMT+8", "-Djava.security.egd=file:/dev/./urandom", "-jar", "top-1.0.jar"]
#ENTRYPOINT ["java", "-Duser.timezone=GMT+8", "-Djava.security.egd=file:/dev/./urandom", "-jar", "top-1.0.jar", "--spring.profiles.active=${envType}"]