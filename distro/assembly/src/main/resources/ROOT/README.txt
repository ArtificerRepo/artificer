== Welcome ==

Welcome to the Artificer Distribution (${project.version}) -- thanks for
downloading!  This distribution comes with the following:

1) bin/*.war - the WARs that make up the Artificer runtime, including
   (but not limited to) the server WAR and the web UI WAR
2) bin/artificer-shell-${project.version}.jar - the Artificer interactive shell.
3) bin/artificer.* - shell and batch scripts to run the Artificer interactive
   shell.
4) demos - a number of demo maven projects to help you get started with
   Artificer.
5) src - all of the Artificer source code, in a number of "-sources" JARs.
6) build.xml - an Ant script that will install and configure Artificer into
   one of:
   A) WildFly 9
7) ddl/*.sql - DDL SQL scripts for creating the necessary DB tables, constraints, and indexes.



== Prerequisites ==

The Artificer application is written in Java. To get started make sure your system has the following:

* Java JDK 1.7 or newer
* Maven 3.0.3 or newer to build and run the demos
* EE application sever: WildFly (http://www.wildfly.org/downloads) or JBoss EAP (http://www.jboss.org/jbossas/downloads)



== Installation ==

Run './install.sh' or 'install.bat'.  Alternatively, if you have Apache Ant 1.7 (or later) installed, simply run 'ant'.

The installer will ask you to choose a runtime platform.  Currently the following platforms are supported:

* WildFly 9

Simply follow the installer instructions to install onto the platform of your choice.  We recommend installing
into a clean version of whatever platform you choose, to minimize the risk of colliding with other projects.
Note that you must have already downloaded and installed the platform on which you wish to run

Finally, please make sure the JBoss admin password you choose (the installer will prompt you for this) contains
letters, numbers, and punctuation (and is at least 8 characters long).



== User Management ==

The Artificer WARs are protected using web application security mechanisms
configured in the web.xml.  By default, Artificer uses single-sign-on (SSO) as the actual authentication
mechanism.  The SSO is provided via integration with the Keycloak framework, a powerful out-of-the-box auth solution.
The actual web.xml configuration uses a standard basic security-context, but SSO
is provided under-the-hood.

The Artificer distribution ships with a *artificer-realm.json* file that's completely pre-configured and can be
directly imported into Keycloak.  Startup WildFly/EAP and visit *localhost:8080/auth/admin/master/console/#/create/realm*.  The initial
administrator account uses "admin" for both the username and password.  There, you can upload the *artificer-realm.json*
file and tweak the realm/accounts.  Note that you can also import the realm the first time you start up WildFly/EAP.
Simply include the following argument:

bin/standalone.sh -c standalone-full.xml -Dkeycloak.import=[ARTIFICER HOME]/artificer-realm.json

By default, the realm import creates an "admin" user (password: "artificer1!").  This user is used to access the Artificer
server, UI, shell, and other tools.  Again, the user is configurable through the Keycloak admin console.

Feel free to manually create a realm for scratch.  However, there are a few requirements
(see *artificer-realm.json* for example values):

1. The realm must be named *artificer*
2. Defined applications must include *artificer-server* and *artificer-ui*
3. To use our themed login page, use *artificer* for the *loginTheme*
4. Each user must have at least *admin* and *user* values for the *realmRole*



== DDL ==

Artificer uses RDBMS (through JPA) for all persistence.  The distribution ships with DDL SQL files for the "big 5"
databases: Postgres, MySQL, Oracle, SQL Server, and DB2/IBM.  In addition, H2 is included (the default DB
in WildFly/EAP).

During installation, a simple, file-based H2 datasource is automatically deployed.
(see `$JBOSS_HOME/standalone/deployments/artificer-h2-ds.xml`).  If you use something other than that datasource, be sure to update the
relevant connection properties in `artificer.properties`.  See the "Datasource" section for more details.

To install the DDL, you have two options.  Obviously, manually importing it, using your favorite SQL client, is great.
We're fans of http://www.squirrelsql.org/[SQuirreL]).
As an example, if you install SQuirreL and its H2 plugin, then start WildFly/EAP with Artificer installed,
you could connect to its H2 datasource with the following connection URL.  Use it to import the DDL SQL.

jdbc:h2:[JBOSS_HOME]/standalone/data/h2/artificer;mvcc=true

Alternatively, Artificer will automatically install the DDL for you.  On startup, it checks to see if the necessary
tables exist.  If not, it will automatically execute the DDL SQL for the DB you've selected in `artificer.properties`.



== Hibernate ==

Out of the box, we provide a fairly standard set of Hibernate configuration defaults.  However, for power users, note
that *any* Hibernate property may be set, either in `artificer.properties`, environment variables, or System properties.
More specifically, literally *any* property prefixed by 'hibernate.' will be handed to Hibernate during Artificer
startup.  More specifics are below:



== Datasource ==

By default, Artificer installs a simple, file-based H2 datasource
(see `$JBOSS_HOME/standalone/deployments/artificer-h2-ds.xml`).  However, any other WildFly/EAP datasource can be used.
Just edit the following in `artificer.properties`:

hibernate.dialect = org.hibernate.dialect.H2Dialect
hibernate.connection.driver_class = org.h2.Driver
hibernate.connection.datasource = java:jboss/datasources/artificerH2
hibernate.connection.username = sa
hibernate.connection.password = sa

Note that a datasource is *not* required, although we typically recommend them.  Plain JDBC connection URLs, including
external instances, are also fully supported (use `hibernate.connection.url`, `hibernate.connection.username`,
and `hibernate.connection.password`).  If a connection URL is used, Artificer will automatically wrap it with
HikariCP, a lightweight and extremely performant connection pool library.

Also note that, due to licensing, we only include the JDBC driver for H2.  For all other supported databases,
you'll need to ensure that their JDBC driver JAR(s) are available on the classpath,
typically through a WildFly/EAP module.



== File Content ==

Artificer supports storing artifacts' file content on the filesystem or in JDBC Blobs.  By default, we use the Blob
approach.  However, this is configurable at runtime.  See the 'artificer.file.storage' property in artificer.properties
(values: 'blob' or 'filesystem').  If you use the filesystem, also include a path with the
'artificer.file.storage.filesystem.path' property -- all content will be stored there.

Although JDBC Blobs are the default (purely because they're convenient), most databases have fairly restrictive size
limits.  Even more importantly, there are many vertical and horizontal performance considerations when dealing with
larger files.  For most use cases, we'd actually recommend using the filesystem.



== Advanced Hibernate Configuration ==

For detailed information of what's possible, please see the Hibernate docs themselves.  However, a few things to highlight:

* Second level entity caching is disabled by default.  Due to the nature of Artificer, where usage is typically focused
on large-scale queries, this caching can actually be a bit of a hindrance.  However, if you work repeatedly with
single artifacts, this may be beneficial in some contexts.  See the 'hibernate.cache.*' properties in artificer.properties.
* By default, we use Lucene filesystem indexes with Hibernate Search.  This is also highly configurable and can use
any number of backends.  See the 'hibernate.search.*' properties in artificer.properties.
* Also with Hibernate Search, many usages could benefit from hibernate-search-infinispan, which introduces in-memory
index caching (clusterable, etc.).  This is also disabled by default, but can be set with the correct properties
and including the hibernate-search-infinispan JAR.
See https://docs.jboss.org/author/display/WFLY8/JPA+Reference+Guide#JPAReferenceGuide-UsingtheInfinispansecondlevelcache
for more info.



== Running the Server and Web UI Separately ==

The Artificer server and web UI are purposefully separated into two WARs, allowing you to run each on separate app
servers.  If you do so, you'll need to update the "artificer-ui.atom-api.endpoint" property in
`artificer-ui.properties`.  By default, it assumes co-location and "localhost".



== Remote Connections ==

If you'd like to allow remote, non-localhost connections to Artificer, you'll need to change two items in
`standalone-full.xml`:

* In <interface name="public">, change the inet-address from "127.0.0.1" to "0.0.0.0".
* In the Keycloak subsystem, change the "auth-server-url" from "localhost" to your IP.



== Startup ==

Once Artificer is installed and configured on your preferred platform, you should be able to go ahead and start it up.
Note that standalone-full is required!

bin/standalone.sh -c standalone-full.xml