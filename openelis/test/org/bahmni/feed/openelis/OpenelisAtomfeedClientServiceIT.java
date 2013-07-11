package org.bahmni.feed.openelis;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import org.bahmni.feed.openelis.event.EventWorkerFactory;
import org.bahmni.feed.openelis.externalreference.dao.ExternalReferenceDao;
import org.bahmni.feed.openelis.externalreference.daoimpl.ExternalReferenceDaoImpl;
import org.bahmni.feed.openelis.externalreference.valueholder.ExternalReference;
import org.hibernate.Transaction;
import org.ict4h.atomfeed.Configuration;
import org.ict4h.atomfeed.client.domain.Marker;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.FeedEnumerator;
import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
import org.ict4h.atomfeed.jdbc.JdbcUtils;
import org.ict4h.atomfeed.jdbc.PropertiesJdbcConnectionProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import us.mn.state.health.lims.hibernate.HibernateUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*@ContextConfiguration(locations = {"classpath*:applicationContext-openelisTest.xml"})*/
public class OpenelisAtomfeedClientServiceIT {
    private   AllFeeds allFeedsMock;
    private AtomFeedProperties atomFeedProperties;


    private AtomFeedClient atomFeedClient;

    private URI notificationsUri;
    private URI firstFeedUri;
    private URI secondFeedUri;
    private URI recentFeedUri;
    Feed first;
    Feed second;
    Feed last;
    OpenelisAllMarkersJdbcImpl allMarkersJdbc;
    private JdbcConnectionProvider jdbcConnectionProvider;
    ExternalReferenceDaoImpl externalReferenceDao;

    static final String PANEL_EVENT_CONTENT = " {\"category\": \"panel\", \"list_price\": \"0.0\", \"name\": \"ECHO\", \"type\": \"service\", \"standard_price\": \"0.0\", \"uom_id\": 1, \"uom_po_id\": 1, \"categ_id\": 33, \"id\": 193}";

    static final String LAB_EVENT_CONTENT = " {\"category\": \"lab\", \"list_price\": \"0.0\", \"name\": \"ECHO\", \"type\": \"service\", \"standard_price\": \"0.0\", \"uom_id\": 1, \"uom_po_id\": 1, \"categ_id\": 33, \"id\": 193}";

    @Before
    public void setUp() throws URISyntaxException {
        atomFeedProperties = mock(AtomFeedProperties.class);
        allFeedsMock = mock(AllFeeds.class);
        jdbcConnectionProvider = new PropertiesJdbcConnectionProvider();
        externalReferenceDao = new ExternalReferenceDaoImpl();
        allMarkersJdbc = new OpenelisAllMarkersJdbcImpl(jdbcConnectionProvider);

        atomFeedClient =  new AtomFeedClient(allFeedsMock,allMarkersJdbc,new AllFailedEventsJdbcImpl(jdbcConnectionProvider),true);

        first = new Feed();
        second = new Feed();
        last = new Feed();

        first.setEntries(getEntries(1,3,PANEL_EVENT_CONTENT));
        second.setEntries(getEntries(4,6,LAB_EVENT_CONTENT));
        last.setEntries(getEntries(7,9,PANEL_EVENT_CONTENT));

        notificationsUri = new URI("http://host/patients/notifications");
        recentFeedUri = new URI("http://host/patients/3");
        secondFeedUri = new URI("http://host/patients/2");
        firstFeedUri = new URI("http://host/patients/1");

        last.setOtherLinks(Arrays.asList(new Link[]{getLink("prev-archive", secondFeedUri),getLink("self", recentFeedUri)}));

        second.setOtherLinks(Arrays.asList(getLink("prev-archive", firstFeedUri), getLink("next-archive", recentFeedUri),getLink("self", secondFeedUri)));

        first.setOtherLinks(Arrays.asList(new Link[]{getLink("next-archive", secondFeedUri),getLink("self", firstFeedUri)}));

    }

    @After
    public void tearDown() throws Exception {
        allMarkersJdbc.delete(notificationsUri);
        ExternalReference reference = externalReferenceDao.getData("1123456");
        Assert.assertNotNull(reference);

        Transaction transaction = HibernateUtil.getSession().beginTransaction();
        externalReferenceDao.deleteData(reference);
        transaction.commit();

    }

    private Link getLink(String archiveType, URI uri) {
        Link link = new Link();
        link.setRel(archiveType);
        link.setHref(uri.toString());
        return link;
    }

    private List<Entry> getEntries(int startNum, int endNum,String eventContent) {
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = startNum; i <= endNum; i++) {

            Entry entry = createEntry(eventContent);
            entry.setId("" + i);
            entries.add(entry);
        }
        return entries;
    }

    private List<String> getEntries(FeedEnumerator feedEnumerator) {
        List<String> entryIds = new ArrayList<String>();
        for(Entry entry : feedEnumerator) {
            entryIds.add(entry.getId());
        }
        return entryIds;
    }


    @Test
    public void shouldCreate() throws URISyntaxException {
  /*      when(atomFeedProperties.getFeedUri("openerp.labtest.feed.generator.uri")).thenReturn("http://host/patients/notifications");
        when(allFeedsMock.getFor(notificationsUri)).thenReturn(last);
        when(allFeedsMock.getFor(recentFeedUri)).thenReturn(last);
        when(allFeedsMock.getFor(secondFeedUri)).thenReturn(second);
        when(allFeedsMock.getFor(firstFeedUri)).thenReturn(first);


        OpeneERPLabTestFeedClient feedClient = new OpeneERPLabTestFeedClient(atomFeedProperties,atomFeedClient,new EventWorkerFactory());
        feedClient.setFeedName("openerp.labtest.feed.generator.uri");
        feedClient.processFeed();

        Marker marker = allMarkersJdbc.get(notificationsUri);
        assertThat(marker.getLastReadEntryId(), is("9") );
    */
    }

    private Entry createEntry(String eventContent) {
        Entry entry = new Entry();
        ArrayList<Content> contents = new ArrayList<Content>();
        Content content = new Content();
        String value =eventContent;
        content.setValue(String.format("%s%s%s", "<![CDATA[", value, "]]>"));
        contents.add(content);
        entry.setContents(contents);

        return entry;
    }

    class OpenelisAllMarkersJdbcImpl extends AllMarkersJdbcImpl{
        public OpenelisAllMarkersJdbcImpl(JdbcConnectionProvider connectionProvider) {
            super(connectionProvider);
        }

        public void delete(URI feedUri) throws SQLException {
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet resultSet = null;
            try {
                connection = jdbcConnectionProvider.getConnection();
                String sql = String.format("delete from %s where feed_uri = ?",
                        JdbcUtils.getTableName(Configuration.getInstance().getSchema(), "markers"));
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, feedUri.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                connection.close();
            }
        }

    }

}



