#!/bin/sh
# 2004-02-13, modified for Debian by deb@zorglub.s.bawue.de
# 2004-06-19, rewritten for Linux/UN*X by deb@zorglub.s.bawue.de
#             (based on Jan Schulz's input)
# 2004-11-28, minor changes for version 0.8.0 by deb@zorglub.s.bawue.de
# 2005-01-08, removed bashims to make script POSIX conform
# 2005-01-16, added usage of FREEMIND_BASE_DIR variable
# 2005-02-18, add -Dfreemind.base.dir to make plugins work, add some ""
#             and enhance debug mode.
# 2005-11-08, adding commons-codec to classpath.
# 2005-11-09, add some dpkg/rpm information and check for Sun/Blackdown VM.
# 2006-10-29, follow links to this script using readlink.
# 2008-02-02, improve Java recognition, add lsb_release, fix -x which being empty
#             add -Dgnu.java.awt.peer.gtk.Graphics=Graphics2D for non-Sun JREs
# 2008-02-03, add debug values script and exit
# 2008-05-20 (fc): -Xmx256M added

# we only want to test the script, not FreeMind itself
if ( echo "${DEBUG}" | grep -qe "script" )
then
	set -x
fi

########## FUNCTIONS DEFINITIONS #######################################

_debug() {
	if [ -n "${DEBUG}" ]
	then
		echo "DEBUG:   $1" >&2 
		shift
		for text in "$@"
		do
			echo "         ${text}" >&2
		done
	fi
}

_error() {
	echo "ERROR:   $1" >&2
	shift
	for text in "$@"
	do
		echo "         ${text}" >&2
	done
}

findjava() {
	# We try hard to find the proper 'java' command
	if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]
	then
		_debug "Using \$JAVACMD to find java virtual machine."
	elif [ -n "${JAVA_BINDIR}" ] && [ -x "${JAVA_BINDIR}/java" ]
	then
		JAVACMD="${JAVA_BINDIR}/java"
		_debug "Using \$JAVA_BINDIR to find java virtual machine."
	elif [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]
	then
		JAVACMD="${JAVA_HOME}/bin/java"
		_debug "Using \$JAVA_HOME to find java virtual machine."
	else
		JAVACMD=$(which java)
		if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]
		then
			_debug "Using \$PATH to find java virtual machine."
		elif [ -x /usr/bin/java ]
		then
			_debug "Using /usr/bin/java to find java virtual machine."
			JAVACMD=/usr/bin/java
		fi
	fi

	# if we were successful, we return 0 else we complain and return 1
	if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]
	then
		_debug "Using '$JAVACMD' as java virtual machine..."
		if [ -n "${DEBUG}" ]
		then
			"$JAVACMD" -version >&2
		fi
		if (! "${JAVACMD}" -version 2>&1 | grep -qe 'Java(TM)')
		then
			_error "Your Java is not a derivative from Sun's code," \
			       "=======================================" \
			       "FREEMIND WILL MOST PROBABLY *NOT* WORK," \
			       "=======================================" \
			       "define JAVACMD, JAVA_BINDIR, JAVA_HOME or PATH in order" \
			       "to point to such a VM. See the manpage of freemind(1) for details."
			JAVA_TYPE=other
		else
			JAVA_TYPE=sun
		fi
		return 0
	else
		_error "Couldn't find a java virtual machine," \
		       "define JAVACMD, JAVA_BINDIR, JAVA_HOME or PATH."
		return 1
	fi
}

_source() {
	if [ -f "$1" ]
	then
		_debug "Sourcing '$1'."
		. "$1"
	fi
}

