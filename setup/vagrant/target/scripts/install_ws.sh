#!/bin/bash

set -x

#install dos2unix
sudo apt-get -y install dos2unix

#install java
dos2unix /vagrant/target/scripts/install_java.sh /vagrant/target/scripts/install_java.sh
chmod +x /vagrant/target/scripts/install_java.sh
/vagrant/target/scripts/install_java.sh