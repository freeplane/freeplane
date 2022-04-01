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
	
	JAVA_SOURCE=
	
	if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]; then
		JAVA_SOURCE="\$JAVACMD"
	elif [ -n "${JAVA_BINDIR}" ] && [ -x "${JAVA_BINDIR}/java" ]; then
		JAVACMD="${JAVA_BINDIR}/java"
		JAVA_SOURCE="\$JAVA_BINDIR"
	elif [ -n "${FREEPLANE_JAVA_HOME}" ] && [ -x "${FREEPLANE_JAVA_HOME}/bin/java" ]; then
		JAVACMD="${FREEPLANE_JAVA_HOME}/bin/java"
		JAVA_SOURCE="\$FREEPLANE_JAVA_HOME"
	elif [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
		JAVACMD="${JAVA_HOME}/bin/java"
		JAVA_SOURCE="\$JAVA_HOME"
	else
		JAVACMD=$(which java)
		if [ -n "${JAVACMD}" ] && [ -x "${JAVACMD}" ]; then
			JAVA_SOURCE="\$PATH"
		elif [ -x /usr/bin/java ]; then
			JAVACMD=/usr/bin/java
			JAVA_SOURCE="/usr/bin/java"
		else
			_error "Couldn't find a java virtual machine," \
				"define JAVACMD, JAVA_BINDIR, JAVA_HOME, FREEPLANE_JAVA_HOME or PATH."
			return 1
		fi
	fi

	JAVA_VERSION=$(${JAVACMD} -version |& grep -E "[[:alnum:]]+ version" | awk '{print $3}' | tr -d '"')
	JAVA_MAJOR_VERSION=$(echo $JAVA_VERSION | awk -F. '{print $1}')
	if [ $JAVA_MAJOR_VERSION -lt 8 ] || [ $JAVA_MAJOR_VERSION -gt 17 ] || [ $JAVA_MAJOR_VERSION -eq 10 ]; then
		if [ -z "${FREEPLANE_USE_UNSUPPORTED_JAVA_VERSION}" ]; then
			_error "Found $JAVACMD in $JAVA_SOURCE."
			_error "It has version $JAVA_VERSION"
			_error "Currently, freeplane requires java version 8 or from 11 to 17"
			_error ""
			_error "Select a supported java version"
			_error "by setting FREEPLANE_JAVA_HOME to a valid java location"
			_error "OR use an unsupported java version"
			_error "by setting FREEPLANE_USE_UNSUPPORTED_JAVA_VERSION to 1"
			return 1
		fi
	fi
	_debug "Using $JAVACMD as specified in $JAVA_SOURCE."

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

if [ $JAVA_MAJOR_VERSION -ge 11 ]; then
	JAVA_OPTS="--add-exports java.desktop/sun.awt=ALL-UNNAMED $JAVA_OPTS"
	JAVA_OPTS="--add-exports java.desktop/sun.swing=ALL-UNNAMED $JAVA_OPTS"
	JAVA_OPTS="--add-opens java.desktop/sun.awt.X11=ALL-UNNAMED $JAVA_OPTS"
	JAVA_OPTS="--add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED $JAVA_OPTS"
	JAVA_OPTS="-Dorg.osgi.framework.system.capabilities=osgi.ee;osgi.ee=\"JavaSE\";version:List=\"1.8,15\" $JAVA_OPTS"
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
