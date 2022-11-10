FROM tomcat:10
EXPOSE 8080
CMD chmod +x /usr/local/tomcat/bin/catalina.sh
CMD ["catalina.sh", "run"]

# copy the environment variables
COPY .env .env