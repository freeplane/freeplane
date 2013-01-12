#!/bin/bash

# we only want to test the script, not Freeplane itself
if ( echo "${DEBUG}" | grep -qe "script" ); then
	set -x
fi

########## FUNCTIONS DEFINITIONS #######################################

_debug() {
	if [ -n "${DEBUG}" ]; then
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
	if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]; then
		_debug "Using \$JAVACMD to find java virtual machine."
	elif [ -n "${JAVA_BINDIR}" ] && [ -x "${JAVA_BINDIR}/java" ]; then
		JAVACMD="${JAVA_BINDIR}/java"
		_debug "Using \$JAVA_BINDIR to find java virtual machine."
	elif [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
		JAVACMD="${JAVA_HOME}/bin/java"
		_debug "Using \$JAVA_HOME to find java virtual machine."
	else
		JAVACMD=$(which java)
		if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]; then
			_debug "Using \$PATH to find java virtual machine."
		elif [ -x /usr/bin/java ]; then
			_debug "Using /usr/bin/java to find java virtual machine."
			JAVACMD=/usr/bin/java
		fi
	fi

	# if we were successful, we return 0 else we complain and return 1
	if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]; then
		_debug "Using '$JAVACMD' as java virtual machine..."
		if [ -n "${DEBUG}" ]; then
			"$JAVACMD" -version >&2
		fi
		if (! "${JAVACMD}" -version 2>&1 | grep -qe 'Java(TM)' -e OpenJDK); then
			_error "Your Java is not a derivative from Sun's code," \
			       "========================================" \
			       "FREEPLANE WILL MOST PROBABLY *NOT* WORK," \
			       "========================================" \
			       "define JAVACMD, JAVA_BINDIR, JAVA_HOME or PATH" \
			       "in order to point to such a VM." \
			       "See the manpage of freeplane(1) for details."
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
	if [ -f "$1" ]; then
		_debug "Sourcing '$1'."
		. "$1"
	fi
}

output_debug_info() {
	if [ -z "${DEBUG}" ]; then
		return 0
	fi
	_debug "Freeplane parameters are '${@}'."
	_debug "$(uname -a)"
	if [ -x "$(which lsb_release 2>/dev/null)" ]; then
		_debug "$(lsb_release -a)"
	else
		_debug "System is not LSB conform, 'lsb_release' does not exist."
	fi
	if [ -x "$(which dpkg 2>/dev/null)" ]; then
		_debug "The following DEB packages are installed:"
		COLUMNS=132 dpkg -l | grep -i -e freeplane >&2
	elif [ -x "$(which rpm 2>/dev/null)" ]; then
		_debug "The following RPM packages are installed:"
		rpm -qa | grep -i -e freeplane >&2
	else
		_debug "Neither dpkg nor rpm is installed."
	fi
}

########## START MAIN PART #############################################

#--------- Put the environment together --------------------------------

userfpdir="${HOME}/.freeplane"
_source /etc/freeplane/freeplanerc
_source "${userfpdir}/freeplanerc"

findjava
if [ $? -ne 0 ]; then
	exit 1
fi

output_debug_info

if [ -x $(which readlink) ] && [ "`echo $OSTYPE | cut -b1-6`" != "darwin" ]; then
	# if we have 'readlink' we can use it to get an absolute path
	# -m should be faster and link does always resolve, else this script
	# wouldn't be called, would it?
	freefile=$(readlink -mn "$0")
	_debug "Link '$0' resolved to '${freefile}'."
else
	freefile="$0"
fi

if [ "`echo $OSTYPE | cut -b1-6`" == "darwin" ]; then
	xdockname='-Xdock:name=Freeplane'
else
	xdockname=""
fi

freepath="$(dirname "${freefile}")"

# we try different possibilities to find framework.jar
for jar in "${FREEPLANE_BASE_DIR}" \
	"${freepath}" "${freepath}/share/freeplane" "${freepath}/freeplane"
do
	if [ -f "${jar}/framework.jar" ]; then
		freedir="${jar}"
		_debug "Freeplane Directory is '${jar}'."
		break
	fi
done

if [ -z "${freedir}" ]; then
	_error "Couldn't find Freeplane under '${freepath}'."
	exit 1
fi

#--------- Call (at last) Freeplane -------------------------------------
if [ "${JAVA_TYPE}" != "sun" ]; then
	# non-Sun environments don't work currently but we try anyway, who knows.
	JAVA_OPTS="-Dgnu.java.awt.peer.gtk.Graphics=Graphics2D $JAVA_OPTS"
fi

_debug "Calling: "\
"${JAVACMD}" -Xmx512m\
 "-Dorg.freeplane.param1=$1"\
 "-Dorg.freeplane.param2=$2"\
 "-Dorg.freeplane.param3=$3"\
 "-Dorg.freeplane.param4=$4"\
 "-Dorg.freeplane.param4=$5"\
 "-Dorg.freeplane.param4=$6"\
 "-Dorg.freeplane.param4=$7"\
 "-Dorg.freeplane.param4=$8"\
 "-Dorg.knopflerfish.framework.bundlestorage=memory"\
 "-Dorg.freeplane.globalresourcedir=${freedir}/resources"\
 "-Dorg.knopflerfish.gosg.jars=reference:file:${freedir}/core/"\
 $JAVA_OPTS\
 $xdockname\
 -jar "${freedir}/framework.jar"\
 -xargs "${freedir}/props.xargs"\
 -xargs "${freedir}/init.xargs"
( echo "${DEBUG}" | grep -qe "exit" ) && exit 0 # do not start Freeplane

# now actually launch Freeplane
"${JAVACMD}" -Xmx512m\
 "-Dorg.freeplane.param1=$1"\
 "-Dorg.freeplane.param2=$2"\
 "-Dorg.freeplane.param3=$3"\
 "-Dorg.freeplane.param4=$4"\
 "-Dorg.freeplane.param4=$5"\
 "-Dorg.freeplane.param4=$6"\
 "-Dorg.freeplane.param4=$7"\
 "-Dorg.freeplane.param4=$8"\
 "-Dorg.knopflerfish.framework.bundlestorage=memory"\
 "-Dorg.freeplane.globalresourcedir=${freedir}/resources"\
 "-Dorg.knopflerfish.gosg.jars=reference:file:${freedir}/core/"\
 $JAVA_OPTS\
 $xdockname\
 -jar "${freedir}/framework.jar"\
 -xargs "${freedir}/props.xargs"\
 -xargs "${freedir}/init.xargs"
