export VER="3.6.3"
curl -O http://www-eu.apache.org/dist/maven/maven-3/${VER}/binaries/apache-maven-${VER}-bin.tar.gz
tar xvf apache-maven-${VER}-bin.tar.gz
mv apache-maven-${VER} /opt/maven
cat <<EOF | sudo tee /etc/profile.d/maven.sh
export MAVEN_HOME=/opt/maven
export PATH=\$PATH:\$MAVEN_HOME/bin
EOF
