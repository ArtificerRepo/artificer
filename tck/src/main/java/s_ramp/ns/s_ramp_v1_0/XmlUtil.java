package s_ramp.ns.s_ramp_v1_0;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XmlUtil {

    public Marshaller getSrampMarshaller() throws JAXBException {
        JAXBContext jaxbContext=JAXBContext.newInstance("org.oasis_open.docs.s_ramp.ns.s_ramp_v1");
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0/xsdmodel.xsd");
        return marshaller;
    }
}
