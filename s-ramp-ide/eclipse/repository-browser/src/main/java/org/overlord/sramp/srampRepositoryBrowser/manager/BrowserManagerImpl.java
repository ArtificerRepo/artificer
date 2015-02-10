package  org.overlord.sramp.srampRepositoryBrowser.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;

/**
 * Browser manager interface impl.
 * 
 * @author Jan Bouska
 * 
 */

public class BrowserManagerImpl implements BrowserManager {

	private SrampAtomApiClient client;
	private boolean isConected = false;

	public static final Logger log = Logger.getLogger(BrowserManagerImpl.class
			.getName());

	public void setConnection(final String endpoint, final String username,
			final String password) throws ServiceFailureException {
		try {
			client = new SrampAtomApiClient(endpoint, username, password, true);
			isConected = true;
			log.log(Level.INFO, "connected.");
		} catch (SrampClientException e) {
			log.log(Level.WARNING, "Problem with connecting", e);
			throw new ServiceFailureException("Problem with connecting", e);
		} catch (SrampAtomException e) {
			log.log(Level.WARNING, "Problem with connecting", e);
			throw new ServiceFailureException("Problem with connecting", e);
		}
	}

	public boolean isConnected() {
		return isConected;
	}

	public SrampClientQuery buildQuery(String query) {
		return client.buildQuery(query);
	}

	public SrampClientQuery listAllArtifacts() {

		return buildQuery("/s-ramp[@derived = 'false']");

	}

	public void uploadArtifact(IFile content, String name, String type,
			Properties prop) throws ServiceFailureException

	{
		if (!content.exists()) {
			log.log(Level.WARNING,
					"Problem in importing the artifact into the S-RAMP repository - file doesn't exists.");
			throw new ServiceFailureException(
					"Problem in importing the artifact into the S-RAMP repository - file doesn't exists.");
		}

		if (name.isEmpty()) {
			name = content.getName();

		}

		

		try (InputStream is = content.getContents()) {

			BaseArtifactType artifact = client.uploadArtifact(
					ArtifactType.valueOf(type)
					, is, name);
			log.log(Level.INFO, "Uploading the artifact " + artifact.getName());

			for (Object pr : prop.keySet()) {
				String propName = (String) pr;
				String propVal = prop.getProperty((String) pr);

				org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property srProperty = new org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property();
				srProperty.setPropertyName(propName);
				srProperty.setPropertyValue(propVal);

				artifact.getProperty().add(srProperty);
			}

			if (!prop.isEmpty()) {
				client.updateArtifactMetaData(artifact);
				log.log(Level.INFO, "Add the property into the artifact - "
						+ artifact.getName());
			}
		} catch (IOException | CoreException e) {
			log.log(Level.WARNING,
					"Problem with importing to S-RAMP - opening file: "
							+ content.getName() + ".");
			throw new ServiceFailureException(
					"Problem with importing to S-RAMP - opening file: "
							+ content.getName() + ".");

		} catch (SrampClientException | SrampAtomException e) {
			log.log(Level.WARNING,
					"Problem with importing to S-RAMP - importing file: "
							+ content.getName() + ".");
			throw new ServiceFailureException(
					"Problem with importing to S-RAMP - importing file: "
							+ content.getName() + ".");

		}

	}

	public void deleteArtifact(String uuid, ArtifactType type)
			throws ServiceFailureException {

		try {
			client.deleteArtifact(uuid, type);
			log.log(Level.INFO, "delete artifact");
		} catch (SrampAtomException | SrampClientException e) {
			log.log(Level.WARNING, "Problem with deleting artifact (uuid = "
					+ uuid + ").", e);
			throw new ServiceFailureException(
					"Problem with deleting artifact (uuid = " + uuid + ").", e);

		}

	}

	public QueryResultSet ExecuteQuery(SrampClientQuery query)
			throws ServiceFailureException {
		try {
			return query.query();
		} catch (SrampAtomException | SrampClientException e) {
			log.log(Level.WARNING, "Problem with execute query (" + query
					+ ").", e);
			throw new ServiceFailureException("Problem with execute query ("
					+ query + ").", e);
		}
	}

	public void importToWorkspace(ArtifactSummary as, IProject project)
			throws ServiceFailureException {

		InputStream str = null;

		try {
			str = client.getArtifactContent(as);
			project.open(null);
			IFile file = project.getFile(as.getName());
			if (file.exists()) {
				file.delete(IResource.NONE, null);
				log.log(Level.INFO,
						"Remove file with same name as imported file.");
			}
			file.create(str, IResource.NONE, null);
			log.log(Level.INFO,
					"Artifact was succesfully imported to workspace.");

		} catch (SrampClientException | SrampAtomException | CoreException e) {
			log.log(Level.WARNING,
					"Problem in importing the artifact into the workspace (file name: "
							+ as.getName() + ").", e);
			throw new ServiceFailureException(
					"Problem in import to workspace (file name: "
							+ as.getName() + ").", e);
		} finally {

			try {
				if (str != null)
					str.close();

			} catch (IOException e) {
				log.log(Level.WARNING, "Problem with closing streams.", e);
			}

		}

	}

}
