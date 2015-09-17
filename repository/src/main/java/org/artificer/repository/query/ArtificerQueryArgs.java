package org.artificer.repository.query;

import javax.persistence.Query;
import java.io.Serializable;

/**
 * Represents ordering and paging arguments, given to an Artificer query.  Note that the fields should *not* be given
 * default values.  If not paged, they should be null!
 *
 * @author Brett Meyer
 */
public class ArtificerQueryArgs implements Serializable {

    private String orderBy;
    private Boolean orderAscending;
    private Integer startIndex;
    private Integer count;

    public ArtificerQueryArgs() {}

    public ArtificerQueryArgs(String orderBy, Boolean orderAscending) {
        initOrdering(orderBy, orderAscending);
    }

    public ArtificerQueryArgs(Integer startPage, Integer startIndex, Integer count) {
        initPaging(startPage, startIndex, count);
    }

    public ArtificerQueryArgs(String orderBy, Boolean orderAscending, Integer startPage, Integer startIndex, Integer count) {
        initOrdering(orderBy, orderAscending);
        initPaging(startPage, startIndex, count);
    }

    private void initOrdering(String orderBy, Boolean orderAscending) {
        this.orderBy = orderBy == null ? "name" : orderBy;
        this.orderAscending = orderAscending == null ? true : orderAscending;
    }

    private void initPaging(Integer startPage, Integer startIndex, Integer count) {
        this.startIndex = startIndex(startPage, startIndex, count);
        this.count = count == null ? 100 : count;
    }

    private int startIndex(Integer startPage, Integer startIndex, Integer count) {
        if (startIndex == null && startPage != null) {
            int c = count != null ? count.intValue() : 100;
            startIndex = (startPage.intValue() - 1) * c;
        }

        if (startIndex == null)
            startIndex = 0;

        return startIndex;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Boolean getOrderAscending() {
        return orderAscending;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public Integer getCount() {
        return count;
    }

    public void applyPaging(Query query) {
        if (count != null) {
            query.setMaxResults(count);
        }
        if (startIndex != null) {
            query.setFirstResult(startIndex);
        }
    }
}
