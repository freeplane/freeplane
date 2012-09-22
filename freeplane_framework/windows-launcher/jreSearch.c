/*
	Launch4j (http://launch4j.sourceforge.net/)
	Cross-platform Java application wrapper for creating Windows native executables.

	Copyright (c) 2004, 2008 Grzegorz Kowal,
							 Ian Roberts (jdk preference patch)
							 Sylvain Mina (single instance patch)

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	Except as contained in this notice, the name(s) of the above copyright holders
	shall not be used in advertising or otherwise to promote the sale, use or other
	dealings in this Software without prior written authorization.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
*/

#include "jreSearch.h"

HMODULE hModule;
FILE* hLog;
BOOL console = FALSE;
BOOL wow64 = FALSE;
int foundJava = NO_JAVA_FOUND;

struct _stat statBuf;
PROCESS_INFORMATION pi;
DWORD priority;

char mutexName[STR] = {0};

char errUrl[256] = {0};
char errTitle[STR] = "Launch4j";
char errMsg[BIG_STR] = {0};

char javaMinVer[STR] = {0};
char javaMaxVer[STR] = {0};
char foundJavaVer[STR] = {0};
char foundJavaKey[_MAX_PATH] = {0};


void setWow64Flag() {
	LPFN_ISWOW64PROCESS fnIsWow64Process = (LPFN_ISWOW64PROCESS)GetProcAddress(
			GetModuleHandle(TEXT("kernel32")), "IsWow64Process");

	if (fnIsWow64Process != NULL) {
		fnIsWow64Process(GetCurrentProcess(), &wow64);
	}
	debug("WOW64:\t\t%s\n", wow64 ? "yes" : "no");
}

void setConsoleFlag() {
     console = TRUE;
}

BOOL regQueryValue(const char* regPath, char* buffer,
		unsigned long bufferLength) {
	HKEY hRootKey;
	char* key;
	char* value;
	if (strstr(regPath, HKEY_CLASSES_ROOT_STR) == regPath) {
		hRootKey = HKEY_CLASSES_ROOT;
	} else if (strstr(regPath, HKEY_CURRENT_USER_STR) == regPath) {
		hRootKey = HKEY_CURRENT_USER;
	} else if (strstr(regPath, HKEY_LOCAL_MACHINE_STR) == regPath) {
		hRootKey = HKEY_LOCAL_MACHINE;
	} else if (strstr(regPath, HKEY_USERS_STR) == regPath) {
		hRootKey = HKEY_USERS;
	} else if (strstr(regPath, HKEY_CURRENT_CONFIG_STR) == regPath) {
		hRootKey = HKEY_CURRENT_CONFIG;
	} else {
		return FALSE;
	}
	key = strchr(regPath, '\\') + 1;
	value = strrchr(regPath, '\\') + 1;
	*(value - 1) = 0;

	HKEY hKey;
	unsigned long datatype;
	BOOL result = FALSE;
	if ((wow64 && RegOpenKeyEx(hRootKey,
								key,
								0,
	        					KEY_READ | KEY_WOW64_64KEY,
								&hKey) == ERROR_SUCCESS)
			|| RegOpenKeyEx(hRootKey,
								key,
								0,
	        					KEY_READ,
								&hKey) == ERROR_SUCCESS) {
		result = RegQueryValueEx(hKey, value, NULL, &datatype, (LPBYTE)buffer, &bufferLength)
				== ERROR_SUCCESS;
		RegCloseKey(hKey);
	}
	*(value - 1) = '\\';
	return result;
}

