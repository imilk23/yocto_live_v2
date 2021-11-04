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

