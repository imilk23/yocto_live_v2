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

## LESSON 4:SDKS
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
2. Build SDK
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

