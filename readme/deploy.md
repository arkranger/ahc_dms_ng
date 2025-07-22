
changes-- 
1. application.properties : spring.profiles.active=uat
2. pom.xml : tomcat dependency
3. CaseDiaryApplication : extends SpringBootServletInitializer
4. log path : /opt/tomcat/logs
5. tail -f /opt/tomcat/logs/case_diary.log
6. adjust paths for security-filter and ignored urls differently

deployment-- 
1. mvn clean install -DskipTests
2. scp -r target/casediary.war root@10.13.0.80:/opt/tomcat/webapps