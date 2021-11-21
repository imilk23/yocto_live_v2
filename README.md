# yocto_live_v2
Live Coding with Yocto Project V2.0

## LESSON 1: Download the first build
1. Clone yocto source
```
git clone https://git.yoctoproject.org/git/poky
git checkout dunfell
```
2. Build
```
source poky/oe-init-build-env
vi conf/local.conf
	edit MACHINE ??= "qemuarm"
bitbake core-image-minimal
```
3. Run
```
runqemu qemuarm nographic
Login: root
poweroff
```

## LESSON 2: Simple layer, custom image and devtool
1. Create bitbake layer
```
bitbake-layers create-layer meta-live
cd build
vi conf/bblayers.conf
	add    /home/binhht/workspace/yocto/meta-live \
bitbake example
```
2. Create example images
```
cd meta-live; mkdir images; cd images
cp ../../../poky/meta/recipes-core/images/core-image-minimal-dev.bb example-image.bb
bitbake example-image
runqemu qemuarm example-image nographic
```
3. Create recipes
```
devtool add https://github.com/LetoThe2nd/this_is.git
devtool edit-recipe this-is
bitbake this-is # build recipe this-is only
vi meta-live/recipes-example/images/example-image.bb
	IMAGE_INSTALL += "bc this-is"
bitbake example-image
```

## LESSON 3: Package dependencies and splitting
1. Create lib
```
devtool add libanswer https://github.com/LetoThe2nd/libanswer
bitbake libanswer
devtool edit-recipe libanswer
	add DEPENDS = “boost”
    RDEPENDS_${PN} = “bc” 
	devtool edit-recipe example-image
		add	IMAGE_INSTALL += "libanswer"
bitbake example-image
runqemu qemuarm example-image nographic
login : root
ask
poweroff
bitbake -c cleansstate libanswer && bitbake libanswer
```
2. Runtime dependencies and compile time dependencies
- DEPENDS: Build time package dependencies. eg: "boost"
- RDEPENDS: Run time package dependencies. eg:  "bc"
```
DEPENDS = "boost"
RDEPENDS_${PN} = "bc"
```
3. Package splitting
```
#split example out of package
devtool edit-recipe libanswer
	add	PACKAGES =+ "${PN}-example"
		FILES_${PN}-example =+ " \
			/usr/bin/ask \
 		"
#add example in image recipe
devtool edit-recipe example-image
	add	IMAGE_INSTALL += "libanswer-example"
```

## LESSON 4: SDKS
1. Add extra image configuration
```
# tools-sdk: make, gcc,..
vi conf/local.conf
	edit EXTRA_IMAGE_FEATURES ?= “ debug-tweaks tools-sdk”
bitbake example-image
runqemu qemuarm example-image nographic
touch main.c
#include<stdio.h>

int main(void){
    printf("Hello\n");
    return 0;
}
gcc main.c
```
2. Build SDK <br>
When we don’t want native compile, so CROSS_COMPILE is another option.
```
vi conf/local.conf
	edit EXTRA_IMAGE_FEATURES ?= "debug-tweaks ssh-server-dropbear"
```
```
# option 1: classic sdk -> do manual building
bitbake example-image -c populate_sdk
./build/tmp/deploy/sdk/poky-glibc-x86_64-example-image-armv7vet2hf-neon-qemuarm-toolchain-3.1.11.sh
```
```
# option 2: external sdk -> build with devtool
bitbake example-image -c populate_sdk_ext
./build/tmp/deploy/sdk/poky-glibc-x86_64-example-image-armv7vet2hf-neon-qemuarm-toolchain-ext-3.1.11.sh
#=============================================================================
#Enter target directory for SDK (default: ~/poky_sdk): ~/workspace/yocto/sdk_ext
source ~/workspace/yocto/sdk_ext/environment-setup-armv7vet2hf-neon-poky-linux-gnueabi
devtool add simplehello https://github.com/LetoThe2nd/simplehello.git
# fix devtool workspace issues
vi build/conf/bblayer.conf
# edit
#	- /home/binhht/workspace/yocto/build/workspace 
#	+ /home/binhht/workspace/yocto/sdk_ext/workspace \
devtool build simplehello

#FIXED: devtool can not deploy binary in sdk_ext directory (conflict env between poky/oe-init-build-env and sdk_ext/env...)
cp -r sdk_ext/tmp/work/armv7vet2hf-neon-poky-linux-gnueabi/simplehello/ build/tmp/work/armv7vet2hf-neon-poky-linux-gnueabi/
devtool deploy-target -s -c simplehello root@192.168.7.2
```

## LESSON 5: Development workflows
1. Build core-minimal-image
```
vi  build/conf/local.conf
# add
DISTRO_FEATURES_append = " systemd pam"
VIRTUAL-RUNTIME_init_manager = "systemd"
 
DISTRO_FEATURES_BACKFILL_CONSIDERED = "sysvinit"

VIRTUAL-RUNTIME_initscripts = ""
```
2. Build core-image-full-cmdline

