#!/bin/bash

# When you change this file, you must take manual action. Read this doc:
# - https://docs.sandstorm.io/en/latest/vagrant-spk/customizing/#setupsh

set -euo pipefail
# This is the ideal place to do things like:
#
#    export DEBIAN_FRONTEND=noninteractive
#    apt-get update
#    apt-get install -y nginx nodejs nodejs-legacy python2.7 mysql-server
#
# If the packages you're installing here need some configuration adjustments,
# this is also a good place to do that:
#
#    sed --in-place='' \
#            --expression 's/^user www-data/#user www-data/' \
#            --expression 's#^pid /run/nginx.pid#pid /var/run/nginx.pid#' \
#            --expression 's/^\s*error_log.*/error_log stderr;/' \
#            --expression 's/^\s*access_log.*/access_log off;/' \
#            /etc/nginx/nginx.conf

# By default, this script does nothing.  You'll have to modify it as
# appropriate for your application.

export DEBIAN_FRONTEND=noninteractive

# Install packages required for YaCy build and run

# Configure apt to use backports repository in order to install java 1.8
echo "deb http://httpredir.debian.org/debian jessie-backports main contrib non-free" > /etc/apt/sources.list.d/backports.list

apt-get update

apt-get install -yq curl ant default-jdk

#apt-get install -yq -t jessie-backports openjdk-8-jdk-headless

#update-java-alternatives -s java-1.8.0-openjdk-amd64

# Install Oracle Jre
#mkdir -p /opt/jre

#curl -fSL http://javadl.oracle.com/webapps/download/AutoDL?BundleId=211989 -o /tmp/jre-latest-linux-x64.tar.gz

#tar -xzf /tmp/jre-latest-linux-x64.tar.gz -C /opt/jre --strip-components=1

#/opt/jre/bin/java -version

# Ensure java is correctly installed
java -version