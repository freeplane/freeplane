package org.freeplane.plugin.workspace;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

public class RepositoryImpl implements Repository {

	@Override
	public String getDescriptor(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getDescriptorKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value getDescriptorValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value[] getDescriptorValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSingleValueDescriptor(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStandardDescriptor(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Session login() throws LoginException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session login(Credentials arg0) throws LoginException,
			RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session login(String arg0) throws LoginException,
			NoSuchWorkspaceException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session login(Credentials arg0, String arg1) throws LoginException,
			NoSuchWorkspaceException, RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

}
