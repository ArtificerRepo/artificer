package s_ramp.ns.s_ramp_v1_0.binding_doc;

import static org.junit.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.Assert;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.Test;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ObjectFactory;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

public class BD_2_3_5_1_Publishing_an_Artifact_Entry_Test {

    /**
     * Publishing an artifact entry.
     */
    @Test
    public void summaryArtifactEntry() {
        
        System.out.println("1. Read the accountingTypes.xsd file from disk");
        Assert.assertNotNull("Cannot locate file accountingTypes.xsd",
                this.getClass().getClassLoader().getResourceAsStream("accountingTypes.xsd"));

        String accountingTypesXSD = new Scanner(this.getClass().getClassLoader().getResourceAsStream("accountingTypes.xsd")).useDelimiter("\\Z").next();
        
        System.out.println("2. Upload it to the repo");
        
    }

}
