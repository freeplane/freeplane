package org.freeplane.api;

import java.io.File;
import java.net.URL;

public interface HeadlessMapCreator {
	HeadlessLoader load(File file);
	HeadlessLoader load(URL file);
	HeadlessLoader load(String file);
}