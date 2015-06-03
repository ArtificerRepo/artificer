package org.artificer.repository.hibernate;

import org.apache.commons.lang.StringUtils;
import org.artificer.repository.filter.ServletCredentialsFilter;
import org.artificer.repository.hibernate.entity.ArtificerStoredQuery;
import org.artificer.repository.hibernate.entity.ArtificerUser;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utility that converts S-RAMP bindings to Artificer JPA entities (and vice versa).
 *
 * @author Brett Meyer
 */
public class HibernateEntityFactory {

    public static List<StoredQuery> storedQueries(List<ArtificerStoredQuery> artificerStoredQueries) {
        List<StoredQuery> srampStoredQueries = new ArrayList<>();
        for (ArtificerStoredQuery artificerStoredQuery : artificerStoredQueries) {
            srampStoredQueries.add(storedQuery(artificerStoredQuery));
        }
        return srampStoredQueries;
    }

    public static ArtificerStoredQuery storedQuery(StoredQuery srampStoredQuery) {
        ArtificerStoredQuery artificerStoredQuery = new ArtificerStoredQuery();
        processStoredQuery(artificerStoredQuery, srampStoredQuery);
        return artificerStoredQuery;
    }

    public static void processStoredQuery(ArtificerStoredQuery artificerStoredQuery, StoredQuery srampStoredQuery) {
        artificerStoredQuery.setQueryExpression(srampStoredQuery.getQueryExpression());
        artificerStoredQuery.setQueryName(srampStoredQuery.getQueryName());
        artificerStoredQuery.getPropertyNames().clear();
        for (String propertyName : srampStoredQuery.getPropertyName()) {
            artificerStoredQuery.getPropertyNames().add(propertyName);
        }
    }

    public static StoredQuery storedQuery(ArtificerStoredQuery artificerStoredQuery) {
        StoredQuery srampStoredQuery = new StoredQuery();
        srampStoredQuery.setQueryExpression(artificerStoredQuery.getQueryExpression());
        srampStoredQuery.setQueryName(artificerStoredQuery.getQueryName());
        for (String propertyName : artificerStoredQuery.getPropertyNames()) {
            srampStoredQuery.getPropertyName().add(propertyName);
        }
        return srampStoredQuery;
    }

    public static ArtificerUser user() {
        ArtificerUser artificerUser = new ArtificerUser();
        artificerUser.setLastActionTime(Calendar.getInstance());
        artificerUser.setUsername(ServletCredentialsFilter.getUsername());
        return artificerUser;
    }

    public static ArtificerUser user(String username, XMLGregorianCalendar time) {
        if (StringUtils.isBlank(username)) {
            return user();
        } else {
            ArtificerUser artificerUser = new ArtificerUser();

            if (time != null) {
                artificerUser.setLastActionTime(time.toGregorianCalendar());
            } else {
                artificerUser.setLastActionTime(Calendar.getInstance());
            }

            if (StringUtils.isNotBlank(username)) {
                artificerUser.setUsername(username);
            } else {
                artificerUser.setUsername(ServletCredentialsFilter.getUsername());
            }

            return artificerUser;
        }
    }

    public static XMLGregorianCalendar calendar(Calendar c) {
        return calendar(c.getTimeInMillis());
    }

    public static XMLGregorianCalendar calendar(long time) {
        try {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(time);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (DatatypeConfigurationException e) {
            return null;
        }
    }
}
