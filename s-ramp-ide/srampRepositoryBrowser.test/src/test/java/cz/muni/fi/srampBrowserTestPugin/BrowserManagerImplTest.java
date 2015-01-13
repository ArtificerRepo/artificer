package cz.muni.fi.srampBrowserTestPugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;

import cz.muni.fi.srampRepositoryBrowser.manager.BrowserManagerImpl;
import cz.muni.fi.srampRepositoryBrowser.manager.ServiceFailureException;

public class BrowserManagerImplTest {

	private BrowserManagerImpl manager;
	private static IFile f;
	private static IProject project;

	@BeforeClass
	public static void before() throws CoreException,
			UnsupportedEncodingException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		project = root.getProject("test");

		if (project.exists()) {
			project.delete(IResource.NONE, null);
		}
		project.create(null);
		project.open(null);

		f = project.getFile("testFile");
		if (f.exists()) {
			f.delete(IResource.NONE, null);

		}

		String s = "test";
		f.create(new ByteArrayInputStream(s.getBytes("UTF-8")), IResource.NONE,
				null);
	}

	@AfterClass
	public static void after() throws CoreException {
		if (project != null) {
			if (project.exists()) {
				project.delete(IResource.NONE, null);
			}
		}
	}

	@Before
	public void setUp() throws Exception {

		manager = new BrowserManagerImpl();
		manager.setConnection("http://localhost:8080/s-ramp-server", "admin",
				"a");

	}

	@After
	public void tearDown() throws CoreException {
		QueryResultSet qs = manager.ExecuteQuery(manager.listAllArtifacts());

		for (ArtifactSummary as : qs) {
			manager.deleteArtifact(as.getUuid(), as.getType());
		}

	}

	@Test
	public void listAllArtifactBasicTest() throws IOException, CoreException {

		QueryResultSet qs = manager.ExecuteQuery(manager.listAllArtifacts());

		assertEquals(0, qs.getTotalResults());

		manager.uploadArtifact(f, "", "", new Properties());
		manager.uploadArtifact(f, "file", "", new Properties());
		manager.uploadArtifact(f, "file", "File", new Properties());
		manager.uploadArtifact(f, "", "File", new Properties());

		qs = manager.ExecuteQuery(manager.listAllArtifacts());

		assertEquals(4, qs.getTotalResults());

	}

	@Test
	public void uploadArtifactBasicTest() throws IOException, CoreException {

		manager.uploadArtifact(f, "", "", new Properties());

		SrampClientQuery qr = manager.buildQuery("/s-ramp[@name =?]");
		qr.parameter(f.getName());
		QueryResultSet qs = manager.ExecuteQuery(qr);

		assertEquals(1, qs.getTotalResults());

		ArtifactSummary as = qs.get(0);
		assertcheckArtifact(f.getName(), ArtifactType.valueOf("Document"), as);
	}

	@Test
	public void uploadArtifactUserTypeNameTest() throws IOException,
			CoreException {

		String name = "name";
		String type = "File";
		ArtifactType ba = ArtifactType.valueOf(type);

		manager.uploadArtifact(f, name, type, new Properties());

		SrampClientQuery qr = manager.buildQuery("/s-ramp[@name =?]");
		qr.parameter(name);
		QueryResultSet qs = manager.ExecuteQuery(qr);
		assertEquals(1, qs.getTotalResults());

		ArtifactSummary as = qs.get(0);

		assertcheckArtifact(name, ba, as);

	}

	@Test(expected = ServiceFailureException.class)
	public void uploadArtifactFileDontExists() {

		IFile f = project.getFile("notExist");

		manager.uploadArtifact(f, "", "", new Properties());

	}

	@Test
	public void deleteArtifactBasicTest() throws IOException, CoreException {

		manager.uploadArtifact(f, "", "", new Properties());

		QueryResultSet qs = manager.ExecuteQuery(manager.listAllArtifacts());

		ArtifactSummary as = qs.get(0);

		manager.deleteArtifact(as.getUuid(), as.getType());
		qs = manager.ExecuteQuery(manager.listAllArtifacts());

		assertEquals(0, qs.getTotalResults());

		manager.uploadArtifact(f, "file", "", new Properties());
		manager.uploadArtifact(f, "file", "File", new Properties());

		String query = "/s-ramp/wsdl/File[@name = 'file']";
		qs = manager.ExecuteQuery(manager.buildQuery(query));
		as = qs.get(0);
		manager.deleteArtifact(as.getUuid(), as.getType());

		qs = manager.ExecuteQuery(manager.listAllArtifacts());
		as = qs.get(0);
		assertcheckArtifact("file", ArtifactType.valueOf("Document"), as);

	}

	@Test(expected = ServiceFailureException.class)
	public void deleteNoArtifactTest() throws IOException {

		manager.deleteArtifact("xx", ArtifactType.valueOf("Document"));
	}

	@Test
	public void executeQueryNameTest() {

		QueryResultSet qs = manager.ExecuteQuery(manager.listAllArtifacts());

		assertEquals(0, qs.getTotalResults());

		String name1 = "file";
		String name2 = "file2";
		manager.uploadArtifact(f, name2, "", new Properties());
		manager.uploadArtifact(f, name1, "", new Properties());
		manager.uploadArtifact(f, name1, "File", new Properties());
		manager.uploadArtifact(f, name2, "File", new Properties());

		SrampClientQuery query = manager.buildQuery("/s-ramp[@name =?]");
		query.parameter(name1);
		qs = manager.ExecuteQuery(query);

		assertEquals(2, qs.getTotalResults());

	}

	@Test
	public void executeQueryTypeTest() {

		QueryResultSet qs = manager.ExecuteQuery(manager.listAllArtifacts());

		assertEquals(0, qs.getTotalResults());

		String name1 = "file";
		String name2 = "file2";
		manager.uploadArtifact(f, name2, "", new Properties());
		manager.uploadArtifact(f, name1, "", new Properties());
		manager.uploadArtifact(f, name1, "", new Properties());
		manager.uploadArtifact(f, name2, "File", new Properties());

		SrampClientQuery query = manager.buildQuery("/s-ramp/ext/File");

		qs = manager.ExecuteQuery(query);

		assertEquals(1, qs.getTotalResults());

		query = manager.buildQuery("/s-ramp/core/Document");

		qs = manager.ExecuteQuery(query);

		assertEquals(3, qs.getTotalResults());

	}

	@Test
	public void importToWorkspaceBasicTest() throws IOException, CoreException {

		manager.uploadArtifact(f, "testFile", "", new Properties());

		SrampClientQuery query = manager.buildQuery("/s-ramp[@name =?]");
		query.parameter("testFile");
		QueryResultSet qs = manager.ExecuteQuery(query);

		manager.importToWorkspace(qs.get(0), project);

		IFile ex = project.getFile("testFile");
		assertTrue(ex.exists());

		InputStream fis = ex.getContents();

		assertEquals("test", IOUtils.toString(fis, "UTF-8"));

	}

	private void assertcheckArtifact(String name, ArtifactType ba,
			ArtifactSummary as) {
		assertEquals(name, as.getName());
		assertEquals(ba.getType(), as.getType().getType());

	}

}
