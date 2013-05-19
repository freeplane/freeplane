package org.freeplane.plugin.workspace.io;

import java.io.IOException;
import java.util.Properties;

public interface ITask {

	public void exec(Properties properties) throws SkipTaskException, CancelExecutionException, IOException;
}