void regSearch(const HKEY hKey, const char* keyName, const int searchType) {
	DWORD x = 0;
	unsigned long size = BIG_STR;
	FILETIME time;
	char buffer[BIG_STR] = {0};
	while (RegEnumKeyEx(
				hKey,			// handle to key to enumerate
				x++,			// index of subkey to enumerate
				buffer,			// address of buffer for subkey name
				&size,			// address for size of subkey buffer
				NULL,			// reserved
				NULL,			// address of buffer for class string
				NULL,			// address for size of class buffer
				&time) == ERROR_SUCCESS) {

		if (strcmp(buffer, javaMinVer) >= 0
				&& (!*javaMaxVer || strcmp(buffer, javaMaxVer) <= 0)
				&& strcmp(buffer, foundJavaVer) > 0) {
			strcpy(foundJavaVer, buffer);
			strcpy(foundJavaKey, keyName);
			appendPath(foundJavaKey, buffer);
			foundJava = searchType;
			debug("Match:\t\t%s\\%s\n", keyName, buffer);
		} else {
			debug("Ignore:\t\t%s\\%s\n", keyName, buffer);
		}
		size = BIG_STR;
	}
}

void regSearchWow(const char* keyName, const int searchType) {
	HKEY hKey;
	debug("64-bit search:\t%s...\n", keyName);
	if (wow64 && RegOpenKeyEx(HKEY_LOCAL_MACHINE,
			keyName,
			0,
            KEY_READ | KEY_WOW64_64KEY,
			&hKey) == ERROR_SUCCESS) {

		regSearch(hKey, keyName, searchType | KEY_WOW64_64KEY);
		RegCloseKey(hKey);
		if ((foundJava & KEY_WOW64_64KEY) != NO_JAVA_FOUND)
		{
			debug("Using 64-bit runtime.\n");
			return;
		}
	}
	debug("32-bit search:\t%s...\n", keyName);
	if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,
			keyName,
			0,
            KEY_READ,
			&hKey) == ERROR_SUCCESS) {
		regSearch(hKey, keyName, searchType);
		RegCloseKey(hKey);
	}
}

void regSearchJreSdk(const char* jreKeyName, const char* sdkKeyName) {
	regSearchWow(jreKeyName, FOUND_JRE);
	regSearchWow(sdkKeyName, FOUND_SDK);
}

BOOL findJavaHome(char* path) {
	regSearchJreSdk("SOFTWARE\\JavaSoft\\Java Runtime Environment",
					"SOFTWARE\\JavaSoft\\Java Development Kit");
	if (foundJava == NO_JAVA_FOUND) {
		regSearchJreSdk("SOFTWARE\\IBM\\Java2 Runtime Environment",
						"SOFTWARE\\IBM\\Java Development Kit");
	}
	if (foundJava != NO_JAVA_FOUND) {
		HKEY hKey;
		if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,
				foundJavaKey,
				0,
	            KEY_READ | (foundJava & KEY_WOW64_64KEY),
				&hKey) == ERROR_SUCCESS) {
			unsigned char buffer[BIG_STR] = {0};
			unsigned long bufferlength = BIG_STR;
			unsigned long datatype;
			if (RegQueryValueEx(hKey, "JavaHome", NULL, &datatype, buffer,
					&bufferlength) == ERROR_SUCCESS) {
				int i = 0;
				do {
					path[i] = buffer[i];
				} while (path[i++] != 0);
				if (foundJava & FOUND_SDK) {
					appendPath(path, "jre");
				}
				RegCloseKey(hKey);
				return TRUE;
			}
			RegCloseKey(hKey);
		}
	}
	return FALSE;
}

/*
 * Extract the executable name, returns path length.
 */
int getExePath(char* exePath) {
    if (GetModuleFileName(hModule, exePath, _MAX_PATH) == 0) {
        return -1;
    }
	return strrchr(exePath, '\\') - exePath;
}

void appendPath(char* basepath, const char* path) {
	if (basepath[strlen(basepath) - 1] != '\\') {
		strcat(basepath, "\\");
	}
	strcat(basepath, path);
}

BOOL isPathOk(const char* path) {
	BOOL result = FALSE;
	if (*path) {
		result = _stat(path, &statBuf) == 0;
	}	
	return result;
}
