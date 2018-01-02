#!/bin/bash
set -x

editConfigFiles() {

sed -i 's/zookeeper.connect=localhost:2181/zookeeper.connect=192.168.10.12:2181,192.168.10.13:2181,192.168.10.14:2181/g' /home/vagrant/kafka/config/server.properties
printf "\ndelete.topic.enable = true" >> /home/vagrant/kafka/config/server.properties
sed -i 's/zookeeper.connection.timeout.ms=6000/zookeeper.connection.timeout.ms=16000/g' /home/vagrant/kafka/config/server.properties
if [ "$1" == "192.168.10.12" ]; 
then  
  sed -i 's/broker.id=0/broker.id=1/g' /home/vagrant/kafka/config/server.properties
elif [ "$1" == "192.168.10.13" ];
then
  sed -i 's/broker.id=0/broker.id=2/g' /home/vagrant/kafka/config/server.properties
elif [ "$1" == "192.168.10.14" ];
then
  sed -i 's/broker.id=0/broker.id=3/g' /home/vagrant/kafka/config/server.properties
fi
}

#Reference: https://www.digitalocean.com/community/tutorials/how-to-install-apache-kafka-on-ubuntu-14-04
mkdir -p /home/vagrant/Downloads
wget "http://www-eu.apache.org/dist/kafka/1.0.0/kafka_2.11-1.0.0.tgz" -O /home/vagrant/Downloads/kafka.tgz
mkdir -p /home/vagrant/kafka && cd /home/vagrant/kafka
tar -xvzf /home/vagrant/Downloads/kafka.tgz --strip 1

   
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

#Add entry in etc hosts
sudo -- sh -c -e "echo '192.168.10.12 net1mc1' >> /etc/hosts"
sudo -- sh -c -e "echo '192.168.10.13 net1mc2' >> /etc/hosts"
sudo -- sh -c -e "echo '192.168.10.14 net1mc3' >> /etc/hosts"


nohup /home/vagrant/kafka/bin/kafka-server-start.sh /home/vagrant/kafka/config/server.properties > /home/vagrant/kafka/kafka.log 2>&1 &



