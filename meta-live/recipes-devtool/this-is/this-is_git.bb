# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=9273579e85f231c463bf432ce84c2479"

SRC_URI = "git://github.com/LetoThe2nd/this_is.git;protocol=https"

# Modify these as desired
PV = "1.0+git${SRCPV}"
SRCREV = "99881898113cf78f5a986e8960fda9a3896c302e"

S = "${WORKDIR}/git"

inherit cmake

# Specify any options you want to pass to cmake using EXTRA_OECMAKE:
EXTRA_OECMAKE = ""

