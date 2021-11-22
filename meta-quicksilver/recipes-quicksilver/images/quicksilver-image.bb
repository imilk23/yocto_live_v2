require recipes-core/images/core-image-minimal.bb

DESCRIPTION = "A small image just capable of running simpleechoserver."

IMAGE_INSTALL_append = " simpleechoserver screen"
