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
	elif [ -n "${FREEPLANE_JAVA_HOME}" ] && [ -x "${FREEPLANE_JAVA_HOME}/bin/java" ]; then
		JAVACMD="${FREEPLANE_JAVA_HOME}/bin/java"
		_debug "Using \$FREEPLANE_JAVA_HOME to find java virtual machine."
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
		if "${JAVACMD}" -version 2>&1 | grep -qe OpenJDK; then
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
__move_old_userfpdir_to_XDG_CONFIG_HOME() {
    if [ -d "$old_userfpdir/1.3.x" -a ! -d "$userfpdir" ] ; then
    	mkdir "$userfpdir"
		mv "$old_userfpdir/1.3.x" "$userfpdir/1.3.x"
		ln -s "$userfpdir/1.3.x" "$old_userfpdir/1.3.x"
    fi
}

old_userfpdir="${HOME}/.freeplane"
userfpdir="${XDG_CONFIG_HOME:-$HOME/.config}/freeplane"
__move_old_userfpdir_to_XDG_CONFIG_HOME
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
  # OpenJDK(7) fixes (don't use OpenJDK6!!)
  JAVA_OPTS="-Dgnu.java.awt.peer.gtk.Graphics=Graphics2D $JAVA_OPTS"

  # this fixes font rendering for some people, see:
  # http://www.freeplane.org/wiki/index.php/Rendering_Issues
  JAVA_OPTS="-Dawt.useSystemAAFontSettings=on -Dswing.aatext=true $JAVA_OPTS"

  # this sometimes helps with visual mindmap distortions (default in openjdk>=8!)
  # (but it can also create trouble so it's disabled by default):
  #JAVA_OPTS="-Dsun.java2d.xrender=True $JAVA_OPTS"
fi

# enable this in order to turn off the splash screen:
#JAVA_OPTS="-Dorg.freeplane.nosplash=true $JAVA_OPTS"

# grant all permissions to shared java extension libs if available
if [ -d "/usr/share/java" ] ; then
	JAVA_OPTS="-Dorg.freeplane.os.lib.ext=/usr/share/java $JAVA_OPTS"
fi

_debug "Calling: "\
"${JAVACMD}" -Xmx512m\
 "-Dorg.freeplane.userfpdir=$userfpdir"\
 "-Dorg.freeplane.old_userfpdir=$old_userfpdir"\
 "-Dorg.freeplane.globalresourcedir=${freedir}/resources"\
 "-Dswing.systemlaf=javax.swing.plaf.metal.MetalLookAndFeel"\
 $JAVA_OPTS\
 $xdockname\
 -jar "${freedir}/freeplanelauncher.jar"\
 "$@"
( echo "${DEBUG}" | grep -qe "exit" ) && exit 0 # do not start Freeplane

# now actually launch Freeplane
"${JAVACMD}" -Xmx512m\
 "-Dorg.freeplane.userfpdir=$userfpdir"\
 "-Dorg.freeplane.old_userfpdir=$old_userfpdir"\
 "-Dorg.freeplane.globalresourcedir=${freedir}/resources"\
 "-Dswing.systemlaf=javax.swing.plaf.metal.MetalLookAndFeel"\
 $JAVA_OPTS\
 $xdockname\
 -jar "${freedir}/freeplanelauncher.jar"\
 "$@"