output_debug_info() {
	if [ -z "${DEBUG}" ]
	then
		return 0
	fi
	_debug "Freemind parameters are '${@}'."
	_debug "$(uname -a)"
	if [ -x "$(which lsb_release 2>/dev/null)" ]
	then
		_debug "$(lsb_release -a)"
	else
		_debug "System is not LSB conform, 'lsb_release' does not exist."
	fi
	if [ -x "$(which dpkg 2>/dev/null)" ]
	then
		_debug "The following DEB packages are installed:"
		COLUMNS=132 dpkg -l | grep -i -e freemind >&2
	elif [ -x "$(which rpm 2>/dev/null)" ]
	then
		_debug "The following RPM packages are installed:"
		rpm -qa | grep -i -e freemind >&2
	else
		_debug "Neither dpkg nor rpm is installed."
	fi
}

########## START MAIN PART #############################################

#--------- Put the environment together --------------------------------

_source /etc/freemind/freemindrc
_source ~/.freemind/freemindrc

findjava
if [ $? -ne 0 ]
then
	exit 1
fi

output_debug_info

if [ -L "$0" ] && [ -x $(which readlink) ]
then # if the script is a link and we have 'readlink' to follow it
	# -m should be faster and link does always resolve, else this script
	# wouldn't be called, would it?
	freefile=$(readlink -mn "$0")
	_debug "Link '$0' resolved to '${freefile}'."
else
	freefile="$0"
fi
freepath=$(dirname "${freefile}")
freepath="${freepath%/bin}" # nothing happens if freemind is not installed
                            # under something/bin

# we try different possibilities to find freemind.jar
for jar in "${FREEMIND_BASE_DIR}" \
	"${freepath}" "${freepath}/share/freemind" "${freepath}/freemind"
do
	if [ -f "${jar}/lib/freemind.jar" ]
	then
		freedir="${jar}"
		_debug "Freemind Directory is '${jar}'."
		break
	fi
done

if [ -z "${freedir}" ]
then
	_error "Couldn't find freemind under '${freepath}'."
	exit 1
fi

if [ ! -f ~/.freemind/patterns.xml ] && [ -f /etc/freemind/patterns.xml ]
then
	if [ ! -d ~/.freemind ]
	then
		_debug "Creating directory ~/.freemind."
		mkdir -p ~/.freemind
	fi
	_debug "Copying patterns.xml to ~/.freemind."
	cp /etc/freemind/patterns.xml ~/.freemind/patterns.xml
fi

#--------- Call (at last) FreeMind -------------------------------------

# The CLASSPATH also lets one specify additional jars, which is good, if
# you want to add a new Look&Feel jar (the motif one is so ugly...).
# 
CLASSPATH="${ADD_JARS}:${CLASSPATH}:${freedir}/lib/freemind.jar:\
${freedir}/lib/jibx/jibx-run.jar:\
${freedir}/lib/jibx/xpp3.jar:\
${freedir}/lib/bindings.jar:\
${freedir}/lib/commons-lang-2.0.jar:\
${freedir}/lib/forms-1.0.5.jar:\
${freedir}"
if [ "${JAVA_TYPE}" = "sun" ]
then
	_debug "Calling: '${JAVACMD} -Dfreemind.base.dir=${freedir} -cp ${CLASSPATH} freemind.main.FreeMindStarter  $@'."
	( echo "${DEBUG}" | grep -qe "exit" ) && exit 0 # do not start FreeMind
	"${JAVACMD}" -Xmx256M -Dfreemind.base.dir="${freedir}" -cp "${CLASSPATH}" freemind.main.FreeMindStarter "$@"
else # non-Sun environments don't work currently.
	_debug "Calling: '${JAVACMD} -Dgnu.java.awt.peer.gtk.Graphics=Graphics2D -Dfreemind.base.dir=${freedir} -cp ${CLASSPATH} freemind.main.FreeMindStarter  $@'."
	( echo "${DEBUG}" | grep -qe "exit" ) && exit 0 # do not start FreeMind
	"${JAVACMD}" -Xmx256M -Dgnu.java.awt.peer.gtk.Graphics=Graphics2D -Dfreemind.base.dir="${freedir}" -cp "${CLASSPATH}" freemind.main.FreeMindStarter "$@"
fi
