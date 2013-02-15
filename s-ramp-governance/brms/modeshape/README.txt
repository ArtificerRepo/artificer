The following commands where used to create the standalone.xml used to configure
the ModeShape S-RAMP Service (and checked into the updates dir):

./jboss-cli.sh
connect
/extension=org.modeshape:add()
./subsystem=modeshape:add
/subsystem=infinispan/cache-container=modeshape:add
/subsystem=infinispan/cache-container=modeshape/local-cache=sramp:add
/subsystem=infinispan/cache-container=modeshape/local-cache=sramp/transaction=TRANSACTION:add(mode=NON_XA)
/subsystem=infinispan/cache-container=modeshape/local-cache=sramp/file-store=FILE_STORE:add(path="modeshape/store/sramp",relative-to="jboss.server.data.dir",passivation=false,purge=false)
:reload
:reload
./subsystem=modeshape/repository=sramp:add(cache-name="sramp",cache-container="modeshape")
:reload 
/subsystem=modeshape/repository=sramp:read-resource(recursive=true)
/subsystem=modeshape/repository=sramp/configuration=index-storage:add()