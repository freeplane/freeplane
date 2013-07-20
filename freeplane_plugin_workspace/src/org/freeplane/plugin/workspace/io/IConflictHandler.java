package org.freeplane.plugin.workspace.io;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public interface IConflictHandler {

	void resolveConflict(File file, Properties properties) throws CancelExecutionException, SkipTaskException, IOException;

}
