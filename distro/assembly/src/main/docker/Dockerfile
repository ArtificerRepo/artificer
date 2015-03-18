FROM jboss/wildfly

RUN $JBOSS_HOME/bin/add-user.sh admin artificer1! --silent

EXPOSE 8787

# Ant installed here to invalidate as fewer layers on rebuild and beeing able to reuse this layer
RUN cd /tmp \
    && curl http://mirrors.ukfast.co.uk/sites/ftp.apache.org/ant/binaries/apache-ant-1.9.4-bin.tar.gz -o apache-ant-1.9.4-bin.tar.gz \
    && bsdtar -xf apache-ant-1.9.4-bin.tar.gz \
    && rm apache-ant-1.9.4-bin.tar.gz

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "-c", "standalone-full.xml", "--debug"]

# These 2 steps are latest as to invalidate as fewer layers as possible with each build
COPY artificer.zip /tmp/
RUN cd /tmp \
    && bsdtar -xf /tmp/artificer.zip \
    && /tmp/apache-ant-1.9.4/bin/ant -f /tmp/artificer/build.xml -Ds-ramp-distro.choices.platform=1 -Ds-ramp-distro.choices.platform.jboss-wildfly-8.path=$JBOSS_HOME -Dejb-jms.password=artificer1! \
    && cp /tmp/artificer/artificer-realm.json $JBOSS_HOME \
    && rm -rf /tmp/artificer \
    && echo 'JAVA_OPTS="$JAVA_OPTS -Djboss.bind.address=0.0.0.0 -Djboss.bind.address.management=0.0.0.0 -Dkeycloak.import=$JBOSS_HOME/artificer-realm.json"' >> $JBOSS_HOME/bin/standalone.conf \
    && sed -i -e "s/Xms64m/Xms1G/g" $JBOSS_HOME/bin/standalone.conf -e "s/Xmx512m/Xmx1G/g" -e "s/MaxPermSize=256m/MaxPermSize=384m/g" $JBOSS_HOME/bin/standalone.conf
