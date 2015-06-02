package org.artificer.repository.hibernate.tool;

import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntologyClass;
import org.artificer.repository.hibernate.audit.ArtificerAuditEntry;
import org.artificer.repository.hibernate.audit.ArtificerAuditItem;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerComment;
import org.artificer.repository.hibernate.entity.ArtificerDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerProperty;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.repository.hibernate.entity.ArtificerStoredQuery;
import org.artificer.repository.hibernate.entity.ArtificerTarget;
import org.artificer.repository.hibernate.entity.ArtificerWsdlDerivedArtifact;
import org.artificer.repository.hibernate.entity.ArtificerWsdlDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerXsdDocumentArtifact;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * This uses the Hibernate Schema Exporter to generate initial DDL using Artificer entities.  However, this should only
 * be used for testing or to initially seed the DDL.  For production use, DDL should be optimized by hand, then used
 * in a Liquibase maintenance strategy.
 *
 * @author Brett Meyer
 */
public class ArtificerSchemaExporter {

    public static void main(String[] args) {
        export("org.hibernate.dialect.H2Dialect", "h2.sql");
        export("org.hibernate.dialect.MySQL5Dialect", "mysql5.sql");
        export("org.hibernate.dialect.PostgreSQL82Dialect", "postgres9.sql");
        export("org.hibernate.dialect.Oracle10gDialect", "oracle10.sql");
        export("org.hibernate.dialect.SQLServer2012Dialect", "mssql2012.sql");
        export("org.hibernate.dialect.DB2Dialect", "db2.sql");
    }

    private static void export(String dialect, String filename) {
        Configuration cfg = new Configuration();

        cfg.addAnnotatedClass(ArtificerArtifact.class);
        cfg.addAnnotatedClass(ArtificerComment.class);
        cfg.addAnnotatedClass(ArtificerDocumentArtifact.class);
        cfg.addAnnotatedClass(ArtificerProperty.class);
        cfg.addAnnotatedClass(ArtificerRelationship.class);
        cfg.addAnnotatedClass(ArtificerStoredQuery.class);
        cfg.addAnnotatedClass(ArtificerTarget.class);
        cfg.addAnnotatedClass(ArtificerWsdlDerivedArtifact.class);
        cfg.addAnnotatedClass(ArtificerWsdlDocumentArtifact.class);
        cfg.addAnnotatedClass(ArtificerXsdDocumentArtifact.class);
        cfg.addAnnotatedClass(ArtificerAuditEntry.class);
        cfg.addAnnotatedClass(ArtificerAuditItem.class);
        cfg.addAnnotatedClass(ArtificerOntology.class);
        cfg.addAnnotatedClass(ArtificerOntologyClass.class);

        cfg.setProperty("hibernate.dialect", dialect);

        SchemaExport schemaExport = new SchemaExport(cfg);
        schemaExport.setDelimiter(";");
        schemaExport.setOutputFile("distro/assembly/src/main/resources/ROOT/ddl/" + filename);
        schemaExport.execute(true, false, false, true);
    }
}
