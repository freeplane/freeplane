package org.freeplane.plugin.workspace;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;

import org.xml.sax.ContentHandler;

public class WorkspaceImpl implements Workspace {

	@Override
	public void clone(String arg0, String arg1, String arg2, boolean arg3)
			throws NoSuchWorkspaceException, ConstraintViolationException,
			VersionException, AccessDeniedException, PathNotFoundException,
			ItemExistsException, LockException, RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copy(String arg0, String arg1)
			throws ConstraintViolationException, VersionException,
			AccessDeniedException, PathNotFoundException, ItemExistsException,
			LockException, RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copy(String arg0, String arg1, String arg2)
			throws NoSuchWorkspaceException, ConstraintViolationException,
			VersionException, AccessDeniedException, PathNotFoundException,
			ItemExistsException, LockException, RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createWorkspace(String arg0) throws AccessDeniedException,
			UnsupportedRepositoryOperationException, RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void createWorkspace(String arg0, String arg1)
			throws AccessDeniedException,
			UnsupportedRepositoryOperationException, NoSuchWorkspaceException,
			RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteWorkspace(String arg0) throws AccessDeniedException,
			UnsupportedRepositoryOperationException, NoSuchWorkspaceException,
			RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getAccessibleWorkspaceNames() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentHandler getImportContentHandler(String arg0, int arg1)
			throws PathNotFoundException, ConstraintViolationException,
			VersionException, LockException, AccessDeniedException,
			RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockManager getLockManager()
			throws UnsupportedRepositoryOperationException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeTypeManager getNodeTypeManager() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservationManager getObservationManager()
			throws UnsupportedRepositoryOperationException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryManager getQueryManager() throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VersionManager getVersionManager()
			throws UnsupportedRepositoryOperationException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importXML(String arg0, InputStream arg1, int arg2)
			throws IOException, VersionException, PathNotFoundException,
			ItemExistsException, ConstraintViolationException,
			InvalidSerializedDataException, LockException,
			AccessDeniedException, RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void move(String arg0, String arg1)
			throws ConstraintViolationException, VersionException,
			AccessDeniedException, PathNotFoundException, ItemExistsException,
			LockException, RepositoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void restore(Version[] arg0, boolean arg1)
			throws ItemExistsException,
			UnsupportedRepositoryOperationException, VersionException,
			LockException, InvalidItemStateException, RepositoryException {
		// TODO Auto-generated method stub

	}

}
