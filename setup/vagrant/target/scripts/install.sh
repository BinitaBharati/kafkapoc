#!/bin/bash

set -x

#install dos2unix
sudo apt-get -y install dos2unix

#install java
dos2unix /vagrant/target/scripts/install_java.sh /vagrant/target/scripts/install_java.sh
chmod +x /vagrant/target/scripts/install_java.sh
/vagrant/target/scripts/install_java.sh

#install zoo-keeper
dos2unix /vagrant/target/scripts/install_zookeeper.sh /vagrant/target/scripts/install_zookeeper2.sh
chmod +x /vagrant/target/scripts/install_zookeeper2.sh
/vagrant/target/scripts/install_zookeeper2.sh

#install kafka
dos2unix /vagrant/target/scripts/install_kafka.sh /vagrant/target/scripts/install_kafka.sh
chmod +x /vagrant/target/scripts/install_kafka.sh
/vagrant/target/scripts/install_kafka.sh

#install auto start up script
do2unix /vagrant/target/scripts/kafka-zoo-init /vagrant/target/scripts/kafka-zoo-init 
sudo cp /vagrant/target/scripts/kafka-zoo-init /etc/init.d/kafka-zoo-init
sudo dos2unix /etc/init.d/kafka-zoo-init /etc/init.d/kafka-zoo-init
sudo chmod 755 /etc/init.d/kafka-zoo-init
sudo update-rc.d kafka-zoo-init defaults






