#!/bin/bash

set -e

echo "127.0.0.1" > /home/ec2-user/codes/AnnotationServicePool/conf/hostname

cd /home/ec2-user/opt/curator
source /home/ec2-user/opt/curator/setEnvVars.sh
# /home/ec2-user/opt/curator/dist/startServers.sh
cd  dist
./startServers.sh

cd /home/ec2-user/codes/AnnotationServicePool
git reset --hard
git pull origin master
./run.sh &

sudo service iptables stop