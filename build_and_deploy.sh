#!/bin/bash
set -xe -o nounset
mvn clean install --batch-mode -Dversion.jdk=1.8 -Dversion.webxml=30
cp target/spiracle-db-stress.war ~/waratekRepos/walter/JavaAutomation/appservers/tomcat9/webapps/
cd ~/waratekRepos/walter/JavaAutomation/
rm -f rules.log

#cp ~/Downloads/ojdbc8.jar appservers/tomcat9/lib/
#rm appservers/tomcat9/lib/ojdbc11.jar

export JAVA_HOME=/jdks/hs/8/jdk-hs-8u331-linux-x64
#export JAVA_HOME=/jdks/hs/11/jdk-hs-11u15-linux-x64
#export JAVA_OPTS="-javaagent:/home/dmacsuibhne/waratekRepos/walter/JavaAutomation/plugin/agent/waratek.jar -Dcom.waratek.WaratekProperties=/home/dmacsuibhne/waratekRepos/walter/manual_waratek.properties"
appservers/tomcat9/bin/catalina.sh run | tee "$HOME/dbperf_$(date +%Y-%m-%d_%H-%M-%S).log"
