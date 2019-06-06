FROM smduarte/sd19-services

MAINTAINER Sérgio Duarte <smd@fct.unl.pt>

WORKDIR /home/sd19/SD19-TP2-50503-50647

COPY target/lib/*.jar /home/sd19/SD19-TP2-50503-50647/

COPY target/*.jar /home/sd19/SD19-TP2-50503-50647/

COPY *.ks /home/sd19/SD19-TP2-50503-50647/

COPY sd2019-tp2.props /props/

ENV CLASSPATH /home/sd19/SD19-TP2-50503-50647/*
