FROM tomcat:10
WORKDIR /usr/local/tomcat
ADD ./target/scc2223-proj1-1.0.war webapps
EXPOSE 8080