## LESSON 6: Kernel handling and development
1. Switch to a specific kernel
```
vi  build/conf/local.conf
# add
	PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-rt"
bitbake core-image-minimal

vi  build/conf/local.conf
# delete
	PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-rt"
```
2. Create new kernel recipes
```
mkdir meta-live/conf/machine
cp poky/meta/conf/machine/qemuarm.conf meta-live/conf/machine/livearm.conf
vi build/conf/local.conf
# edit
MACHINE ??= livearm
```
```
#download kernel v4.14
mkdir linux
git clone git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git -b linux-4.14.y
git show HEAD
```
```
#create recipes-kernel
cd meta-live; mkdir recipes-kernel
cp -r poky/meta-skeleton/recipes-kernel/linux/ meta-live/recipes-kernel/
mv linux-yocto-custom.bb linux-stable.bb
mv linux-yocto-custom/ linux-stable/
cp CD linux/arch/arm/configs/multi_v7_defconfig meta-live/recipes-kernel/linux-stable/defconfig
vi meta-live/recipes-kernel/linux-stable.bb
# edit
SRC_URI = " \
git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git;protocol=git;nocheckout=1;name=machine;branch=linux-4.14.y \
	file://defconfig \
"
KCONFIG_MODE="--alldefconfig"
LINUX_VERSION ?= "4.14.y"
SRCREV_livearm="0447aa205abe1c0c016b4f7fa9d7c08d920b5c8e"
PV = "4.14.254"
COMPATIBLE_MACHINE = "livearm"

vi  meta-live/conf/machine/livearm.conf
# add
	PREFERRED_PROVIDER_virtual/kernel = "linux-stable"
	IMAGE_CLASSES += "qemuboot"
	IMAGE_FSTYPES += "ext4"
```

## LESSON 7: Distros, machines, images and local.conf
1. Distros introduction <br>
In short, distros are distribution of configuration files (glibc, libraries, toolchains,..)
```
# view bitbake environment
bitbake -e core-image-minimal | less
```

2. Create new distro configuration
```
cd meta-live; mkdir conf/distro
vi livedistro.conf
# add
	require conf/distro/poky.conf

	DISTRO = "livedistro"
	DISTRO_NAME = "livedistro (Yocto Live Coding Reference Distro)"
	DISTRO_VERSION = "1.0.0"
	DISTRO_CODENAME = "warrior"

	DISTRO_FEATURES_append = " systemd pam"
	VIRTUAL-RUNTIME_init_manager = "systemd"
 
	DISTRO_FEATURES_BACKFILL_CONSIDERED = "sysvinit"

	VIRTUAL-RUNTIME_initscripts = ""

vi build/conf/local.conf
# edit
	DISTRO ?= "livedistro"

bitbake core-image-minimal
```

## LESSON 8: Real hardware
wic list images

## LESSON 10: Building and customizing containers

## LESSON 11: Getting started with C/C++ đevelopment
1. Create target binary
```
mkdir noclue; cd noclue
vi noclue.cpp
#add 
	#include <iostream>

int main(){
	std::cout << "you totally have no clue" << std::endl;
	return 0;
}

vi CMakeLists.txt
#add 
cmake_minimum_required(VERSION 2.8)

project(noclue)

add_executable(noclue noclue.cpp)

install(TARGETS noclue DESTINATION bin)

cmake .
make

devtool add meta-live/noclue
vi build/workspace/recipes/noclue/noclue.bb

# make sure noclue directory is clear
devtool reset noclue
devtool add --no-same-dir meta-live/noclue
bitbake noclue

vi build/conf/local.conf
#add
CORE_IMAGE_EXTRA_INSTALL = "noclue"
bitbake core-image-minimal
runqemu livearm nographic slirp
which noclue
#/usr/bin/noclue

poweroff
```

## LESSON 12: Project setup and kas
1. Setup project manually
```
cd yocto
git clone git://git.openembedded.org/meta-openembedded
git clone git://git.yoctoproject.org/meta-ti
git clone git://git.yoctoproject.org/meta-arm
# check out to dunfell
vi build/conf/bblayers.conf
BBLAYERS ?= " \
  /home/binhht/workspace/yocto/meta-ti \
  /home/binhht/workspace/yocto/meta-arm/meta-arm-toolchain \
  /home/binhht/workspace/yocto/meta-arm/meta-arm \
  /home/binhht/workspace/yocto/meta-openembedded/meta-python \
  /home/binhht/workspace/yocto/meta-openembedded/meta-oe \
  /home/binhht/workspace/yocto/poky/meta \
  /home/binhht/workspace/yocto/poky/meta-poky \
  /home/binhht/workspace/yocto/poky/meta-yocto-bsp \
  "
	vi build/conf/local.conf
MACHINE ??= "beaglebone"
DISTRO  ??= "poky"

bitbake core-image-minimal
```
2. Setup project with KAS <br>
To be continued...

