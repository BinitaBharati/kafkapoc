#!/bin/bash

set -x

editConfigFiles() {

echo "server.1=192.168.10.12:2888:3888" | sudo tee -a /etc/zookeeper/conf/zoo.cfg
echo "server.2=192.168.10.13:2888:3888" | sudo tee -a /etc/zookeeper/conf/zoo.cfg
echo "server.3=192.168.10.14:2888:3888" | sudo tee -a /etc/zookeeper/conf/zoo.cfg

#Enable purging of zookeeper old data
echo "autopurge.snapRetainCount=3" | sudo tee -a /etc/zookeeper/conf/zoo.cfg
echo "autopurge.purgeInterval=1" | sudo tee -a /etc/zookeeper/conf/zoo.cfg

#Remove information entry in the file.Its a uncommented line unfortunately.
sudo sed -i '/replace this text/d' /var/lib/zookeeper/myid
if [ "$1" == "192.168.10.12" ]; 
then  
  echo "1" | sudo tee -a /var/lib/zookeeper/myid
elif [ "$1" == "192.168.10.13" ];
then
  echo "2" | sudo tee -a /var/lib/zookeeper/myid
elif [ "$1" == "192.168.10.14" ];
then
  echo "3" | sudo tee -a /var/lib/zookeeper/myid
fi
}

#install zoo-keeper
#Reference : https://www.cloudera.com/documentation/enterprise/5-5-x/topics/cdh_ig_zookeeper_package_install.html
sudo apt-get update
sudo apt-get -y install zookeeper

ifconfig -a | grep 192.168.10.12
s1=$?
ifconfig -a | grep 192.168.10.13
s2=$?
ifconfig -a | grep 192.168.10.14
s3=$?

if [ $s1 -eq 0 ]; then
    editConfigFiles 192.168.10.12
elif [ $s2 -eq 0 ]; then
    editConfigFiles 192.168.10.13
elif [ $s3 -eq 0 ]; then
    editConfigFiles 192.168.10.14
fi

#Start zookeeper service
nohup /usr/share/zookeeper/bin/zkServer.sh start &

#install zookeeper startup scripts. Installation of this also automatically starts zookeeper service.
#sudo apt-get install zookeeperd

#Start ZooKeeper service
#sudo service zookeeper start