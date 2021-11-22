# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
#
# The following license files were not able to be identified and are
# represented as "Unknown" below, you will need to check them yourself:
#   LICENSE
LICENSE = "Unknown"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e4224ccaecb14d942c71d31bef20d78c"

SRC_URI = " \
	git://github.com/TheYoctoJester/simple_echo_server;protocol=https \
	file://simpleechoserver.service \
"

# Modify these as desired
PV = "1.0+git${SRCPV}"
SRCREV = "670f02380fa00be3d2f83b597b2811052f1991ca"

S = "${WORKDIR}/git"

DEPENDS = "boost"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "simpleechoserver.service"

do_install_append() {
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -d ${D}${systemd_unitdir}/system
		for service in ${SYSTEMD_SERVICE_${PN}}; do
			install -m 0644 ${WORKDIR}/${service} ${D}${systemd_unitdir}/system/
			sed -i -e 's,@BINDIR@,${bindir},g' ${D}${systemd_unitdir}/system/${service}
		done
	fi
}

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE = ""

