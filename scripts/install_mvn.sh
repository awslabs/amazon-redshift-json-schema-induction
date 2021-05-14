export VER="3.6.3"
curl -O ftp://ftp.osuosl.org/pub/apache/maven/maven-3/${VER}/binaries/apache-maven-${VER}-bin.tar.gz
tar xvf apache-maven-${VER}-bin.tar.gz
mv apache-maven-${VER} /opt/maven
cat <<EOF | sudo tee /etc/profile.d/maven.sh
export MAVEN_HOME=/opt/maven
export PATH=\$PATH:\$MAVEN_HOME/bin
EOF
