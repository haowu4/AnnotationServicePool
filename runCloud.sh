#!/bin/bash 
echo "Hello World" > /home/ec2-user/opt/curator/hello
cd /home/ec2-user/opt/curator
source /home/ec2-user/opt/curator/setEnvVars.sh
# /home/ec2-user/opt/curator/dist/startServers.sh
cd  dist
./startServers.sh
service iptables stop
